package com.mycompany.tiketwahana;

import com.mycompany.tiketwahana.forms.LoginForm;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Main Application Entry Point
 * @author salter
 */
public class MainApp {
    
    public static void main(String[] args) {        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
        
        // Start the application on EDT
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}