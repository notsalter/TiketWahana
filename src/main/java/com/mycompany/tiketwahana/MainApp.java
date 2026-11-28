package com.mycompany.tiketwahana;

import com.mycompany.tiketwahana.forms.LoginForm;
import java.awt.EventQueue;
import javax.swing.UIManager;

/** Launches the desktop app on Swing's EDT. */
public final class MainApp {
    private MainApp() {}
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Look and feel sistem tidak tersedia: " + e.getMessage());
        }
        EventQueue.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
