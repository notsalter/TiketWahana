package com.mycompany.tiketwahana.service;

import com.mycompany.tiketwahana.core.Audit;
import com.mycompany.tiketwahana.core.Codes;
import com.mycompany.tiketwahana.core.Database;
import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.core.Role;
import com.mycompany.tiketwahana.core.SessionUser;
import com.mycompany.tiketwahana.domain.CartLine;
import com.mycompany.tiketwahana.domain.Sale;
import com.mycompany.tiketwahana.domain.Wahana;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The only place money is written. One sale = one transaction: transaksi row,
 * one transaksi_detail per wahana, one tiket per seat, and the invariant
 * SUM(detail.subtotal) == transaksi.total_amount == SUM(tiket.total_harga).
 */
public final class SalesService {

    /** No PPN line in this park; the column exists so the receipt can show it. */
    private static final BigDecimal TAX_RATE = BigDecimal.ZERO;

    private final Database database;
    private final WahanaService wahanaService;

    public SalesService(Database database) {
        this.database = database;
        this.wahanaService = new WahanaService(database);
    }

    public Sale.Result sell(SessionUser user, Sale.Request request) throws ServiceException {
        require(user, Role.Permission.SELL, "Peran Anda tidak boleh menjual tiket.");
        validate(request);

        try (com.mycompany.tiketwahana.core.ConnectionPool.Lease lease = database.borrow()) {
            Connection c = lease.connection();
            c.setAutoCommit(false);
            try {
                int shiftId = openShiftId(c, user.id());
                if (shiftId < 0) {
                    throw new ServiceException("Buka shift dulu sebelum menjual tiket.");
                }
                for (CartLine line : request.lines()) {
                    ensureCapacity(c, line.wahana(), request.visitDate(), line.quantity());
                }

                BigDecimal subtotal = Money.ZERO;
                for (CartLine line : request.lines()) {
                    subtotal = subtotal.add(line.subtotal());
                }
                subtotal = Money.scale(subtotal);
                BigDecimal discount = Money.scale(request.discount().min(subtotal).max(Money.ZERO));
                BigDecimal taxable = subtotal.subtract(discount);
                BigDecimal tax = Money.scale(taxable.multiply(TAX_RATE));
                BigDecimal total = Money.scale(taxable.add(tax));
                BigDecimal paid = Money.scale(request.paid());
                if (paid.compareTo(total) < 0) {
                    throw new ServiceException("Pembayaran kurang " + Money.format(total.subtract(paid)) + ".");
                }
                BigDecimal change = Money.scale(paid.subtract(total));
                if (change.compareTo(Money.scale(request.cash())) > 0) {
                    throw new ServiceException("Kembalian melebihi uang tunai yang diterima.");
                }

                Inserted inserted = insertTransaction(c, user, shiftId, request, subtotal, discount, tax,
                        total, paid, change);
                int transactionId = inserted.id();
                String transactionCode = inserted.code();

                List<String> ticketCodes = new ArrayList<>();
                for (CartLine line : request.lines()) {
                    int detailId = insertDetail(c, transactionId, line);
                    for (int seat = 0; seat < line.quantity(); seat++) {
                        ticketCodes.add(insertTicket(c, detailId, line.wahana(), request));
                    }
                }
                Audit.record(c, user, "SALE", "transaksi", String.valueOf(transactionId),
                        transactionCode + " " + Money.plain(total) + " " + request.ticketCount() + " tiket");
                c.commit();
                return new Sale.Result(transactionId, transactionCode, subtotal, discount, tax, total, paid, change,
                        request.ticketCount(), ticketCodes);
            } catch (SQLException e) {
                c.rollback();
                throw new ServiceException("Transaksi gagal dan dibatalkan: " + e.getMessage(), e);
            } catch (ServiceException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Tidak dapat menghubungi database: " + e.getMessage(), e);
        }
    }

    /** Marks a sale void and returns its seats to capacity. */
    public void voidSale(SessionUser user, int transactionId, String reason) throws ServiceException {
        require(user, Role.Permission.VOID, "Peran Anda tidak boleh membatalkan transaksi.");
        try (com.mycompany.tiketwahana.core.ConnectionPool.Lease lease = database.borrow()) {
            Connection c = lease.connection();
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE transaksi SET status='VOID' WHERE id=? AND status='PAID'")) {
                    ps.setInt(1, transactionId);
                    if (ps.executeUpdate() != 1) {
                        throw new ServiceException("Transaksi tidak ditemukan atau sudah dibatalkan.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE tiket t JOIN transaksi_detail d ON t.transaksi_detail_id = d.id "
                                + "SET t.status='REFUND' WHERE d.transaksi_id=?")) {
                    ps.setInt(1, transactionId);
                    ps.executeUpdate();
                }
                Audit.record(c, user, "VOID", "transaksi", String.valueOf(transactionId), reason);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw new ServiceException("Gagal membatalkan transaksi: " + e.getMessage(), e);
            } catch (ServiceException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Tidak dapat menghubungi database: " + e.getMessage(), e);
        }
    }

    private record Inserted(int id, String code) {
    }

    private Inserted insertTransaction(Connection c, SessionUser user, int shiftId, Sale.Request request,
                                  BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total,
                                  BigDecimal paid, BigDecimal change) throws SQLException {
        String sql = "INSERT INTO transaksi (kode_transaksi, admin_id, shift_id, nama_customer, total_tiket, "
                + "subtotal, discount, tax, total_amount, payment_amount, payment_cash, payment_debit, payment_qris, "
                + "change_amount, metode_pembayaran, notes, status) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, 'PAID')";
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = Codes.transaction();
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, code);
                ps.setInt(2, user.id());
                if (shiftId > 0) {
                    ps.setInt(3, shiftId);
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setString(4, request.customer().trim());
                ps.setInt(5, request.ticketCount());
                ps.setBigDecimal(6, subtotal);
                ps.setBigDecimal(7, discount);
                ps.setBigDecimal(8, tax);
                ps.setBigDecimal(9, total);
                ps.setBigDecimal(10, paid);
                ps.setBigDecimal(11, Money.scale(request.cash()));
                ps.setBigDecimal(12, Money.scale(request.debit()));
                ps.setBigDecimal(13, Money.scale(request.qris()));
                ps.setBigDecimal(14, change);
                ps.setString(15, dominantMethod(request));
                ps.setString(16, request.notes());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Kode transaksi gagal dibuat");
                    }
                    return new Inserted(keys.getInt(1), code);
                }
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    continue;
                }
                throw e;
            }
        }
        throw new SQLException("Kode transaksi gagal dibuat setelah 5 percobaan");
    }

    private static String dominantMethod(Sale.Request r) {
        BigDecimal cash = Money.scale(r.cash());
        BigDecimal debit = Money.scale(r.debit());
        BigDecimal qris = Money.scale(r.qris());
        if (debit.compareTo(cash) >= 0 && debit.compareTo(qris) >= 0 && debit.signum() > 0) {
            return "DEBIT";
        }
        if (qris.compareTo(cash) > 0) {
            return "QRIS";
        }
        return "CASH";
    }

    private int insertDetail(Connection c, int transactionId, CartLine line) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO transaksi_detail (transaksi_id, wahana_id, jumlah_tiket, harga_satuan, subtotal) "
                        + "VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transactionId);
            ps.setInt(2, line.wahana().id());
            ps.setInt(3, line.quantity());
            ps.setBigDecimal(4, line.wahana().harga());
            ps.setBigDecimal(5, line.subtotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Detail transaksi gagal dibuat");
                }
                return keys.getInt(1);
            }
        }
    }

    private String insertTicket(Connection c, int detailId, Wahana wahana, Sale.Request request) throws SQLException {
        String sql = "INSERT INTO tiket (kode_tiket, nama_pengunjung, no_telepon, wahana_id, transaksi_detail_id, "
                + "tanggal_kunjungan, jumlah_tiket, total_harga, status) VALUES (?,?,?,?,?,?,1,?, 'AKTIF')";
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = Codes.ticket();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, code);
                ps.setString(2, request.customer().trim());
                ps.setString(3, request.phone() == null || request.phone().isBlank() ? null : request.phone().trim());
                ps.setInt(4, wahana.id());
                ps.setInt(5, detailId);
                ps.setObject(6, request.visitDate());
                ps.setBigDecimal(7, wahana.harga());
                ps.executeUpdate();
                return code;
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    continue;
                }
                throw e;
            }
        }
        throw new SQLException("Kode tiket gagal dibuat setelah 5 percobaan");
    }

    private void ensureCapacity(Connection c, Wahana wahana, LocalDate date, int quantity)
            throws SQLException, ServiceException {
        int sold = wahanaService.seatsSold(c, wahana.id(), date);
        int remaining = wahana.kapasitas() - sold;
        if (quantity > remaining) {
            throw new ServiceException(String.format(
                    "%s tinggal %d tempat untuk %s, tidak cukup untuk %d tiket.",
                    wahana.nama(), Math.max(0, remaining), DateText.format(date), quantity));
        }
    }

    private int openShiftId(Connection c, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id FROM shifts WHERE admin_id=? AND status='OPEN' ORDER BY opened_at DESC LIMIT 1")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private static void validate(Sale.Request request) throws ServiceException {
        if (request == null) {
            throw new ServiceException("Transaksi kosong.");
        }
        if (request.customer() == null || request.customer().trim().isEmpty()) {
            throw new ServiceException("Nama pengunjung harus diisi.");
        }
        if (request.customer().trim().length() > 100) {
            throw new ServiceException("Nama pengunjung maksimal 100 karakter.");
        }
        if (request.visitDate() == null) {
            throw new ServiceException("Tanggal kunjungan harus dipilih.");
        }
        if (request.visitDate().isBefore(LocalDate.now())) {
            throw new ServiceException("Tanggal kunjungan tidak boleh di masa lalu.");
        }
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new ServiceException("Belum ada tiket di keranjang.");
        }
        for (CartLine line : request.lines()) {
            if (line.quantity() <= 0) {
                throw new ServiceException("Jumlah tiket harus lebih dari 0.");
            }
            if (line.quantity() > 50) {
                throw new ServiceException("Maksimal 50 tiket per baris; buat baris baru untuk jumlah lebih besar.");
            }
            if (!line.wahana().sellable()) {
                throw new ServiceException(line.wahana().nama() + " sedang tidak dijual.");
            }
        }
    }

    private static void require(SessionUser user, Role.Permission permission, String message) throws ServiceException {
        if (user == null || !user.can(permission)) {
            throw new ServiceException(message);
        }
    }
}
