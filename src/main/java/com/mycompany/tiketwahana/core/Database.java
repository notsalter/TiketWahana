package com.mycompany.tiketwahana.core;

import java.sql.Connection;
import java.sql.SQLException;

/** Single entry point for connections, backed by a small hand-rolled pool. */
public final class Database implements AutoCloseable {

    private static Database shared;
    private final ConnectionPool pool;

    private Database(String url, String user, String password, int size) {
        this.pool = new ConnectionPool(url, user, password, size);
    }

    public static synchronized Database shared() {
        if (shared == null) {
            shared = new Database(Env.get("db.url"), Env.get("db.user"), Env.get("db.password"), Env.getInt("db.pool.size", 6));
        }
        return shared;
    }

    /** Test hook: bind a specific instance without touching the real config. */
    public static synchronized void setShared(Database database) {
        if (shared != null && shared != database) {
            shared.close();
        }
        shared = database;
    }

    public static Database create(String url, String user, String password, int size) {
        return new Database(url, user, password, size);
    }

    public ConnectionPool pool() {
        return pool;
    }

    public ConnectionPool.Lease borrow() throws SQLException {
        return pool.borrow();
    }

    /** Convenience for one-shot work: borrows, runs, releases. */
    public <T> T withConnection(SqlFunction<T> work) throws SQLException {
        try (ConnectionPool.Lease lease = pool.borrow()) {
            return work.apply(lease.connection());
        }
    }

    public void run(SqlConsumer work) throws SQLException {
        try (ConnectionPool.Lease lease = pool.borrow()) {
            work.accept(lease.connection());
        }
    }

    @Override
    public void close() {
        pool.close();
    }

    @FunctionalInterface
    public interface SqlFunction<T> {
        T apply(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlConsumer {
        void accept(Connection connection) throws SQLException;
    }
}
