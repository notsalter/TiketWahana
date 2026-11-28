package com.mycompany.tiketwahana.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** Rounded surface with one soft shadow. The only container style in the app. */
public class Card extends JPanel {

    private int radius = Theme.R_LG;
    private boolean shadow = true;
    private Color fill;

    public Card() {
        this(null);
    }

    public Card(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XL, Theme.GAP_XL, Theme.GAP_XL, Theme.GAP_XL));
    }

    public Card flat() {
        this.shadow = false;
        return this;
    }

    public Card radius(int value) {
        this.radius = value;
        return this;
    }

    public Card padding(int top, int left, int bottom, int right) {
        setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return this;
    }

    public Card fill(Color color) {
        this.fill = color;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Theme.Palette p = Theme.p();
        int w = getWidth();
        int h = getHeight();
        if (shadow) {
            g2.setColor(new Color(p.shadow().getRed(), p.shadow().getGreen(), p.shadow().getBlue(), 10));
            g2.fill(new RoundRectangle2D.Double(0, 2, w, h - 2, radius, radius));
            g2.setColor(new Color(p.shadow().getRed(), p.shadow().getGreen(), p.shadow().getBlue(), 14));
            g2.fill(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, radius, radius));
        }
        g2.setColor(fill == null ? p.surface() : fill);
        g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, radius, radius));
        g2.setColor(p.border());
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, radius, radius));
        g2.dispose();
        super.paintComponent(g);
    }
}
