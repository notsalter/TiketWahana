package com.mycompany.tiketwahana.forms;

import com.mycompany.tiketwahana.database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    
    public LoginForm() {
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        setTitle("Login - Sistem Manajemen Tiket Wahana");
        setSize(400, 300);
        setResizable(false);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(76, 175, 80));
        titlePanel.setPreferredSize(new Dimension(400, 60));
        JLabel titleLabel = new JLabel("SISTEM MANAJEMEN TIKET WAHANA");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Username label and field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);
        
        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));        loginButton.setBackground(new Color(76, 175, 80));
        loginButton.setForeground(Color.BLACK);
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.addActionListener(new LoginActionListener());
        formPanel.add(loginButton, gbc);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Set enter key to trigger login
        getRootPane().setDefaultButton(loginButton);
    }
    
    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                showErrorDialog("Username atau password salah!");
                return;
            }
            
            if (authenticateUser(username, password)) {
                // Success - show success message and open main form
                showSuccessDialog("Login berhasil!\nSelamat datang Administrator\nRole: ADMIN");
                this.openMainForm();
                dispose();
            } else {
                showErrorDialog("Username atau password salah!\nGunakan kredensial demo yang tersedia.");
            }
        }
          private boolean authenticateUser(String username, String password) {
            // Database authentication
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (conn != null) {
                    String sql = "SELECT id, full_name, role FROM users WHERE username = ? AND password = ? AND is_active = TRUE";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        // Store user info for session
                        String fullName = rs.getString("full_name");
                        String role = rs.getString("role");
                        System.out.println("Login successful - User: " + fullName + ", Role: " + role);
                        return true;
                    }
                }
            } catch (SQLException ex) {
                System.err.println("Database authentication error: " + ex.getMessage());
                ex.printStackTrace();
                
                // Fallback to demo credentials if database fails
                if ("admin".equals(username) && "admin".equals(password)) {
                    System.out.println("Using fallback demo credentials");
                    return true;
                }        }
            
            return false;
        }
        
        private void openMainForm() {
            SwingUtilities.invokeLater(() -> {
                new MainForm().setVisible(true);
            });
        }
        
        private void showErrorDialog(String message) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JLabel iconLabel = new JLabel("❌");
            iconLabel.setFont(new Font("Arial", Font.PLAIN, 24));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            
            JLabel messageLabel = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(messageLabel, BorderLayout.CENTER);
            
            JOptionPane.showMessageDialog(LoginForm.this, panel, "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
        
        private void showSuccessDialog(String message) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JLabel iconLabel = new JLabel("ℹ️");
            iconLabel.setFont(new Font("Arial", Font.PLAIN, 24));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            
            JLabel messageLabel = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(messageLabel, BorderLayout.CENTER);
            
            JOptionPane.showMessageDialog(LoginForm.this, panel, "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
