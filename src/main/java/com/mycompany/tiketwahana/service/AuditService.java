package com.mycompany.tiketwahana.service;

import com.mycompany.tiketwahana.core.Database;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AuditService {

    private final Database database;

    public AuditService(Database database) {
        this.database = database;
    }

    public record Entry(int id, LocalDateTime at, String user, String action, String entity, String entityId,
                        String detail) {
    }

    public List<Entry> recent(String filter, int limit) throws ServiceException {
        List<Entry> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT a.id, a.at, COALESCE(u.full_name,'sistem') AS who, a.action, a.entity, a.entity_id, a.detail "
                + "FROM audit_log a LEFT JOIN users u ON u.id = a.user_id ");
        if (filter != null && !filter.isBlank()) {
            sb.append("WHERE a.action LIKE ? OR a.entity_id LIKE ? OR a.detail LIKE ? OR u.full_name LIKE ? ");
        }
        sb.append("ORDER BY a.at DESC, a.id DESC LIMIT ?");
        final String sql = sb.toString();
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    int i = 1;
                    if (filter != null && !filter.isBlank()) {
                        String like = "%" + filter.trim() + "%";
                        for (int k = 0; k < 4; k++) {
                            ps.setString(i++, like);
                        }
                    }
                    ps.setInt(i, limit);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(new Entry(rs.getInt("id"), rs.getObject("at", LocalDateTime.class),
                                    rs.getString("who"), rs.getString("action"), rs.getString("entity"),
                                    rs.getString("entity_id"), rs.getString("detail")));
                        }
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat audit: " + e.getMessage(), e);
        }
        return out;
    }
}
