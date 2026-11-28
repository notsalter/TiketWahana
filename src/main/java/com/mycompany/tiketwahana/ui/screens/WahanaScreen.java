package com.mycompany.tiketwahana.ui.screens;

import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.domain.Wahana;
import com.mycompany.tiketwahana.service.ServiceException;
import com.mycompany.tiketwahana.ui.AppContext;
import com.mycompany.tiketwahana.ui.Buttons;
import com.mycompany.tiketwahana.ui.Card;
import com.mycompany.tiketwahana.ui.DataTable;
import com.mycompany.tiketwahana.ui.EmptyState;
import com.mycompany.tiketwahana.ui.FlatButton;
import com.mycompany.tiketwahana.ui.Fonts;
import com.mycompany.tiketwahana.ui.Pill;
import com.mycompany.tiketwahana.ui.SectionHeader;
import com.mycompany.tiketwahana.ui.TextInput;
import com.mycompany.tiketwahana.ui.Theme;
import com.mycompany.tiketwahana.ui.Toast;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class WahanaScreen extends Screen {
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID","Kode","Nama wahana","Harga","Kapasitas","Durasi","Status"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=DataTable.create(new String[]{"ID","Kode","Nama wahana","Harga","Kapasitas","Durasi","Status"});
    private List<Wahana> current=List.of();
    public WahanaScreen(AppContext ctx){super(ctx,"wahana"); add(header(),BorderLayout.NORTH); add(body(),BorderLayout.CENTER);}
    private JComponent header(){var add=Buttons.primary("Tambah wahana","plus");add.addActionListener(e->edit(null));return new SectionHeader("Wahana","Atur katalog, harga, kapasitas, dan status atraksi.",add);}
    private JComponent body(){Card card=new Card(new BorderLayout(0,12));card.padding(18,18,18,18);table.setModel(model);DataTable.money(table,3);DataTable.pill(table,6);DataTable.width(table,0,45);DataTable.width(table,1,80);DataTable.width(table,4,90);DataTable.width(table,5,80);card.add(DataTable.scroll(table),BorderLayout.CENTER);var actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));actions.setOpaque(false);var edit=Buttons.secondary("Edit","edit");var del=Buttons.danger("Hapus","trash");edit.addActionListener(e->{int r=table.getSelectedRow();if(r>=0)edit(current.get(table.convertRowIndexToModel(r)));else Toast.info(this,"Pilih wahana terlebih dahulu.");});del.addActionListener(e->deleteSelected());actions.add(edit);actions.add(del);card.add(actions,BorderLayout.SOUTH);return card;}
    @Override public void onShow(){new SwingWorker<List<Wahana>,Void>(){@Override protected List<Wahana> doInBackground()throws Exception{return ctx.wahana.list(false);}@Override protected void done(){try{current=get();model.setRowCount(0);for(Wahana w:current)model.addRow(new Object[]{w.id(),w.kode(),w.nama(),w.harga(),w.kapasitas(),w.durasiMenit()+" menit",w.status().name()});}catch(Exception e){Toast.error(WahanaScreen.this,"Wahana tidak dapat dimuat: "+e.getMessage());}}}.execute();}
    private void deleteSelected(){int r=table.getSelectedRow();if(r<0){Toast.info(this,"Pilih wahana terlebih dahulu.");return;}Wahana w=current.get(table.convertRowIndexToModel(r));if(JOptionPane.showConfirmDialog(this,"Hapus "+w.nama()+"? Data tiket lama akan mencegah penghapusan.","Hapus wahana",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;new SwingWorker<Void,Void>(){@Override protected Void doInBackground()throws Exception{ctx.wahana.delete(ctx.user,w.id());return null;}@Override protected void done(){try{get();Toast.success(WahanaScreen.this,"Wahana dihapus.");onShow();}catch(Exception e){Toast.error(WahanaScreen.this,e.getMessage());}}}.execute();}
    private void edit(Wahana original){JPanel p=new JPanel(new GridLayout(0,2,8,8));p.setBorder(BorderFactory.createEmptyBorder(10,4,4,4));TextInput code=new TextInput("W001"),name=new TextInput("Nama wahana"),price=new TextInput("25000"),capacity=new TextInput("50"),duration=new TextInput("30");JTextArea desc=new JTextArea(3,20);desc.setLineWrap(true);JComboBox<Wahana.Status> status=new JComboBox<>(Wahana.Status.values());if(original!=null){code.setText(original.kode());name.setText(original.nama());price.setText(Money.plain(original.harga()));capacity.setText(String.valueOf(original.kapasitas()));duration.setText(String.valueOf(original.durasiMenit()));desc.setText(original.deskripsi());status.setSelectedItem(original.status());}p.add(new JLabel("Kode"));p.add(code);p.add(new JLabel("Nama"));p.add(name);p.add(new JLabel("Harga"));p.add(price);p.add(new JLabel("Kapasitas"));p.add(capacity);p.add(new JLabel("Durasi (menit)"));p.add(duration);p.add(new JLabel("Status"));p.add(status);p.add(new JLabel("Deskripsi"));p.add(desc);int ok=JOptionPane.showConfirmDialog(this,p,original==null?"Tambah wahana":"Edit wahana",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);if(ok!=JOptionPane.OK_OPTION)return;try{BigDecimal h=Money.parse(price.text());int cap=Integer.parseInt(capacity.text()),dur=Integer.parseInt(duration.text());if(original==null)ctx.wahana.create(ctx.user,code.text(),name.text(),desc.getText(),h,cap,dur,(Wahana.Status)status.getSelectedItem());else ctx.wahana.update(ctx.user,original.id(),code.text(),name.text(),desc.getText(),h,cap,dur,(Wahana.Status)status.getSelectedItem());Toast.success(this,"Wahana disimpan.");onShow();}catch(Exception e){Toast.error(this,e.getMessage());}}
}
