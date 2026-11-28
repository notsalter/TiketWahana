package com.mycompany.tiketwahana.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.DoubleConsumer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

/** Shared easing and one-shot animations. Durations live in the design brief. */
public final class Motion {

    public static final int FAST = 120;
    public static final int PANEL = 180;
    public static final int SCREEN = 240;

    private static boolean enabled = true;

    private Motion() {}

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean enabled() {
        return enabled;
    }

    public static double easeOutCubic(double t) {
        double f = 1 - t;
        return 1 - f * f * f;
    }

    /** Runs progress 0..1 over durationMs, honouring the reduce-motion setting. */
    public static void run(int durationMs, DoubleConsumer progress) {
        if (!enabled || durationMs <= 0) {
            progress.accept(1);
            return;
        }
        long start = System.nanoTime();
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            double raw = Math.min(1.0, (System.nanoTime() - start) / (durationMs * 1_000_000.0));
            progress.accept(easeOutCubic(raw));
            if (raw >= 1.0) {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.setCoalesce(true);
        progress.accept(0);
        timer.start();
    }

    /** Fades a component and its children from transparent to opaque. */
    public static void fadeIn(JComponent component, int durationMs) {
        AlphaLayer layer = new AlphaLayer(component);
        run(durationMs, v -> {
            layer.alpha = (float) v;
            layer.repaint();
        });
    }

    /** A JPanel that paints its children with a shared alpha, for crossfades. */
    public static final class AlphaLayer extends JPanel {
        private final JComponent content;
        private float alpha = 1f;

        public AlphaLayer(JComponent content) {
            this.content = content;
            setLayout(new java.awt.BorderLayout());
            setOpaque(false);
            add(content, java.awt.BorderLayout.CENTER);
        }

        public void setAlpha(float value) {
            this.alpha = Math.max(0f, Math.min(1f, value));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            super.paintChildren(g2);
            g2.dispose();
        }
    }
}
