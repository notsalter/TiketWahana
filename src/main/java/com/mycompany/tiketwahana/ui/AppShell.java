package com.mycompany.tiketwahana.ui;

import com.mycompany.tiketwahana.core.Role;
import com.mycompany.tiketwahana.core.SessionUser;
import com.mycompany.tiketwahana.ui.screens.Screen;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/** Header, rail, content. One window, no dispose-and-reopen flashes. */
public class AppShell extends JPanel {

    private record Nav(String route, String label, String icon, Role.Permission permission) {
    }

    private static final List<Nav> NAV = List.of(
            new Nav("dashboard", "Beranda", "grid", null),
            new Nav("pos", "Kasir", "cart", Role.Permission.SELL),
            new Nav("tickets", "Tiket", "ticket", Role.Permission.REDEEM),
            new Nav("wahana", "Wahana", "park", Role.Permission.MANAGE_WAHANA),
            new Nav("report", "Laporan", "chart", Role.Permission.VIEW_REPORT),
            new Nav("shift", "Shift", "cash", Role.Permission.SHIFT),
            new Nav("audit", "Audit", "list", Role.Permission.AUDIT),
            new Nav("settings", "Pengaturan", "gear", null));

    private final AppContext ctx;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, Screen> screens = new LinkedHashMap<>();
    private final Map<String, NavItem> navItems = new LinkedHashMap<>();
    private final JLabel shiftChip = new JLabel();
    private final JLabel avatar = new JLabel();
    private final List<JComponent> retint = new ArrayList<>();
    private String current = "dashboard";

    public AppShell(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Theme.p().bg());
        content.setOpaque(true);
        content.setBackground(Theme.p().bg());
        add(buildRail(), BorderLayout.WEST);
        add(buildBody(), BorderLayout.CENTER);
        installKeys();
        navigate("dashboard");
    }

    private JComponent buildRail() {
        JPanel rail = new JPanel();
        rail.setLayout(new BoxLayout(rail, BoxLayout.Y_AXIS));
        rail.setBackground(Theme.p().surface());
        rail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.p().border()),
                BorderFactory.createEmptyBorder(20, 16, 16, 16)));
        rail.setPreferredSize(new Dimension(232, 0));

        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        brand.add(new Logo(34), BorderLayout.WEST);
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("TiketWahana");
        name.setFont(Fonts.semibold(15));
        name.setForeground(Theme.p().ink());
        JLabel tag = new JLabel("Panel operasional");
        tag.setFont(Fonts.regular(11));
        tag.setForeground(Theme.p().muted());
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandText.add(name);
        brandText.add(tag);
        brand.add(brandText, BorderLayout.CENTER);
        rail.add(brand);
        rail.add(Box.createVerticalStrut(26));

        rail.add(sectionLabel("Operasional"));
        addNav(rail, "dashboard");
        addNav(rail, "pos");
        addNav(rail, "tickets");
        rail.add(Box.createVerticalStrut(18));
        rail.add(sectionLabel("Kelola"));
        addNav(rail, "wahana");
        addNav(rail, "report");
        addNav(rail, "shift");
        rail.add(Box.createVerticalStrut(18));
        rail.add(sectionLabel("Sistem"));
        addNav(rail, "audit");
        addNav(rail, "settings");
        rail.add(Box.createVerticalGlue());

        JPanel footer = new JPanel(new BorderLayout(6, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel version = new JLabel("v2.0  ·  Java " + System.getProperty("java.version").split("\\.")[0]);
        version.setFont(Fonts.regular(10));
        version.setForeground(Theme.p().muted());
        footer.add(version, BorderLayout.WEST);
        rail.add(footer);
        return rail;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(Fonts.semibold(10));
        label.setForeground(Theme.p().muted());
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addNav(JPanel rail, String route) {
        Nav nav = NAV.stream().filter(n -> n.route().equals(route)).findFirst().orElseThrow();
        if (nav.permission() != null && !ctx.user.can(nav.permission())) {
            return;
        }
        NavItem item = new NavItem(nav);
        navItems.put(route, item);
        rail.add(item);
        rail.add(Box.createVerticalStrut(2));
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(true);
        body.setBackground(Theme.p().bg());
        body.add(buildHeader(), BorderLayout.NORTH);
        body.add(content, BorderLayout.CENTER);
        return body;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(Theme.p().surface());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.p().border()),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        header.setPreferredSize(new Dimension(0, 68));

        FlatButton search = new FlatButton("Cari aksi, wahana, atau kode tiket", FlatButton.Variant.SECONDARY,
                Icons.of("search", 15, Theme.p().muted()));
        search.setFont(Fonts.regular(12));
        search.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        search.setPreferredSize(new Dimension(340, 38));
        search.addActionListener(e -> openPalette());
        retint.add(search);

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        left.add(search, BorderLayout.WEST);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        shiftChip.setFont(Fonts.regular(12));
        shiftChip.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        shiftChip.setOpaque(false);
        shiftChip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        shiftChip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigate("shift");
            }
        });
        right.add(shiftChip);

        FlatButton themeButton = Buttons.icon(Theme.mode() == Theme.Mode.DARK ? "sun" : "moon",
                "Ganti tema (Ctrl+J)", FlatButton.Variant.GHOST);
        themeButton.addActionListener(e -> toggleTheme());
        retint.add(themeButton);
        right.add(themeButton);

        FlatButton palette = Buttons.icon("search", "Command palette (Ctrl+K)", FlatButton.Variant.GHOST);
        palette.addActionListener(e -> openPalette());
        palette.setVisible(false);
        right.add(palette);

        right.add(divider());
        right.add(userBlock());

        FlatButton logout = Buttons.icon("power", "Keluar", FlatButton.Variant.GHOST);
        logout.addActionListener(e -> logout());
        retint.add(logout);
        right.add(logout);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent divider() {
        JPanel line = new JPanel();
        line.setBackground(Theme.p().border());
        line.setPreferredSize(new Dimension(1, 28));
        return line;
    }

    private JComponent userBlock() {
        JPanel block = new JPanel(new BorderLayout(10, 0));
        block.setOpaque(false);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setHorizontalAlignment(JLabel.CENTER);
        avatar.setFont(Fonts.semibold(12));
        paintAvatar();
        block.add(avatar, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(ctx.user.fullName());
        name.setFont(Fonts.semibold(12));
        name.setForeground(Theme.p().ink());
        JLabel role = new JLabel(ctx.user.role().label());
        role.setFont(Fonts.regular(11));
        role.setForeground(Theme.p().muted());
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(name);
        text.add(role);
        block.add(text, BorderLayout.CENTER);
        return block;
    }

    private void paintAvatar() {
        String initials = initials(ctx.user.fullName());
        avatar.setText(initials);
        avatar.setForeground(Theme.p().accent());
        avatar.setIcon(new javax.swing.Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.p().accentSoft());
                g2.fillOval(0, 0, 34, 34);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 34;
            }

            @Override
            public int getIconHeight() {
                return 34;
            }
        });
    }

    private static String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    public void navigate(String route) {
        Screen screen = screens.get(route);
        if (screen == null) {
            screen = Screens.create(route, ctx);
            screens.put(route, screen);
            content.add(screen, route);
        }
        current = route;
        cards.show(content, route);
        navItems.forEach((key, item) -> item.setActive(key.equals(route)));
        refreshShiftChip();
        try {
            screen.onShow();
        } catch (RuntimeException ignored) {
            // a screen that fails to load shows its own empty state
        }
        Motion.fadeIn(screen, Motion.SCREEN);
    }

    public String currentRoute() {
        return current;
    }

    public void refreshShiftChip() {
        ctx.refreshShift();
        if (ctx.shiftOpen()) {
            shiftChip.setText("Shift " + ctx.shift.kode().substring(6) + " · buka");
            shiftChip.setForeground(Theme.p().accent());
            shiftChip.setIcon(Icons.of("check", 13, Theme.p().accent()));
        } else {
            shiftChip.setText("Shift belum dibuka");
            shiftChip.setForeground(Theme.p().warn());
            shiftChip.setIcon(Icons.of("alert", 13, Theme.p().warn()));
        }
        shiftChip.setHorizontalTextPosition(JLabel.RIGHT);
        shiftChip.setIconTextGap(8);
        shiftChip.repaint();
    }

    public void toggleTheme() {
        Theme.toggle();
        com.mycompany.tiketwahana.core.Env.properties().setProperty("ui.theme",
                Theme.mode() == Theme.Mode.DARK ? "dark" : "light");
        applyThemeToTree();
    }

    private void applyThemeToTree() {
        SwingUtilities.updateComponentTreeUI(this);
        setBackground(Theme.p().bg());
        content.setBackground(Theme.p().bg());
        avatar.setForeground(Theme.p().accent());
        paintAvatar();
        refreshShiftChip();
        screens.values().forEach(Screen::onShow);
        repaint();
    }

    private void openPalette() {
        List<CommandPalette.Action> actions = new ArrayList<>();
        for (Nav nav : NAV) {
            if (nav.permission() != null && !ctx.user.can(nav.permission())) {
                continue;
            }
            actions.add(new CommandPalette.Action(nav.label(), "Buka layar " + nav.label().toLowerCase(),
                    () -> navigate(nav.route())));
        }
        actions.add(new CommandPalette.Action("Ganti tema", "Terang / gelap", this::toggleTheme));
        actions.add(new CommandPalette.Action("Muat ulang layar", "F5", this::reloadCurrent));
        try {
            for (var wahana : ctx.wahana.list(true)) {
                actions.add(new CommandPalette.Action(wahana.kode() + " · " + wahana.nama(),
                        "Buka kasir untuk " + wahana.kode(), () -> navigate("pos")));
            }
        } catch (Exception ignored) {
            // palette still works without the catalogue
        }
        new CommandPalette(SwingUtilities.getWindowAncestor(this), actions).open();
    }

    private void reloadCurrent() {
        Screen screen = screens.get(current);
        if (screen != null) {
            screen.onShow();
            Toast.info(this, "Layar dimuat ulang.");
        }
    }

    private void logout() {
        int answer = javax.swing.JOptionPane.showConfirmDialog(this, "Keluar dari sesi ini?", "Keluar",
                javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (answer == javax.swing.JOptionPane.YES_OPTION && ctx.onLogout != null) {
            ctx.onLogout.run();
        }
    }

    private void installKeys() {
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_K, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                "palette", this::openPalette);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_J, java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                "theme", this::toggleTheme);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh", this::reloadCurrent);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "sell", () -> navigate("pos"));
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_1, java.awt.event.InputEvent.ALT_DOWN_MASK), "nav1",
                () -> navigate("dashboard"));
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_2, java.awt.event.InputEvent.ALT_DOWN_MASK), "nav2",
                () -> navigate("pos"));
    }

    private void bind(KeyStroke stroke, String name, Runnable action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.run();
            }
        });
    }

    private final class NavItem extends JPanel {
        private final Nav nav;
        private boolean active;
        private boolean hover;
        private final JLabel iconLabel = new JLabel();
        private final JLabel textLabel = new JLabel();

        NavItem(Nav nav) {
            this.nav = nav;
            setOpaque(false);
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            iconLabel.setPreferredSize(new Dimension(20, 20));
            textLabel.setText(nav.label());
            textLabel.setFont(Fonts.regular(13));
            add(iconLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);
            retint();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    navigate(nav.route());
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    // keeps the pointer cursor honest over child labels
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            });
        }

        void setActive(boolean value) {
            active = value;
            retint();
            repaint();
        }

        private void retint() {
            Color color = active ? Theme.p().accent() : (hover ? Theme.p().ink() : Theme.p().muted());
            iconLabel.setIcon(Icons.of(nav.icon(), 17, color));
            textLabel.setForeground(color);
            textLabel.setFont(active ? Fonts.semibold(13) : Fonts.regular(13));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active || hover) {
                g2.setColor(active ? Theme.p().accentSoft() : Theme.p().surfaceAlt());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), Theme.R_SM, Theme.R_SM));
            }
            if (active) {
                g2.setColor(Theme.p().accent());
                g2.fill(new RoundRectangle2D.Double(0, 8, 3, getHeight() - 16, 3, 3));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded accent square with a ticket monogram. Drawn, not an asset. */
    public static final class Logo extends JComponent {
        private final int size;

        public Logo(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.p().accent());
            g2.fill(new RoundRectangle2D.Double(0, 0, size, size, size * 0.32, size * 0.32));
            g2.setColor(Color.WHITE);
            g2.setFont(Fonts.bold(size * 0.46f));
            String mark = "T";
            java.awt.FontMetrics fm = g2.getFontMetrics();
            g2.drawString(mark, (size - fm.stringWidth(mark)) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
