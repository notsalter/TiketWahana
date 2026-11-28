package com.mycompany.tiketwahana.docs;

import com.mycompany.tiketwahana.core.*;
import com.mycompany.tiketwahana.ui.*;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Runs the real application on a virtual display and photographs each screen.
 * Used to produce the README images and to prove the window renders with data.
 */
public final class CaptureMain {

    private CaptureMain() {}

    public static void main(String[] args) throws Exception {
        String url = System.getProperty("mysql.url");
        String password = System.getProperty("mysql.password", "");
        Database database = Database.create(url, "root", password, 4);
        try (ConnectionPool.Lease lease = database.borrow()) {
            Migrations.run(lease.connection());
            Auth.bootstrapAdmin(lease.connection());
        }
        SessionUser user = database.withConnection(c -> {
            try {
                return Auth.login(c, "admin", "rahasia-kuat-2026");
            } catch (AuthException e) {
                throw new java.sql.SQLException("login failed", e);
            }
        });
        Path dir = Path.of("docs", "screenshots");
        Files.createDirectories(dir);

        AtomicReference<MainWindow> windowRef = new AtomicReference<>();
        AppContext context = new AppContext(database, user);
        SwingUtilities.invokeAndWait(() -> {
            Theme.install(Theme.Mode.LIGHT);
            MainWindow window = new MainWindow(context, () -> { });
            window.setSize(1440, 900);
            window.setLocation(0, 0);
            window.setVisible(true);
            windowRef.set(window);
        });
        Thread.sleep(2500);
        MainWindow window = windowRef.get();
        Robot robot = new Robot();

        String[] routes = {"dashboard", "pos", "tickets", "report", "wahana", "shift", "audit", "settings"};
        for (String route : routes) {
            SwingUtilities.invokeAndWait(() -> window.shell().navigate(route));
            Thread.sleep(1500);
            shoot(robot, window, dir.resolve(route + ".png"));
        }
        SwingUtilities.invokeAndWait(() -> {
            window.shell().navigate("dashboard");
            window.shell().toggleTheme();
        });
        Thread.sleep(1500);
        shoot(robot, window, dir.resolve("dashboard-dark.png"));
        database.close();
        System.exit(0);
    }

    private static void shoot(Robot robot, MainWindow window, Path target) throws Exception {
        Rectangle bounds = window.getBounds();
        BufferedImage image = robot.createScreenCapture(bounds);
        ImageIO.write(image, "png", target.toFile());
        System.out.println("captured " + target + " " + image.getWidth() + "x" + image.getHeight());
    }
}
