package com.mycompany.tiketwahana.service;

import com.mycompany.tiketwahana.core.Audit;
import com.mycompany.tiketwahana.core.Codes;
import com.mycompany.tiketwahana.core.ConnectionPool;
import com.mycompany.tiketwahana.core.Database;
import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.core.Role;
import com.mycompany.tiketwahana.core.SessionUser;
import com.mycompany.tiketwahana.domain.Shift;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Cash drawer sessions: open with a float, close against a count. */
public final class ShiftService {

    private final Database database;

    public ShiftService(Database database) {
        this.database = database;
    }

    public Shift currentOpen(int userId) throws ServiceException {
        try {
            return database.withConnection(c -> {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT s.id, s.kode_shift, u.full_name, s.opened_at, s.closed_at, s.opening_float, "
                                + "s.expected_cash, s.counted_cash, s.variance, s.status "
                                + "FROM shifts s JOIN users u ON u.id = s.admin_id "
                                + "WHERE s.admin_id=? AND s.status='OPEN' ORDER BY s.opened_at DESC LIMIT 1")) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? map(rs) : null;
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat shift: " + e.getMessage(), e);
        }
    }

    public Shift open(SessionUser user, BigDecimal openingFloat) throws ServiceException {
        if (user == null || !user.can(Role.Permission.SHIFT)) {
            throw new ServiceException("Peran Anda tidak boleh membuka shift.");
        }
        if (openingFloat == null || openingFloat.signum() < 0) {
            throw new ServiceException("Modal awal tidak boleh negatif.");
        }
        try (ConnectionPool.Lease lease = database.borrow()) {
            Connection c = lease.connection();
            c.setAutoCommit(false);
            try {
                Shift existing = currentOpen(user.id());
                if (existing != null) {
                    throw new ServiceException("Masih ada shift terbuka (" + existing.kode() + "). Tutup dulu.");
                }
                String code = Codes.shift();
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO shifts (kode_shift, admin_id, opening_float) VALUES (?,?,?)")) {
                    ps.setString(1, code);
                    ps.setInt(2, user.id());
                    ps.setBigDecimal(3, Money.scale(openingFloat));
                    ps.executeUpdate();
                }
                Audit.record(c, user, "SHIFT_OPEN", "shifts", code, Money.plain(openingFloat));
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw new ServiceException("Gagal membuka shift: " + e.getMessage(), e);
            } catch (ServiceException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Tidak dapat menghubungi database: " + e.getMessage(), e);
        }
        return currentOpen(user.id());
    }

    /** Expected cash = float + cash taken in this shift, net of change handed back. */
    public BigDecimal expectedCash(int shiftId) throws ServiceException {
        try {
            return database.withConnection(c -> {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT (SELECT opening_float FROM shifts WHERE id=?) "
                                + "+ COALESCE(SUM(payment_cash - change_amount),0) "
                                + "FROM transaksi WHERE shift_id=? AND status='PAID'")) {
                    ps.setInt(1, shiftId);
                    ps.setInt(2, shiftId);
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? Money.scale(rs.getBigDecimal(1)) : Money.ZERO;
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal menghitung kas shift: " + e.getMessage(), e);
        }
    }

    public Shift close(SessionUser user, int shiftId, BigDecimal countedCash) throws ServiceException {
        if (user == null || !user.can(Role.Permission.SHIFT)) {
            throw new ServiceException("Peran Anda tidak boleh menutup shift.");
        }
        if (countedCash == null || countedCash.signum() < 0) {
            throw new ServiceException("Uang terhitung tidak boleh negatif.");
        }
        BigDecimal expected = expectedCash(shiftId);
        BigDecimal variance = Money.scale(countedCash.subtract(expected));
        try (ConnectionPool.Lease lease = database.borrow()) {
            Connection c = lease.connection();
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE shifts SET closed_at=NOW(), expected_cash=?, counted_cash=?, variance=?, "
                                + "status='CLOSED' WHERE id=? AND status='OPEN'")) {
                    ps.setBigDecimal(1, expected);
                    ps.setBigDecimal(2, Money.scale(countedCash));
                    ps.setBigDecimal(3, variance);
                    ps.setInt(4, shiftId);
                    if (ps.executeUpdate() != 1) {
                        throw new ServiceException("Shift tidak ditemukan atau sudah ditutup.");
                    }
                }
                Audit.record(c, user, "SHIFT_CLOSE", "shifts", String.valueOf(shiftId),
                        "selisih " + Money.plain(variance));
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw new ServiceException("Gagal menutup shift: " + e.getMessage(), e);
            } catch (ServiceException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Tidak dapat menghubungi database: " + e.getMessage(), e);
        }
        return byId(shiftId);
    }

    public Shift byId(int shiftId) throws ServiceException {
        try {
            return database.withConnection(c -> {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT s.id, s.kode_shift, u.full_name, s.opened_at, s.closed_at, s.opening_float, "
                                + "s.expected_cash, s.counted_cash, s.variance, s.status "
                                + "FROM shifts s JOIN users u ON u.id = s.admin_id WHERE s.id=?")) {
                    ps.setInt(1, shiftId);
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? map(rs) : null;
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat shift: " + e.getMessage(), e);
        }
    }

    public List<Shift> recent(int limit) throws ServiceException {
        List<Shift> out = new ArrayList<>();
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT s.id, s.kode_shift, u.full_name, s.opened_at, s.closed_at, s.opening_float, "
                                + "s.expected_cash, s.counted_cash, s.variance, s.status "
                                + "FROM shifts s JOIN users u ON u.id = s.admin_id "
                                + "ORDER BY s.opened_at DESC LIMIT ?")) {
                    ps.setInt(1, limit);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(map(rs));
                        }
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat daftar shift: " + e.getMessage(), e);
        }
        return out;
    }

    private static Shift map(ResultSet rs) throws SQLException {
        return new Shift(
                rs.getInt("id"),
                rs.getString("kode_shift"),
                rs.getString("full_name"),
                rs.getObject("opened_at", LocalDateTime.class),
                rs.getObject("closed_at", LocalDateTime.class),
                Money.scale(rs.getBigDecimal("opening_float")),
                rs.getBigDecimal("expected_cash") == null ? null : Money.scale(rs.getBigDecimal("expected_cash")),
                rs.getBigDecimal("counted_cash") == null ? null : Money.scale(rs.getBigDecimal("counted_cash")),
                rs.getBigDecimal("variance") == null ? null : Money.scale(rs.getBigDecimal("variance")),
                rs.getString("status"));
    }
}
