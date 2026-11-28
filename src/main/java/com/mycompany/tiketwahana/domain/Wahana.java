package com.mycompany.tiketwahana.domain;

import java.math.BigDecimal;

public record Wahana(int id, String kode, String nama, String deskripsi, BigDecimal harga,
                     int kapasitas, int durasiMenit, Status status) {

    public enum Status {
        AKTIF, MAINTENANCE, TUTUP;

        public String label() {
            return switch (this) {
                case AKTIF -> "Aktif";
                case MAINTENANCE -> "Perawatan";
                case TUTUP -> "Tutup";
            };
        }

        public static Status of(String raw) {
            try {
                return valueOf(raw);
            } catch (RuntimeException e) {
                return TUTUP;
            }
        }
    }

    public boolean sellable() {
        return status == Status.AKTIF;
    }
}
