package com.mycompany.tiketwahana.ui;

import javax.swing.Icon;

public final class Buttons {

    private Buttons() {}

    public static FlatButton primary(String text) {
        return new FlatButton(text, FlatButton.Variant.PRIMARY, null);
    }

    public static FlatButton primary(String text, String icon) {
        return new FlatButton(text, FlatButton.Variant.PRIMARY, Icons.of(icon, 16, java.awt.Color.WHITE));
    }

    public static FlatButton secondary(String text) {
        return new FlatButton(text, FlatButton.Variant.SECONDARY, null);
    }

    public static FlatButton secondary(String text, String icon) {
        return new FlatButton(text, FlatButton.Variant.SECONDARY, Icons.of(icon, 16, Theme.p().ink()));
    }

    public static FlatButton ghost(String text) {
        return new FlatButton(text, FlatButton.Variant.GHOST, null);
    }

    public static FlatButton ghost(String text, String icon) {
        return new FlatButton(text, FlatButton.Variant.GHOST, Icons.of(icon, 16, Theme.p().muted()));
    }

    public static FlatButton danger(String text) {
        return new FlatButton(text, FlatButton.Variant.DANGER, null);
    }

    public static FlatButton danger(String text, String icon) {
        return new FlatButton(text, FlatButton.Variant.DANGER, Icons.of(icon, 16, java.awt.Color.WHITE));
    }

    /** Square icon-only button for toolbars and row actions. */
    public static FlatButton icon(String name, String tooltip, FlatButton.Variant variant) {
        FlatButton b = new FlatButton("", variant, Icons.of(name, 16,
                variant == FlatButton.Variant.PRIMARY || variant == FlatButton.Variant.DANGER
                        ? java.awt.Color.WHITE : Theme.p().muted()));
        b.setToolTipText(tooltip);
        b.setPreferredSize(new java.awt.Dimension(38, 38));
        b.height(38);
        return b;
    }

    public static Icon icon(String name, int size, java.awt.Color color) {
        return Icons.of(name, size, color);
    }
}
