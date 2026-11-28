package com.mycompany.tiketwahana.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Page and card headings: title, one line of context, optional trailing action. */
public class SectionHeader extends JPanel {

    public SectionHeader(String title, String subtitle) {
        this(title, subtitle, null);
    }

    public SectionHeader(String title, String subtitle, JComponent trailing) {
        setOpaque(false);
        setLayout(new BorderLayout(16, 0));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(Fonts.semibold(20));
        t.setForeground(Theme.p().ink());
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(t);
        if (subtitle != null && !subtitle.isEmpty()) {
            text.add(Box.createVerticalStrut(4));
            JLabel s = new JLabel(subtitle);
            s.setFont(Fonts.regular(13));
            s.setForeground(Theme.p().muted());
            s.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(s);
        }
        add(text, BorderLayout.WEST);
        if (trailing != null) {
            add(trailing, BorderLayout.EAST);
        }
        setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
    }

    public SectionHeader compact() {
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        return this;
    }
}
