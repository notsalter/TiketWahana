package com.mycompany.tiketwahana.forms;

import com.mycompany.tiketwahana.database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class WahanaManagementForm extends JDialog {
    private JTable wahanaTable;
    private DefaultTableModel tableModel;
    private JTextField txtKode, txtNama, txtHarga, txtKapasitas, txtDurasi;
    private JComboBox<String> cmbStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh;
    
    public WahanaManagementForm(JFrame parent) {
        super(parent, "Manajemen Wahana", true);
        initComponents();
        loadWahanaData();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setSize(800, 600);
        setLayout(new BorderLayout());
        
        // Top panel for form inputs
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Data Wahana"));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Form fields
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Kode Wahana:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtKode = new JTextField(10);
        formPanel.add(txtKode, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Nama Wahana:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtNama = new JTextField(15);
        formPanel.add(txtNama, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Harga Tiket:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtHarga = new JTextField(10);
        formPanel.add(txtHarga, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Kapasitas Max:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtKapasitas = new JTextField(10);
        formPanel.add(txtKapasitas, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Durasi (menit):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtDurasi = new JTextField(10);
        formPanel.add(txtDurasi, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbStatus = new JComboBox<>(new String[]{"AKTIF", "MAINTENANCE", "TUTUP"});
        formPanel.add(cmbStatus, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
          btnAdd = new JButton("Tambah");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setForeground(Color.BLACK);
        btnAdd.addActionListener(e -> addWahana());
        
        btnUpdate = new JButton("Update");
        btnUpdate.setBackground(new Color(52, 152, 219));
        btnUpdate.setForeground(Color.BLACK);
        btnUpdate.addActionListener(e -> updateWahana());
        
        btnDelete = new JButton("Hapus");
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.BLACK);
        btnDelete.addActionListener(e -> deleteWahana());
        
        btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(149, 165, 166));
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.addActionListener(e -> loadWahanaData());
        
        JButton btnClear = new JButton("Clear");
        btnClear.setBackground(new Color(155, 89, 182));
        btnClear.setForeground(Color.BLACK);
        btnClear.addActionListener(e -> clearForm());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClear);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(buttonPanel, gbc);
        
        // Table for displaying wahana data
        String[] columns = {"ID", "Kode", "Nama", "Harga", "Kapasitas", "Durasi", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        wahanaTable = new JTable(tableModel);
        wahanaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wahanaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedWahana();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(wahanaTable);
        scrollPane.setPreferredSize(new Dimension(780, 300));
        
        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadWahanaData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT * FROM wahana ORDER BY kode_wahana";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("kode_wahana"),
                        rs.getString("nama_wahana"),
                        String.format("Rp %,.0f", rs.getDouble("harga_tiket")),
                        rs.getInt("kapasitas_max"),
                        rs.getInt("durasi_menit"),
                        rs.getString("status")
                    };
                    tableModel.addRow(row);
                }
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }
    
    private void loadSelectedWahana() {
        int selectedRow = wahanaTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtKode.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtNama.setText(tableModel.getValueAt(selectedRow, 2).toString());
            String harga = tableModel.getValueAt(selectedRow, 3).toString();
            txtHarga.setText(harga.replaceAll("[^0-9]", ""));
            txtKapasitas.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtDurasi.setText(tableModel.getValueAt(selectedRow, 5).toString());
            cmbStatus.setSelectedItem(tableModel.getValueAt(selectedRow, 6).toString());
        }
    }
    
    private void addWahana() {
        if (validateInput()) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (conn != null) {
                    String sql = "INSERT INTO wahana (kode_wahana, nama_wahana, harga_tiket, kapasitas_max, durasi_menit, status) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, txtKode.getText().trim());
                    pstmt.setString(2, txtNama.getText().trim());
                    pstmt.setDouble(3, Double.parseDouble(txtHarga.getText().trim()));
                    pstmt.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
                    pstmt.setInt(5, Integer.parseInt(txtDurasi.getText().trim()));
                    pstmt.setString(6, cmbStatus.getSelectedItem().toString());
                    
                    pstmt.executeUpdate();
                    pstmt.close();
                    
                    JOptionPane.showMessageDialog(this, "Wahana berhasil ditambahkan!");
                    clearForm();
                    loadWahanaData();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error adding wahana: " + e.getMessage());
            }
        }
    }
    
    private void updateWahana() {
        int selectedRow = wahanaTable.getSelectedRow();
        if (selectedRow >= 0 && validateInput()) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (conn != null) {
                    int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                    String sql = "UPDATE wahana SET kode_wahana=?, nama_wahana=?, harga_tiket=?, kapasitas_max=?, durasi_menit=?, status=? WHERE id=?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, txtKode.getText().trim());
                    pstmt.setString(2, txtNama.getText().trim());
                    pstmt.setDouble(3, Double.parseDouble(txtHarga.getText().trim()));
                    pstmt.setInt(4, Integer.parseInt(txtKapasitas.getText().trim()));
                    pstmt.setInt(5, Integer.parseInt(txtDurasi.getText().trim()));
                    pstmt.setString(6, cmbStatus.getSelectedItem().toString());
                    pstmt.setInt(7, id);
                    
                    pstmt.executeUpdate();
                    pstmt.close();
                    
                    JOptionPane.showMessageDialog(this, "Wahana berhasil diupdate!");
                    clearForm();
                    loadWahanaData();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating wahana: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih wahana yang akan diupdate!");
        }
    }
    
    private void deleteWahana() {
        int selectedRow = wahanaTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus wahana ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    if (conn != null) {
                        int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                        String sql = "DELETE FROM wahana WHERE id=?";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, id);
                        
                        pstmt.executeUpdate();
                        pstmt.close();
                        
                        JOptionPane.showMessageDialog(this, "Wahana berhasil dihapus!");
                        clearForm();
                        loadWahanaData();
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error deleting wahana: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih wahana yang akan dihapus!");
        }
    }
    
    private boolean validateInput() {
        if (txtKode.getText().trim().isEmpty() || txtNama.getText().trim().isEmpty() ||
            txtHarga.getText().trim().isEmpty() || txtKapasitas.getText().trim().isEmpty() ||
            txtDurasi.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!");
            return false;
        }
        
        try {
            Double.parseDouble(txtHarga.getText().trim());
            Integer.parseInt(txtKapasitas.getText().trim());
            Integer.parseInt(txtDurasi.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga, Kapasitas, dan Durasi harus berupa angka!");
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        txtKode.setText("");
        txtNama.setText("");
        txtHarga.setText("");
        txtKapasitas.setText("");
        txtDurasi.setText("");
        cmbStatus.setSelectedIndex(0);
        wahanaTable.clearSelection();
    }
}
