package com.mycompany.tiketwahana.service;

import com.mycompany.tiketwahana.core.Audit;
import com.mycompany.tiketwahana.core.ConnectionPool;
import com.mycompany.tiketwahana.core.Database;
import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.core.Role;
import com.mycompany.tiketwahana.core.SessionUser;
import com.mycompany.tiketwahana.domain.TicketRow;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TicketService {

    private final Database database;

    public TicketService(Database database) {
        this.database = database;
    }

    private static final String SELECT = "SELECT t.id, t.kode_tiket, t.nama_pengunjung, t.no_telepon, "
            + "w.nama_wahana, w.kode_wahana, t.tanggal_kunjungan, t.total_harga, t.status, t.redeemed_at, "
            + "u.full_name AS redeemed_by FROM tiket t JOIN wahana w ON w.id = t.wahana_id "
            + "LEFT JOIN users u ON u.id = t.redeemed_by ";

    public TicketRow find(String code) throws ServiceException {
        try {
            return database.withConnection(c -> {
                try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE t.kode_tiket = ?")) {
                    ps.setString(1, code.trim().toUpperCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? map(rs) : null;
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal mencari tiket: " + e.getMessage(), e);
        }
    }

    /** Marks a ticket used. The database row is the source of truth, not the screen. */
    public TicketRow redeem(SessionUser user, String code) throws ServiceException {
        if (user == null || !user.can(Role.Permission.REDEEM)) {
            throw new ServiceException("Peran Anda tidak boleh memvalidasi tiket.");
        }
        String normalized = code == null ? "" : code.trim().toUpperCase();
        if (normalized.isEmpty()) {
            throw new ServiceException("Masukkan kode tiket.");
        }
        try (ConnectionPool.Lease lease = database.borrow()) {
            Connection c = lease.connection();
            c.setAutoCommit(false);
            try {
                int ticketId;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT id, status FROM tiket WHERE kode_tiket = ? FOR UPDATE")) {
                    ps.setString(1, normalized);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ServiceException("Tiket " + normalized + " tidak ditemukan.");
                        }
                        ticketId = rs.getInt("id");
                        String status = rs.getString("status");
                        switch (status) {
                            case "DIGUNAKAN" -> throw new ServiceException("Tiket ini sudah dipakai.");
                            case "REFUND" -> throw new ServiceException("Tiket ini sudah direfund.");
                            case "EXPIRED" -> throw new ServiceException("Tiket ini sudah kedaluwarsa.");
                            default -> { }
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE tiket SET status='DIGUNAKAN', redeemed_at=NOW(), redeemed_by=? "
                                + "WHERE id=? AND status='AKTIF'")) {
                    ps.setInt(1, user.id());
                    ps.setInt(2, ticketId);
                    if (ps.executeUpdate() != 1) {
                        throw new ServiceException("Tiket ini baru saja dipakai di tempat lain.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO pengunjung_log (tiket_id, admin_id, status) VALUES (?,?, 'MASUK')")) {
                    ps.setInt(1, ticketId);
                    ps.setInt(2, user.id());
                    ps.executeUpdate();
                }
                Audit.record(c, user, "REDEEM", "tiket", normalized, null);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw new ServiceException("Gagal memvalidasi tiket: " + e.getMessage(), e);
            } catch (ServiceException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Tidak dapat menghubungi database: " + e.getMessage(), e);
        }
        return find(normalized);
    }

    public List<TicketRow> recent(int limit) throws ServiceException {
        List<TicketRow> out = new ArrayList<>();
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(SELECT + "ORDER BY t.created_at DESC LIMIT ?")) {
                    ps.setInt(1, limit);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(map(rs));
                        }
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat tiket: " + e.getMessage(), e);
        }
        return out;
    }

    private static TicketRow map(ResultSet rs) throws SQLException {
        return new TicketRow(
                rs.getInt("id"),
                rs.getString("kode_tiket"),
                rs.getString("nama_pengunjung"),
                rs.getString("no_telepon"),
                rs.getString("nama_wahana"),
                rs.getString("kode_wahana"),
                rs.getObject("tanggal_kunjungan", java.time.LocalDate.class),
                Money.scale(rs.getBigDecimal("total_harga")),
                rs.getString("status"),
                rs.getObject("redeemed_at", LocalDateTime.class),
                rs.getString("redeemed_by"));
    }
}
