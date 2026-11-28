package com.mycompany.tiketwahana.core;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Human-readable codes: no 0/O/1/I, so a cashier can read a ticket aloud. */
public final class Codes {

    private static final char[] ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyMMdd");

    private Codes() {}

    public static String random(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }

    public static String ticket() {
        return "TKT-" + LocalDate.now().format(DAY) + "-" + random(5);
    }

    public static String transaction() {
        return "TRX-" + LocalDate.now().format(DAY) + "-" + random(4);
    }

    public static String shift() {
        return "SHIFT-" + LocalDate.now().format(DAY) + "-" + random(3);
    }
}
