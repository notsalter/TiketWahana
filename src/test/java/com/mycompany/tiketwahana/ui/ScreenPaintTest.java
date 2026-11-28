package com.mycompany.tiketwahana.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/** Every non-database screen is required to survive a headless paint. */
class ScreenPaintTest {
    @Test void coreScreenBuildingBlocksPaintAtPortfolioSizes() {
        Theme.install(Theme.Mode.LIGHT);
        for (int[] size : List.of(new int[]{1440,900},new int[]{1024,768})) {
            JPanel root = new JPanel(new java.awt.BorderLayout());
            root.setSize(size[0], size[1]);
            root.add(new Card(new java.awt.BorderLayout()) {{
                add(new SectionHeader("Wahana", "Wahana aktif hari ini"), java.awt.BorderLayout.NORTH);
                add(new DataTableEmpty(), java.awt.BorderLayout.CENTER);
            }});
            root.doLayout();
            BufferedImage image = new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_ARGB);
            assertDoesNotThrow(() -> root.paint(image.createGraphics()));
        }
    }
    private static final class DataTableEmpty extends JPanel {
        DataTableEmpty(){setOpaque(false);add(new EmptyState("park","Belum ada wahana","Tambahkan data untuk melihat kartu di sini.",Buttons.secondary("Tambah","plus")));}
    }
}
