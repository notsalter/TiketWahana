package com.mycompany.tiketwahana.domain;

import com.mycompany.tiketwahana.core.Money;
import java.math.BigDecimal;

/** One editable line in the point-of-sale cart. */
public final class CartLine {

    private final Wahana wahana;
    private int quantity;

    public CartLine(Wahana wahana, int quantity) {
        this.wahana = wahana;
        this.quantity = Math.max(1, quantity);
    }

    public Wahana wahana() {
        return wahana;
    }

    public int quantity() {
        return quantity;
    }

    public void quantity(int value) {
        this.quantity = Math.max(0, value);
    }

    public BigDecimal subtotal() {
        return Money.multiply(wahana.harga(), quantity);
    }
}
