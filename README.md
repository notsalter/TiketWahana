# Sistem Manajemen Tiket Wahana

## Deskripsi Proyek
Aplikasi desktop untuk mengelola sistem tiket wahana (taman hiburan) menggunakan Java Swing dan database MySQL. Aplikasi ini memungkinkan administrator untuk mengelola wahana, transaksi tiket, dan laporan penjualan.

## Fitur Utama
- **Login System**: Autentikasi pengguna dengan role-based access
- **Dashboard Wahana**: Tampilan utama menampilkan semua wahana yang tersedia
- **Management Features**: Kelola wahana, transaksi tiket, laporan, dan pengaturan
- **Modern UI**: Interface yang modern dan user-friendly dengan hover effects

## Wahana yang Tersedia
1. **W001 - Kereta Gantung** (Rp 25.000)
2. **W002 - Sea World** (Rp 35.000)
3. **W003 - Otomotive Museum** (Rp 20.000)
4. **W004 - Dunia Fantasi** (Rp 40.000)
5. **W005 - Roller Coaster** (Rp 50.000)
6. **W006 - Water Park** (Rp 30.000)

## Teknologi yang Digunakan
- **Java 17+**: Bahasa pemrograman utama
- **Java Swing**: Framework GUI
- **MySQL 8.0**: Database management system
- **JDBC**: Database connectivity
- **Maven**: Build management
- **NetBeans**: IDE development

## Setup dan Instalasi

### 1. Prerequisites
- Java Development Kit (JDK) 17 atau lebih tinggi
- MySQL Server (XAMPP/WAMP recommended)
- Apache Maven 3.6+
- NetBeans IDE (optional)

### 2. Database Setup
1. Jalankan MySQL server (melalui XAMPP/WAMP)
2. Buka phpMyAdmin di `http://localhost/phpmyadmin`
3. Import file `database_schema.sql` untuk membuat database dan tabel
4. Database `ticket_wahana_db` akan terbuat otomatis
5. Pastikan MySQL berjalan di port 3306

### 3. Konfigurasi Database
Default configuration (dapat diubah di `DatabaseConnection.java`):
```
URL: jdbc:mysql://localhost:3306/ticket_wahana_db
Username: root
Password: (kosong)
```

### 4. Build dan Run Aplikasi

#### Via Command Line:
```bash
# Compile project
mvn clean compile

# Run application
mvn exec:java
```

#### Via NetBeans:
1. Buka project di NetBeans
2. Right-click project → "Clean and Build"
3. Right-click project → "Run"

#### Via IDE lain:
1. Compile semua .java files
2. Run `MainApp.java` sebagai main class

### 5. Login Credentials
Default login untuk testing:
- **Username**: `admin`
- **Password**: `admin`

## Struktur Database

### Tabel Utama:
- `users`: Data pengguna dan authentication
- `wahana`: Data wahana/atraksi
- `tiket`: Data tiket yang dijual
- `transaksi`: Data transaksi penjualan
- `transaksi_detail`: Detail item dalam transaksi
- `pengunjung_log`: Log keluar masuk pengunjung

### Views untuk Reporting:
- `view_tiket_aktif`: Tiket yang masih aktif
- `view_penjualan_harian`: Laporan penjualan harian
- `view_wahana_populer`: Wahana terpopuler

## Struktur Project
```
TiketWahana/
├── src/main/java/com/mycompany/tiketwahana/
│   ├── MainApp.java                 # Main application entry point
│   ├── database/
│   │   └── DatabaseConnection.java  # Database connection management
│   └── forms/
│       ├── LoginForm.java          # Login interface
│       └── MainForm.java           # Main dashboard
├── database_schema.sql             # Database schema
├── pom.xml                        # Maven configuration
└── README.md                      # This file
```

## Fitur Interface

### Login Form
- Modern login interface dengan validasi
- Support untuk berbagai role user
- Error handling yang user-friendly

### Main Dashboard
- **Header**: Judul aplikasi dan info user
- **Content Area**: Grid layout menampilkan semua wahana
- **Bottom Panel**: Tombol navigasi untuk fitur utama
- **Footer**: Informasi sistem

### Wahana Cards
- Display kode wahana dan status
- Informasi harga, kapasitas, dan durasi
- Hover effects untuk interaktivity
- Click untuk detail informasi

### Customization Buttons
- **Kelola Wahana**: Management wahana (coming soon)
- **Transaksi Tiket**: Penjualan tiket (coming soon)
- **Laporan**: Report dan analytics (coming soon)
- **Pengaturan**: System settings (coming soon)
- **Logout**: Keluar dari sistem

## Development Notes

### Design Patterns Used:
- MVC (Model-View-Controller) pattern
- Singleton pattern untuk database connection
- Observer pattern untuk UI events

### Color Scheme:
- Primary: Green (#4CAF50)
- Secondary: Blue (#3498DB)
- Accent: Orange (#E67E22)
- Background: Light Gray (#ECF0F1)

### Future Enhancements:
1. Implementasi CRUD untuk manajemen wahana
2. Sistem transaksi tiket real-time
3. Report generator dan analytics
4. Barcode/QR code untuk tiket
5. Payment gateway integration
6. Mobile app companion

## Troubleshooting

### Common Issues:
1. **Database Connection Failed**
   - Pastikan MySQL server berjalan
   - Check username/password di DatabaseConnection.java
   - Verify database exists

2. **Compilation Errors**
   - Check Java version (requires JDK 17+)
   - Verify Maven dependencies
   - Clean and rebuild project

3. **UI Display Issues**
   - Check system look and feel compatibility
   - Verify Swing components
   - Update graphics drivers

## Contact & Support
Developed by: Salter  
Project: TiketWahana Management System  
Version: 1.0.0  
Date: June 2025  

---
*Sistem Manajemen Tiket Wahana - Kelola semua wahana dan transaksi dengan mudah*
