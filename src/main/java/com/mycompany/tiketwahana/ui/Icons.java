package com.mycompany.tiketwahana.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/** Line icons drawn with Path2D: crisp at any scale, no image files in the repo. */
public final class Icons {

    private Icons() {}

    public static Icon of(String name, int size, Color color) {
        return new VectorIcon(name, size, color);
    }

    public static Icon of(String name, int size) {
        return new VectorIcon(name, size, Theme.p().muted());
    }

    private static final class VectorIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;

        VectorIcon(String name, int size, Color color) {
            this.name = name;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            double s = size / 24.0;
            g2.translate(x, y);
            g2.scale(s, s);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            draw(g2, name);
            g2.dispose();
        }
    }

    private static void draw(Graphics2D g, String name) {
        switch (name) {
            case "grid" -> {
                g.draw(new RoundRectangle2D.Double(4, 4, 7, 7, 2, 2));
                g.draw(new RoundRectangle2D.Double(13, 4, 7, 7, 2, 2));
                g.draw(new RoundRectangle2D.Double(4, 13, 7, 7, 2, 2));
                g.draw(new RoundRectangle2D.Double(13, 13, 7, 7, 2, 2));
            }
            case "cart" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3, 4);
                p.lineTo(6, 4);
                p.lineTo(8.5, 15);
                p.lineTo(18, 15);
                p.lineTo(20, 7);
                p.lineTo(7, 7);
                g.draw(p);
                g.fill(new Ellipse2D.Double(8, 18, 2.4, 2.4));
                g.fill(new Ellipse2D.Double(16.5, 18, 2.4, 2.4));
            }
            case "ticket" -> {
                g.draw(new RoundRectangle2D.Double(3, 6, 18, 12, 3, 3));
                java.awt.geom.Line2D dash = new java.awt.geom.Line2D.Double(12, 7, 12, 17);
                java.awt.Stroke old = g.getStroke();
                g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1,
                        new float[] {2f, 2.4f}, 0));
                g.draw(dash);
                g.setStroke(old);
                g.draw(new java.awt.geom.Line2D.Double(15, 10, 18, 10));
                g.draw(new java.awt.geom.Line2D.Double(15, 14, 18, 14));
            }
            case "park" -> {
                g.draw(new Ellipse2D.Double(6, 3, 12, 12));
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * 2 * i / 8;
                    g.draw(new java.awt.geom.Line2D.Double(12, 9, 12 + Math.cos(a) * 6, 9 + Math.sin(a) * 6));
                }
                g.fill(new Ellipse2D.Double(11, 8, 2, 2));
                g.draw(new java.awt.geom.Line2D.Double(12, 15, 12, 21));
                g.draw(new java.awt.geom.Line2D.Double(7, 21, 17, 21));
            }
            case "chart" -> {
                g.draw(new java.awt.geom.Line2D.Double(4, 20, 20, 20));
                g.fill(new RoundRectangle2D.Double(5.5, 12, 3.2, 8, 1.4, 1.4));
                g.fill(new RoundRectangle2D.Double(10.4, 8, 3.2, 12, 1.4, 1.4));
                g.fill(new RoundRectangle2D.Double(15.3, 4, 3.2, 16, 1.4, 1.4));
            }
            case "cash" -> {
                g.draw(new RoundRectangle2D.Double(2.5, 6, 19, 12, 3, 3));
                g.draw(new Ellipse2D.Double(9.5, 9, 5, 6));
            }
            case "list" -> {
                for (int i = 0; i < 3; i++) {
                    double y = 7 + i * 5;
                    g.fill(new Ellipse2D.Double(3.5, y - 1, 2, 2));
                    g.draw(new java.awt.geom.Line2D.Double(8, y, 20, y));
                }
            }
            case "gear" -> {
                g.draw(new Ellipse2D.Double(8.5, 8.5, 7, 7));
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * 2 * i / 8;
                    g.draw(new java.awt.geom.Line2D.Double(12 + Math.cos(a) * 7, 12 + Math.sin(a) * 7,
                            12 + Math.cos(a) * 9.6, 12 + Math.sin(a) * 9.6));
                }
            }
            case "plus" -> {
                g.draw(new java.awt.geom.Line2D.Double(12, 5, 12, 19));
                g.draw(new java.awt.geom.Line2D.Double(5, 12, 19, 12));
            }
            case "minus" -> g.draw(new java.awt.geom.Line2D.Double(5, 12, 19, 12));
            case "search" -> {
                g.draw(new Ellipse2D.Double(4.5, 4.5, 11, 11));
                g.draw(new java.awt.geom.Line2D.Double(14.5, 14.5, 20, 20));
            }
            case "sun" -> {
                g.draw(new Ellipse2D.Double(8.5, 8.5, 7, 7));
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI * 2 * i / 8;
                    g.draw(new java.awt.geom.Line2D.Double(12 + Math.cos(a) * 7.6, 12 + Math.sin(a) * 7.6,
                            12 + Math.cos(a) * 9.8, 12 + Math.sin(a) * 9.8));
                }
            }
            case "moon" -> {
                Path2D p = new Path2D.Double();
                p.append(new Arc2D.Double(4, 3, 17, 17, 60, 240, Arc2D.OPEN), false);
                p.append(new Arc2D.Double(8, 1, 14, 16, 250, 120, Arc2D.OPEN), true);
                g.draw(p);
            }
            case "close" -> {
                g.draw(new java.awt.geom.Line2D.Double(6, 6, 18, 18));
                g.draw(new java.awt.geom.Line2D.Double(18, 6, 6, 18));
            }
            case "check" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(5, 12.5);
                p.lineTo(10, 17);
                p.lineTo(19, 7);
                g.draw(p);
            }
            case "trash" -> {
                g.draw(new java.awt.geom.Line2D.Double(4, 7, 20, 7));
                g.draw(new RoundRectangle2D.Double(6, 7, 12, 13, 2, 2));
                g.draw(new java.awt.geom.Line2D.Double(9.5, 4.5, 14.5, 4.5));
                g.draw(new java.awt.geom.Line2D.Double(10, 11, 10, 16));
                g.draw(new java.awt.geom.Line2D.Double(14, 11, 14, 16));
            }
            case "edit" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(4, 20);
                p.lineTo(5, 15);
                p.lineTo(15.5, 4.5);
                p.lineTo(19.5, 8.5);
                p.lineTo(9, 19);
                p.closePath();
                g.draw(p);
            }
            case "print" -> {
                g.draw(new RoundRectangle2D.Double(4, 9, 16, 8, 2, 2));
                g.draw(new Rectangle2D.Double(7, 4, 10, 5));
                g.draw(new Rectangle2D.Double(7, 15, 10, 6));
            }
            case "lock" -> {
                g.draw(new RoundRectangle2D.Double(5, 10.5, 14, 10, 3, 3));
                g.draw(new Arc2D.Double(8, 3.5, 8, 10, 0, 180, Arc2D.OPEN));
            }
            case "logout" -> {
                g.draw(new java.awt.geom.Line2D.Double(4, 4, 4, 20));
                g.draw(new java.awt.geom.Line2D.Double(4, 4, 13, 4));
                g.draw(new java.awt.geom.Line2D.Double(4, 20, 13, 20));
                Path2D p = new Path2D.Double();
                p.moveTo(10, 12);
                p.lineTo(20, 12);
                p.moveTo(16, 8);
                p.lineTo(20, 12);
                p.lineTo(16, 16);
                g.draw(p);
            }
            case "user" -> {
                g.draw(new Ellipse2D.Double(8.5, 4, 7, 7));
                Path2D p = new Path2D.Double();
                p.moveTo(4.5, 20);
                p.curveTo(5.5, 15.5, 9, 13.5, 12, 13.5);
                p.curveTo(15, 13.5, 18.5, 15.5, 19.5, 20);
                g.draw(p);
            }
            case "chevronDown" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(6, 9.5);
                p.lineTo(12, 15.5);
                p.lineTo(18, 9.5);
                g.draw(p);
            }
            case "chevronRight" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(9.5, 6);
                p.lineTo(15.5, 12);
                p.lineTo(9.5, 18);
                g.draw(p);
            }
            case "refresh" -> {
                g.draw(new Arc2D.Double(4, 4, 16, 16, 40, 280, Arc2D.OPEN));
                Path2D p = new Path2D.Double();
                p.moveTo(20, 4);
                p.lineTo(20, 9);
                p.lineTo(15, 9);
                g.draw(p);
            }
            case "download" -> {
                g.draw(new java.awt.geom.Line2D.Double(12, 4, 12, 15));
                Path2D p = new Path2D.Double();
                p.moveTo(7.5, 11);
                p.lineTo(12, 15.5);
                p.lineTo(16.5, 11);
                g.draw(p);
                g.draw(new java.awt.geom.Line2D.Double(5, 19, 19, 19));
            }
            case "alert" -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 4);
                p.lineTo(21, 19.5);
                p.lineTo(3, 19.5);
                p.closePath();
                g.draw(p);
                g.draw(new java.awt.geom.Line2D.Double(12, 10, 12, 14));
                g.fill(new Ellipse2D.Double(11.2, 15.6, 1.6, 1.6));
            }
            case "clock" -> {
                g.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
                g.draw(new java.awt.geom.Line2D.Double(12, 7.5, 12, 12.5));
                g.draw(new java.awt.geom.Line2D.Double(12, 12.5, 15.5, 14.5));
            }
            case "qr" -> {
                g.draw(new RoundRectangle2D.Double(3.5, 3.5, 6.5, 6.5, 1.5, 1.5));
                g.draw(new RoundRectangle2D.Double(14, 3.5, 6.5, 6.5, 1.5, 1.5));
                g.draw(new RoundRectangle2D.Double(3.5, 14, 6.5, 6.5, 1.5, 1.5));
                g.fill(new Rectangle2D.Double(14, 14, 3, 3));
                g.fill(new Rectangle2D.Double(18, 18, 2.5, 2.5));
                g.fill(new Rectangle2D.Double(14, 19, 2.5, 2));
            }
            case "power" -> {
                g.draw(new Arc2D.Double(4.5, 5, 15, 15, 70, 320, Arc2D.OPEN));
                g.draw(new java.awt.geom.Line2D.Double(12, 3, 12, 11));
            }
            default -> g.draw(new Ellipse2D.Double(6, 6, 12, 12));
        }
    }
}
