package com.mycompany.tiketwahana;

import com.mycompany.tiketwahana.forms.LoginForm;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Kelas Utama Aplikasi Tiket Wahana
 * Berfungsi sebagai titik masuk program dan menginisialisasi tampilan login
 * @author salter
 */
public class MainApp {
    
    public static void main(String[] args) {        // Mengatur tampilan sesuai sistem operasi
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Tidak dapat mengatur tampilan sistem: " + e.getMessage());
        }
        
        // Jalankan aplikasi di thread EDT
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}