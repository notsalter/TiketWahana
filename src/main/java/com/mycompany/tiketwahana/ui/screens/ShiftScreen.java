package com.mycompany.tiketwahana.ui.screens;

import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.domain.Shift;
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
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class ShiftScreen extends Screen {
    private final JLabel state=new JLabel();
    private final JLabel expected=new JLabel();
    private final DefaultTableModel model=new DefaultTableModel(new Object[]{"Kode","Kasir","Dibuka","Ditutup","Modal","Selisih","Status"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=DataTable.create(new String[]{"Kode","Kasir","Dibuka","Ditutup","Modal","Selisih","Status"});
    public ShiftScreen(AppContext ctx){super(ctx,"shift");add(header(),BorderLayout.NORTH);add(body(),BorderLayout.CENTER);}
    private JComponent header(){JPanel actions=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT,8,0));actions.setOpaque(false);var open=Buttons.primary("Buka shift","plus");var close=Buttons.secondary("Tutup shift","check");open.addActionListener(e->open());close.addActionListener(e->close());actions.add(open);actions.add(close);return new SectionHeader("Shift kasir","Catat modal awal dan cocokkan kas di akhir hari.",actions);}
    private JComponent body(){JPanel body=new JPanel(new BorderLayout(0,16));body.setOpaque(false);Card active=new Card(new GridLayout(1,2,20,0));active.padding(18,20,18,20);active.add(stat("Status shift",state));active.add(stat("Kas yang diharapkan",expected));body.add(active,BorderLayout.NORTH);Card list=new Card(new BorderLayout());list.padding(18,18,18,18);table.setModel(model);DataTable.money(table,4);DataTable.money(table,5);DataTable.pill(table,6);list.add(DataTable.scroll(table),BorderLayout.CENTER);body.add(list,BorderLayout.CENTER);return body;}
    private JPanel stat(String title,JLabel value){JPanel p=new JPanel(new BorderLayout());p.setOpaque(false);JLabel t=new JLabel(title);t.setFont(Fonts.regular(12));t.setForeground(Theme.p().muted());value.setFont(Fonts.semibold(20));value.setForeground(Theme.p().ink());p.add(t,BorderLayout.NORTH);p.add(value,BorderLayout.CENTER);return p;}
    @Override public void onShow(){new SwingWorker<List<Shift>,Void>(){@Override protected List<Shift> doInBackground()throws Exception{return ctx.shifts.recent(50);}@Override protected void done(){try{ctx.refreshShift();if(ctx.shiftOpen()){state.setText("Terbuka · "+ctx.shift.kode());expected.setText(Money.format(ctx.shifts.expectedCash(ctx.shift.id())));}else{state.setText("Belum dibuka");expected.setText("-");}model.setRowCount(0);for(Shift s:get())model.addRow(new Object[]{s.kode(),s.kasir(),s.openedAt(),s.closedAt(),s.openingFloat(),s.variance(),s.status()});}catch(Exception e){Toast.error(ShiftScreen.this,e.getMessage());}}}.execute();}
    private void open(){String raw=JOptionPane.showInputDialog(this,"Modal awal kas", "0");if(raw==null)return;try{var amount=Money.parse(raw);new SwingWorker<Shift,Void>(){@Override protected Shift doInBackground()throws Exception{return ctx.shifts.open(ctx.user,amount);}@Override protected void done(){try{get();Toast.success(ShiftScreen.this,"Shift dibuka.");onShow();}catch(Exception e){Toast.error(ShiftScreen.this,e.getMessage());}}}.execute();}catch(Exception e){Toast.error(this,e.getMessage());}}
    private void close(){if(!ctx.shiftOpen()){Toast.info(this,"Belum ada shift terbuka.");return;}String raw=JOptionPane.showInputDialog(this,"Kas terhitung",Money.plain(shiftsExpectedSafe()));if(raw==null)return;try{var amount=Money.parse(raw);int id=ctx.shift.id();new SwingWorker<Shift,Void>(){@Override protected Shift doInBackground()throws Exception{return ctx.shifts.close(ctx.user,id,amount);}@Override protected void done(){try{Shift s=get();Toast.success(ShiftScreen.this,"Shift ditutup. Selisih: "+Money.format(s.variance()));onShow();}catch(Exception e){Toast.error(ShiftScreen.this,e.getMessage());}}}.execute();}catch(Exception e){Toast.error(this,e.getMessage());}}
    private java.math.BigDecimal shiftsExpectedSafe(){try{return ctx.shift==null?Money.ZERO:ctx.shifts.expectedCash(ctx.shift.id());}catch(Exception e){return Money.ZERO;}}
}
