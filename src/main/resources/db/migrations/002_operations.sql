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
