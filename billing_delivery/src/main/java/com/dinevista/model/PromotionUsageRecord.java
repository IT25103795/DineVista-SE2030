package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * An auditable record that one promotion was applied to one invoice, owned by
 * the Billing, Promotions & Discounts module (Nawarathna N. M. I. N. / IT25103797).
 * Mirrors the {@code promotion_usage} table in database/schema.sql and enforces
 * FR22 / BR-PRO-01: discounts are only ever applied through this recorded path,
 * which keeps promotion usage limits enforceable and auditable.
 */
public class PromotionUsageRecord implements Serializable {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final long id;
    private final long promotionId;
    private final String promotionCode;
    private final long invoiceId;
    private final String customerKey;
    private final BigDecimal discountApplied;
    private final LocalDateTime usedAt;

    public PromotionUsageRecord(long id, long promotionId, String promotionCode, long invoiceId,
                                 String customerKey, BigDecimal discountApplied, LocalDateTime usedAt) {
        this.id = id;
        this.promotionId = promotionId;
        this.promotionCode = promotionCode;
        this.invoiceId = invoiceId;
        this.customerKey = customerKey == null ? "" : customerKey;
        this.discountApplied = (discountApplied == null ? BigDecimal.ZERO : discountApplied)
                .setScale(2, RoundingMode.HALF_UP);
        this.usedAt = usedAt == null ? LocalDateTime.now() : usedAt;
    }

    public long getId() { return id; }
    public long getPromotionId() { return promotionId; }
    public String getPromotionCode() { return promotionCode; }
    public long getInvoiceId() { return invoiceId; }
    public String getCustomerKey() { return customerKey; }
    public BigDecimal getDiscountApplied() { return discountApplied; }
    public LocalDateTime getUsedAt() { return usedAt; }

    public String getDiscountAppliedDisplay() { return "LKR " + String.format("%,.2f", discountApplied); }
    public String getUsedAtDisplay() { return usedAt.format(DISPLAY_FORMAT); }
}
