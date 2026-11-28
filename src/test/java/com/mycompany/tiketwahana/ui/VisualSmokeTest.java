package com.mycompany.tiketwahana.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Paints the visual primitives at the two reference window sizes.
 * Screens that need a live MySQL connection are smoke-tested by construction of
 * their components; their visual primitives do not touch the network.
 */
class VisualSmokeTest {
    @BeforeAll static void theme() {
        System.setProperty("java.awt.headless", "true");
        Theme.install(Theme.Mode.LIGHT);
    }
    @Test void iconAndBarcodePaintAt100And150PercentTargets() {
        for (int[] size : new int[][] {{1440,900},{1024,768}}) {
            for (Theme.Mode mode : Theme.Mode.values()) {
                Theme.install(mode);
                JPanelHarness canvas = new JPanelHarness(size[0], size[1]);
                BufferedImage image = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics();
                assertDoesNotThrow(() -> canvas.paint(g));
                g.dispose();
                assertNotNull(image.getRGB(size[0]/2, size[1]/2));
            }
        }
    }
    @Test void barcodeHasReadableGeometry() {
        assertEquals(11 + 11 + 13, new Barcode("").bits().length()); // start + checksum + Code 128 stop
        assertTrue(new Barcode("TKT-261128-ABCDE").bits().length() > 100);
    }

    private static final class JPanelHarness extends javax.swing.JPanel {
        JPanelHarness(int width, int height) {
            setSize(width, height);
            setLayout(new java.awt.FlowLayout());
            add(new Barcode("TKT-261128-ABCDE"));
            for (String icon : List.of("grid","cart","ticket","park","chart","cash","gear","sun","moon","check","alert")) {
                add(new javax.swing.JLabel(Icons.of(icon, 24, Theme.p().accent())));
            }
        }
    }
}
