package com.mycompany.tiketwahana.ui;

import com.mycompany.tiketwahana.ui.screens.AuditScreen;
import com.mycompany.tiketwahana.ui.screens.DashboardScreen;
import com.mycompany.tiketwahana.ui.screens.PosScreen;
import com.mycompany.tiketwahana.ui.screens.ReportScreen;
import com.mycompany.tiketwahana.ui.screens.SettingsScreen;
import com.mycompany.tiketwahana.ui.screens.ShiftScreen;
import com.mycompany.tiketwahana.ui.screens.TicketScreen;
import com.mycompany.tiketwahana.ui.screens.WahanaScreen;
import com.mycompany.tiketwahana.ui.screens.Screen;

public final class Screens {
    private Screens() {}

    public static Screen create(String route, AppContext ctx) {
        return switch (route) {
            case "dashboard" -> new DashboardScreen(ctx);
            case "pos" -> new PosScreen(ctx);
            case "tickets" -> new TicketScreen(ctx);
            case "wahana" -> new WahanaScreen(ctx);
            case "report" -> new ReportScreen(ctx);
            case "shift" -> new ShiftScreen(ctx);
            case "audit" -> new AuditScreen(ctx);
            case "settings" -> new SettingsScreen(ctx);
            default -> new DashboardScreen(ctx);
        };
    }
}
