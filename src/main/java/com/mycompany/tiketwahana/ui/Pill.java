package com.mycompany.tiketwahana.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/** Status chip. Colour is derived from the domain word, never chosen ad hoc. */
public class Pill extends JLabel {

    private final Color fg;
    private final Color bg;

    public Pill(String text, Color fg, Color bg) {
        super(text);
        this.fg = fg;
        this.bg = bg;
        setFont(Fonts.semibold(11));
        setForeground(fg);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
    }

    public static Pill status(String raw) {
        String v = raw == null ? "" : raw.trim().toUpperCase();
        Theme.Palette p = Theme.p();
        return switch (v) {
            case "AKTIF", "PAID", "OPEN", "MASUK" -> new Pill(label(v), p.accent(), p.accentSoft());
            case "DIGUNAKAN", "CLOSED" -> new Pill(label(v), p.muted(), p.surfaceAlt());
            case "MAINTENANCE", "PENDING" -> new Pill(label(v), p.warn(), p.warnSoft());
            case "TUTUP", "EXPIRED" -> new Pill(label(v), p.muted(), p.surfaceAlt());
            case "VOID", "REFUND", "KELUAR" -> new Pill(label(v), p.danger(), p.dangerSoft());
            default -> new Pill(label(v), p.muted(), p.surfaceAlt());
        };
    }

    private static String label(String raw) {
        return switch (raw) {
            case "AKTIF" -> "Aktif";
            case "MAINTENANCE" -> "Perawatan";
            case "TUTUP" -> "Tutup";
            case "DIGUNAKAN" -> "Dipakai";
            case "EXPIRED" -> "Kedaluwarsa";
            case "REFUND" -> "Refund";
            case "VOID" -> "Dibatalkan";
            case "PAID" -> "Lunas";
            case "OPEN" -> "Buka";
            case "CLOSED" -> "Tutup";
            case "MASUK" -> "Masuk";
            case "KELUAR" -> "Keluar";
            default -> raw;
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        int h = getHeight();
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), h, h, h));
        g2.dispose();
        super.paintComponent(g);
    }
}
