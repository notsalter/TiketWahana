package com.mycompany.tiketwahana.forms;

import com.mycompany.tiketwahana.database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LaporanForm extends JDialog {
    private JTable tabelLaporan;
    private JComboBox<String> jenisPeriode;
    private JTextField tanggalAwal, tanggalAkhir;
    private JLabel totalPendapatan, totalTiket;
    private JFrame parentFrame;

    public LaporanForm(JFrame parent) {
        super(parent, "Laporan Penjualan Tiket", true);
        this.parentFrame = parent;
        initComponents();
        setLocationRelativeTo(parent);
        loadDefaultReport();
    }

    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(76, 175, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("LAPORAN PENJUALAN TIKET WAHANA");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(new Color(236, 240, 241));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Laporan"));        filterPanel.add(new JLabel("Periode:"));
        jenisPeriode = new JComboBox<>(new String[]{"Semua Data", "Hari Ini", "Minggu Ini", "Bulan Ini", "Custom"});
        jenisPeriode.setSelectedItem("Semua Data");
        jenisPeriode.addActionListener(e -> updateDateRange());
        filterPanel.add(jenisPeriode);        filterPanel.add(new JLabel("Dari:"));
        tanggalAwal = new JTextField(10);
        tanggalAwal.setText("2020-01-01");
        tanggalAwal.setEditable(false);
        filterPanel.add(tanggalAwal);

        filterPanel.add(new JLabel("Sampai:"));
        tanggalAkhir = new JTextField(10);
        tanggalAkhir.setText("2030-12-31");
        tanggalAkhir.setEditable(false);
        filterPanel.add(tanggalAkhir);JButton btnFilter = new JButton("Filter");
        btnFilter.setBackground(new Color(52, 152, 219));
        btnFilter.setForeground(Color.BLACK);
        btnFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadLaporanData();
            }
        });
        filterPanel.add(btnFilter);

        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Ringkasan"));
        summaryPanel.setBackground(Color.WHITE);

        JPanel pendapatanPanel = new JPanel(new BorderLayout());
        pendapatanPanel.setBackground(new Color(46, 204, 113));
        pendapatanPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel labelPendapatan = new JLabel("Total Pendapatan");
        labelPendapatan.setForeground(Color.WHITE);
        labelPendapatan.setFont(new Font("Arial", Font.BOLD, 12));
        totalPendapatan = new JLabel("Rp 0");
        totalPendapatan.setForeground(Color.WHITE);
        totalPendapatan.setFont(new Font("Arial", Font.BOLD, 20));
        pendapatanPanel.add(labelPendapatan, BorderLayout.NORTH);
        pendapatanPanel.add(totalPendapatan, BorderLayout.CENTER);

        JPanel tiketPanel = new JPanel(new BorderLayout());
        tiketPanel.setBackground(new Color(155, 89, 182));
        tiketPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel labelTiket = new JLabel("Total Tiket Terjual");
        labelTiket.setForeground(Color.WHITE);
        labelTiket.setFont(new Font("Arial", Font.BOLD, 12));
        totalTiket = new JLabel("0 tiket");
        totalTiket.setForeground(Color.WHITE);
        totalTiket.setFont(new Font("Arial", Font.BOLD, 20));
        tiketPanel.add(labelTiket, BorderLayout.NORTH);
        tiketPanel.add(totalTiket, BorderLayout.CENTER);

        summaryPanel.add(pendapatanPanel);
        summaryPanel.add(tiketPanel);        // Table panel
        String[] columnNames = {"Tanggal", "Kode Wahana", "Nama Wahana", "Durasi (menit)", "Jumlah Tiket", "Harga Satuan", "Total"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelLaporan = new JTable(model);
        tabelLaporan.setRowHeight(25);
        tabelLaporan.getTableHeader().setBackground(new Color(52, 152, 219));
        tabelLaporan.getTableHeader().setForeground(Color.WHITE);
        tabelLaporan.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tabelLaporan);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Detail Transaksi"));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(236, 240, 241));        JButton btnExport = new JButton("Export Excel");
        btnExport.setBackground(new Color(46, 204, 113));
        btnExport.setForeground(Color.BLACK);
        btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportToCSV();
            }
        });

        JButton btnPrint = new JButton("Print");
        btnPrint.setBackground(new Color(149, 165, 166));
        btnPrint.setForeground(Color.BLACK);
        btnPrint.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    tabelLaporan.print();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LaporanForm.this, 
                        "Error saat mencetak: " + ex.getMessage());
                }
            }
        });        JButton btnTutup = new JButton("Tutup");
        btnTutup.setBackground(new Color(231, 76, 60));
        btnTutup.setForeground(Color.BLACK);
        btnTutup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });

        buttonPanel.add(btnExport);
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnTutup);// Pengaturan Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(summaryPanel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }    private void loadDefaultReport() {
        loadLaporanData();
    }
    
    private void updateDateRange() {
        String selectedPeriod = (String) jenisPeriode.getSelectedItem();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
          switch (selectedPeriod) {
            case "Semua Data":
                tanggalAwal.setText("2020-01-01");
                tanggalAkhir.setText("2030-12-31");
                tanggalAwal.setEditable(false);
                tanggalAkhir.setEditable(false);
                break;
            case "Hari Ini":
                tanggalAwal.setText(sdf.format(today));
                tanggalAkhir.setText(sdf.format(today));
                tanggalAwal.setEditable(false);
                tanggalAkhir.setEditable(false);
                break;
            case "Minggu Ini":
                // Hitung tanggal awal minggu (Senin)
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(today);
                cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
                tanggalAwal.setText(sdf.format(cal.getTime()));
                tanggalAkhir.setText(sdf.format(today));
                tanggalAwal.setEditable(false);
                tanggalAkhir.setEditable(false);
                break;
            case "Bulan Ini":
                // Hitung tanggal awal bulan
                java.util.Calendar calMonth = java.util.Calendar.getInstance();
                calMonth.setTime(today);
                calMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);
                tanggalAwal.setText(sdf.format(calMonth.getTime()));
                tanggalAkhir.setText(sdf.format(today));
                tanggalAwal.setEditable(false);
                tanggalAkhir.setEditable(false);
                break;
            case "Custom":
                tanggalAwal.setEditable(true);
                tanggalAkhir.setEditable(true);
                break;        }
        // Auto-load data when period changes (except Custom)
        if (!"Custom".equals(selectedPeriod)) {
            loadLaporanData();
        }
    }private void loadLaporanData() {
        // Validate date input first
        if (!validateDateInput()) {
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) tabelLaporan.getModel();
        model.setRowCount(0);

        String tanggalMulai = tanggalAwal.getText();
        String tanggalSelesai = tanggalAkhir.getText();
        double totalPendapatanValue = 0;
        int totalTiketValue = 0;        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                // Query untuk menampilkan semua data tiket tanpa pengelompokan
                String sql = "SELECT DATE(t.created_at) as tanggal, " +
                           "w.kode_wahana, w.nama_wahana, w.durasi_menit, " +
                           "t.jumlah_tiket as jumlah_tiket, " +
                           "w.harga_tiket as harga_satuan, " +
                           "t.total_harga as total " +
                           "FROM tiket t " +
                           "JOIN wahana w ON t.wahana_id = w.id " +
                           "WHERE DATE(t.created_at) BETWEEN ? AND ? " +
                           "ORDER BY t.created_at DESC";

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, tanggalMulai);
                pstmt.setString(2, tanggalSelesai);
                ResultSet rs = pstmt.executeQuery();

                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));                while (rs.next()) {
                    String tanggal = rs.getString("tanggal");
                    String kodeWahana = rs.getString("kode_wahana");
                    String namaWahana = rs.getString("nama_wahana");
                    int durasi = rs.getInt("durasi_menit");
                    int jumlahTiket = rs.getInt("jumlah_tiket");
                    double hargaSatuan = rs.getDouble("harga_satuan");
                    double total = rs.getDouble("total");

                    totalPendapatanValue += total;
                    totalTiketValue += jumlahTiket;

                    Object[] row = {
                        tanggal,
                        kodeWahana,
                        namaWahana,
                        durasi + " menit",
                        jumlahTiket + " tiket",
                        formatter.format(hargaSatuan),
                        formatter.format(total)
                    };
                    model.addRow(row);
                }

                rs.close();
                pstmt.close();
                conn.close();
            } else {
                // Tambahkan data contoh jika database gagal
                addSampleData(model);
                totalPendapatanValue = 2750000;
                totalTiketValue = 85;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Kesalahan saat memuat data laporan: " + e.getMessage(),
                "Error Database", JOptionPane.ERROR_MESSAGE);
            // Tambahkan data contoh jika database gagal
            addSampleData(model);
            totalPendapatanValue = 2750000;
            totalTiketValue = 85;
        }

        // Perbarui ringkasan
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        totalPendapatan.setText(formatter.format(totalPendapatanValue));
        totalTiket.setText(totalTiketValue + " tiket");
    }    private void addSampleData(DefaultTableModel model) {
        String[][] sampleData = {
            {"2024-12-11", "W001", "Kereta Gantung", "45", "15 tiket", "Rp 25.000", "Rp 375.000"},
            {"2024-12-11", "W002", "Sea World", "60", "20 tiket", "Rp 35.000", "Rp 700.000"},
            {"2024-12-11", "W003", "Otomotive Museum", "90", "8 tiket", "Rp 20.000", "Rp 160.000"},
            {"2024-12-11", "W004", "Dunia Fantasi", "120", "12 tiket", "Rp 40.000", "Rp 480.000"},
            {"2024-12-11", "W005", "Roller Coaster", "5", "18 tiket", "Rp 50.000", "Rp 900.000"},
            {"2024-12-11", "W006", "Water Park", "180", "10 tiket", "Rp 30.000", "Rp 300.000"}
        };

        for (String[] row : sampleData) {
            model.addRow(row);
        }
    }
    
    private void exportToCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Laporan");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
            fileChooser.setSelectedFile(new java.io.File("Laporan_Tiket_" + 
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new java.io.File(file.getAbsolutePath() + ".csv");
                }
                
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
                    // Header
                    writer.println("LAPORAN PENJUALAN TIKET WAHANA");
                    writer.println("Periode: " + tanggalAwal.getText() + " s/d " + tanggalAkhir.getText());
                    writer.println("Tanggal Cetak: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
                    writer.println("Total Pendapatan: " + totalPendapatan.getText());
                    writer.println("Total Tiket: " + totalTiket.getText());
                    writer.println();
                    
                    // Column headers
                    DefaultTableModel model = (DefaultTableModel) tabelLaporan.getModel();
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        writer.print(model.getColumnName(i));
                        if (i < model.getColumnCount() - 1) writer.print(",");
                    }
                    writer.println();
                    
                    // Data rows
                    for (int i = 0; i < model.getRowCount(); i++) {
                        for (int j = 0; j < model.getColumnCount(); j++) {
                            String value = model.getValueAt(i, j).toString();
                            // Bungkus dengan tanda kutip jika mengandung koma
                            if (value.contains(",")) {
                                value = "\"" + value + "\"";
                            }
                            writer.print(value);
                            if (j < model.getColumnCount() - 1) writer.print(",");
                        }
                        writer.println();
                    }
                    
                    JOptionPane.showMessageDialog(this, 
                        "Laporan berhasil diekspor ke:\n" + file.getAbsolutePath(),
                        "Export Berhasil", JOptionPane.INFORMATION_MESSAGE);
                        
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Kesalahan saat mengekspor: " + ex.getMessage(),
                        "Error Export", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Kesalahan: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void refreshData() {
        loadLaporanData();
    }
      private boolean validateDateInput() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false); // Strict parsing
            
            Date startDate = sdf.parse(tanggalAwal.getText());
            Date endDate = sdf.parse(tanggalAkhir.getText());
            
            if (startDate.after(endDate)) {
                JOptionPane.showMessageDialog(this, 
                    "Tanggal mulai tidak boleh lebih besar dari tanggal akhir!",
                    "Error Validasi", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Skip future date check for "Semua Data" option
            String selectedPeriod = (String) jenisPeriode.getSelectedItem();
            if (!"Semua Data".equals(selectedPeriod)) {
                // Check if dates are not in the future
                Date today = new Date();
                if (startDate.after(today) || endDate.after(today)) {
                    JOptionPane.showMessageDialog(this, 
                        "Tanggal tidak boleh melebihi hari ini!",
                        "Error Validasi", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Format tanggal salah! Gunakan format: yyyy-MM-dd\nContoh: 2024-12-11",
                "Error Format Tanggal", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
