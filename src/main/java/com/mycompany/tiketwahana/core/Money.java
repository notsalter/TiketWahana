package com.mycompany.tiketwahana.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/** Rupiah arithmetic. Every amount in the app is a 2-dp BigDecimal produced here. */
public final class Money {

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final Locale ID = Locale.forLanguageTag("id-ID");

    private Money() {}

    public static BigDecimal of(String value) {
        return scale(new BigDecimal(value));
    }

    public static BigDecimal of(long value) {
        return scale(BigDecimal.valueOf(value));
    }

    public static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiply(BigDecimal unit, int quantity) {
        return scale(unit.multiply(BigDecimal.valueOf(quantity)));
    }

    public static BigDecimal sum(Iterable<BigDecimal> values) {
        BigDecimal total = ZERO;
        for (BigDecimal v : values) {
            total = total.add(v);
        }
        return scale(total);
    }

    /**
     * Parses user input: "25000", "25.000", "Rp 25.000", "25000,50".
     * Negative amounts are rejected; this app never stores them.
     */
    public static BigDecimal parse(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Nilai kosong");
        }
        String s = raw.trim().replace("Rp", "").replace("rp", "").replaceAll("\\s+", "");
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Nilai kosong");
        }
        if (s.startsWith("-")) {
            throw new IllegalArgumentException("Nilai tidak boleh negatif");
        }
        int comma = s.indexOf(',');
        if (comma >= 0) {
            s = s.replace(".", "").replace(',', '.');
        } else if (s.matches("\\d{1,3}(\\.\\d{3})+")) {
            s = s.replace(".", "");
        }
        if (!s.matches("\\d+(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException("Format angka tidak valid: " + raw.trim());
        }
        return scale(new BigDecimal(s));
    }

    /** "Rp 25.000" for whole amounts, "Rp 25.000,50" otherwise. */
    public static String format(BigDecimal value) {
        BigDecimal v = scale(value == null ? ZERO : value);
        boolean whole = v.remainder(BigDecimal.ONE).signum() == 0;
        NumberFormat nf = NumberFormat.getInstance(ID);
        nf.setMinimumFractionDigits(whole ? 0 : 2);
        nf.setMaximumFractionDigits(2);
        return "Rp " + nf.format(v);
    }

    /** Plain digits for CSV export and receipts. */
    public static String plain(BigDecimal value) {
        return scale(value == null ? ZERO : value).toPlainString();
    }

    public static boolean positive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }
}
