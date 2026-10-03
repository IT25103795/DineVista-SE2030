package com.dinevista.strategy;

import java.math.BigDecimal;

/** Deducts a fixed currency amount regardless of invoice subtotal. */
public final class FixedAmountDiscountStrategy implements DiscountStrategy {
    @Override public BigDecimal calculate(BigDecimal subtotal, BigDecimal value) {
        return value;
    }
}
