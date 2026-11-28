package com.mycompany.tiketwahana.ui.screens;

import com.mycompany.tiketwahana.ui.AppContext;
import com.mycompany.tiketwahana.ui.Theme;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/** A full page. The shell creates it once, then calls onShow() on every visit. */
public abstract class Screen extends JPanel {

    protected final AppContext ctx;

    protected Screen(AppContext ctx, String route) {
        this.ctx = ctx;
        setName(route);
        setOpaque(true);
        setBackground(Theme.p().bg());
        setLayout(new java.awt.BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP_XL, Theme.GAP_XL, Theme.GAP_XL, Theme.GAP_XL));
    }

    /** Reload data from the database. Called on navigation and on F5. */
    public abstract void onShow();
}
