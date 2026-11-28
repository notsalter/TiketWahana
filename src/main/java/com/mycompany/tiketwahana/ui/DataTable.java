package com.mycompany.tiketwahana.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/** Tables that read like the rest of the app: striped, roomy, right-aligned money. */
public final class DataTable {

    private DataTable() {}

    public static JTable create(String[] columns) {
        JTable table = new JTable(new Object[0][0], columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFocusable(false);
        table.setBackground(Theme.p().surface());
        table.setForeground(Theme.p().ink());
        table.setSelectionBackground(Theme.p().accentSoft());
        table.setSelectionForeground(Theme.p().ink());
        table.setDefaultRenderer(Object.class, new Zebra());
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new HeaderRenderer());
        return table;
    }

    public static void money(JTable table, int columnIndex) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        column.setCellRenderer(new MoneyRenderer());
    }

    public static void pill(JTable table, int columnIndex) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        column.setCellRenderer(new PillRenderer());
    }

    public static void width(JTable table, int columnIndex, int width) {
        table.getColumnModel().getColumn(columnIndex).setPreferredWidth(width);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(width * 3);
    }

    public static JScrollPane scroll(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getViewport().setBackground(Theme.p().surface());
        Theme.ScrollUI.apply(pane.getVerticalScrollBar());
        Theme.ScrollUI.apply(pane.getHorizontalScrollBar());
        return pane;
    }

    static Color rowColor(JTable table, int row) {
        boolean selected = table.isRowSelected(row);
        if (selected) {
            return Theme.p().accentSoft();
        }
        return row % 2 == 0 ? Theme.p().surface() : Theme.p().surfaceAlt();
    }

    private static final class Zebra extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            label.setBackground(rowColor(table, row));
            label.setForeground(Theme.p().ink());
            label.setFont(Fonts.regular(13));
            label.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            label.setVerticalAlignment(SwingConstants.CENTER);
            return label;
        }
    }

    private static final class MoneyRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String text = value instanceof BigDecimal b ? com.mycompany.tiketwahana.core.Money.format(b)
                    : String.valueOf(value);
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, text, isSelected, false, row, column);
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            label.setBackground(rowColor(table, row));
            label.setForeground(Theme.p().ink());
            label.setFont(Fonts.mono(12));
            label.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 16));
            return label;
        }
    }

    private static final class PillRenderer implements TableCellRenderer {
        private final JPanel host = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            host.removeAll();
            host.setBackground(rowColor(table, row));
            host.add(Pill.status(value == null ? "" : value.toString()));
            return host;
        }
    }

    private static final class HeaderRenderer implements TableCellRenderer {
        private final JLabel label = new JLabel();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            label.setText(value == null ? "" : value.toString().toUpperCase());
            label.setFont(Fonts.semibold(11));
            label.setForeground(Theme.p().muted());
            label.setBackground(Theme.p().surface());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.p().border()),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)));
            return label;
        }
    }

    /** Table header restyled after a theme switch. */
    public static void retheme(JTable table) {
        table.setBackground(Theme.p().surface());
        table.setForeground(Theme.p().ink());
        table.setSelectionBackground(Theme.p().accentSoft());
        table.getTableHeader().repaint();
        table.repaint();
    }

    static Graphics2D soft(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    static Font small() {
        return Fonts.regular(12);
    }
}
