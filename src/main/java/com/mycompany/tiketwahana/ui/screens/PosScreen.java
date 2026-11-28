package com.mycompany.tiketwahana.ui.screens;

import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.domain.CartLine;
import com.mycompany.tiketwahana.domain.Sale;
import com.mycompany.tiketwahana.domain.Wahana;
import com.mycompany.tiketwahana.service.ServiceException;
import com.mycompany.tiketwahana.ui.AppContext;
import com.mycompany.tiketwahana.ui.Barcode;
import com.mycompany.tiketwahana.ui.Buttons;
import com.mycompany.tiketwahana.ui.Card;
import com.mycompany.tiketwahana.ui.EmptyState;
import com.mycompany.tiketwahana.ui.FlatButton;
import com.mycompany.tiketwahana.ui.Fonts;
import com.mycompany.tiketwahana.ui.Icons;
import com.mycompany.tiketwahana.ui.SectionHeader;
import com.mycompany.tiketwahana.ui.TextInput;
import com.mycompany.tiketwahana.ui.Theme;
import com.mycompany.tiketwahana.ui.Toast;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeListener;

/** A deliberately dense POS: catalogue left, cart right, no modal maze. */
public class PosScreen extends Screen {

    private final JPanel catalogue = new JPanel(new GridLayout(0, 2, 12, 12));
    private final JPanel cart = new JPanel();
    private final JPanel cartLines = new JPanel();
    private final JLabel subtotalLabel = moneyLabel();
    private final JLabel discountLabel = moneyLabel();
    private final JLabel totalLabel = moneyLabel();
    private final JLabel changeLabel = moneyLabel();
    private final TextInput search = new TextInput("Cari wahana");
    private final TextInput customer = new TextInput("Nama pengunjung");
    private final TextInput phone = new TextInput("Nomor telepon (opsional)");
    private final TextInput discount = new TextInput("Diskon (opsional)");
    private final TextInput cashReceived = new TextInput("Tunai");
    private final TextInput debitReceived = new TextInput("Debit");
    private final TextInput qrisReceived = new TextInput("QRIS");
    private final JSpinner date = new JSpinner(new SpinnerNumberModel(0, 0, 365, 1));
    private final JLabel dateLabel = new JLabel("Hari ini");
    private final List<Wahana> allWahana = new ArrayList<>();
    private final List<CartLine> lines = new ArrayList<>();
    private final List<ParkedSale> parked = new ArrayList<>();
    private boolean loading;

    private record ParkedSale(String customer, String phone, List<CartLine> lines, String discount,
                              String cash, String debit, String qris, int dayOffset) {
    }

    public PosScreen(AppContext ctx) {
        super(ctx, "pos");
        add(new SectionHeader("Kasir", "Buat tiket untuk pengunjung dan lihat totalnya sebelum pembayaran."), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        search.onChange(this::filterCatalogue);
        cashReceived.onChange(this::updateTotals);
        debitReceived.onChange(this::updateTotals);
        qrisReceived.onChange(this::updateTotals);
        discount.onChange(this::updateTotals);
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setOpaque(false);
        body.add(buildCatalogue());
        body.add(buildCart());
        return body;
    }

    private JComponent buildCatalogue() {
        Card card = new Card(new BorderLayout(0, 14));
        card.padding(18, 18, 18, 18);
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        JLabel title = new JLabel("Pilih wahana");
        title.setFont(Fonts.semibold(15));
        title.setForeground(Theme.p().ink());
        top.add(title, BorderLayout.WEST);
        top.add(search, BorderLayout.CENTER);
        card.add(top, BorderLayout.NORTH);
        catalogue.setOpaque(false);
        JScrollPane scroll = new JScrollPane(catalogue);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        Theme.ScrollUI.apply(scroll.getVerticalScrollBar());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildCart() {
        Card card = new Card(new BorderLayout(0, 14));
        card.padding(18, 18, 18, 18);
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Keranjang");
        title.setFont(Fonts.semibold(15));
        title.setForeground(Theme.p().ink());
        JLabel count = new JLabel();
        count.setName("cartCount");
        count.setFont(Fonts.regular(12));
        count.setForeground(Theme.p().muted());
        heading.add(title, BorderLayout.WEST);
        heading.add(count, BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);

        cart.setOpaque(false);
        cart.setLayout(new BorderLayout(0, 12));
        cartLines.setOpaque(false);
        cartLines.setLayout(new BoxLayout(cartLines, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(cartLines);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        Theme.ScrollUI.apply(scroll.getVerticalScrollBar());
        cart.add(scroll, BorderLayout.CENTER);
        cart.add(buildCheckout(), BorderLayout.SOUTH);
        card.add(cart, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildCheckout() {
        JPanel checkout = new JPanel();
        checkout.setOpaque(false);
        checkout.setLayout(new BoxLayout(checkout, BoxLayout.Y_AXIS));
        checkout.add(separator());
        checkout.add(Box.createVerticalStrut(12));
        JPanel guest = new JPanel(new GridLayout(1, 2, 8, 0));
        guest.setOpaque(false);
        guest.add(customer);
        guest.add(phone);
        checkout.add(guest);
        checkout.add(Box.createVerticalStrut(8));

        JPanel visit = new JPanel(new BorderLayout(8, 0));
        visit.setOpaque(false);
        JLabel dateTitle = new JLabel("Tanggal kunjungan");
        dateTitle.setFont(Fonts.regular(12));
        dateTitle.setForeground(Theme.p().muted());
        dateLabel.setFont(Fonts.semibold(12));
        dateLabel.setForeground(Theme.p().ink());
        date.setVisible(false);
        date.addChangeListener(e -> dateLabel.setText(visitDate().toString()));
        FlatButton tomorrow = Buttons.ghost("Besok");
        tomorrow.addActionListener(e -> {
            date.setValue(Math.min(365, ((Number) date.getValue()).intValue() + 1));
            dateLabel.setText(visitDate().toString());
        });
        visit.add(dateTitle, BorderLayout.WEST);
        visit.add(dateLabel, BorderLayout.CENTER);
        visit.add(tomorrow, BorderLayout.EAST);
        checkout.add(visit);
        checkout.add(Box.createVerticalStrut(10));

        checkout.add(summaryRow("Subtotal", subtotalLabel));
        checkout.add(Box.createVerticalStrut(6));
        JPanel discountRow = new JPanel(new BorderLayout(8, 0));
        discountRow.setOpaque(false);
        JLabel dl = new JLabel("Diskon");
        dl.setFont(Fonts.regular(12));
        dl.setForeground(Theme.p().muted());
        discountRow.add(dl, BorderLayout.WEST);
        discountRow.add(discount, BorderLayout.EAST);
        checkout.add(discountRow);
        checkout.add(Box.createVerticalStrut(8));
        checkout.add(summaryRow("Total", totalLabel));
        checkout.add(Box.createVerticalStrut(10));
        JLabel paymentLabel = new JLabel("Pembayaran dapat dibagi ke beberapa metode");
        paymentLabel.setFont(Fonts.regular(11)); paymentLabel.setForeground(Theme.p().muted());
        checkout.add(paymentLabel); checkout.add(Box.createVerticalStrut(6));
        JPanel payment = new JPanel(new GridLayout(1, 3, 8, 0)); payment.setOpaque(false);
        payment.add(cashReceived); payment.add(debitReceived); payment.add(qrisReceived);
        checkout.add(payment);
        checkout.add(Box.createVerticalStrut(7));
        checkout.add(summaryRow("Kembalian", changeLabel));
        checkout.add(Box.createVerticalStrut(12));

        JPanel saleActions = new JPanel(new GridLayout(1, 2, 8, 0));
        saleActions.setOpaque(false);
        FlatButton park = Buttons.secondary("Parkir transaksi", "clock");
        FlatButton resume = Buttons.secondary("Pulihkan terakhir", "refresh");
        park.addActionListener(e -> parkSale());
        resume.addActionListener(e -> resumeSale());
        saleActions.add(park); saleActions.add(resume);
        checkout.add(saleActions);
        checkout.add(Box.createVerticalStrut(8));
        FlatButton sell = Buttons.primary("Selesaikan penjualan", "check");
        sell.setName("sellButton");
        sell.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        sell.addActionListener(e -> sell());
        checkout.add(sell);
        return checkout;
    }

    private JLabel moneyLabel() {
        JLabel label = new JLabel(Money.format(Money.ZERO));
        label.setFont(Fonts.mono(12));
        label.setForeground(Theme.p().ink());
        return label;
    }

    private JPanel summaryRow(String text, JLabel amount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setFont(Fonts.regular(12));
        label.setForeground(Theme.p().muted());
        row.add(label, BorderLayout.WEST);
        row.add(amount, BorderLayout.EAST);
        return row;
    }

    private JComponent separator() {
        JSeparator s = new JSeparator();
        s.setForeground(Theme.p().border());
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    @Override
    public void onShow() {
        if (loading) return;
        loading = true;
        new SwingWorker<List<Wahana>, Void>() {
            @Override protected List<Wahana> doInBackground() throws Exception { return ctx.wahana.list(true); }
            @Override protected void done() {
                loading = false;
                try {
                    allWahana.clear(); allWahana.addAll(get()); filterCatalogue();
                } catch (Exception e) { Toast.error(PosScreen.this, "Wahana tidak dapat dimuat: " + e.getMessage()); }
            }
        }.execute();
    }

    private void filterCatalogue() {
        String q = search.text().toLowerCase();
        catalogue.removeAll();
        for (Wahana w : allWahana) {
            if (q.isBlank() || w.nama().toLowerCase().contains(q) || w.kode().toLowerCase().contains(q)) {
                catalogue.add(wahanaCard(w));
            }
        }
        if (catalogue.getComponentCount() == 0) {
            catalogue.add(new EmptyState("park", "Wahana tidak ditemukan", "Coba kata pencarian lain.", null));
        }
        catalogue.revalidate(); catalogue.repaint();
    }

    private JComponent wahanaCard(Wahana w) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setOpaque(true);
        card.setBackground(Theme.p().surface());
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.p().border()),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        JLabel icon = new JLabel(Icons.of("park", 22, Theme.p().accent()));
        JLabel code = new JLabel(w.kode());
        code.setFont(Fonts.mono(10)); code.setForeground(Theme.p().muted());
        JLabel name = new JLabel(w.nama());
        name.setFont(Fonts.semibold(13)); name.setForeground(Theme.p().ink());
        JLabel price = new JLabel(Money.format(w.harga()));
        price.setFont(Fonts.mono(11)); price.setForeground(Theme.p().accent());
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(code); text.add(name); text.add(price);
        card.add(icon, BorderLayout.WEST); card.add(text, BorderLayout.CENTER);
        card.addMouseListener(new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { addLine(w); }});
        return card;
    }

    private void addLine(Wahana w) {
        for (CartLine line : lines) {
            if (line.wahana().id() == w.id()) { line.quantity(line.quantity() + 1); renderCart(); return; }
        }
        lines.add(new CartLine(w, 1)); renderCart();
    }

    private void renderCart() {
        cartLines.removeAll();
        int tickets = 0;
        for (CartLine line : lines) {
            tickets += line.quantity();
            cartLines.add(cartLine(line)); cartLines.add(Box.createVerticalStrut(8));
        }
        if (lines.isEmpty()) cartLines.add(new EmptyState("cart", "Keranjang kosong", "Pilih wahana di sebelah kiri untuk memulai.", null));
        cartLines.revalidate(); cartLines.repaint();
        JLabel count = findCartCount();
        if (count != null) count.setText(tickets + " tiket");
        updateTotals();
    }

    private JLabel findCartCount() {
        // Heading label is outside this method's scope; find by component tree.
        return findNamed(this, "cartCount");
    }

    private static JLabel findNamed(java.awt.Container c, String name) {
        for (Component child : c.getComponents()) {
            if (child instanceof JLabel l && name.equals(l.getName())) return l;
            if (child instanceof java.awt.Container nested) { JLabel found = findNamed(nested, name); if (found != null) return found; }
        }
        return null;
    }

    private JComponent cartLine(CartLine line) {
        JPanel row = new JPanel(new BorderLayout(8, 0)); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        JLabel name = new JLabel("<html><b>" + line.wahana().nama() + "</b><br><font color='#6B7280'>" + Money.format(line.wahana().harga()) + " / tiket</font></html>");
        name.setFont(Fonts.regular(12)); name.setForeground(Theme.p().ink());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0)); controls.setOpaque(false);
        FlatButton minus = Buttons.icon("minus", "Kurangi", FlatButton.Variant.GHOST); minus.setPreferredSize(new Dimension(30,30)); minus.height(30);
        JLabel qty = new JLabel(String.valueOf(line.quantity())); qty.setFont(Fonts.mono(12)); qty.setForeground(Theme.p().ink()); qty.setHorizontalAlignment(JLabel.CENTER); qty.setPreferredSize(new Dimension(24,30));
        FlatButton plus = Buttons.icon("plus", "Tambah", FlatButton.Variant.GHOST); plus.setPreferredSize(new Dimension(30,30)); plus.height(30);
        minus.addActionListener(e -> { line.quantity(line.quantity()-1); if(line.quantity()==0) lines.remove(line); renderCart(); });
        plus.addActionListener(e -> { line.quantity(line.quantity()+1); renderCart(); });
        controls.add(minus); controls.add(qty); controls.add(plus);
        row.add(name, BorderLayout.CENTER); row.add(controls, BorderLayout.EAST);
        return row;
    }

    private LocalDate visitDate() { return LocalDate.now().plusDays(((Number) date.getValue()).longValue()); }

    private BigDecimal discountValue() {
        try { return Money.parse(discount.text().isBlank() ? "0" : discount.text()); } catch (Exception e) { return Money.ZERO; }
    }

    private BigDecimal subtotal() { BigDecimal v=Money.ZERO; for(CartLine l:lines)v=v.add(l.subtotal()); return Money.scale(v); }
    private BigDecimal total() { return Money.scale(subtotal().subtract(discountValue().min(subtotal()).max(Money.ZERO))); }
    private void updateTotals() {
        BigDecimal sub=subtotal(), dis=discountValue().min(sub).max(Money.ZERO), total=Money.scale(sub.subtract(dis));
        subtotalLabel.setText(Money.format(sub)); discountLabel.setText(Money.format(dis)); totalLabel.setText(Money.format(total));
        BigDecimal paid = parsePayment(cashReceived.text()).add(parsePayment(debitReceived.text())).add(parsePayment(qrisReceived.text()));
        changeLabel.setText(Money.format(Money.scale(paid.subtract(total).max(Money.ZERO))));
    }

    private void parkSale() {
        if (lines.isEmpty()) { Toast.info(this, "Keranjang masih kosong."); return; }
        List<CartLine> copy = new ArrayList<>();
        for (CartLine line : lines) copy.add(new CartLine(line.wahana(), line.quantity()));
        parked.add(new ParkedSale(customer.text(), phone.text(), copy, discount.text(), cashReceived.text(),
                debitReceived.text(), qrisReceived.text(), ((Number) date.getValue()).intValue()));
        lines.clear(); customer.clear(); phone.clear(); discount.clear(); cashReceived.clear();
        debitReceived.clear(); qrisReceived.clear(); date.setValue(0); renderCart();
        Toast.success(this, "Transaksi diparkir. Dapat dipulihkan selama sesi ini.");
    }

    private void resumeSale() {
        if (parked.isEmpty()) { Toast.info(this, "Belum ada transaksi yang diparkir."); return; }
        ParkedSale draft = parked.remove(parked.size() - 1);
        lines.clear();
        for (CartLine line : draft.lines()) lines.add(new CartLine(line.wahana(), line.quantity()));
        customer.setText(draft.customer()); phone.setText(draft.phone()); discount.setText(draft.discount());
        cashReceived.setText(draft.cash()); debitReceived.setText(draft.debit()); qrisReceived.setText(draft.qris());
        date.setValue(draft.dayOffset()); dateLabel.setText(visitDate().toString()); renderCart();
        Toast.info(this, "Transaksi dipulihkan dari parkiran.");
    }

    private BigDecimal parsePayment(String raw) {
        return raw == null || raw.isBlank() ? Money.ZERO : Money.parse(raw);
    }

    private void sell() {
        if (!ctx.shiftOpen()) { Toast.warn(this, "Buka shift terlebih dahulu sebelum menjual tiket."); return; }
        BigDecimal cash, debit, qris;
        try { cash=parsePayment(cashReceived.text()); debit=parsePayment(debitReceived.text()); qris=parsePayment(qrisReceived.text()); }
        catch(Exception e){ Toast.error(this,"Nominal pembayaran belum valid."); return; }
        if (cash.add(debit).add(qris).signum() <= 0) { Toast.error(this,"Masukkan minimal satu nominal pembayaran."); return; }
        Sale.Request request = new Sale.Request(customer.text(), phone.text(), visitDate(), new ArrayList<>(lines), discountValue(), cash, debit, qris, null);
        new SwingWorker<Sale.Result,Void>() {
            String failure;
            @Override protected Sale.Result doInBackground(){try{return ctx.sales.sell(ctx.user,request);}catch(ServiceException e){failure=e.getMessage();return null;}}
            @Override protected void done(){if(failure!=null){Toast.error(PosScreen.this,failure);return;}try{Sale.Result r=get();lines.clear();customer.clear();phone.clear();discount.clear();cashReceived.clear();debitReceived.clear();qrisReceived.clear();renderCart();showReceipt(r);Toast.success(PosScreen.this,"Penjualan "+r.transactionCode()+" berhasil. "+r.ticketCount()+" tiket dibuat.");}catch(Exception e){Toast.error(PosScreen.this,"Penjualan gagal: "+e.getMessage());}}
        }.execute();
    }
    private void showReceipt(Sale.Result sale) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), "Struk " + sale.transactionCode(), java.awt.Dialog.ModalityType.MODELESS);
        dialog.setLayout(new BorderLayout(0, 10));
        dialog.add(new com.mycompany.tiketwahana.ui.Receipt(sale), BorderLayout.CENTER);
        var print = com.mycompany.tiketwahana.ui.Buttons.primary("Cetak struk", "print");
        print.addActionListener(e -> { try { com.mycompany.tiketwahana.ui.Receipt.print(sale); } catch (Exception ex) { com.mycompany.tiketwahana.ui.Toast.error(dialog, "Printer tidak tersedia: " + ex.getMessage()); } });
        var close = com.mycompany.tiketwahana.ui.Buttons.secondary("Tutup"); close.addActionListener(e -> dialog.dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8)); actions.add(close); actions.add(print); dialog.add(actions, BorderLayout.SOUTH);
        dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

}
