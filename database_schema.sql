-- Ticket Wahana Management System Database Schema
-- Database: ticket_wahana_db
-- Run this script in phpMyAdmin

CREATE DATABASE IF NOT EXISTS ticket_wahana_db;
USE ticket_wahana_db;

-- Users table for login authentication
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN') DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Wahana (Attractions) table
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
);

-- Tiket (Tickets) table
CREATE TABLE IF NOT EXISTS tiket (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_tiket VARCHAR(20) UNIQUE NOT NULL,
    nama_pengunjung VARCHAR(100) NOT NULL,
    no_telepon VARCHAR(15),
    wahana_id INT NOT NULL,
    tanggal_kunjungan DATE NOT NULL,
    jam_kunjungan TIME,
    jumlah_tiket INT DEFAULT 1,
    total_harga DECIMAL(10,2) NOT NULL,
    status ENUM('AKTIF', 'DIGUNAKAN', 'EXPIRED') DEFAULT 'AKTIF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (wahana_id) REFERENCES wahana(id) ON DELETE RESTRICT
);

-- Transaksi (Transactions) table
CREATE TABLE IF NOT EXISTS transaksi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode_transaksi VARCHAR(20) UNIQUE NOT NULL,
    tanggal_transaksi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    admin_id INT,
    nama_customer VARCHAR(100) NOT NULL,
    total_tiket INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_amount DECIMAL(10,2) NOT NULL,
    change_amount DECIMAL(10,2) NOT NULL,
    metode_pembayaran ENUM('CASH', 'DEBIT', 'QRIS') DEFAULT 'CASH',
    notes TEXT,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Transaksi detail table
CREATE TABLE IF NOT EXISTS transaksi_detail (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaksi_id INT NOT NULL,
    tiket_id INT NOT NULL,
    wahana_id INT NOT NULL,
    jumlah_tiket INT NOT NULL,
    harga_satuan DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (transaksi_id) REFERENCES transaksi(id) ON DELETE CASCADE,
    FOREIGN KEY (tiket_id) REFERENCES tiket(id) ON DELETE RESTRICT,
    FOREIGN KEY (wahana_id) REFERENCES wahana(id) ON DELETE RESTRICT
);

-- Pengunjung (Visitors) log table
CREATE TABLE IF NOT EXISTS pengunjung_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tiket_id INT NOT NULL,
    waktu_masuk TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    waktu_keluar TIMESTAMP NULL,
    status ENUM('MASUK', 'KELUAR') DEFAULT 'MASUK',
    admin_id INT,
    FOREIGN KEY (tiket_id) REFERENCES tiket(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Insert default data
INSERT INTO users (username, password, full_name, role) VALUES 
('admin', 'admin', 'Administrator', 'ADMIN');

INSERT INTO wahana (kode_wahana, nama_wahana, deskripsi, harga_tiket, kapasitas_max, durasi_menit) VALUES 
('W001', 'Kereta Gantung', 'Nikmati pemandangan dari ketinggian dengan kereta gantung yang aman dan nyaman', 25000, 40, 45),
('W002', 'Sea World', 'Jelajahi dunia bawah laut dengan berbagai koleksi ikan dan biota laut', 35000, 100, 60),
('W003', 'Otomotive Museum', 'Museum koleksi mobil klasik dan modern dari berbagai era', 20000, 50, 90),
('W004', 'Dunia Fantasi', 'Wahana permainan seru untuk keluarga dengan berbagai atraksi menarik', 40000, 80, 120),
('W005', 'Roller Coaster', 'Wahana ekstrem yang menantang adrenalin dengan loop dan tanjakan tinggi', 50000, 24, 5),
('W006', 'Water Park', 'Kolam renang dengan berbagai wahana air yang menyegarkan', 30000, 150, 180);

-- Create indexes for better performance
CREATE INDEX idx_tiket_kode ON tiket(kode_tiket);
CREATE INDEX idx_tiket_tanggal ON tiket(tanggal_kunjungan);
CREATE INDEX idx_transaksi_tanggal ON transaksi(tanggal_transaksi);
CREATE INDEX idx_transaksi_kode ON transaksi(kode_transaksi);
CREATE INDEX idx_wahana_kode ON wahana(kode_wahana);

-- Views for reporting
CREATE VIEW view_tiket_aktif AS
SELECT 
    t.id,
    t.kode_tiket,
    t.nama_pengunjung,
    t.no_telepon,
    w.nama_wahana,
    w.kode_wahana,
    t.tanggal_kunjungan,
    t.jam_kunjungan,
    t.jumlah_tiket,
    t.total_harga,
    t.status
FROM tiket t
JOIN wahana w ON t.wahana_id = w.id
WHERE t.status = 'AKTIF'
ORDER BY t.tanggal_kunjungan DESC;

CREATE VIEW view_penjualan_harian AS
SELECT 
    DATE(tr.tanggal_transaksi) as tanggal,
    COUNT(*) as total_transaksi,
    SUM(tr.total_tiket) as total_tiket_terjual,
    SUM(tr.total_amount) as total_pendapatan,
    AVG(tr.total_amount) as rata_rata_transaksi
FROM transaksi tr
GROUP BY DATE(tr.tanggal_transaksi)
ORDER BY tanggal DESC;

CREATE VIEW view_wahana_populer AS
SELECT 
    w.kode_wahana,
    w.nama_wahana,
    w.harga_tiket,
    COUNT(t.id) as total_tiket_terjual,
    SUM(t.total_harga) as total_pendapatan
FROM wahana w
LEFT JOIN tiket t ON w.id = t.wahana_id
WHERE t.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
GROUP BY w.id, w.kode_wahana, w.nama_wahana, w.harga_tiket
ORDER BY total_tiket_terjual DESC;
