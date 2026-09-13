package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A promotion / discount code owned by the Billing, Promotions & Discounts
 * module (Nawarathna N. M. I. N. / IT25103797). Mirrors the {@code promotion}
 * table in database/schema.sql.
 *
 * discountType is one of PERCENTAGE, FIXED_AMOUNT (BR-PRO-01).
 */
public class PromotionRecord implements Serializable {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final long id;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumSpend;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer usageLimit;
    private boolean active;

    public PromotionRecord(long id, String code, String name, String discountType,
                            BigDecimal discountValue, BigDecimal minimumSpend,
                            LocalDate startDate, LocalDate endDate, Integer usageLimit, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.discountType = discountType;
        this.discountValue = (discountValue == null ? BigDecimal.ZERO : discountValue).setScale(2, RoundingMode.HALF_UP);
        this.minimumSpend = (minimumSpend == null ? BigDecimal.ZERO : minimumSpend).setScale(2, RoundingMode.HALF_UP);
        this.startDate = startDate;
        this.endDate = endDate;
        this.usageLimit = usageLimit;
        this.active = active;
    }

    public long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = (discountValue == null ? BigDecimal.ZERO : discountValue).setScale(2, RoundingMode.HALF_UP);
    }
    public BigDecimal getMinimumSpend() { return minimumSpend; }
    public void setMinimumSpend(BigDecimal minimumSpend) {
        this.minimumSpend = (minimumSpend == null ? BigDecimal.ZERO : minimumSpend).setScale(2, RoundingMode.HALF_UP);
    }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isPercentage() { return "PERCENTAGE".equals(discountType); }

    /** True when the promotion is enabled and today falls within its validity window (BR-PRO-01). */
    public boolean isCurrentlyValid() {
        LocalDate today = LocalDate.now();
        return active
                && (startDate == null || !today.isBefore(startDate))
                && (endDate == null || !today.isAfter(endDate));
    }

    /** Computes the discount for a given subtotal, capped so it never exceeds the subtotal. */
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        BigDecimal raw = isPercentage()
                ? subtotal.multiply(discountValue).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : discountValue;
        if (raw.compareTo(subtotal) > 0) raw = subtotal;
        if (raw.compareTo(BigDecimal.ZERO) < 0) raw = BigDecimal.ZERO;
        return raw.setScale(2, RoundingMode.HALF_UP);
    }

    public String getDiscountValueDisplay() {
        return isPercentage()
                ? discountValue.stripTrailingZeros().toPlainString() + "%"
                : "LKR " + String.format("%,.2f", discountValue);
    }

    public String getMinimumSpendDisplay() {
        return minimumSpend.compareTo(BigDecimal.ZERO) <= 0
                ? "No minimum" : "LKR " + String.format("%,.2f", minimumSpend);
    }

    public String getStartDateDisplay() { return startDate == null ? "—" : startDate.format(DATE_FORMAT); }
    public String getEndDateDisplay() { return endDate == null ? "—" : endDate.format(DATE_FORMAT); }
    public String getUsageLimitDisplay() { return usageLimit == null ? "Unlimited" : usageLimit.toString(); }

    public String getStatusLabel() {
        if (!active) return "Inactive";
        return isCurrentlyValid() ? "Active" : "Expired";
    }

    public String getStatusCss() {
        if (!active) return "cancelled";
        return isCurrentlyValid() ? "confirmed" : "pending";
    }
}
