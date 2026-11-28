-- TiketWahana 2.0 schema. The app applies these numbered migrations automatically.
-- This file is also importable in phpMyAdmin from a fresh database.

-- 001 initial schema, carried over from database_schema.sql (v1).
-- Foreign keys are explicitly named so later migrations can alter them.

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL DEFAULT '',
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN') DEFAULT 'ADMIN',
    must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wahana (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_wahana VARCHAR(10) UNIQUE NOT NULL,
    nama_wahana VARCHAR(100) NOT NULL,
    deskripsi TEXT,
    harga_tiket DECIMAL(10,2) NOT NULL,
    kapasitas_max INT DEFAULT 50,
    durasi_menit INT DEFAULT 30,
    status ENUM('AKTIF', 'MAINTENANCE', 'TUTUP') DEFAULT 'AKTIF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tiket (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_tiket VARCHAR(20) UNIQUE NOT NULL,
    nama_pengunjung VARCHAR(100) NOT NULL,
    no_telepon VARCHAR(20),
    wahana_id INT NOT NULL,
    tanggal_kunjungan DATE NOT NULL,
    jam_kunjungan TIME,
    jumlah_tiket INT DEFAULT 1,
    total_harga DECIMAL(10,2) NOT NULL,
    status ENUM('AKTIF', 'DIGUNAKAN', 'EXPIRED') DEFAULT 'AKTIF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tiket_wahana FOREIGN KEY (wahana_id) REFERENCES wahana(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS transaksi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_transaksi VARCHAR(24) UNIQUE NOT NULL,
    tanggal_transaksi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    admin_id INT,
    nama_customer VARCHAR(100) NOT NULL,
    total_tiket INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_amount DECIMAL(10,2) NOT NULL,
    change_amount DECIMAL(10,2) NOT NULL,
    metode_pembayaran ENUM('CASH', 'DEBIT', 'QRIS') DEFAULT 'CASH',
    notes TEXT,
    CONSTRAINT fk_transaksi_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS transaksi_detail (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaksi_id INT NOT NULL,
    tiket_id INT NOT NULL,
    wahana_id INT NOT NULL,
    jumlah_tiket INT NOT NULL,
    harga_satuan DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detail_transaksi FOREIGN KEY (transaksi_id) REFERENCES transaksi(id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_tiket FOREIGN KEY (tiket_id) REFERENCES tiket(id) ON DELETE RESTRICT,
    CONSTRAINT fk_detail_wahana FOREIGN KEY (wahana_id) REFERENCES wahana(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pengunjung_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tiket_id INT NOT NULL,
    waktu_masuk TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    waktu_keluar TIMESTAMP NULL,
    status ENUM('MASUK', 'KELUAR') DEFAULT 'MASUK',
    admin_id INT,
    CONSTRAINT fk_log_tiket FOREIGN KEY (tiket_id) REFERENCES tiket(id) ON DELETE CASCADE,
    CONSTRAINT fk_log_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @idx_tiket_date = (SELECT IF(COUNT(*) = 0,
    'CREATE INDEX idx_tiket_tanggal ON tiket(tanggal_kunjungan)', 'SELECT 1')
    FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tiket' AND INDEX_NAME = 'idx_tiket_tanggal');
PREPARE idx_tiket_date_stmt FROM @idx_tiket_date; EXECUTE idx_tiket_date_stmt; DEALLOCATE PREPARE idx_tiket_date_stmt;
SET @idx_trans_date = (SELECT IF(COUNT(*) = 0,
    'CREATE INDEX idx_transaksi_tanggal ON transaksi(tanggal_transaksi)', 'SELECT 1')
    FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transaksi' AND INDEX_NAME = 'idx_transaksi_tanggal');
PREPARE idx_trans_date_stmt FROM @idx_trans_date; EXECUTE idx_trans_date_stmt; DEALLOCATE PREPARE idx_trans_date_stmt;

CREATE OR REPLACE VIEW view_tiket_aktif AS
SELECT t.id, t.kode_tiket, t.nama_pengunjung, t.no_telepon,
       w.nama_wahana, w.kode_wahana, t.tanggal_kunjungan, t.jam_kunjungan,
       t.jumlah_tiket, t.total_harga, t.status
FROM tiket t
JOIN wahana w ON t.wahana_id = w.id
WHERE t.status = 'AKTIF'
ORDER BY t.tanggal_kunjungan DESC;

CREATE OR REPLACE VIEW view_penjualan_harian AS
SELECT DATE(tr.tanggal_transaksi) AS tanggal,
       COUNT(*) AS total_transaksi,
       SUM(tr.total_tiket) AS total_tiket_terjual,
       SUM(tr.total_amount) AS total_pendapatan,
       AVG(tr.total_amount) AS rata_rata_transaksi
FROM transaksi tr
GROUP BY DATE(tr.tanggal_transaksi)
ORDER BY tanggal DESC;

INSERT IGNORE INTO wahana (kode_wahana, nama_wahana, deskripsi, harga_tiket, kapasitas_max, durasi_menit) VALUES
('W001', 'Kereta Gantung', 'Nikmati pemandangan dari ketinggian dengan kereta gantung yang aman dan nyaman', 25000, 40, 45),
('W002', 'Sea World', 'Jelajahi dunia bawah laut dengan berbagai koleksi ikan dan biota laut', 35000, 100, 60),
('W003', 'Otomotive Museum', 'Museum koleksi mobil klasik dan modern dari berbagai era', 20000, 50, 90),
('W004', 'Dunia Fantasi', 'Wahana permainan seru untuk keluarga dengan berbagai atraksi menarik', 40000, 80, 120),
('W005', 'Roller Coaster', 'Wahana ekstrem yang menantang adrenalin dengan loop dan tanjakan tinggi', 50000, 24, 5),
('W006', 'Water Park', 'Kolam renang dengan berbagai wahana air yang menyegarkan', 30000, 150, 180);


CREATE TABLE IF NOT EXISTS schema_version (version VARCHAR(20) PRIMARY KEY, applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB;

-- 002: RBAC, hashed credentials, shifts, audit trail, ledger-correct sales,
-- and reporting views that agree with the transaction table.

-- 1. Roles and credentials -------------------------------------------------
ALTER TABLE users
    MODIFY COLUMN role ENUM('ADMIN','KASIR','MANAGER') NOT NULL DEFAULT 'KASIR';

-- Existing v1 installs have a plaintext password column but no hash columns.
-- INFORMATION_SCHEMA makes this idempotent on both fresh and upgraded installs.
SET @add_hash = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '' AFTER username',
    'SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'password_hash');
PREPARE add_hash_stmt FROM @add_hash;
EXECUTE add_hash_stmt;
DEALLOCATE PREPARE add_hash_stmt;
SET @add_must_change = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE users ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'must_change_password');
PREPARE add_must_change_stmt FROM @add_must_change;
EXECUTE add_must_change_stmt;
DEALLOCATE PREPARE add_must_change_stmt;

-- 2. Shifts and audit log --------------------------------------------------
CREATE TABLE IF NOT EXISTS shifts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_shift VARCHAR(24) UNIQUE NOT NULL,
    admin_id INT NOT NULL,
    opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NULL,
    opening_float DECIMAL(12,2) NOT NULL DEFAULT 0,
    expected_cash DECIMAL(12,2) NULL,
    counted_cash DECIMAL(12,2) NULL,
    variance DECIMAL(12,2) NULL,
    status ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',
    CONSTRAINT fk_shift_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id INT NULL,
    action VARCHAR(40) NOT NULL,
    entity VARCHAR(40) NULL,
    entity_id VARCHAR(40) NULL,
    detail VARCHAR(500) NULL,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_audit_at ON audit_log(at);

-- 3. Ledger-correct sales --------------------------------------------------
ALTER TABLE transaksi
    ADD COLUMN shift_id INT NULL,
    ADD COLUMN subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN discount DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN tax DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN payment_cash DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN payment_debit DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN payment_qris DECIMAL(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN status ENUM('PAID','VOID') NOT NULL DEFAULT 'PAID',
    ADD CONSTRAINT fk_transaksi_shift FOREIGN KEY (shift_id) REFERENCES shifts(id) ON DELETE SET NULL;

-- transaksi_detail no longer points at a single ticket; one line owns N tickets.
ALTER TABLE transaksi_detail DROP FOREIGN KEY fk_detail_tiket;
ALTER TABLE transaksi_detail DROP COLUMN tiket_id;

ALTER TABLE tiket
    ADD COLUMN transaksi_detail_id INT NULL,
    ADD COLUMN redeemed_at TIMESTAMP NULL,
    ADD COLUMN redeemed_by INT NULL,
    MODIFY COLUMN status ENUM('AKTIF','DIGUNAKAN','EXPIRED','REFUND') NOT NULL DEFAULT 'AKTIF',
    ADD CONSTRAINT fk_tiket_detail FOREIGN KEY (transaksi_detail_id) REFERENCES transaksi_detail(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_tiket_redeemer FOREIGN KEY (redeemed_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_tiket_detail ON tiket(transaksi_detail_id);

-- 4. Views that agree with the ledger -------------------------------------
CREATE OR REPLACE VIEW view_wahana_populer AS
SELECT w.kode_wahana, w.nama_wahana, w.harga_tiket,
       COUNT(t.id) AS total_tiket_terjual,
       COALESCE(SUM(t.total_harga), 0) AS total_pendapatan
FROM wahana w
LEFT JOIN tiket t ON w.id = t.wahana_id
     AND t.status <> 'REFUND'
     AND t.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
GROUP BY w.id, w.kode_wahana, w.nama_wahana, w.harga_tiket
ORDER BY total_tiket_terjual DESC;

CREATE OR REPLACE VIEW view_penjualan_harian AS
SELECT DATE(tr.tanggal_transaksi) AS tanggal,
       COUNT(*) AS total_transaksi,
       SUM(tr.total_tiket) AS total_tiket_terjual,
       SUM(tr.total_amount) AS total_pendapatan,
       AVG(tr.total_amount) AS rata_rata_transaksi
FROM transaksi tr
WHERE tr.status = 'PAID'
GROUP BY DATE(tr.tanggal_transaksi)
ORDER BY tanggal DESC;


INSERT IGNORE INTO schema_version (version) VALUES ('001'), ('002');
