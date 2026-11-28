package com.mycompany.tiketwahana.ui;

import com.mycompany.tiketwahana.core.Database;
import com.mycompany.tiketwahana.core.SessionUser;
import com.mycompany.tiketwahana.domain.Shift;
import com.mycompany.tiketwahana.service.AuditService;
import com.mycompany.tiketwahana.service.ReportService;
import com.mycompany.tiketwahana.service.SalesService;
import com.mycompany.tiketwahana.service.ShiftService;
import com.mycompany.tiketwahana.service.TicketService;
import com.mycompany.tiketwahana.service.WahanaService;
import java.math.BigDecimal;

/** Everything a screen needs, wired once at login. */
public final class AppContext {

    public final Database database;
    public final WahanaService wahana;
    public final SalesService sales;
    public final TicketService tickets;
    public final ReportService reports;
    public final ShiftService shifts;
    public final AuditService audit;
    public SessionUser user;
    public Shift shift;
    public Runnable onLogout;

    public AppContext(Database database, SessionUser user) {
        this.database = database;
        this.user = user;
        this.wahana = new WahanaService(database);
        this.sales = new SalesService(database);
        this.tickets = new TicketService(database);
        this.reports = new ReportService(database);
        this.shifts = new ShiftService(database);
        this.audit = new AuditService(database);
        refreshShift();
    }

    public void refreshShift() {
        try {
            shift = shifts.currentOpen(user.id());
        } catch (Exception e) {
            shift = null;
        }
    }

    public boolean shiftOpen() {
        return shift != null;
    }

    public BigDecimal openingFloat() {
        return shift == null ? BigDecimal.ZERO : shift.openingFloat();
    }
}
