package com.mycompany.tiketwahana.ui.screens;

import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.domain.ReportRow;
import com.mycompany.tiketwahana.service.ReportService;
import com.mycompany.tiketwahana.service.ServiceException;
import com.mycompany.tiketwahana.ui.AppContext;
import com.mycompany.tiketwahana.ui.Buttons;
import com.mycompany.tiketwahana.ui.Card;
import com.mycompany.tiketwahana.ui.DataTable;
import com.mycompany.tiketwahana.ui.Fonts;
import com.mycompany.tiketwahana.ui.SectionHeader;
import com.mycompany.tiketwahana.ui.TextInput;
import com.mycompany.tiketwahana.ui.Theme;
import com.mycompany.tiketwahana.ui.Toast;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class ReportScreen extends Screen {
    private final TextInput from=new TextInput("YYYY-MM-DD"),to=new TextInput("YYYY-MM-DD");
    private final JLabel summary=new JLabel("-");
    private final DefaultTableModel model=new DefaultTableModel(new Object[]{"Tanggal","Kode","Wahana","Durasi","Tiket","Harga satuan","Total"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=DataTable.create(new String[]{"Tanggal","Kode","Wahana","Durasi","Tiket","Harga satuan","Total"});
    private List<ReportRow> rows=List.of();
    public ReportScreen(AppContext ctx){super(ctx,"report");from.setText(LocalDate.now().withDayOfMonth(1).toString());to.setText(LocalDate.now().toString());add(header(),BorderLayout.NORTH);add(body(),BorderLayout.CENTER);}
    private JComponent header(){JPanel controls=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));controls.setOpaque(false);controls.add(new JLabel("Dari"));controls.add(from);controls.add(new JLabel("sampai"));controls.add(to);var load=Buttons.secondary("Tampilkan","refresh");var export=Buttons.primary("Ekspor CSV","download");load.addActionListener(e->load());export.addActionListener(e->export());controls.add(load);controls.add(export);return new SectionHeader("Laporan penjualan","Mengikuti tanggal kunjungan, bukan waktu input transaksi.",controls);}
    private JComponent body(){Card c=new Card(new BorderLayout(0,12));c.padding(18,18,18,18);summary.setFont(Fonts.semibold(14));summary.setForeground(Theme.p().ink());c.add(summary,BorderLayout.NORTH);table.setModel(model);DataTable.money(table,5);DataTable.money(table,6);c.add(DataTable.scroll(table),BorderLayout.CENTER);return c;}
    @Override public void onShow(){load();}
    private void load(){try{LocalDate f=LocalDate.parse(from.text()),t=LocalDate.parse(to.text());if(t.isBefore(f)){Toast.warn(this,"Tanggal akhir harus setelah tanggal mulai.");return;}new SwingWorker<List<ReportRow>,Void>(){@Override protected List<ReportRow> doInBackground()throws Exception{return ctx.reports.daily(f,t);}@Override protected void done(){try{rows=get();render();}catch(Exception e){Toast.error(ReportScreen.this,e.getMessage());}}}.execute();}catch(Exception e){Toast.error(this,"Format tanggal harus YYYY-MM-DD.");}}
    private void render(){model.setRowCount(0);for(ReportRow r:rows)model.addRow(new Object[]{r.tanggal(),r.kodeWahana(),r.namaWahana(),r.durasiMenit()+" menit",r.jumlahTiket(),r.hargaSatuan(),r.total()});var s=ctx.reports.summarize(rows);summary.setText(rows.size()+" baris  ·  "+s.tickets()+" tiket  ·  "+Money.format(s.revenue()));}
    private void export(){if(rows.isEmpty()){Toast.info(this,"Tidak ada data untuk diekspor.");return;}JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File("laporan-"+from.text()+"-"+to.text()+".csv"));if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;try(BufferedWriter w=Files.newBufferedWriter(chooser.getSelectedFile().toPath(),StandardCharsets.UTF_8)){w.write("Tanggal,Kode,Wahana,Durasi,Tiket,Harga Satuan,Total\n");for(ReportRow r:rows){w.write(String.format("%s,%s,\"%s\",%d,%d,%s,%s\n",r.tanggal(),r.kodeWahana(),r.namaWahana().replace("\"","\"\""),r.durasiMenit(),r.jumlahTiket(),Money.plain(r.hargaSatuan()),Money.plain(r.total())));}Toast.success(this,"CSV tersimpan di "+chooser.getSelectedFile().getName()+".");}catch(IOException e){Toast.error(this,"CSV tidak dapat ditulis: "+e.getMessage());}}
}
