package com.mycompany.tiketwahana.integration;

import static org.junit.jupiter.api.Assertions.*;
import com.mycompany.tiketwahana.core.*;
import com.mycompany.tiketwahana.domain.*;
import com.mycompany.tiketwahana.service.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named="mysql.smoke", matches="true")
class MySqlSmokeTest {
    private Database db;
    private SessionUser user;
    @BeforeEach void setup() throws Exception {
        db=Database.create(System.getProperty("mysql.url"),"root",System.getProperty("mysql.password"),4);
        try(var lease=db.borrow()) { Migrations.run(lease.connection()); Auth.bootstrapAdmin(lease.connection()); }
        db.withConnection(c -> { try(var ps=c.prepareStatement("UPDATE users SET password_hash=?,must_change_password=FALSE WHERE username='admin'")){ps.setString(1,Auth.hash("strong-pass-2026"));ps.executeUpdate();} return null; });
        try(var lease=db.borrow()){user=Auth.login(lease.connection(),"admin","strong-pass-2026");}
        db.withConnection(c->{try(var ps=c.prepareStatement("INSERT INTO users(username,password_hash,full_name,role,must_change_password) VALUES('kasir',?,'Kasir Demo','KASIR',FALSE) ON DUPLICATE KEY UPDATE id=id")){ps.setString(1,Auth.hash("strong-pass-2026"));ps.executeUpdate();}return null;});
        try(var lease=db.borrow()){user=Auth.login(lease.connection(),"kasir","strong-pass-2026");}
        new ShiftService(db).open(user,Money.of("100000"));
    }
    @AfterEach void close(){if(db!=null)db.close();}
    @Test void endToEndSaleRedemptionAndBalancedLedger() throws Exception {
        Wahana w1=new WahanaService(db).list(true).get(0),w2=new WahanaService(db).list(true).get(1);
        Sale.Request req=new Sale.Request("Demo Visitor","0812",LocalDate.now(),List.of(new CartLine(w1,2),new CartLine(w2,1)),Money.ZERO, w1.harga().multiply(BigDecimal.valueOf(2)).add(w2.harga()),Money.ZERO,Money.ZERO,null);
        Sale.Result sale=new SalesService(db).sell(user,req);
        assertEquals(3,sale.ticketCount());
        assertEquals(sale.total(),sale.paid());
        BigDecimal ledger=db.withConnection(c->{try(var ps=c.prepareStatement("SELECT (SELECT total_amount FROM transaksi WHERE id=?), (SELECT COALESCE(SUM(total_harga),0) FROM tiket t JOIN transaksi_detail d ON d.id=t.transaksi_detail_id WHERE d.transaksi_id=?), (SELECT COALESCE(SUM(subtotal),0) FROM transaksi_detail WHERE transaksi_id=?)")){ps.setInt(1,sale.transactionId());ps.setInt(2,sale.transactionId());ps.setInt(3,sale.transactionId());try(var rs=ps.executeQuery()){assertTrue(rs.next());assertEquals(rs.getBigDecimal(1),rs.getBigDecimal(2));assertEquals(rs.getBigDecimal(1),rs.getBigDecimal(3));return rs.getBigDecimal(1);}}});
        assertEquals(sale.total(),ledger);
        TicketRow first=new TicketService(db).recent(1).get(0);
        assertTrue(new TicketService(db).redeem(user,first.kode()).active()==false);
        assertThrows(ServiceException.class,()->new TicketService(db).redeem(user,first.kode()));
        ShiftService shifts = new ShiftService(db);
        Shift open = shifts.currentOpen(user.id());
        BigDecimal expectedCash = shifts.expectedCash(open.id());
        Shift closed=shifts.close(user,open.id(),expectedCash);
        assertEquals(Money.ZERO,closed.variance());
    }
}
