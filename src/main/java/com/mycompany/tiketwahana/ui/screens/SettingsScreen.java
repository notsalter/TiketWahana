package com.mycompany.tiketwahana.ui.screens;

import com.mycompany.tiketwahana.core.Auth;
import com.mycompany.tiketwahana.core.ConnectionPool;
import com.mycompany.tiketwahana.core.Env;
import com.mycompany.tiketwahana.core.SessionUser;
import com.mycompany.tiketwahana.ui.AppContext;
import com.mycompany.tiketwahana.ui.Buttons;
import com.mycompany.tiketwahana.ui.Card;
import com.mycompany.tiketwahana.ui.Fonts;
import com.mycompany.tiketwahana.ui.SectionHeader;
import com.mycompany.tiketwahana.ui.TextInput;
import com.mycompany.tiketwahana.ui.Theme;
import com.mycompany.tiketwahana.ui.Toast;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/** Settings only exposes settings that actually persist or actually execute. */
public class SettingsScreen extends Screen {
    private final JLabel themeState=new JLabel();
    public SettingsScreen(AppContext ctx){super(ctx,"settings");add(new SectionHeader("Pengaturan","Preferensi panel dan keamanan akun."),BorderLayout.NORTH);add(body(),BorderLayout.CENTER);}
    private JComponent body(){JPanel body=new JPanel();body.setOpaque(false);body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));Card appearance=new Card(new BorderLayout(18,0));appearance.padding(20,20,20,20);JPanel labels=new JPanel();labels.setOpaque(false);labels.setLayout(new BoxLayout(labels,BoxLayout.Y_AXIS));JLabel t=new JLabel("Tampilan");t.setFont(Fonts.semibold(15));t.setForeground(Theme.p().ink());JLabel s=new JLabel("Pilih tema yang nyaman untuk shift panjang.");s.setFont(Fonts.regular(12));s.setForeground(Theme.p().muted());labels.add(t);labels.add(Box.createVerticalStrut(4));labels.add(s);appearance.add(labels,BorderLayout.CENTER);var toggle=Buttons.secondary("Ganti tema","moon");toggle.addActionListener(e->{Theme.toggle();themeState.setText(Theme.mode().name());Toast.success(this,"Tema diubah ke "+Theme.mode().name().toLowerCase()+".");});appearance.add(toggle,BorderLayout.EAST);themeState.setFont(Fonts.mono(12));themeState.setForeground(Theme.p().muted());appearance.add(themeState,BorderLayout.SOUTH);body.add(appearance);body.add(Box.createVerticalStrut(16));Card account=new Card(new BorderLayout(18,0));account.padding(20,20,20,20);JPanel al=new JPanel();al.setOpaque(false);al.setLayout(new BoxLayout(al,BoxLayout.Y_AXIS));JLabel at=new JLabel("Keamanan akun");at.setFont(Fonts.semibold(15));at.setForeground(Theme.p().ink());JLabel ac=new JLabel("Password akun "+ctx.user.username()+" disimpan dengan PBKDF2-HMAC-SHA256.");ac.setFont(Fonts.regular(12));ac.setForeground(Theme.p().muted());al.add(at);al.add(Box.createVerticalStrut(4));al.add(ac);account.add(al,BorderLayout.CENTER);var change=Buttons.secondary("Ganti password","lock");change.addActionListener(e->changePassword());account.add(change,BorderLayout.EAST);body.add(account);body.add(Box.createVerticalStrut(16));Card about=new Card(new BorderLayout());about.padding(20,20,20,20);JLabel text=new JLabel("<html><b>TiketWahana 2.0</b><br>Java Swing · Java2D · JDBC · MySQL 8<br><br>Backup, restore, dan log sistem yang tidak benar-benar berjalan sengaja tidak ditampilkan.</html>");text.setFont(Fonts.regular(13));text.setForeground(Theme.p().ink());about.add(text);body.add(about);body.add(Box.createVerticalGlue());return body;}
    @Override public void onShow(){themeState.setText(Theme.mode().name());}
    private void changePassword(){TextInput current=new TextInput("Password saat ini"),next=new TextInput("Password baru"),confirm=new TextInput("Ulangi password baru");JPanel p=new JPanel(new GridLayout(0,1,0,8));p.add(current);p.add(next);p.add(confirm);if(javax.swing.JOptionPane.showConfirmDialog(this,p,"Ganti password",javax.swing.JOptionPane.OK_CANCEL_OPTION)!=javax.swing.JOptionPane.OK_OPTION)return;if(!next.text().equals(confirm.text())){Toast.error(this,"Konfirmasi password tidak cocok.");return;}try(ConnectionPool.Lease lease=ctx.database.borrow()){Auth.changePassword(lease.connection(),ctx.user,current.text(),next.text());Toast.success(this,"Password diperbarui.");}catch(Exception e){Toast.error(this,e.getMessage());}}
}
