package com.mycompany.tiketwahana.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReportRow(LocalDate tanggal, String kodeWahana, String namaWahana, int durasiMenit,
                        int jumlahTiket, BigDecimal hargaSatuan, BigDecimal total) {
}
