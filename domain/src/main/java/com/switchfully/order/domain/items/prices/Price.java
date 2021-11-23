package com.switchfully.order.domain.items.prices;

import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public final class Price {

    private BigDecimal amount;

    public Price() {
    }

    private Price(BigDecimal amount) {
        this.amount = amount;
    }

    public static Price create(BigDecimal amount) {
        return new Price(amount);
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmountAsFloat(float amountAsFloat) {
        this.amount = new BigDecimal(amountAsFloat);
    }

    public float getAmountAsFloat() {
        return amount.floatValue();
    }

    public static Price add(Price price1, Price price2) {
        return Price.create(price1.getAmount().add(price2.getAmount()));
    }

    public boolean sameAs(Price otherPrice) {
        return amount.equals(otherPrice.getAmount());
    }

    @Override
    public String toString() {
        return "Price{" + "amount=" + amount + '}';
    }
}
