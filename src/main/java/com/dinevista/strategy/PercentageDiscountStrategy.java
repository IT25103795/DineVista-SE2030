package com.dinevista.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Calculates a percentage of the invoice subtotal. */
public final class PercentageDiscountStrategy implements DiscountStrategy {
    @Override public BigDecimal calculate(BigDecimal subtotal, BigDecimal value) {
        return subtotal.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
