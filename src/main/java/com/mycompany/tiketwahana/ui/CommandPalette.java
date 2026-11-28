package com.mycompany.tiketwahana.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/** Ctrl+K: every action, every wahana, four keystrokes away. */
public final class CommandPalette extends JDialog {

    public record Action(String label, String hint, Runnable run) {
    }

    private final DefaultListModel<Action> model = new DefaultListModel<>();
    private final JList<Action> list = new JList<>(model);
    private final TextInput search = new TextInput("Cari aksi, wahana, atau kode tiket");
    private final List<Action> all = new ArrayList<>();

    public CommandPalette(Window owner, List<Action> actions) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        all.addAll(actions);
        setLayout(new BorderLayout());
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.p().surface());
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.p().border()),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        root.add(search, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new Renderer());
        list.setFixedCellHeight(46);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(480, 340));
        Theme.ScrollUI.apply(scroll.getVerticalScrollBar());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        root.add(scroll, BorderLayout.CENTER);

        JLabel foot = new JLabel("Enter untuk jalankan  ·  Esc untuk tutup");
        foot.setFont(Fonts.regular(11));
        foot.setForeground(Theme.p().muted());
        root.add(foot, BorderLayout.SOUTH);

        setContentPane(root);
        filter("");
        search.onChange(() -> filter(search.text()));

        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int index = list.getSelectedIndex();
            if (index >= 0) {
                list.ensureIndexIsVisible(index);
            }
        });
        search.onEnter(this::runSelected);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        getRootPane().getActionMap().put("down", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                move(1);
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        getRootPane().getActionMap().put("up", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                move(-1);
            }
        });
        setSize(520, 440);
        setLocationRelativeTo(owner);
    }

    public void open() {
        SwingUtilities.invokeLater(search::requestFocus);
        setVisible(true);
    }

    private void move(int delta) {
        if (model.isEmpty()) {
            return;
        }
        int next = Math.floorMod(list.getSelectedIndex() + delta, model.size());
        list.setSelectedIndex(next);
    }

    private void runSelected() {
        Action action = list.getSelectedValue();
        if (action != null) {
            dispose();
            SwingUtilities.invokeLater(action.run());
        }
    }

    private void filter(String query) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        model.clear();
        for (Action a : all) {
            if (q.isEmpty() || a.label().toLowerCase(Locale.ROOT).contains(q)
                    || a.hint().toLowerCase(Locale.ROOT).contains(q)) {
                model.addElement(a);
            }
        }
        if (!model.isEmpty()) {
            list.setSelectedIndex(0);
        }
    }

    private final class Renderer implements ListCellRenderer<Action> {
        private final JPanel panel = new JPanel(new BorderLayout(12, 0));
        private final JLabel title = new JLabel();
        private final JLabel hint = new JLabel();

        Renderer() {
            panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            JPanel text = new JPanel(new BorderLayout());
            text.setOpaque(false);
            title.setFont(Fonts.regular(13));
            hint.setFont(Fonts.regular(11));
            title.setForeground(Theme.p().ink());
            hint.setForeground(Theme.p().muted());
            text.add(title, BorderLayout.NORTH);
            text.add(hint, BorderLayout.SOUTH);
            panel.add(text, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Action> list, Action value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            title.setText(value.label());
            hint.setText(value.hint());
            panel.setBackground(isSelected ? Theme.p().accentSoft() : Theme.p().surface());
            panel.setOpaque(true);
            return panel;
        }
    }
}
