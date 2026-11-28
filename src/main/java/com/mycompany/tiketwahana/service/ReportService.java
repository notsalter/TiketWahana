package com.mycompany.tiketwahana.service;

import com.mycompany.tiketwahana.core.Database;
import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.domain.ReportRow;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reporting reads tiket.tanggal_kunjungan (the date the guest visits), not the
 * moment the row happened to be inserted.
 */
public final class ReportService {

    private final Database database;

    public ReportService(Database database) {
        this.database = database;
    }

    public record Summary(BigDecimal revenue, int tickets, int transactions) {
    }

    public record Kpis(BigDecimal revenueToday, int ticketsToday, String busiestWahana, int busiestCount,
                       int capacityUsedToday, int capacityTotal) {
    }

    public List<ReportRow> daily(LocalDate from, LocalDate to) throws ServiceException {
        List<ReportRow> out = new ArrayList<>();
        String sql = "SELECT t.tanggal_kunjungan, w.kode_wahana, w.nama_wahana, w.durasi_menit, "
                + "COUNT(t.id) AS jumlah, w.harga_tiket, COALESCE(SUM(t.total_harga),0) AS total "
                + "FROM tiket t JOIN wahana w ON w.id = t.wahana_id "
                + "WHERE t.tanggal_kunjungan BETWEEN ? AND ? AND t.status <> 'REFUND' "
                + "GROUP BY t.tanggal_kunjungan, w.id, w.kode_wahana, w.nama_wahana, w.durasi_menit, w.harga_tiket "
                + "ORDER BY t.tanggal_kunjungan DESC, w.kode_wahana";
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setObject(1, from);
                    ps.setObject(2, to);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(new ReportRow(
                                    rs.getObject("tanggal_kunjungan", LocalDate.class),
                                    rs.getString("kode_wahana"),
                                    rs.getString("nama_wahana"),
                                    rs.getInt("durasi_menit"),
                                    rs.getInt("jumlah"),
                                    Money.scale(rs.getBigDecimal("harga_tiket")),
                                    Money.scale(rs.getBigDecimal("total"))));
                        }
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat laporan: " + e.getMessage(), e);
        }
        return out;
    }

    public Summary summarize(List<ReportRow> rows) {
        BigDecimal revenue = Money.ZERO;
        int tickets = 0;
        for (ReportRow row : rows) {
            revenue = revenue.add(row.total());
            tickets += row.jumlahTiket();
        }
        int transactions = rows.isEmpty() ? 0 : 0;
        return new Summary(Money.scale(revenue), tickets, transactions);
    }

    public Kpis kpis() throws ServiceException {
        try {
            return database.withConnection(c -> {
                BigDecimal revenue = Money.ZERO;
                int tickets = 0;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COALESCE(SUM(total_amount),0), COALESCE(SUM(total_tiket),0) FROM transaksi "
                                + "WHERE status='PAID' AND DATE(tanggal_transaksi)=CURDATE()")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            revenue = Money.scale(rs.getBigDecimal(1));
                            tickets = rs.getInt(2);
                        }
                    }
                }
                String busiest = "-";
                int busiestCount = 0;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT w.nama_wahana, COUNT(t.id) AS n FROM tiket t JOIN wahana w ON w.id=t.wahana_id "
                                + "WHERE t.tanggal_kunjungan=CURDATE() AND t.status<>'REFUND' "
                                + "GROUP BY w.id, w.nama_wahana ORDER BY n DESC LIMIT 1")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            busiest = rs.getString(1);
                            busiestCount = rs.getInt(2);
                        }
                    }
                }
                int capacityTotal = 0;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COALESCE(SUM(kapasitas_max),0) FROM wahana WHERE status='AKTIF'")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            capacityTotal = rs.getInt(1);
                        }
                    }
                }
                int used = soldToday(c);
                return new Kpis(revenue, tickets, busiest, busiestCount, used, capacityTotal);
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat ringkasan hari ini: " + e.getMessage(), e);
        }
    }

    private static int soldToday(java.sql.Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM tiket WHERE tanggal_kunjungan=CURDATE() AND status IN ('AKTIF','DIGUNAKAN')")) {
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Revenue per day for the last n days, oldest first, zero-filled. */
    public List<BigDecimal> revenueSeries(int days) throws ServiceException {
        List<BigDecimal> out = new ArrayList<>();
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT d.d, COALESCE(SUM(t.total_amount),0) FROM "
                                + "(SELECT CURDATE() - INTERVAL seq DAY AS d FROM "
                                + "(SELECT 0 seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 "
                                + "UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 "
                                + "UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 "
                                + "UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 "
                                + "UNION SELECT 20 UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 "
                                + "UNION SELECT 25 UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29) s) d "
                                + "LEFT JOIN transaksi t ON DATE(t.tanggal_transaksi)=d.d AND t.status='PAID' "
                                + "GROUP BY d.d ORDER BY d.d")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(Money.scale(rs.getBigDecimal(2)));
                        }
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat tren pendapatan: " + e.getMessage(), e);
        }
        return out;
    }

    /** [dayOfWeek 1..7][hour 8..21] ticket counts for the last eight weeks. */
    public int[][] heatmap() throws ServiceException {
        int[][] grid = new int[7][24];
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT DAYOFWEEK(tanggal_kunjungan) AS dow, HOUR(COALESCE(jam_kunjungan, created_at)) AS hr, "
                                + "COUNT(*) AS n FROM tiket WHERE tanggal_kunjungan >= CURDATE() - INTERVAL 8 WEEK "
                                + "GROUP BY dow, hr")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int dow = rs.getInt("dow");
                            int hr = Math.max(0, Math.min(23, rs.getInt("hr")));
                            grid[dow - 1][hr] += rs.getInt("n");
                        }
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat peta kunjungan: " + e.getMessage(), e);
        }
        return grid;
    }

    public List<TicketRowView> recentTransactions(int limit) throws ServiceException {
        List<TicketRowView> out = new ArrayList<>();
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT id, kode_transaksi, nama_customer, total_tiket, total_amount, status, tanggal_transaksi "
                                + "FROM transaksi ORDER BY tanggal_transaksi DESC LIMIT ?")) {
                    ps.setInt(1, limit);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            out.add(new TicketRowView(
                                    rs.getInt("id"),
                                    rs.getString("kode_transaksi"),
                                    rs.getString("nama_customer"),
                                    rs.getInt("total_tiket"),
                                    Money.scale(rs.getBigDecimal("total_amount")),
                                    rs.getString("status"),
                                    rs.getTimestamp("tanggal_transaksi").toLocalDateTime()));
                        }
                    }
                }
            });
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat transaksi: " + e.getMessage(), e);
        }
        return out;
    }

    public record TicketRowView(int id, String kode, String customer, int tickets, BigDecimal total, String status,
                                java.time.LocalDateTime at) {
    }

    static Date date(LocalDate d) {
        return Date.valueOf(d);
    }
}
