package com.mycompany.tiketwahana.database;

import java.sql.*;
import javax.swing.JOptionPane;

/**
 * Kelas untuk mengelola koneksi database MySQL
 * Menggunakan pola Singleton untuk memastikan hanya ada satu koneksi aktif
 */
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/ticket_wahana_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    
    private static Connection connection = null;
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Koneksi database berhasil!");
            }
            return connection;
        } catch (ClassNotFoundException e) {            JOptionPane.showMessageDialog(null, "Driver MySQL tidak ditemukan: " + e.getMessage());
            System.err.println("Driver MySQL tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {            JOptionPane.showMessageDialog(null, "Koneksi database gagal: " + e.getMessage());
            System.err.println("Koneksi database gagal: " + e.getMessage());
        }
        return null;
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Koneksi database ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("Kesalahan saat menutup koneksi: " + e.getMessage());
        }
    }
}
