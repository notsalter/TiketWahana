package com.mycompany.tiketwahana.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TicketRow(int id, String kode, String namaPengunjung, String noTelepon, String wahana,
                        String kodeWahana, LocalDate tanggalKunjungan, BigDecimal harga, String status,
                        LocalDateTime redeemedAt, String redeemedBy) {

    public boolean active() {
        return "AKTIF".equals(status);
    }
}
