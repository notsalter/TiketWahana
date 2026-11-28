package com.mycompany.tiketwahana.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** One sentence and one action, never a blank panel. */
public class EmptyState extends JPanel {

    public EmptyState(String icon, String title, String subtitle, JButton action) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(48, 24, 48, 24));

        JLabel glyph = new JLabel(Icons.of(icon, 28, Theme.p().muted())) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.p().surfaceAlt());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        glyph.setHorizontalAlignment(JLabel.CENTER);
        glyph.setAlignmentX(Component.CENTER_ALIGNMENT);
        glyph.setPreferredSize(new Dimension(56, 56));
        glyph.setMaximumSize(new Dimension(56, 56));
        glyph.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.semibold(15));
        titleLabel.setForeground(Theme.p().ink());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("<html><div style='text-align:center;width:320px'>" + subtitle + "</div></html>");
        subtitleLabel.setFont(Fonts.regular(13));
        subtitleLabel.setForeground(Theme.p().muted());
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(glyph);
        add(Box.createVerticalStrut(14));
        add(titleLabel);
        add(Box.createVerticalStrut(6));
        add(subtitleLabel);
        if (action != null) {
            action.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(Box.createVerticalStrut(18));
            add(action);
        }
    }
}
