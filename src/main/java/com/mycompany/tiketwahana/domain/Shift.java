package com.mycompany.tiketwahana.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Shift(int id, String kode, String kasir, LocalDateTime openedAt, LocalDateTime closedAt,
                    BigDecimal openingFloat, BigDecimal expectedCash, BigDecimal countedCash,
                    BigDecimal variance, String status) {

    public boolean open() {
        return "OPEN".equals(status);
    }
}
