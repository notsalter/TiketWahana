package com.mycompany.tiketwahana.ui.screens;

import com.mycompany.tiketwahana.domain.TicketRow;
import com.mycompany.tiketwahana.service.ServiceException;
import com.mycompany.tiketwahana.ui.AppContext;
import com.mycompany.tiketwahana.ui.Buttons;
import com.mycompany.tiketwahana.ui.Card;
import com.mycompany.tiketwahana.ui.DataTable;
import com.mycompany.tiketwahana.ui.FlatButton;
import com.mycompany.tiketwahana.ui.Fonts;
import com.mycompany.tiketwahana.ui.SectionHeader;
import com.mycompany.tiketwahana.ui.TextInput;
import com.mycompany.tiketwahana.ui.Toast;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class TicketScreen extends Screen {
    private final TextInput code=new TextInput("Masukkan kode tiket, misalnya TKT-261128-ABCD");
    private final JLabel result=new JLabel(" ");
    private final DefaultTableModel model=new DefaultTableModel(new Object[]{"Kode","Pengunjung","Wahana","Tanggal","Harga","Status","Dipakai oleh"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=DataTable.create(new String[]{"Kode","Pengunjung","Wahana","Tanggal","Harga","Status","Dipakai oleh"});
    public TicketScreen(AppContext ctx){super(ctx,"tickets");add(header(),BorderLayout.NORTH);add(body(),BorderLayout.CENTER);code.onEnter(this::redeem);}
    private JComponent header(){var redeem=Buttons.primary("Validasi tiket","check");redeem.addActionListener(e->redeem());return new SectionHeader("Validasi tiket","Satu kode, satu kali masuk. Status diperbarui langsung di database.",redeem);}
    private JComponent body(){JPanel body=new JPanel(new BorderLayout(0,16));body.setOpaque(false);Card action=new Card(new BorderLayout(12,0));action.padding(16,18,16,18);action.add(code,BorderLayout.CENTER);action.add(result,BorderLayout.SOUTH);result.setFont(Fonts.regular(12));body.add(action,BorderLayout.NORTH);Card list=new Card(new BorderLayout());list.padding(18,18,18,18);table.setModel(model);DataTable.money(table,4);DataTable.pill(table,5);list.add(DataTable.scroll(table),BorderLayout.CENTER);body.add(list,BorderLayout.CENTER);return body;}
    private void redeem(){new SwingWorker<TicketRow,Void>(){String failure;@Override protected TicketRow doInBackground(){try{return ctx.tickets.redeem(ctx.user,code.text());}catch(ServiceException e){failure=e.getMessage();return null;}}@Override protected void done(){if(failure!=null){Toast.error(TicketScreen.this,failure);result.setText(failure);result.setForeground(com.mycompany.tiketwahana.ui.Theme.p().danger());return;}try{TicketRow t=get();result.setText("Masuk diterima · "+t.namaPengunjung()+" · "+t.wahana());result.setForeground(com.mycompany.tiketwahana.ui.Theme.p().accent());Toast.success(TicketScreen.this,"Tiket "+t.kode()+" divalidasi.");onShow();code.clear();}catch(Exception e){Toast.error(TicketScreen.this,e.getMessage());}}}.execute();}
    @Override public void onShow(){new SwingWorker<List<TicketRow>,Void>(){@Override protected List<TicketRow> doInBackground()throws Exception{return ctx.tickets.recent(100);}@Override protected void done(){try{model.setRowCount(0);for(TicketRow t:get())model.addRow(new Object[]{t.kode(),t.namaPengunjung(),t.wahana(),t.tanggalKunjungan(),t.harga(),t.status(),t.redeemedBy()==null?"-":t.redeemedBy()});}catch(Exception e){Toast.error(TicketScreen.this,e.getMessage());}}}.execute();}
}
