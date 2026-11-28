package com.mycompany.tiketwahana.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Sale {

    private Sale() {}

    /** Everything the cashier entered, before validation. */
    public record Request(String customer, String phone, LocalDate visitDate, List<CartLine> lines,
                          BigDecimal discount, BigDecimal cash, BigDecimal debit, BigDecimal qris, String notes) {

        public BigDecimal paid() {
            return cash.add(debit).add(qris);
        }

        public int ticketCount() {
            return lines.stream().mapToInt(CartLine::quantity).sum();
        }
    }

    /** What the sale actually produced, for the receipt and the toast. */
    public record Result(int transactionId, String transactionCode, BigDecimal subtotal, BigDecimal discount,
                         BigDecimal tax, BigDecimal total, BigDecimal paid, BigDecimal change,
                         int ticketCount, List<String> ticketCodes) {
    }
}
