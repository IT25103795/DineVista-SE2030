package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * An auditable stock movement against one ingredient.
 * Mirrors the {@code stock_transaction} table in database/schema.sql.
 * transactionType is one of PURCHASE, USAGE, ADJUSTMENT, WASTE, RETURN.
 */
public class StockTransactionRecord implements Serializable {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final long id;
    private final long ingredientId;
    private final String ingredientName;
    private final String transactionType;
    private final BigDecimal quantity;
    private final String referenceNote;
    private final String performedBy;
    private final LocalDateTime transactionTime;

    public StockTransactionRecord(long id, long ingredientId, String ingredientName,
                                   String transactionType, BigDecimal quantity,
                                   String referenceNote, String performedBy,
                                   LocalDateTime transactionTime) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.referenceNote = referenceNote;
        this.performedBy = performedBy;
        this.transactionTime = transactionTime;
    }

    public long getId() { return id; }
    public long getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public String getTransactionType() { return transactionType; }
    public BigDecimal getQuantity() { return quantity; }
    public String getReferenceNote() { return referenceNote; }
    public String getPerformedBy() { return performedBy; }
    public LocalDateTime getTransactionTime() { return transactionTime; }

    /** True when the movement increases stock on hand (PURCHASE, RETURN). */
    public boolean isInbound() {
        return "PURCHASE".equals(transactionType) || "RETURN".equals(transactionType);
    }

    public String getQuantityDisplay() {
        String sign = isInbound() ? "+" : "-";
        if ("ADJUSTMENT".equals(transactionType)) sign = quantity.signum() >= 0 ? "+" : "";
        return sign + quantity.abs().stripTrailingZeros().toPlainString();
    }

    public String getTransactionTimeDisplay() {
        return transactionTime == null ? "—" : transactionTime.format(DISPLAY_FORMAT);
    }

    public String getTypeCss() {
        switch (transactionType) {
            case "PURCHASE":
            case "RETURN":
                return "confirmed";
            case "WASTE":
                return "cancelled";
            case "ADJUSTMENT":
                return "pending";
            default:
                return "processing";
        }
    }
}
