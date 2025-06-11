package com.mycompany.tiketwahana.forms;

import com.mycompany.tiketwahana.database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransaksiForm extends JDialog {
    private JTextField txtNama, txtTelepon, txtJumlah, txtTotal, txtBayar, txtKembalian;
    private JComboBox<String> cmbWahana;
    private JLabel lblHarga;
    private JButton btnHitung, btnSimpan, btnReset;
    private double hargaSatuan = 0;
    
    public TransaksiForm(JFrame parent) {
        super(parent, "Transaksi Tiket", true);
        initComponents();
        loadWahanaData();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setSize(500, 400);
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Customer Info
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("TRANSAKSI TIKET WAHANA");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(76, 175, 80));
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Nama Pengunjung:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtNama = new JTextField(20);
        mainPanel.add(txtNama, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("No. Telepon:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtTelepon = new JTextField(20);
        mainPanel.add(txtTelepon, gbc);
        
        // Wahana Selection
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Pilih Wahana:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbWahana = new JComboBox<>();
        cmbWahana.addActionListener(e -> updateHarga());
        mainPanel.add(cmbWahana, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Harga Satuan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        lblHarga = new JLabel("Rp 0");
        lblHarga.setFont(new Font("Arial", Font.BOLD, 14));
        lblHarga.setForeground(new Color(230, 126, 34));
        mainPanel.add(lblHarga, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Jumlah Tiket:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtJumlah = new JTextField(20);
        txtJumlah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                hitungTotal();
            }
        });
        mainPanel.add(txtJumlah, gbc);
        
        // Payment Info
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Total Harga:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtTotal = new JTextField(20);
        txtTotal.setEditable(false);
        txtTotal.setBackground(new Color(240, 240, 240));
        txtTotal.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(txtTotal, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Jumlah Bayar:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtBayar = new JTextField(20);
        txtBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                hitungKembalian();
            }
        });
        mainPanel.add(txtBayar, gbc);
        
        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Kembalian:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtKembalian = new JTextField(20);
        txtKembalian.setEditable(false);
        txtKembalian.setBackground(new Color(240, 240, 240));
        txtKembalian.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(txtKembalian, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
          btnHitung = new JButton("Hitung Total");
        btnHitung.setBackground(new Color(52, 152, 219));
        btnHitung.setForeground(Color.BLACK);
        btnHitung.addActionListener(e -> hitungTotal());
        
        btnSimpan = new JButton("Simpan Transaksi");
        btnSimpan.setBackground(new Color(46, 204, 113));
        btnSimpan.setForeground(Color.BLACK);
        btnSimpan.addActionListener(e -> simpanTransaksi());
        
        btnReset = new JButton("Reset");
        btnReset.setBackground(new Color(149, 165, 166));
        btnReset.setForeground(Color.BLACK);
        btnReset.addActionListener(e -> resetForm());
        
        buttonPanel.add(btnHitung);
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnReset);
        
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void loadWahanaData() {
        cmbWahana.removeAllItems();
        cmbWahana.addItem("-- Pilih Wahana --");
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT kode_wahana, nama_wahana, harga_tiket FROM wahana WHERE status = 'AKTIF' ORDER BY kode_wahana";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                    String item = rs.getString("kode_wahana") + " - " + rs.getString("nama_wahana") + 
                                 " (Rp " + String.format("%,.0f", rs.getDouble("harga_tiket")) + ")";
                    cmbWahana.addItem(item);
                }
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading wahana: " + e.getMessage());
        }
    }
    
    private void updateHarga() {
        if (cmbWahana.getSelectedIndex() > 0) {
            try {
                String selectedItem = cmbWahana.getSelectedItem().toString();
                String kodeWahana = selectedItem.split(" - ")[0];
                
                Connection conn = DatabaseConnection.getConnection();
                if (conn != null) {
                    String sql = "SELECT harga_tiket FROM wahana WHERE kode_wahana = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, kodeWahana);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        hargaSatuan = rs.getDouble("harga_tiket");
                        lblHarga.setText("Rp " + String.format("%,.0f", hargaSatuan));
                        hitungTotal();
                    }
                    rs.close();
                    pstmt.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error getting price: " + e.getMessage());
            }
        } else {
            hargaSatuan = 0;
            lblHarga.setText("Rp 0");
            txtTotal.setText("");
        }
    }
    
    private void hitungTotal() {
        try {
            if (!txtJumlah.getText().trim().isEmpty() && hargaSatuan > 0) {
                int jumlah = Integer.parseInt(txtJumlah.getText().trim());
                double total = hargaSatuan * jumlah;
                txtTotal.setText("Rp " + String.format("%,.0f", total));
                hitungKembalian();
            }
        } catch (NumberFormatException e) {
            txtTotal.setText("");
        }
    }
    
    private void hitungKembalian() {
        try {
            if (!txtBayar.getText().trim().isEmpty() && !txtTotal.getText().trim().isEmpty()) {
                double bayar = Double.parseDouble(txtBayar.getText().trim());
                String totalStr = txtTotal.getText().replaceAll("[^0-9]", "");
                if (!totalStr.isEmpty()) {
                    double total = Double.parseDouble(totalStr);
                    double kembalian = bayar - total;
                    txtKembalian.setText("Rp " + String.format("%,.0f", kembalian));
                }
            }
        } catch (NumberFormatException e) {
            txtKembalian.setText("");
        }
    }
    
    private void simpanTransaksi() {
        if (validateInput()) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (conn != null) {
                    conn.setAutoCommit(false);
                    
                    // Generate transaction code
                    String kodeTransaksi = "TRX" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    
                    // Get wahana ID
                    String selectedItem = cmbWahana.getSelectedItem().toString();
                    String kodeWahana = selectedItem.split(" - ")[0];
                    
                    String sqlWahana = "SELECT id FROM wahana WHERE kode_wahana = ?";
                    PreparedStatement pstmtWahana = conn.prepareStatement(sqlWahana);
                    pstmtWahana.setString(1, kodeWahana);
                    ResultSet rsWahana = pstmtWahana.executeQuery();
                    int wahanaId = 0;
                    if (rsWahana.next()) {
                        wahanaId = rsWahana.getInt("id");
                    }
                    rsWahana.close();
                    pstmtWahana.close();
                    
                    // Insert transaction
                    String sqlTransaksi = "INSERT INTO transaksi (kode_transaksi, nama_customer, total_tiket, total_amount, payment_amount, change_amount) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement pstmtTransaksi = conn.prepareStatement(sqlTransaksi, Statement.RETURN_GENERATED_KEYS);
                    
                    int jumlah = Integer.parseInt(txtJumlah.getText().trim());
                    double total = Double.parseDouble(txtTotal.getText().replaceAll("[^0-9]", ""));
                    double bayar = Double.parseDouble(txtBayar.getText().trim());
                    double kembalian = Double.parseDouble(txtKembalian.getText().replaceAll("[^0-9]", ""));
                    
                    pstmtTransaksi.setString(1, kodeTransaksi);
                    pstmtTransaksi.setString(2, txtNama.getText().trim());
                    pstmtTransaksi.setInt(3, jumlah);
                    pstmtTransaksi.setDouble(4, total);
                    pstmtTransaksi.setDouble(5, bayar);
                    pstmtTransaksi.setDouble(6, kembalian);
                    
                    pstmtTransaksi.executeUpdate();
                    
                    ResultSet generatedKeys = pstmtTransaksi.getGeneratedKeys();
                    int transaksiId = 0;
                    if (generatedKeys.next()) {
                        transaksiId = generatedKeys.getInt(1);
                    }
                    generatedKeys.close();
                    pstmtTransaksi.close();
                    
                    // Insert tickets
                    for (int i = 0; i < jumlah; i++) {
                        String kodeTicket = "TKT" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + 
                                          String.format("%03d", i + 1) + System.currentTimeMillis() % 1000;
                        
                        String sqlTiket = "INSERT INTO tiket (kode_tiket, nama_pengunjung, no_telepon, wahana_id, tanggal_kunjungan, jumlah_tiket, total_harga) VALUES (?, ?, ?, ?, CURDATE(), 1, ?)";
                        PreparedStatement pstmtTiket = conn.prepareStatement(sqlTiket);
                        pstmtTiket.setString(1, kodeTicket);
                        pstmtTiket.setString(2, txtNama.getText().trim());
                        pstmtTiket.setString(3, txtTelepon.getText().trim());
                        pstmtTiket.setInt(4, wahanaId);
                        pstmtTiket.setDouble(5, hargaSatuan);
                        pstmtTiket.executeUpdate();
                        pstmtTiket.close();
                    }
                    
                    conn.commit();
                    conn.setAutoCommit(true);
                    
                    JOptionPane.showMessageDialog(this, 
                        "Transaksi berhasil!\n" +
                        "Kode Transaksi: " + kodeTransaksi + "\n" +
                        "Total: Rp " + String.format("%,.0f", total) + "\n" +
                        "Bayar: Rp " + String.format("%,.0f", bayar) + "\n" +
                        "Kembalian: Rp " + String.format("%,.0f", kembalian),
                        "Transaksi Berhasil", JOptionPane.INFORMATION_MESSAGE);
                    
                    resetForm();
                }
            } catch (SQLException | NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error saving transaction: " + e.getMessage());
            }
        }
    }
    
    private boolean validateInput() {
        if (txtNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama pengunjung harus diisi!");
            return false;
        }
        
        if (cmbWahana.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Pilih wahana terlebih dahulu!");
            return false;
        }
        
        try {
            int jumlah = Integer.parseInt(txtJumlah.getText().trim());
            if (jumlah <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah tiket harus lebih dari 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah tiket harus berupa angka!");
            return false;
        }
        
        try {
            double bayar = Double.parseDouble(txtBayar.getText().trim());
            double total = Double.parseDouble(txtTotal.getText().replaceAll("[^0-9]", ""));
            if (bayar < total) {
                JOptionPane.showMessageDialog(this, "Jumlah bayar kurang dari total harga!");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah bayar harus berupa angka!");
            return false;
        }
        
        return true;
    }
    
    private void resetForm() {
        txtNama.setText("");
        txtTelepon.setText("");
        txtJumlah.setText("");
        txtTotal.setText("");
        txtBayar.setText("");
        txtKembalian.setText("");
        cmbWahana.setSelectedIndex(0);
        lblHarga.setText("Rp 0");
        hargaSatuan = 0;
    }
}
