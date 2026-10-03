package com.dinevista.strategy;

/** Selects the strategy represented by the persisted promotion type. */
public final class DiscountStrategies {
    private static final DiscountStrategy PERCENTAGE = new PercentageDiscountStrategy();
    private static final DiscountStrategy FIXED = new FixedAmountDiscountStrategy();

    private DiscountStrategies() { }

    public static DiscountStrategy forType(String type) {
        if ("PERCENTAGE".equals(type)) return PERCENTAGE;
        if ("FIXED_AMOUNT".equals(type)) return FIXED;
        throw new IllegalArgumentException("Unsupported discount type: " + type);
    }
}
