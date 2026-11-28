package com.mycompany.tiketwahana.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * One design system, configured once through UIManager. Components never invent
 * their own colours; they ask the palette.
 */
public final class Theme {

    public enum Mode { LIGHT, DARK }

    public record Palette(Color bg, Color surface, Color surfaceAlt, Color ink, Color muted, Color border,
                          Color accent, Color accentSoft, Color danger, Color dangerSoft, Color warn,
                          Color warnSoft, Color onAccent, Color shadow) {
    }

    public static final int R_SM = 8;
    public static final int R_MD = 12;
    public static final int R_LG = 16;
    public static final int GAP_XS = 4;
    public static final int GAP_S = 8;
    public static final int GAP_M = 12;
    public static final int GAP_L = 16;
    public static final int GAP_XL = 24;
    public static final int GAP_XXL = 32;

    private static final Palette LIGHT = new Palette(
            new Color(0xF6F7F9), new Color(0xFFFFFF), new Color(0xF1F3F6),
            new Color(0x111827), new Color(0x6B7280), new Color(0xE5E7EB),
            new Color(0x16A34A), new Color(0xDCFCE7),
            new Color(0xDC2626), new Color(0xFEE2E2),
            new Color(0xD97706), new Color(0xFEF3C7),
            new Color(0xFFFFFF), new Color(15, 23, 42, 22));

    private static final Palette DARK = new Palette(
            new Color(0x0B0F14), new Color(0x121821), new Color(0x182130),
            new Color(0xE5E7EB), new Color(0x94A3B8), new Color(0x1F2937),
            new Color(0x22C55E), new Color(0x123524),
            new Color(0xF87171), new Color(0x3A1414),
            new Color(0xFBBF24), new Color(0x3A2A0B),
            new Color(0x06240F), new Color(0, 0, 0, 90));

    private static Mode mode = Mode.LIGHT;

    private Theme() {}

    public static Palette p() {
        return mode == Mode.DARK ? DARK : LIGHT;
    }

    public static Mode mode() {
        return mode;
    }

    public static void install(Mode next) {
        mode = next;
        Palette p = p();
        UIManager.put("Panel.background", p.bg());
        UIManager.put("Viewport.background", p.bg());
        UIManager.put("OptionPane.background", p.surface());
        UIManager.put("OptionPane.messageForeground", p.ink());
        UIManager.put("Label.foreground", p.ink());
        UIManager.put("Label.disabledForeground", p.muted());
        UIManager.put("Table.background", p.surface());
        UIManager.put("Table.foreground", p.ink());
        UIManager.put("Table.selectionBackground", p.accentSoft());
        UIManager.put("Table.selectionForeground", p.ink());
        UIManager.put("Table.gridColor", p.border());
        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(0, 0, 0, 0));
        UIManager.put("TableHeader.background", p.surface());
        UIManager.put("TableHeader.foreground", p.muted());
        UIManager.put("TableHeader.separatorColor", p.border());
        UIManager.put("TableHeader.bottomSeparatorColor", p.border());
        UIManager.put("ScrollPane.background", p.bg());
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumb", p.border());
        UIManager.put("ScrollBar.track", p.bg());
        UIManager.put("ScrollBar.background", p.bg());
        UIManager.put("TextField.background", p.surface());
        UIManager.put("TextField.foreground", p.ink());
        UIManager.put("TextField.caretForeground", p.accent());
        UIManager.put("TextField.selectionBackground", p.accentSoft());
        UIManager.put("TextField.selectionForeground", p.ink());
        UIManager.put("TextField.inactiveBackground", p.surfaceAlt());
        UIManager.put("FormattedTextField.background", p.surface());
        UIManager.put("PasswordField.background", p.surface());
        UIManager.put("PasswordField.foreground", p.ink());
        UIManager.put("PasswordField.caretForeground", p.accent());
        UIManager.put("ComboBox.background", p.surface());
        UIManager.put("ComboBox.foreground", p.ink());
        UIManager.put("ComboBox.selectionBackground", p.accentSoft());
        UIManager.put("ComboBox.selectionForeground", p.ink());
        UIManager.put("ComboBox.buttonBackground", p.surface());
        UIManager.put("PopupMenu.background", p.surface());
        UIManager.put("PopupMenu.foreground", p.ink());
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(p.border()));
        UIManager.put("MenuItem.background", p.surface());
        UIManager.put("MenuItem.foreground", p.ink());
        UIManager.put("MenuItem.selectionBackground", p.surfaceAlt());
        UIManager.put("MenuItem.selectionForeground", p.ink());
        UIManager.put("ToolTip.background", p.ink());
        UIManager.put("ToolTip.foreground", p.surface());
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(6, 10, 6, 10));
        UIManager.put("FileChooser.background", p.surface());
        UIManager.put("FileChooser.listBackground", p.surface());
        UIManager.put("SplitPane.background", p.border());
        UIManager.put("SplitPane.dividerSize", 1);
        UIManager.put("ProgressBar.background", p.surfaceAlt());
        UIManager.put("ProgressBar.foreground", p.accent());

        Font base = Fonts.regular(14);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(base));
            }
        }
        UIManager.put("Table.font", Fonts.regular(13));
        UIManager.put("TableHeader.font", Fonts.medium(11));
        UIManager.put("ToolTip.font", Fonts.regular(12));

        for (Window window : Window.getWindows()) {
            if (window instanceof Frame) {
                javax.swing.SwingUtilities.updateComponentTreeUI(window);
            }
            window.repaint();
        }
    }

    public static void toggle() {
        install(mode == Mode.DARK ? Mode.LIGHT : Mode.DARK);
    }

    /** Thin scrollbars: no arrow buttons, rounded thumb, 4px of air. */
    public static final class ScrollUI extends BasicScrollBarUI {
        @Override
        protected javax.swing.JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected javax.swing.JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private static javax.swing.JButton zeroButton() {
            javax.swing.JButton b = new javax.swing.JButton();
            b.setPreferredSize(new java.awt.Dimension(0, 0));
            b.setMinimumSize(new java.awt.Dimension(0, 0));
            b.setMaximumSize(new java.awt.Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle trackBounds) {
            g.setColor(p().bg());
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        @Override
        protected void paintThumb(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(p().border());
            int inset = 2;
            g2.fillRoundRect(thumbBounds.x + inset, thumbBounds.y + inset,
                    thumbBounds.width - inset * 2, thumbBounds.height - inset * 2, 6, 6);
            g2.dispose();
        }

        public static void apply(JScrollBar bar) {
            bar.setUI(new ScrollUI());
            bar.setUnitIncrement(18);
            bar.setOpaque(false);
        }
    }

    public static void scrollbars(JComponent... components) {
        for (JComponent c : components) {
            if (c instanceof javax.swing.JScrollPane sp) {
                ScrollUI.apply(sp.getVerticalScrollBar());
                ScrollUI.apply(sp.getHorizontalScrollBar());
                sp.setBorder(BorderFactory.createEmptyBorder());
                sp.setOpaque(false);
                sp.getViewport().setOpaque(false);
            }
        }
    }
}
