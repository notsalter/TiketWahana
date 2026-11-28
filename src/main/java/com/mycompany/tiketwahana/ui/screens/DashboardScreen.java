package com.mycompany.tiketwahana.ui.screens;

import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.domain.ReportRow;
import com.mycompany.tiketwahana.domain.Wahana;
import com.mycompany.tiketwahana.service.ReportService;
import com.mycompany.tiketwahana.service.ServiceException;
import com.mycompany.tiketwahana.ui.AppContext;
import com.mycompany.tiketwahana.ui.Buttons;
import com.mycompany.tiketwahana.ui.Card;
import com.mycompany.tiketwahana.ui.DataTable;
import com.mycompany.tiketwahana.ui.EmptyState;
import com.mycompany.tiketwahana.ui.Fonts;
import com.mycompany.tiketwahana.ui.Icons;
import com.mycompany.tiketwahana.ui.Motion;
import com.mycompany.tiketwahana.ui.SectionHeader;
import com.mycompany.tiketwahana.ui.Theme;
import com.mycompany.tiketwahana.ui.Toast;
import com.mycompany.tiketwahana.ui.charts.Sparkline;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class DashboardScreen extends Screen {

    private final JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
    private final JPanel trendHost = new JPanel(new BorderLayout());
    private final JPanel wahanaHost = new JPanel();
    private final DefaultTableModel recentModel =
            new DefaultTableModel(new Object[] {"Kode", "Pelanggan", "Tiket", "Total", "Status", "Waktu"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
    private final JTable recentTable = DataTable.create(new String[] {"Kode", "Pelanggan", "Tiket", "Total",
            "Status", "Waktu"});
    private final JLabel greeting = new JLabel();

    public DashboardScreen(AppContext ctx) {
        super(ctx, "dashboard");
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JLabel date = new JLabel(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))));
        date.setFont(Fonts.regular(13));
        date.setForeground(Theme.p().muted());
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        greeting.setFont(Fonts.semibold(22));
        greeting.setForeground(Theme.p().ink());
        greeting.setText("Halo, " + ctx.user.fullName().split(" ")[0]);
        left.add(greeting);
        left.add(Box.createVerticalStrut(4));
        left.add(date);

        JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        var refresh = Buttons.secondary("Muat ulang", "refresh");
        refresh.addActionListener(e -> onShow());
        var sell = Buttons.primary("Jual tiket", "cart");
        sell.addActionListener(e -> navigate("pos"));
        right.add(refresh);
        right.add(sell);
        return new SectionHeader("", "", right) {
            {
                removeAll();
                add(left, BorderLayout.WEST);
                add(right, BorderLayout.EAST);
                setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
            }
        };
    }

    private void navigate(String route) {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (w instanceof com.mycompany.tiketwahana.ui.MainWindow mw) {
            mw.shell().navigate(route);
        }
    }

    private JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        kpiRow.setOpaque(false);
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        body.add(kpiRow);
        body.add(Box.createVerticalStrut(16));

        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 0));
        charts.setOpaque(false);
        charts.setAlignmentX(Component.LEFT_ALIGNMENT);
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        Card trend = new Card(new BorderLayout(0, 8));
        JLabel trendTitle = new JLabel("Pendapatan 30 hari");
        trendTitle.setFont(Fonts.semibold(14));
        trendTitle.setForeground(Theme.p().ink());
        trend.add(trendTitle, BorderLayout.NORTH);
        trendHost.setOpaque(false);
        trend.add(trendHost, BorderLayout.CENTER);
        charts.add(trend);

        Card seats = new Card(new BorderLayout(0, 12));
        JLabel seatsTitle = new JLabel("Kursi terjual hari ini");
        seatsTitle.setFont(Fonts.semibold(14));
        seatsTitle.setForeground(Theme.p().ink());
        seats.add(seatsTitle, BorderLayout.NORTH);
        wahanaHost.setOpaque(false);
        wahanaHost.setLayout(new BoxLayout(wahanaHost, BoxLayout.Y_AXIS));
        seats.add(wahanaHost, BorderLayout.CENTER);
        charts.add(seats);
        body.add(charts);
        body.add(Box.createVerticalStrut(16));

        Card recent = new Card(new BorderLayout(0, 12));
        JLabel recentTitle = new JLabel("Transaksi terakhir");
        recentTitle.setFont(Fonts.semibold(14));
        recentTitle.setForeground(Theme.p().ink());
        recent.add(recentTitle, BorderLayout.NORTH);
        recentTable.setModel(recentModel);
        DataTable.money(recentTable, 3);
        DataTable.pill(recentTable, 4);
        recent.add(DataTable.scroll(recentTable), BorderLayout.CENTER);
        recent.setPreferredSize(new Dimension(0, 240));
        body.add(recent);
        return body;
    }

    @Override
    public void onShow() {
        new SwingWorker<Void, Void>() {
            ReportService.Kpis kpis;
            List<BigDecimal> series = new ArrayList<>();
            List<ReportService.TicketRowView> recent = new ArrayList<>();
            List<Wahana> wahana = new ArrayList<>();
            ServiceException failure;

            @Override
            protected Void doInBackground() {
                try {
                    kpis = ctx.reports.kpis();
                    series = ctx.reports.revenueSeries(30);
                    recent = ctx.reports.recentTransactions(6);
                    wahana = ctx.wahana.list(true);
                } catch (ServiceException e) {
                    failure = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (failure != null) {
                    Toast.error(DashboardScreen.this, failure.getMessage());
                    return;
                }
                render(kpis, series, recent, wahana);
            }
        }.execute();
    }

    private void render(ReportService.Kpis kpis, List<BigDecimal> series,
                        List<ReportService.TicketRowView> recent, List<Wahana> wahana) {
        kpiRow.removeAll();
        kpiRow.add(kpi("Pendapatan hari ini", Money.format(kpis.revenueToday()), "transaksi lunas", "cash",
                Theme.p().accent()));
        kpiRow.add(kpi("Tiket hari ini", String.valueOf(kpis.ticketsToday()), "tiket terjual", "ticket",
                Theme.p().ink()));
        kpiRow.add(kpi("Wahana tersibuk", kpis.busiestCount() == 0 ? "-" : kpis.busiestWahana(),
                kpis.busiestCount() + " tiket hari ini", "park", Theme.p().warn()));
        kpiRow.add(kpi("Kapasitas terpakai", kpis.capacityTotal() == 0 ? "0%"
                        : Math.round(kpis.capacityUsedToday() * 100f / kpis.capacityTotal()) + "%",
                kpis.capacityUsedToday() + " dari " + kpis.capacityTotal() + " kursi", "chart", Theme.p().ink()));
        kpiRow.revalidate();
        kpiRow.repaint();

        trendHost.removeAll();
        trendHost.add(new Sparkline(series), BorderLayout.CENTER);
        trendHost.revalidate();
        trendHost.repaint();

        wahanaHost.removeAll();
        if (wahana.isEmpty()) {
            wahanaHost.add(new EmptyState("park", "Belum ada wahana aktif",
                    "Tambahkan wahana di menu Wahana untuk mulai menjual.", null));
        } else {
            for (Wahana w : wahana) {
                int sold = seatsToday(w);
                wahanaHost.add(row(w, sold));
                wahanaHost.add(Box.createVerticalStrut(10));
            }
        }
        wahanaHost.revalidate();
        wahanaHost.repaint();

        recentModel.setRowCount(0);
        for (ReportService.TicketRowView t : recent) {
            recentModel.addRow(new Object[] {t.kode(), t.customer(), t.tickets(), t.total(), t.status(), t.at()});
        }
        if (recent.isEmpty()) {
            recentModel.addRow(new Object[] {"-", "Belum ada transaksi hari ini", "", Money.ZERO, "", ""});
        }
    }

    private int seatsToday(Wahana w) {
        try {
            return ctx.wahana.seatsSoldToday(w.id());
        } catch (ServiceException e) {
            return 0;
        }
    }

    private JComponent row(Wahana w, int sold) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JLabel name = new JLabel(w.nama());
        name.setFont(Fonts.regular(12));
        name.setForeground(Theme.p().ink());
        JLabel count = new JLabel(sold + " / " + w.kapasitas());
        count.setFont(Fonts.mono(11));
        count.setForeground(Theme.p().muted());
        row.add(name, BorderLayout.WEST);
        row.add(count, BorderLayout.EAST);

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.p().surfaceAlt());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 4, 4));
                double ratio = Math.min(1.0, w.kapasitas() == 0 ? 0 : (double) sold / w.kapasitas());
                if (ratio > 0) {
                    g2.setColor(ratio > 0.9 ? Theme.p().danger() : Theme.p().accent());
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() * ratio, getHeight(), 4, 4));
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 6));
        JPanel wrap = new JPanel(new BorderLayout(0, 5));
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        wrap.add(row, BorderLayout.NORTH);
        wrap.add(bar, BorderLayout.CENTER);
        return wrap;
    }

    private JComponent kpi(String label, String value, String caption, String icon, Color accent) {
        Card card = new Card(new BorderLayout(0, 6));
        card.radius(Theme.R_MD).padding(16, 18, 16, 18);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Fonts.regular(12));
        l.setForeground(Theme.p().muted());
        JLabel glyph = new JLabel(Icons.of(icon, 16, accent));
        top.add(l, BorderLayout.WEST);
        top.add(glyph, BorderLayout.EAST);

        JLabel v = new JLabel(value);
        v.setFont(Fonts.semibold(value.length() > 12 ? 18 : 24));
        v.setForeground(Theme.p().ink());
        JLabel c = new JLabel(caption);
        c.setFont(Fonts.regular(11));
        c.setForeground(Theme.p().muted());

        card.add(top, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        card.add(c, BorderLayout.SOUTH);
        return card;
    }
}
