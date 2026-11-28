package com.mycompany.tiketwahana.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2-HMAC-SHA256 credentials, no external dependency.
 * Stored form: pbkdf2$sha256$210000$<b64 salt>$<b64 hash>.
 * Every failure path returns "denied" - there is no fallback account.
 */
public final class Auth {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final int MIN_PASSWORD_LENGTH = 8;

    private Auth() {}

    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$sha256$" + ITERATIONS + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 5 || !"pbkdf2".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[2]);
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] expected = Base64.getDecoder().decode(parts[4]);
            byte[] actual = derive(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith("pbkdf2$");
    }

    public static void checkPolicy(String password) throws AuthException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new AuthException("Password minimal " + MIN_PASSWORD_LENGTH + " karakter.");
        }
        if (password.equalsIgnoreCase("admin") || password.equalsIgnoreCase("password")
                || password.equals("12345678")) {
            throw new AuthException("Password terlalu mudah ditebak.");
        }
    }

    /** Authenticates against the database. Any SQL failure is a denial, never a bypass. */
    public static SessionUser login(Connection connection, String username, String password) throws AuthException {
        String sql = "SELECT id, username, full_name, role, password_hash, must_change_password, is_active "
                + "FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username == null ? "" : username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new AuthException("Username atau password salah.");
                }
                if (!rs.getBoolean("is_active")) {
                    throw new AuthException("Akun ini tidak aktif.");
                }
                String stored = rs.getString("password_hash");
                if (!isHashed(stored) || !verify(password, stored)) {
                    throw new AuthException("Username atau password salah.");
                }
                return new SessionUser(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        Role.of(rs.getString("role")),
                        rs.getBoolean("must_change_password"));
            }
        } catch (SQLException e) {
            throw new AuthException("Tidak dapat memverifikasi akun: " + e.getMessage(), e);
        }
    }

    public static void changePassword(Connection connection, SessionUser user, String current, String next)
            throws AuthException {
        checkPolicy(next);
        String sql = "SELECT password_hash FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, user.id());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !verify(current, rs.getString(1))) {
                    throw new AuthException("Password saat ini salah.");
                }
            }
        } catch (SQLException e) {
            throw new AuthException("Gagal memeriksa password: " + e.getMessage(), e);
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET password_hash = ?, must_change_password = FALSE WHERE id = ?")) {
            ps.setString(1, hash(next));
            ps.setInt(2, user.id());
            if (ps.executeUpdate() != 1) {
                throw new AuthException("Password tidak dapat diperbarui.");
            }
        } catch (SQLException e) {
            throw new AuthException("Gagal memperbarui password: " + e.getMessage(), e);
        }
    }

    private static byte[] derive(String password, byte[] salt) {
        return derive(password, salt, ITERATIONS);
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 tidak tersedia", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Ensures a first admin exists with a hashed password the user must change. */
    public static void bootstrapAdmin(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, password_hash FROM users WHERE username = 'admin'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement ins = connection.prepareStatement(
                            "INSERT INTO users (username, password, password_hash, full_name, role, must_change_password) "
                                    + "VALUES ('admin','', ?, 'Administrator', 'ADMIN', TRUE)")) {
                        ins.setString(1, hash("admin12345"));
                        ins.executeUpdate();
                    }
                    return;
                }
                String stored = rs.getString("password_hash");
                if (!isHashed(stored)) {
                    try (PreparedStatement upd = connection.prepareStatement(
                            "UPDATE users SET password_hash = ?, must_change_password = TRUE WHERE id = ?")) {
                        upd.setString(1, hash("admin12345"));
                        upd.setInt(2, rs.getInt("id"));
                        upd.executeUpdate();
                    }
                }
            }
        }
    }

    public static String utf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
