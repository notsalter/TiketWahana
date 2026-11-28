package com.mycompany.tiketwahana.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateText {

    private static final DateTimeFormatter LONG =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("id-ID"));
    private static final DateTimeFormatter SHORT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("id-ID"));
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"));
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("id-ID"));

    private DateText() {}

    public static String format(LocalDate date) {
        return date == null ? "-" : date.format(LONG);
    }

    public static String shortDate(LocalDate date) {
        return date == null ? "-" : date.format(SHORT);
    }

    public static String stamp(LocalDateTime at) {
        return at == null ? "-" : at.format(STAMP);
    }

    public static String time(LocalDateTime at) {
        return at == null ? "-" : at.format(TIME);
    }
}
