package com.mycompany.tiketwahana.ui.charts;

import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.ui.Fonts;
import com.mycompany.tiketwahana.ui.Theme;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JComponent;

/** 30-day revenue trend, drawn with a path and a gradient, no chart library. */
public class Sparkline extends JComponent {

    private final List<BigDecimal> values;

    public Sparkline(List<BigDecimal> values) {
        this.values = values;
        setOpaque(false);
        setPreferredSize(new Dimension(420, 150));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        int w = getWidth();
        int h = getHeight();
        Theme.Palette p = Theme.p();

        if (values == null || values.size() < 2) {
            g2.setColor(p.muted());
            g2.setFont(Fonts.regular(12));
            g2.drawString("Belum ada data", 8, h / 2);
            g2.dispose();
            return;
        }

        double max = 0;
        for (BigDecimal v : values) {
            max = Math.max(max, v.doubleValue());
        }
        if (max <= 0) {
            max = 1;
        }
        double stepX = (double) (w - 16) / (values.size() - 1);
        double baseY = h - 28;

        Path2D line = new Path2D.Double();
        for (int i = 0; i < values.size(); i++) {
            double x = 8 + i * stepX;
            double y = baseY - (values.get(i).doubleValue() / max) * (baseY - 12);
            if (i == 0) {
                line.moveTo(x, y);
            } else {
                line.lineTo(x, y);
            }
        }
        Path2D area = new Path2D.Double(line);
        area.lineTo(8 + (values.size() - 1) * stepX, baseY);
        area.lineTo(8, baseY);
        area.closePath();

        g2.setPaint(new java.awt.GradientPaint(0f, 0f, withAlpha(p.accent(), 60), 0f, (float) (baseY + 8), withAlpha(p.accent(), 0)));
        g2.fill(area);
        g2.setColor(p.accent());
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(line);

        // last point
        double lastX = 8 + (values.size() - 1) * stepX;
        double lastY = baseY - (values.get(values.size() - 1).doubleValue() / max) * (baseY - 12);
        g2.setColor(p.surface());
        g2.fillOval((int) lastX - 4, (int) lastY - 4, 8, 8);
        g2.setColor(p.accent());
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval((int) lastX - 4, (int) lastY - 4, 8, 8);

        // baseline + labels
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(p.border());
        g2.drawLine(8, (int) baseY, w - 8, (int) baseY);
        g2.setFont(Fonts.regular(10));
        g2.setColor(p.muted());
        g2.drawString("30 hari lalu", 8, h - 8);
        String peak = Money.format(BigDecimal.valueOf(max));
        int peakWidth = g2.getFontMetrics().stringWidth(peak);
        g2.drawString(peak, w - 8 - peakWidth, 14);
        g2.dispose();
    }

    private static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}
