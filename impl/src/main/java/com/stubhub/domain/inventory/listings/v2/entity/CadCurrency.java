package com.stubhub.domain.inventory.listings.v2.entity;

import java.math.BigDecimal;

public class CadCurrency {
    Long id;
    BigDecimal amount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
