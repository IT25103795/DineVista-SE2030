package com.dinevista.service;

import com.dinevista.model.PromotionRecord;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Runnable proof that billing switches concrete discount algorithms at runtime. */
public final class DiscountStrategySelfTest {
    public static void main(String[] args) {
        PromotionRecord promotion = new PromotionRecord(1, "LAB7STRATEGY", "Strategy demo",
                "PERCENTAGE", new BigDecimal("10"), BigDecimal.ZERO,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), null, true);
        expect(promotion.calculateDiscount(new BigDecimal("2000")), "200.00");
        promotion.setDiscountType("FIXED_AMOUNT");
        promotion.setDiscountValue(new BigDecimal("350"));
        expect(promotion.calculateDiscount(new BigDecimal("2000")), "350.00");
        promotion.setDiscountValue(new BigDecimal("3000"));
        expect(promotion.calculateDiscount(new BigDecimal("2000")), "2000.00");
        System.out.println("PASS: Strategy switches percentage to fixed amount and caps the discount.");
    }

    private static void expect(BigDecimal actual, String expected) {
        if (actual.compareTo(new BigDecimal(expected)) != 0) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
}
