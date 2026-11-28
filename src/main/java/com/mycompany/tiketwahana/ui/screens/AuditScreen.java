package com.mycompany.tiketwahana.ui.screens;

import com.mycompany.tiketwahana.service.AuditService;
import com.mycompany.tiketwahana.service.ServiceException;
import com.mycompany.tiketwahana.ui.AppContext;
import com.mycompany.tiketwahana.ui.Buttons;
import com.mycompany.tiketwahana.ui.Card;
import com.mycompany.tiketwahana.ui.DataTable;
import com.mycompany.tiketwahana.ui.SectionHeader;
import com.mycompany.tiketwahana.ui.TextInput;
import com.mycompany.tiketwahana.ui.Toast;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class AuditScreen extends Screen {
 private final TextInput filter=new TextInput("Cari aksi, kode, atau nama pengguna");
 private final DefaultTableModel model=new DefaultTableModel(new Object[]{"Waktu","Pengguna","Aksi","Objek","Detail"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
 private final JTable table=DataTable.create(new String[]{"Waktu","Pengguna","Aksi","Objek","Detail"});
 public AuditScreen(AppContext ctx){super(ctx,"audit");var refresh=Buttons.secondary("Muat ulang","refresh");refresh.addActionListener(e->load());var head=new SectionHeader("Audit trail","Jejak akses, uang, tiket, dan perubahan data.",refresh);add(head,BorderLayout.NORTH);Card c=new Card(new BorderLayout(0,12));c.padding(18,18,18,18);c.add(filter,BorderLayout.NORTH);table.setModel(model);c.add(DataTable.scroll(table),BorderLayout.CENTER);add(c,BorderLayout.CENTER);filter.onEnter(this::load);}
 @Override public void onShow(){load();}
 private void load(){new SwingWorker<List<AuditService.Entry>,Void>(){@Override protected List<AuditService.Entry> doInBackground()throws Exception{return ctx.audit.recent(filter.text(),200);}@Override protected void done(){try{model.setRowCount(0);for(var e:get())model.addRow(new Object[]{e.at(),e.user(),e.action(),(e.entity()==null?"":e.entity())+" "+(e.entityId()==null?"":e.entityId()),e.detail()});}catch(Exception e){Toast.error(AuditScreen.this,e.getMessage());}}}.execute();}
}
