package com.mycompany.tiketwahana.forms;

import com.mycompany.tiketwahana.database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainForm extends JFrame {
    
    @SuppressWarnings("unused")
    public MainForm() {
        initComponents();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Sistem Manajemen Tiket Wahana - ADMIN");        
        // Panel utama
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(76, 175, 80));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        
        JLabel titleLabel = new JLabel("SISTEM MANAJEMEN TIKET WAHANA");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Administrator - Kelola semua wahana dan transaksi tiket");
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel userLabel = new JLabel("Administrator (ADMIN)");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        userLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(userLabel, BorderLayout.EAST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Area konten utama - Tampilan Wahana
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        contentPanel.setBackground(new Color(236, 240, 241));        
        // Panel tampilan wahana saat ini
        JPanel wahanaDisplayPanel = new JPanel();
        wahanaDisplayPanel.setLayout(new BoxLayout(wahanaDisplayPanel, BoxLayout.Y_AXIS));
        wahanaDisplayPanel.setBackground(Color.WHITE);
        wahanaDisplayPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2), 
                "Wahana Tersedia Hari Ini", 0, 0, new Font("Arial", Font.BOLD, 16), new Color(76, 175, 80)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Muat data wahana dari database
        List<String[]> wahanaData = loadWahanaFromDatabase();
        
        // Hitung ukuran grid berdasarkan jumlah item
        int itemCount = wahanaData.size();
        int cols = Math.min(3, itemCount);
        int rows = (int) Math.ceil((double) itemCount / cols);
        
        JPanel wahanaGrid = new JPanel(new GridLayout(rows, cols, 15, 15));
        wahanaGrid.setBackground(Color.WHITE);
        
        for (String[] wahana : wahanaData) {
            JPanel wahanaCard = createWahanaCard(wahana[0], wahana[1], wahana[2], wahana[3], wahana[4], wahana[5]);
            wahanaGrid.add(wahanaCard);
        }
        
        wahanaDisplayPanel.add(wahanaGrid);
        
        JScrollPane scrollPane = new JScrollPane(wahanaDisplayPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel bawah untuk kustomisasi
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(236, 240, 241));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        // Panel tombol kustomisasi
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        customPanel.setBackground(new Color(236, 240, 241));
        
        JButton btnManageWahana = createCustomButton("Kelola Wahana", new Color(52, 152, 219));
        JButton btnTransaksi = createCustomButton("Transaksi Tiket", new Color(46, 204, 113));
        JButton btnLaporan = createCustomButton("Laporan", new Color(155, 89, 182));
        JButton btnPengaturan = createCustomButton("Pengaturan", new Color(149, 165, 166));
        JButton btnLogout = createCustomButton("Logout", new Color(231, 76, 60));        // Tambah action listener untuk tombol-tombol
        btnManageWahana.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openWahanaManagement();
            }
        });
        btnTransaksi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openTransaksiForm();
            }
        });
        btnLaporan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openLaporanForm();
            }
        });
        btnPengaturan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openPengaturanForm();
            }
        });
        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(MainForm.this, 
                    "Apakah Anda yakin ingin logout?", "Konfirmasi Logout", 
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                    new LoginForm().setVisible(true);
                }
            }
        });
        
        customPanel.add(btnManageWahana);
        customPanel.add(btnTransaksi);
        customPanel.add(btnLaporan);
        customPanel.add(btnPengaturan);
        customPanel.add(btnLogout);        
        bottomPanel.add(customPanel, BorderLayout.CENTER);
        
        // Panel footer info
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(44, 62, 80));
        footerPanel.setPreferredSize(new Dimension(0, 30));
        
        JLabel footerLabel = new JLabel("Sistem Tiket Wahana - Kelola semua wahana dan transaksi dengan mudah");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerPanel.add(footerLabel);
        
        // Gabung panel bawah dan footer jadi satu panel selatan
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(bottomPanel, BorderLayout.CENTER);
        southPanel.add(footerPanel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private List<String[]> loadWahanaFromDatabase() {
        List<String[]> wahanaList = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT kode_wahana, nama_wahana, harga_tiket, kapasitas_max, durasi_menit, status FROM wahana WHERE status = 'AKTIF'";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                    String[] wahana = {
                        rs.getString("kode_wahana"),
                        rs.getString("nama_wahana"),
                        "Rp " + String.format("%,.0f", rs.getDouble("harga_tiket")),
                        rs.getInt("kapasitas_max") + " orang",
                        rs.getInt("durasi_menit") + " menit",
                        rs.getString("status")
                    };
                    wahanaList.add(wahana);
                }
                rs.close();
                stmt.close();            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Jika database gagal, gunakan data hardcode sebagai fallback
            wahanaList.add(new String[]{"W001", "Kereta Gantung", "Rp 25,000", "40 orang", "45 menit", "AKTIF"});
            wahanaList.add(new String[]{"W002", "Sea World", "Rp 35,000", "100 orang", "60 menit", "AKTIF"});
            wahanaList.add(new String[]{"W003", "Otomotive Museum", "Rp 20,000", "50 orang", "90 menit", "AKTIF"});
            wahanaList.add(new String[]{"W004", "Dunia Fantasi", "Rp 40,000", "80 orang", "120 menit", "AKTIF"});
            wahanaList.add(new String[]{"W005", "Roller Coaster", "Rp 50,000", "24 orang", "5 menit", "AKTIF"});
            wahanaList.add(new String[]{"W006", "Water Park", "Rp 30,000", "150 orang", "180 menit", "AKTIF"});
        }
        return wahanaList;
    }
    
    private JPanel createWahanaCard(String kode, String nama, String harga, String kapasitas, String durasi, String status) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Header dengan kode wahana dan status
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(76, 175, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        JLabel kodeLabel = new JLabel(kode);
        kodeLabel.setForeground(Color.WHITE);
        kodeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel statusLabel = new JLabel(status);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(46, 204, 113));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
        headerPanel.add(kodeLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        // Panel konten
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel namaLabel = new JLabel(nama);
        namaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        namaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel hargaLabel = new JLabel(harga);
        hargaLabel.setFont(new Font("Arial", Font.BOLD, 16));
        hargaLabel.setForeground(new Color(230, 126, 34));
        hargaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel kapasitasLabel = new JLabel("Kapasitas: " + kapasitas);
        kapasitasLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        kapasitasLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel durasiLabel = new JLabel("Durasi: " + durasi);
        durasiLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        durasiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        contentPanel.add(namaLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(hargaLabel);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(kapasitasLabel);
        contentPanel.add(durasiLabel);
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);
          // Efek hover untuk interaksi
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(248, 249, 250));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
                    BorderFactory.createEmptyBorder(14, 14, 14, 14)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
                ));
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JOptionPane.showMessageDialog(MainForm.this, 
                    "Detail Wahana: " + nama + "\n" +
                    "Kode: " + kode + "\n" +
                    "Harga: " + harga + "\n" +
                    "Kapasitas: " + kapasitas + "\n" +
                    "Durasi: " + durasi + "\n" +
                    "Status: " + status,
                    "Informasi Wahana", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        return card;
    }    private JButton createCustomButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(140, 45));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
          // Efek hover untuk tombol
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = backgroundColor;
            
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
      private void openWahanaManagement() {
        WahanaManagementForm wahanaForm = new WahanaManagementForm(this);
        wahanaForm.setVisible(true);        // Refresh tampilan utama setelah manajemen wahana
        SwingUtilities.invokeLater(() -> {
            this.dispose();
            new MainForm().setVisible(true);
        });
    }
    
    private void openTransaksiForm() {
        TransaksiForm transaksiForm = new TransaksiForm(this);
        transaksiForm.setVisible(true);
    }
      private void openLaporanForm() {
        LaporanForm laporanForm = new LaporanForm(this);
        laporanForm.setVisible(true);
    }
    
    private void openPengaturanForm() {
        PengaturanForm pengaturanForm = new PengaturanForm(this);
        pengaturanForm.setVisible(true);
    }
}