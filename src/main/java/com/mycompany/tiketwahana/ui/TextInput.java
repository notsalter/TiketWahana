package com.mycompany.tiketwahana.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

/** Rounded text field with placeholder text, focus ring and an error state. */
public class TextInput extends JPanel {

    private final JTextField field;
    private final String hint;
    private boolean invalid;

    public TextInput(String placeholder) {
        this(placeholder, false);
    }

    private TextInput(String placeholder, boolean password) {
        this.hint = placeholder == null ? "" : placeholder;
        this.field = password ? new HintPassword() : new HintField();
        ((Hint) field).setHint(this.hint);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        field.setFont(Fonts.regular(13));
        field.setForeground(Theme.p().ink());
        field.setCaretColor(Theme.p().accent());
        field.setSelectionColor(Theme.p().accentSoft());
        field.setSelectedTextColor(Theme.p().ink());
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(field, BorderLayout.CENTER);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    public static TextInput password(String placeholder) {
        return new TextInput(placeholder, true);
    }

    public JTextField field() {
        return field;
    }

    public String text() {
        if (field instanceof JPasswordField pf) {
            char[] chars = pf.getPassword();
            return new String(chars == null ? new char[0] : chars).trim();
        }
        return field.getText().trim();
    }

    public void setText(String value) {
        field.setText(value == null ? "" : value);
    }

    public void clear() {
        field.setText("");
        invalid = false;
        repaint();
    }

    public void setInvalid(boolean value) {
        this.invalid = value;
        repaint();
    }

    public boolean invalid() {
        return invalid;
    }

    public void onEnter(Runnable action) {
        field.addActionListener(e -> action.run());
    }

    public void onChange(Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                action.run();
            }
        });
    }

    public void addDocumentListener(DocumentListener listener) {
        field.getDocument().addDocumentListener(listener);
    }

    @Override
    public void requestFocus() {
        field.requestFocusInWindow();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        field.setEnabled(enabled);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Theme.Palette p = Theme.p();
        g2.setColor(isEnabled() ? p.surface() : p.surfaceAlt());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), Theme.R_SM, Theme.R_SM));
        Color border = invalid ? p.danger() : (focusWithin() ? p.accent() : p.border());
        g2.setColor(border);
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, Theme.R_SM, Theme.R_SM));
        g2.dispose();
    }

    private boolean focusWithin() {
        java.awt.Component c = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return c != null && javax.swing.SwingUtilities.isDescendingFrom(c, this);
    }

    private interface Hint {
        void setHint(String hint);
    }

    private class HintField extends JTextField implements Hint {
        private String hint = "";

        @Override
        public void setHint(String hint) {
            this.hint = hint;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintHint(this, g, hint);
        }
    }

    private class HintPassword extends JPasswordField implements Hint {
        private String hint = "";

        @Override
        public void setHint(String hint) {
            this.hint = hint;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paintHint(this, g, hint);
        }
    }

    private void paintHint(JTextField field, Graphics g, String hint) {
        if (hint == null || hint.isEmpty() || !field.getText().isEmpty() || field.isFocusOwner()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Theme.p().muted());
        g2.setFont(Fonts.regular(13));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, 1, (field.getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
