package com.mycompany.tiketwahana.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A deliberately small connection pool: a bounded queue of live connections, one
 * validation probe per borrow, and rollback-on-return so a failed transaction can
 * never leak a dirty connection back into circulation.
 */
public final class ConnectionPool implements AutoCloseable {

    private final String url;
    private final String user;
    private final String password;
    private final int max;
    private final BlockingQueue<Connection> idle;
    private final AtomicInteger live = new AtomicInteger();
    private volatile boolean closed;

    public ConnectionPool(String url, String user, String password, int max) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.max = Math.max(1, max);
        this.idle = new ArrayBlockingQueue<>(this.max);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver MySQL tidak ditemukan", e);
        }
    }

    public Lease borrow() throws SQLException {
        if (closed) {
            throw new SQLException("Pool sudah ditutup");
        }
        Connection connection = idle.poll();
        while (connection != null && !isUsable(connection)) {
            discard(connection);
            connection = idle.poll();
        }
        if (connection == null) {
            int current = live.get();
            if (current >= max || !live.compareAndSet(current, current + 1)) {
                // Pool is saturated: wait for a lease to come back rather than opening a 7th socket.
                try {
                    connection = idle.poll(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Menunggu koneksi database dibatalkan", e);
                }
                if (connection == null) {
                    throw new SQLException("Tidak ada koneksi database yang tersedia");
                }
            } else {
                try {
                    connection = open();
                } catch (SQLException e) {
                    live.decrementAndGet();
                    throw e;
                }
            }
        }
        return new Lease(this, connection);
    }

    private Connection open() throws SQLException {
        Connection c = DriverManager.getConnection(url, user, password);
        c.setAutoCommit(true);
        return c;
    }

    private static boolean isUsable(Connection c) {
        try {
            return !c.isClosed() && c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private void discard(Connection c) {
        try {
            c.close();
        } catch (SQLException ignored) {
            // already dead
        }
        live.decrementAndGet();
    }

    private void release(Connection c) {
        if (c == null) {
            return;
        }
        try {
            if (!c.getAutoCommit()) {
                c.rollback();
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            discard(c);
            return;
        }
        if (closed || !idle.offer(c)) {
            discard(c);
        }
    }

    public int liveCount() {
        return live.get();
    }

    @Override
    public void close() {
        closed = true;
        Connection c;
        while ((c = idle.poll()) != null) {
            discard(c);
        }
    }

    /** A borrowed connection; closing the lease returns it to the pool. */
    public static final class Lease implements AutoCloseable {
        private final ConnectionPool pool;
        private final Connection connection;
        private boolean released;

        private Lease(ConnectionPool pool, Connection connection) {
            this.pool = pool;
            this.connection = connection;
        }

        public Connection connection() {
            return connection;
        }

        @Override
        public void close() {
            if (!released) {
                released = true;
                pool.release(connection);
            }
        }
    }
}
