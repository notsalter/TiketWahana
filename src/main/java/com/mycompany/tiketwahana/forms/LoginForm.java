package com.mycompany.tiketwahana.forms;

import com.mycompany.tiketwahana.core.Auth;
import com.mycompany.tiketwahana.core.AuthException;
import com.mycompany.tiketwahana.core.ConnectionPool;
import com.mycompany.tiketwahana.core.Database;
import com.mycompany.tiketwahana.core.Env;
import com.mycompany.tiketwahana.core.Migrations;
import com.mycompany.tiketwahana.core.SessionUser;
import com.mycompany.tiketwahana.ui.Buttons;
import com.mycompany.tiketwahana.ui.Card;
import com.mycompany.tiketwahana.ui.Fonts;
import com.mycompany.tiketwahana.ui.Icons;
import com.mycompany.tiketwahana.ui.MainWindow;
import com.mycompany.tiketwahana.ui.Motion;
import com.mycompany.tiketwahana.ui.TextInput;
import com.mycompany.tiketwahana.ui.Theme;
import com.mycompany.tiketwahana.ui.Toast;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.KeyStroke;

/** Login is a real database gate; database errors cannot turn into a demo bypass. */
public class LoginForm extends JFrame {

    private final TextInput username = new TextInput("Nama pengguna");
    private final TextInput password = TextInput.password("Password");
    private final JLabel error = new JLabel();
    private final JLabel status = new JLabel("Menyiapkan database…");
    private final com.mycompany.tiketwahana.ui.FlatButton login = Buttons.primary("Masuk", "chevronRight").height(46);
    private Database database;

    public LoginForm() {
        super("TiketWahana · Masuk");
        Theme.install(Env.get("ui.theme").equalsIgnoreCase("dark") ? Theme.Mode.DARK : Theme.Mode.LIGHT);
        Motion.setEnabled(!"reduced".equalsIgnoreCase(Env.get("ui.motion")));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(build());
        setMinimumSize(new Dimension(980, 680));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(login);
        password.onEnter(this::submit);
        login.addActionListener(e -> submit());
        installKeys();
        prepareDatabase();
    }

    private JPanel build() {
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.p().bg());
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Two quiet geometric fields create depth without a gradient-heavy UI.
                g2.setColor(Theme.p().accentSoft());
                g2.fillOval(getWidth() - 340, -180, 500, 500);
                g2.setColor(Theme.p().surfaceAlt());
                g2.fillOval(-180, getHeight() - 240, 380, 380);
                g2.dispose();
            }
        };
        root.setBorder(BorderFactory.createEmptyBorder(36, 44, 36, 44));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.62;
        c.weighty = 1;
        root.add(buildStory(), c);
        c.gridx = 1;
        c.weightx = 0.38;
        c.insets = new Insets(0, 32, 0, 0);
        root.add(buildLogin(), c);
        return root;
    }

    private JPanel buildStory() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("PANEL OPERASIONAL");
        eyebrow.setFont(Fonts.semibold(11));
        eyebrow.setForeground(Theme.p().accent());
        JLabel title = new JLabel("Satu tempat untuk\nsemua wahana.");
        title.setText("<html><div style='font-size:34px;line-height:120%'>Satu tempat untuk<br>semua wahana.</div></html>");
        title.setFont(Fonts.bold(34));
        title.setForeground(Theme.p().ink());
        JLabel copy = new JLabel("<html><div style='width:420px;line-height:145%'>Kelola penjualan, kapasitas, shift, dan laporan<br>taman hiburan dalam satu panel yang tenang.</div></html>");
        copy.setFont(Fonts.regular(15));
        copy.setForeground(Theme.p().muted());
        panel.add(Box.createVerticalGlue());
        panel.add(eyebrow);
        panel.add(Box.createVerticalStrut(16));
        panel.add(title);
        panel.add(Box.createVerticalStrut(16));
        panel.add(copy);
        panel.add(Box.createVerticalStrut(32));
        panel.add(feature("chart", "Ringkasan yang bisa dibaca sekilas", "KPI dan tren penjualan, tanpa spreadsheet."));
        panel.add(Box.createVerticalStrut(12));
        panel.add(feature("ticket", "Tiket yang tetap terlacak", "Setiap tiket punya kode dan status yang jelas."));
        panel.add(Box.createVerticalGlue());
        JLabel credit = new JLabel("TiketWahana  ·  Java Swing + JDBC");
        credit.setFont(Fonts.regular(11));
        credit.setForeground(Theme.p().muted());
        panel.add(credit);
        return panel;
    }

    private JPanel feature(String icon, String title, String subtitle) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(460, 44));
        JLabel glyph = new JLabel(Icons.of(icon, 18, Theme.p().accent()));
        glyph.setPreferredSize(new Dimension(28, 28));
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(Fonts.semibold(13));
        t.setForeground(Theme.p().ink());
        JLabel s = new JLabel(subtitle);
        s.setFont(Fonts.regular(12));
        s.setForeground(Theme.p().muted());
        copy.add(t);
        copy.add(s);
        row.add(glyph, BorderLayout.WEST);
        row.add(copy, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildLogin() {
        Card card = new Card(new BorderLayout());
        card.padding(36, 36, 32, 36);
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("T");
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);
        logo.setFont(Fonts.bold(22));
        logo.setOpaque(true);
        logo.setBackground(Theme.p().accent());
        logo.setPreferredSize(new Dimension(48, 48));
        logo.setMaximumSize(new Dimension(48, 48));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("Selamat datang kembali");
        heading.setFont(Fonts.semibold(21));
        heading.setForeground(Theme.p().ink());
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Masuk untuk melanjutkan ke panel.");
        sub.setFont(Fonts.regular(13));
        sub.setForeground(Theme.p().muted());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(logo);
        form.add(Box.createVerticalStrut(24));
        form.add(heading);
        form.add(Box.createVerticalStrut(6));
        form.add(sub);
        form.add(Box.createVerticalStrut(30));
        form.add(label("Nama pengguna"));
        form.add(Box.createVerticalStrut(7));
        username.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(username);
        form.add(Box.createVerticalStrut(16));
        form.add(label("Password"));
        form.add(Box.createVerticalStrut(7));
        password.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(password);
        form.add(Box.createVerticalStrut(10));
        error.setFont(Fonts.regular(12));
        error.setForeground(Theme.p().danger());
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(error);
        form.add(Box.createVerticalStrut(16));
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        form.add(login);
        form.add(Box.createVerticalStrut(16));
        status.setFont(Fonts.regular(11));
        status.setForeground(Theme.p().muted());
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(status);
        form.add(Box.createVerticalGlue());

        JLabel first = new JLabel("Akun pertama: admin / admin12345");
        first.setFont(Fonts.regular(11));
        first.setForeground(Theme.p().muted());
        first.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(first);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.semibold(12));
        label.setForeground(Theme.p().ink());
        return label;
    }

    private void prepareDatabase() {
        setBusy(true, "Menyiapkan database…");
        new SwingWorker<Void, Void>() {
            String failure;

            @Override
            protected Void doInBackground() {
                try {
                    database = Database.shared();
                    try (ConnectionPool.Lease lease = database.borrow()) {
                        Connection c = lease.connection();
                        Migrations.run(c);
                        Auth.bootstrapAdmin(c);
                    }
                } catch (Exception e) {
                    failure = e.getMessage() == null ? "Database tidak dapat disiapkan." : e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure == null) {
                    setBusy(false, "Database siap.");
                } else {
                    setBusy(false, "Database belum tersambung.");
                    showError(failure);
                }
            }
        }.execute();
    }

    private void submit() {
        error.setText("");
        if (username.text().isBlank() || password.text().isBlank()) {
            showError("Isi nama pengguna dan password.");
            return;
        }
        setBusy(true, "Memverifikasi akun…");
        new SwingWorker<SessionUser, Void>() {
            String failure;

            @Override
            protected SessionUser doInBackground() {
                try (ConnectionPool.Lease lease = Database.shared().borrow()) {
                    return Auth.login(lease.connection(), username.text(), password.text());
                } catch (AuthException | SQLException e) {
                    failure = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    SessionUser user = get();
                    if (user == null) {
                        showError(failure == null ? "Username atau password salah." : failure);
                        return;
                    }
                    openMain(user);
                } catch (Exception e) {
                    showError("Login gagal: " + e.getMessage());
                } finally {
                    setBusy(false, "");
                }
            }
        }.execute();
    }

    private void openMain(SessionUser user) {
        if (user.mustChangePassword()) {
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, user);
            dialog.setVisible(true);
            if (!dialog.changed) {
                showError("Password baru diperlukan untuk melanjutkan.");
                return;
            }
        }
        setVisible(false);
        MainWindow window = new MainWindow(new com.mycompany.tiketwahana.ui.AppContext(database, user), () -> {
            username.clear();
            password.clear();
            error.setText("");
            setVisible(true);
            toFront();
        });
        window.setVisible(true);
    }

    private void setBusy(boolean busy, String message) {
        login.setEnabled(!busy);
        username.setEnabled(!busy);
        password.setEnabled(!busy);
        status.setText(message == null ? "" : message);
    }

    private void showError(String message) {
        error.setText("<html>" + escape(message) + "</html>");
        error.setToolTipText(message);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void installKeys() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
        getRootPane().getActionMap().put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                submit();
            }
        });
    }

    private final class ChangePasswordDialog extends JDialog {
        private final SessionUser user;
        private final TextInput current = TextInput.password("Password sementara");
        private final TextInput next = TextInput.password("Password baru (minimal 8 karakter)");
        private final TextInput confirm = TextInput.password("Ulangi password baru");
        private final JLabel error = new JLabel();
        private boolean changed;

        ChangePasswordDialog(JFrame owner, SessionUser user) {
            super(owner, "Buat password baru", true);
            this.user = user;
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            JPanel panel = new JPanel();
            panel.setBackground(Theme.p().bg());
            panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 24, 28));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            JLabel title = new JLabel("Buat password baru");
            title.setFont(Fonts.semibold(20));
            title.setForeground(Theme.p().ink());
            JLabel copy = new JLabel("Akun ini memakai password sementara. Pilih password pribadi sebelum masuk.");
            copy.setFont(Fonts.regular(12));
            copy.setForeground(Theme.p().muted());
            panel.add(title);
            panel.add(Box.createVerticalStrut(6));
            panel.add(copy);
            panel.add(Box.createVerticalStrut(22));
            panel.add(field("Password saat ini", current));
            panel.add(Box.createVerticalStrut(12));
            panel.add(field("Password baru", next));
            panel.add(Box.createVerticalStrut(12));
            panel.add(field("Konfirmasi", confirm));
            panel.add(Box.createVerticalStrut(10));
            error.setForeground(Theme.p().danger());
            error.setFont(Fonts.regular(12));
            panel.add(error);
            panel.add(Box.createVerticalStrut(14));
            var save = Buttons.primary("Simpan password", "check");
            save.addActionListener(e -> change());
            panel.add(save);
            setContentPane(panel);
            setSize(480, 470);
            setLocationRelativeTo(owner);
        }

        private JPanel field(String label, TextInput input) {
            JPanel p = new JPanel(new BorderLayout(0, 6));
            p.setOpaque(false);
            JLabel l = new JLabel(label);
            l.setFont(Fonts.semibold(12));
            l.setForeground(Theme.p().ink());
            p.add(l, BorderLayout.NORTH);
            p.add(input, BorderLayout.CENTER);
            return p;
        }

        private void change() {
            if (!next.text().equals(confirm.text())) {
                error.setText("Konfirmasi password tidak cocok.");
                return;
            }
            try (ConnectionPool.Lease lease = Database.shared().borrow()) {
                Auth.changePassword(lease.connection(), user, current.text(), next.text());
                changed = true;
                dispose();
            } catch (Exception e) {
                error.setText("<html>" + escape(e.getMessage()) + "</html>");
            }
        }
    }
}
