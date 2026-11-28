package com.mycompany.tiketwahana.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Non-blocking confirmations. Dialogs are reserved for real decisions. */
public final class Toast {

    public enum Kind { SUCCESS, INFO, WARN, ERROR }

    private Toast() {}

    public static void success(Component origin, String message) {
        show(origin, message, Kind.SUCCESS);
    }

    public static void info(Component origin, String message) {
        show(origin, message, Kind.INFO);
    }

    public static void warn(Component origin, String message) {
        show(origin, message, Kind.WARN);
    }

    public static void error(Component origin, String message) {
        show(origin, message, Kind.ERROR);
    }

    public static void show(Component origin, String message, Kind kind) {
        if (origin == null || message == null) {
            return;
        }
        javax.swing.JRootPane root = SwingUtilities.getRootPane(origin);
        if (root == null) {
            return;
        }
        JLayeredPane pane = root.getLayeredPane();
        Stack stack = (Stack) pane.getClientProperty("tiket.toastStack");
        if (stack == null) {
            stack = new Stack(pane);
            pane.putClientProperty("tiket.toastStack", stack);
            pane.add(stack, JLayeredPane.POPUP_LAYER);
        }
        stack.push(message, kind);
    }

    private static final class Stack extends JPanel {
        private final JLayeredPane pane;

        Stack(JLayeredPane pane) {
            this.pane = pane;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            pane.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    reflow();
                }
            });
        }

        void push(String message, Kind kind) {
            Card card = new Card(new BorderLayout(12, 0));
            card.radius(Theme.R_MD).padding(14, 16, 14, 12);
            card.setAlignmentX(Component.RIGHT_ALIGNMENT);
            card.setMaximumSize(new Dimension(400, 200));

            Theme.Palette p = Theme.p();
            Color accent = switch (kind) {
                case SUCCESS -> p.accent();
                case INFO -> p.ink();
                case WARN -> p.warn();
                case ERROR -> p.danger();
            };
            String iconName = switch (kind) {
                case SUCCESS -> "check";
                case INFO -> "ticket";
                case WARN -> "alert";
                case ERROR -> "alert";
            };

            JLabel icon = new JLabel(Icons.of(iconName, 18, accent));
            JLabel text = new JLabel("<html><body style='width:250px'>" + escape(message) + "</body></html>");
            text.setFont(Fonts.regular(13));
            text.setForeground(p.ink());

            JLabel close = new JLabel(Icons.of("close", 14, p.muted()));
            close.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            close.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    dismiss(card);
                }
            });

            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.setOpaque(false);
            textPanel.add(text, BorderLayout.CENTER);

            card.add(icon, BorderLayout.WEST);
            card.add(textPanel, BorderLayout.CENTER);
            card.add(close, BorderLayout.EAST);

            add(card);
            add(Box.createVerticalStrut(8));
            revalidate();
            reflow();

            Timer life = new Timer(4200, e -> dismiss(card));
            life.setRepeats(false);
            life.start();
        }

        private void dismiss(Card card) {
            remove(card);
            // drop the strut that followed it
            if (getComponentCount() > 0) {
                remove(getComponentCount() - 1);
            }
            revalidate();
            reflow();
            pane.repaint();
        }

        private void reflow() {
            int width = 400;
            int height = Math.max(getPreferredSize().height, 1);
            int x = Math.max(0, pane.getWidth() - width - 24);
            int y = Math.max(0, pane.getHeight() - height - 24);
            setBounds(x, y, width, height);
        }

        private static String escape(String text) {
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    /** Small anchor used by tests to make sure the layer paints. */
    public static void paintBackground(JComponent c, Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.p().surface());
        g2.fill(new RoundRectangle2D.Double(0, 0, c.getWidth(), c.getHeight(), Theme.R_MD, Theme.R_MD));
        g2.dispose();
        javax.swing.BorderFactory.createEmptyBorder();
    }
}
