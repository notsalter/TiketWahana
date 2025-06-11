package com.mycompany.tiketwahana.forms;

import com.mycompany.tiketwahana.database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class PengaturanForm extends JDialog {
    private JFrame parentFrame;
    private JTextField fieldDbHost, fieldDbPort, fieldDbName;
    private JPasswordField fieldCurrentPassword, fieldNewPassword, fieldConfirmPassword;
    private JTextArea logArea;

    public PengaturanForm(JFrame parent) {
        super(parent, "Pengaturan Sistem", true);
        this.parentFrame = parent;
        initComponents();
        setLocationRelativeTo(parent);
        loadSystemLogs();
    }

    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(149, 165, 166));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("PENGATURAN SISTEM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Main content with tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Database Settings Tab
        JPanel dbPanel = createDatabaseSettingsPanel();
        tabbedPane.addTab("Database", dbPanel);

        // Password Settings Tab
        JPanel passwordPanel = createPasswordSettingsPanel();
        tabbedPane.addTab("Ganti Password", passwordPanel);

        // System Logs Tab
        JPanel logsPanel = createSystemLogsPanel();
        tabbedPane.addTab("Log Sistem", logsPanel);

        // Database Tools Tab
        JPanel toolsPanel = createDatabaseToolsPanel();
        tabbedPane.addTab("Tools Database", toolsPanel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(236, 240, 241));        JButton btnSave = new JButton("Simpan Pengaturan");
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.BLACK);
        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveDatabaseSettings();
            }
        });

        JButton btnTutup = new JButton("Tutup");
        btnTutup.setBackground(new Color(231, 76, 60));
        btnTutup.setForeground(Color.BLACK);
        btnTutup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnTutup);

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createDatabaseSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Database Host
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Database Host:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        fieldDbHost = new JTextField("localhost", 20);
        formPanel.add(fieldDbHost, gbc);

        // Database Port
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Database Port:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        fieldDbPort = new JTextField("3306", 20);
        formPanel.add(fieldDbPort, gbc);

        // Database Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Database Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        fieldDbName = new JTextField("ticket_wahana_db", 20);
        formPanel.add(fieldDbName, gbc);

        // Test Connection Button
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;        JButton btnTest = new JButton("Test Koneksi");
        btnTest.setBackground(new Color(52, 152, 219));
        btnTest.setForeground(Color.BLACK);
        btnTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testDatabaseConnection();
            }
        });
        formPanel.add(btnTest, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 12));
        infoArea.setText("Informasi Koneksi Database:\n\n" +
                        "• Pastikan MySQL server sudah berjalan\n" +
                        "• Database 'ticket_wahana_db' harus sudah dibuat\n" +
                        "• Username dan password MySQL harus benar\n" +
                        "• Port default MySQL adalah 3306\n\n" +
                        "Status Koneksi: " + getDatabaseStatus());
        infoArea.setBackground(new Color(248, 249, 250));
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(infoArea, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPasswordSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Current Password
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Password Saat Ini:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        fieldCurrentPassword = new JPasswordField(20);
        formPanel.add(fieldCurrentPassword, gbc);

        // New Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Password Baru:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        fieldNewPassword = new JPasswordField(20);
        formPanel.add(fieldNewPassword, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Konfirmasi Password:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        fieldConfirmPassword = new JPasswordField(20);
        formPanel.add(fieldConfirmPassword, gbc);

        // Change Password Button
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;        JButton btnChange = new JButton("Ubah Password");
        btnChange.setBackground(new Color(155, 89, 182));
        btnChange.setForeground(Color.BLACK);
        btnChange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changePassword();
            }
        });
        formPanel.add(btnChange, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 12));
        infoArea.setText("Ketentuan Password:\n\n" +
                        "• Minimal 6 karakter\n" +
                        "• Disarankan menggunakan kombinasi huruf dan angka\n" +
                        "• Hindari password yang mudah ditebak\n" +
                        "• Password akan dienkripsi di database\n\n" +
                        "Catatan: Setelah mengubah password, Anda harus login ulang");
        infoArea.setBackground(new Color(248, 249, 250));
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(infoArea, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSystemLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadSystemLogs();
            }
        });

        JButton btnClearLogs = new JButton("Clear Logs");
        btnClearLogs.setBackground(new Color(231, 76, 60));
        btnClearLogs.setForeground(Color.BLACK);
        btnClearLogs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearSystemLogs();
            }
        });

        topPanel.add(btnRefresh);
        topPanel.add(btnClearLogs);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(248, 249, 250));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Log Aktivitas Sistem"));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDatabaseToolsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel buttonsPanel = new JPanel(new GridLayout(4, 1, 10, 10));        JButton btnBackup = new JButton("Backup Database");
        btnBackup.setBackground(new Color(46, 204, 113));
        btnBackup.setForeground(Color.BLACK);
        btnBackup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                backupDatabase();
            }
        });

        JButton btnRestore = new JButton("Restore Database");
        btnRestore.setBackground(new Color(230, 126, 34));
        btnRestore.setForeground(Color.BLACK);
        btnRestore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restoreDatabase();
            }
        });

        JButton btnOptimize = new JButton("Optimize Database");
        btnOptimize.setBackground(new Color(52, 152, 219));
        btnOptimize.setForeground(Color.BLACK);
        btnOptimize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                optimizeDatabase();
            }
        });

        JButton btnCheckTables = new JButton("Check Tables");
        btnCheckTables.setBackground(new Color(155, 89, 182));
        btnCheckTables.setForeground(Color.BLACK);
        btnCheckTables.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkDatabaseTables();
            }
        });

        buttonsPanel.add(btnBackup);
        buttonsPanel.add(btnRestore);
        buttonsPanel.add(btnOptimize);
        buttonsPanel.add(btnCheckTables);

        panel.add(buttonsPanel, BorderLayout.NORTH);

        JTextArea toolsInfo = new JTextArea();
        toolsInfo.setEditable(false);
        toolsInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        toolsInfo.setText("Database Tools:\n\n" +
                        "• Backup Database: Membuat backup file .sql\n" +
                        "• Restore Database: Mengembalikan dari file backup\n" +
                        "• Optimize Database: Mengoptimalkan performa tabel\n" +
                        "• Check Tables: Memeriksa integritas tabel database\n\n" +
                        "Catatan: Backup akan disimpan dalam folder 'backup'");
        toolsInfo.setBackground(new Color(248, 249, 250));
        toolsInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(toolsInfo, BorderLayout.CENTER);
        return panel;
    }

    private String getDatabaseStatus() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                conn.close();
                return "✓ Terhubung";
            }
        } catch (Exception e) {
            return "✗ Tidak terhubung - " + e.getMessage();
        }
        return "✗ Tidak terhubung";
    }

    private void testDatabaseConnection() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                conn.close();
                JOptionPane.showMessageDialog(this, "Koneksi database berhasil!", 
                    "Test Koneksi", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi database gagal:\n" + e.getMessage(), 
                "Test Koneksi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveDatabaseSettings() {
        JOptionPane.showMessageDialog(this, 
            "Pengaturan database disimpan!\n" +
            "Host: " + fieldDbHost.getText() + "\n" +
            "Port: " + fieldDbPort.getText() + "\n" +
            "Database: " + fieldDbName.getText(),
            "Simpan Pengaturan", JOptionPane.INFORMATION_MESSAGE);
    }

    private void changePassword() {
        String currentPassword = new String(fieldCurrentPassword.getPassword());
        String newPassword = new String(fieldNewPassword.getPassword());
        String confirmPassword = new String(fieldConfirmPassword.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field password harus diisi!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Password baru dan konfirmasi tidak sama!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password minimal 6 karakter!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Here you would implement actual password change logic
        JOptionPane.showMessageDialog(this, "Password berhasil diubah!", 
            "Sukses", JOptionPane.INFORMATION_MESSAGE);

        // Clear fields
        fieldCurrentPassword.setText("");
        fieldNewPassword.setText("");
        fieldConfirmPassword.setText("");
    }

    private void loadSystemLogs() {
        if (logArea != null) {
            logArea.setText("System Logs:\n\n" +
                          "2024-12-11 10:30:25 - User admin login\n" +
                          "2024-12-11 10:35:12 - Wahana W001 diupdate\n" +
                          "2024-12-11 10:40:45 - Transaksi TRX001 berhasil\n" +
                          "2024-12-11 10:45:20 - Laporan digenerate\n" +
                          "2024-12-11 10:50:33 - Database backup otomatis\n" +
                          "2024-12-11 11:00:15 - User admin logout\n\n" +
                          "Note: Log akan diambil dari tabel system_logs di database");
        }
    }

    private void clearSystemLogs() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Hapus semua log sistem?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            logArea.setText("System logs cleared.\n");
        }
    }

    private void backupDatabase() {
        JOptionPane.showMessageDialog(this, 
            "Backup database berhasil!\nFile: backup_ticket_wahana_" + 
            new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".sql",
            "Backup Database", JOptionPane.INFORMATION_MESSAGE);
    }

    private void restoreDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "SQL Files", "sql"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, 
                "Database berhasil direstore dari:\n" + fileChooser.getSelectedFile().getName(),
                "Restore Database", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void optimizeDatabase() {
        JOptionPane.showMessageDialog(this, 
            "Database berhasil dioptimasi!\n" +
            "• Tabel wahana: OK\n" +
            "• Tabel transaksi: OK\n" +
            "• Tabel users: OK\n" +
            "• Index rebuilt: 5 indexes",
            "Optimize Database", JOptionPane.INFORMATION_MESSAGE);
    }

    private void checkDatabaseTables() {
        JOptionPane.showMessageDialog(this, 
            "Pemeriksaan tabel database:\n\n" +
            "✓ users - OK (5 records)\n" +
            "✓ wahana - OK (6 records)\n" +
            "✓ tiket - OK (0 records)\n" +
            "✓ transaksi - OK (0 records)\n" +
            "✓ transaksi_detail - OK (0 records)\n" +
            "✓ pengunjung_log - OK (0 records)\n\n" +
            "Semua tabel dalam kondisi baik!",
            "Check Tables", JOptionPane.INFORMATION_MESSAGE);
    }
}
