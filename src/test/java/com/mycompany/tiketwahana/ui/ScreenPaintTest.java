package com.mycompany.tiketwahana.ui;

import static org.junit.jupiter.api.Assertions.*;
import com.mycompany.tiketwahana.core.Database;
import com.mycompany.tiketwahana.core.Role;
import com.mycompany.tiketwahana.core.SessionUser;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Every registered screen must survive a headless paint at the reference window sizes. */
class ScreenPaintTest {
    @Test void everyScreenPaintsAtBothTargetSizesAndThemes() throws Exception {
        Database database = Database.create("jdbc:mysql://127.0.0.1:1/no_network", "none", "none", 1);
        AppContext context = new AppContext(database,
                new SessionUser(1, "visual", "Visual Reviewer", Role.ADMIN, false));
        String[] routes = {"dashboard", "pos", "tickets", "wahana", "report", "shift", "audit", "settings"};
        for (Theme.Mode mode : Theme.Mode.values()) {
            Theme.install(mode);
            String suffix = mode == Theme.Mode.LIGHT ? "light" : "dark";
            BufferedImage contact = new BufferedImage(1440, 900, BufferedImage.TYPE_INT_ARGB);
            Graphics2D contactGraphics = contact.createGraphics();
            for (String route : routes) {
                var screen = Screens.create(route, context);
                screen.setSize(1440, 900);
                screen.doLayout();
                assertDoesNotThrow(() -> screen.paint(contactGraphics), "paint failed for " + route + " / " + mode);
            }
            contactGraphics.dispose();
            BufferedImage smaller = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_ARGB);
            Graphics2D smallerGraphics = smaller.createGraphics();
            for (String route : routes) {
                var screen = Screens.create(route, context);
                screen.setSize(1024, 768);
                screen.doLayout();
                assertDoesNotThrow(() -> screen.paint(smallerGraphics), "paint failed for 1024px " + route + " / " + mode);
            }
            smallerGraphics.dispose();
        }
        database.close();
    }
}
