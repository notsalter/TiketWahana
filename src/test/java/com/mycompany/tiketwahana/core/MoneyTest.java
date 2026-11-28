package com.mycompany.tiketwahana.core;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {
    @Test void parsesIndonesianWholeAmount() {
        assertEquals(new BigDecimal("25000.00"), Money.parse("Rp 25.000"));
        assertEquals(new BigDecimal("25000.00"), Money.parse("25.000"));
    }
    @Test void parsesDecimalComma() {
        assertEquals(new BigDecimal("25000.50"), Money.parse("Rp 25.000,50"));
    }
    @Test void rejectsInvalidAndNegative() {
        assertThrows(IllegalArgumentException.class, () -> Money.parse("-100"));
        assertThrows(IllegalArgumentException.class, () -> Money.parse("25abc"));
    }
    @Test void formatsRupiah() {
        assertEquals("Rp 25.000", Money.format(Money.of("25000")));
        assertEquals("Rp 25.000,50", Money.format(Money.of("25000.50")));
    }
}
