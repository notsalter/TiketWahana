package com.mycompany.tiketwahana.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Append-only trail for anything that moves money, access or stock. */
public final class Audit {

    private Audit() {}

    public static void record(Connection connection, SessionUser user, String action,
                              String entity, String entityId, String detail) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO audit_log (user_id, action, entity, entity_id, detail) VALUES (?,?,?,?,?)")) {
            if (user == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, user.id());
            }
            ps.setString(2, action);
            ps.setString(3, entity);
            ps.setString(4, entityId);
            ps.setString(5, detail == null ? null : detail.substring(0, Math.min(500, detail.length())));
            ps.executeUpdate();
        } catch (SQLException ignored) {
            // auditing must never abort a sale that already committed
        }
    }

    public static void record(Database database, SessionUser user, String action,
                              String entity, String entityId, String detail) {
        try {
            database.run(c -> record(c, user, action, entity, entityId, detail));
        } catch (SQLException ignored) {
            // see above
        }
    }
}
