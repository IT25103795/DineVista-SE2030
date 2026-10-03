package com.dinevista.strategy;

import java.math.BigDecimal;

/** Interchangeable discount calculation for one eligible promotion. */
public interface DiscountStrategy {
    BigDecimal calculate(BigDecimal subtotal, BigDecimal value);
}
