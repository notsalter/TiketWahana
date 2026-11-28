package com.mycompany.tiketwahana.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Applies db/migrations/NNN_*.sql in order, once each, tracked in schema_version.
 * No Flyway, no Liquibase: the splitter below understands comments and quoting,
 * which is all these files need.
 */
public final class Migrations {

    private static final List<String> FILES = List.of("001_initial.sql", "002_portfolio.sql");

    private Migrations() {}

    public static List<String> run(Connection connection) throws SQLException, IOException {
        ensureVersionTable(connection);
        List<String> applied = new ArrayList<>();
        for (String file : FILES) {
            String version = file.substring(0, file.indexOf('_'));
            if (alreadyApplied(connection, version)) {
                continue;
            }
            String sql = read(file);
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (Statement statement = connection.createStatement()) {
                    for (String stmt : split(sql)) {
                        statement.execute(stmt);
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement("INSERT INTO schema_version (version) VALUES (?)")) {
                    ps.setString(1, version);
                    ps.executeUpdate();
                }
                connection.commit();
                applied.add(file);
            } catch (SQLException e) {
                connection.rollback();
                throw new SQLException("Migrasi " + file + " gagal: " + e.getMessage(), e);
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
        return applied;
    }

    private static void ensureVersionTable(Connection connection) throws SQLException {
        try (Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS schema_version ("
                    + "version VARCHAR(20) PRIMARY KEY,"
                    + "applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB");
        }
    }

    private static boolean alreadyApplied(Connection connection, String version) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM schema_version WHERE version = ?")) {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static String read(String file) throws IOException {
        try (InputStream in = Migrations.class.getResourceAsStream("/db/migrations/" + file)) {
            if (in == null) {
                throw new IOException("Migrasi tidak ditemukan: " + file);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        }
    }

    /** Splits on semicolons that are outside quotes and not inside comments. */
    static List<String> split(String sql) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean single = false, dbl = false, backtick = false, lineComment = false, block = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            char next = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';
            if (lineComment) {
                if (c == '\n') {
                    lineComment = false;
                }
                continue;
            }
            if (block) {
                if (c == '*' && next == '/') {
                    block = false;
                    i++;
                }
                continue;
            }
            if (!single && !dbl && !backtick) {
                if (c == '-' && next == '-') {
                    lineComment = true;
                    i++;
                    continue;
                }
                if (c == '/' && next == '*') {
                    block = true;
                    i++;
                    continue;
                }
            }
            if (c == '\'' && !dbl && !backtick) {
                single = !single;
            } else if (c == '"' && !single && !backtick) {
                dbl = !dbl;
            } else if (c == '`' && !single && !dbl) {
                backtick = !backtick;
            }
            if (c == ';' && !single && !dbl && !backtick) {
                addIfUseful(out, cur);
                cur.setLength(0);
                continue;
            }
            cur.append(c);
        }
        addIfUseful(out, cur);
        return out;
    }

    private static void addIfUseful(List<String> out, StringBuilder cur) {
        String s = cur.toString().trim();
        if (!s.isEmpty()) {
            out.add(s);
        }
    }
}
