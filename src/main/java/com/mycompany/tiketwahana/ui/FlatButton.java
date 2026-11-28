package com.mycompany.tiketwahana.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;

/** Flat button with a 120ms hover, a visible focus ring and four intents. */
public class FlatButton extends JButton {

    public enum Variant { PRIMARY, SECONDARY, GHOST, DANGER }

    private final Variant variant;
    private float hover;
    private boolean pointerOver;
    private int radius = Theme.R_SM;
    private int fixedHeight = 38;

    public FlatButton(String text, Variant variant, Icon icon) {
        super(text);
        this.variant = variant;
        setIcon(icon);
        setIconTextGap(8);
        setFont(Fonts.semibold(13));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setRolloverEnabled(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pointerOver = true;
                Motion.run(Motion.FAST, v -> {
                    hover = (float) v;
                    repaint();
                });
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pointerOver = false;
                Motion.run(Motion.FAST, v -> {
                    hover = 1f - (float) v;
                    repaint();
                });
            }
        });
    }

    public FlatButton height(int px) {
        this.fixedHeight = px;
        revalidate();
        return this;
    }

    public FlatButton radius(int px) {
        this.radius = px;
        return this;
    }

    public FlatButton bold() {
        setFont(Fonts.bold(13));
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(fixedHeight, d.height));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    private Color backgroundFor(Theme.Palette p) {
        return switch (variant) {
            case PRIMARY -> lerp(p.accent(), p.accent().darker(), hover * 0.28f);
            case SECONDARY -> lerp(p.surface(), p.surfaceAlt(), hover);
            case GHOST -> lerp(new Color(0, 0, 0, 0), p.surfaceAlt(), hover);
            case DANGER -> lerp(p.danger(), p.danger().darker(), hover * 0.28f);
        };
    }

    private Color foregroundFor(Theme.Palette p) {
        return switch (variant) {
            case PRIMARY, DANGER -> Color.WHITE;
            case SECONDARY -> p.ink();
            case GHOST -> pointerOver ? p.ink() : p.muted();
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Theme.Palette p = Theme.p();
        int w = getWidth();
        int h = getHeight();

        Color bg = backgroundFor(p);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, radius, radius));
        if (variant == Variant.SECONDARY) {
            g2.setColor(p.border());
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, radius, radius));
        }
        if (isFocusOwner()) {
            g2.setColor(p.accent());
            g2.setStroke(new java.awt.BasicStroke(1.6f));
            g2.draw(new RoundRectangle2D.Double(2.5, 2.5, w - 5, h - 5, radius, radius));
        }
        if (!isEnabled()) {
            g2.setColor(new Color(255, 255, 255, 120));
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, radius, radius));
        }

        FontMetrics fm = g2.getFontMetrics(getFont());
        Icon icon = getIcon();
        int textWidth = fm.stringWidth(getText());
        int iconWidth = icon == null ? 0 : icon.getIconWidth() + (getText().isEmpty() ? 0 : getIconTextGap());
        int contentWidth = textWidth + iconWidth;
        int x = (w - contentWidth) / 2;
        int baseline = (h - fm.getHeight()) / 2 + fm.getAscent();
        Color fg = isEnabled() ? foregroundFor(p) : p.muted();
        if (icon != null) {
            icon.paintIcon(this, g2, x, (h - icon.getIconHeight()) / 2);
            x += iconWidth;
        }
        g2.setColor(fg);
        g2.setFont(getFont());
        g2.drawString(getText(), x, baseline);
        g2.dispose();
    }

    static Color lerp(Color a, Color b, float t) {
        float f = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * f),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * f),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * f),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * f));
    }

    public Insets insets() {
        return getInsets();
    }
}
