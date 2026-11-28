package com.mycompany.tiketwahana.core;

import static org.junit.jupiter.api.Assertions.*;
import com.mycompany.tiketwahana.ui.Barcode;
import org.junit.jupiter.api.Test;

class BarcodeTest {
    @Test void code128BHasStartChecksumAndStop() {
        String bits = new Barcode("TKT-261128-ABCDE").bits();
        assertTrue(bits.startsWith("11010010000"));
        assertTrue(bits.endsWith("1100011101011"));
        assertTrue(bits.length() > 11);
    }
    @Test void checksumChangesWithPayload() {
        assertNotEquals(new Barcode("A").bits(), new Barcode("B").bits());
    }
}
