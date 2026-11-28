package com.mycompany.tiketwahana.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Top-level window. Screens stay inside this frame for clean navigation. */
public final class MainWindow extends JFrame {

    private final AppContext context;
    private final AppShell shell;

    public MainWindow(AppContext context, Runnable onLogout) {
        super("TiketWahana · Panel operasional");
        this.context = context;
        this.context.onLogout = () -> {
            dispose();
            if (onLogout != null) {
                SwingUtilities.invokeLater(onLogout);
            }
        };
        this.shell = new AppShell(context);
        setContentPane(shell);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                context.database.close();
                dispose();
                System.exit(0);
            }
        });
        setMinimumSize(new Dimension(1050, 700));
        setSize(1440, 900);
        setLocationRelativeTo(null);
    }

    public AppShell shell() {
        return shell;
    }
}
