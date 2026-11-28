package com.mycompany.tiketwahana.service;

import com.mycompany.tiketwahana.core.Database;
import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.core.SessionUser;
import com.mycompany.tiketwahana.domain.Wahana;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class WahanaService {

    private final Database database;

    public WahanaService(Database database) {
        this.database = database;
    }

    public List<Wahana> list(boolean onlySellable) throws ServiceException {
        String sql = "SELECT id, kode_wahana, nama_wahana, deskripsi, harga_tiket, kapasitas_max, durasi_menit, status "
                + "FROM wahana " + (onlySellable ? "WHERE status = 'AKTIF' " : "") + "ORDER BY kode_wahana";
        List<Wahana> out = new ArrayList<>();
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(map(rs));
                    }
                }
            });
            return out;
        } catch (SQLException e) {
            throw new ServiceException("Gagal memuat data wahana: " + e.getMessage(), e);
        }
    }

    public Wahana byKode(Connection c, String kode) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, kode_wahana, nama_wahana, deskripsi, harga_tiket, kapasitas_max, durasi_menit, status "
                        + "FROM wahana WHERE kode_wahana = ?")) {
            ps.setString(1, kode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Seats already sold for a wahana on a visit date; refunds do not occupy capacity. */
    public int seatsSold(Connection c, int wahanaId, LocalDate date) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(jumlah_tiket),0) FROM tiket WHERE wahana_id = ? AND tanggal_kunjungan = ? "
                        + "AND status IN ('AKTIF','DIGUNAKAN')")) {
            ps.setInt(1, wahanaId);
            ps.setObject(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void create(SessionUser user, String kode, String nama, String deskripsi, BigDecimal harga,
                       int kapasitas, int durasi, Wahana.Status status) throws ServiceException {
        requireManage(user);
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO wahana (kode_wahana, nama_wahana, deskripsi, harga_tiket, kapasitas_max, "
                                + "durasi_menit, status) VALUES (?,?,?,?,?,?,?)")) {
                    ps.setString(1, kode);
                    ps.setString(2, nama);
                    ps.setString(3, deskripsi);
                    ps.setBigDecimal(4, Money.scale(harga));
                    ps.setInt(5, kapasitas);
                    ps.setInt(6, durasi);
                    ps.setString(7, status.name());
                    ps.executeUpdate();
                }
                com.mycompany.tiketwahana.core.Audit.record(c, user, "WAHANA_CREATE", "wahana", kode, nama);
            });
        } catch (SQLException e) {
            throw new ServiceException(friendly(e, "menambah"), e);
        }
    }

    public void update(SessionUser user, int id, String kode, String nama, String deskripsi, BigDecimal harga,
                       int kapasitas, int durasi, Wahana.Status status) throws ServiceException {
        requireManage(user);
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE wahana SET kode_wahana=?, nama_wahana=?, deskripsi=?, harga_tiket=?, kapasitas_max=?, "
                                + "durasi_menit=?, status=? WHERE id=?")) {
                    ps.setString(1, kode);
                    ps.setString(2, nama);
                    ps.setString(3, deskripsi);
                    ps.setBigDecimal(4, Money.scale(harga));
                    ps.setInt(5, kapasitas);
                    ps.setInt(6, durasi);
                    ps.setString(7, status.name());
                    ps.setInt(8, id);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Wahana tidak ditemukan");
                    }
                }
                com.mycompany.tiketwahana.core.Audit.record(c, user, "WAHANA_UPDATE", "wahana", String.valueOf(id), nama);
            });
        } catch (SQLException e) {
            throw new ServiceException(friendly(e, "memperbarui"), e);
        }
    }

    public void delete(SessionUser user, int id) throws ServiceException {
        requireManage(user);
        try {
            database.run(c -> {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM wahana WHERE id=?")) {
                    ps.setInt(1, id);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Wahana tidak ditemukan");
                    }
                }
                com.mycompany.tiketwahana.core.Audit.record(c, user, "WAHANA_DELETE", "wahana", String.valueOf(id), null);
            });
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("foreign key")) {
                throw new ServiceException("Wahana ini sudah punya tiket terjual, jadi tidak bisa dihapus. "
                        + "Ubah statusnya menjadi TUTUP.", e);
            }
            throw new ServiceException("Gagal menghapus wahana: " + e.getMessage(), e);
        }
    }

    public int seatsSoldToday(int wahanaId) throws ServiceException {
        try {
            return database.withConnection(c -> seatsSold(c, wahanaId, LocalDate.now()));
        } catch (SQLException e) {
            throw new ServiceException("Gagal menghitung kapasitas: " + e.getMessage(), e);
        }
    }

    private static void requireManage(SessionUser user) throws ServiceException {
        if (user == null || !user.can(com.mycompany.tiketwahana.core.Role.Permission.MANAGE_WAHANA)) {
            throw new ServiceException("Peran Anda tidak boleh mengubah data wahana.");
        }
    }

    private static String friendly(SQLException e, String verb) {
        String m = e.getMessage() == null ? "" : e.getMessage();
        if (m.contains("Duplicate entry")) {
            return "Kode wahana sudah dipakai. Gunakan kode lain.";
        }
        if (m.contains("foreign key")) {
            return "Wahana ini masih dipakai tiket lain.";
        }
        return "Gagal " + verb + " wahana: " + m;
    }

    static Wahana map(ResultSet rs) throws SQLException {
        return new Wahana(
                rs.getInt("id"),
                rs.getString("kode_wahana"),
                rs.getString("nama_wahana"),
                rs.getString("deskripsi"),
                Money.scale(rs.getBigDecimal("harga_tiket")),
                rs.getInt("kapasitas_max"),
                rs.getInt("durasi_menit"),
                Wahana.Status.of(rs.getString("status")));
    }
}
