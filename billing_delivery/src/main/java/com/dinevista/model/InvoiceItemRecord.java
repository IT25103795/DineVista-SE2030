package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A single billable line on an {@link InvoiceRecord}, owned by the Billing,
 * Promotions & Discounts module (Nawarathna N. M. I. N. / IT25103797).
 * Mirrors the {@code invoice_item} table in database/schema.sql.
 */
public class InvoiceItemRecord implements Serializable {
    private final long id;
    private final String description;
    private final BigDecimal quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal lineTotal;

    public InvoiceItemRecord(long id, String description, BigDecimal quantity, BigDecimal unitPrice) {
        this.id = id;
        this.description = description;
        this.quantity = (quantity == null ? BigDecimal.ONE : quantity).setScale(2, RoundingMode.HALF_UP);
        this.unitPrice = (unitPrice == null ? BigDecimal.ZERO : unitPrice).setScale(2, RoundingMode.HALF_UP);
        this.lineTotal = this.quantity.multiply(this.unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    public long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }

    public String getQuantityDisplay() { return quantity.stripTrailingZeros().toPlainString(); }
    public String getUnitPriceDisplay() { return "LKR " + String.format("%,.2f", unitPrice); }
    public String getLineTotalDisplay() { return "LKR " + String.format("%,.2f", lineTotal); }
}
