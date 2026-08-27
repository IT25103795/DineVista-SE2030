package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A single stock-controlled ingredient owned by the Inventory Management module.
 * Mirrors the {@code ingredient} table in database/schema.sql.
 */
public class IngredientRecord implements Serializable {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final long id;
    private String name;
    private String unit;
    private BigDecimal currentQuantity;
    private BigDecimal reorderLevel;
    private BigDecimal unitCost;
    private String supplierName;
    private LocalDateTime lastUpdated;

    public IngredientRecord(long id, String name, String unit, BigDecimal currentQuantity,
                             BigDecimal reorderLevel, BigDecimal unitCost, String supplierName,
                             LocalDateTime lastUpdated) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.currentQuantity = scale(currentQuantity);
        this.reorderLevel = scale(reorderLevel);
        this.unitCost = unitCost == null ? null : unitCost.setScale(2, RoundingMode.HALF_UP);
        this.supplierName = supplierName;
        this.lastUpdated = lastUpdated;
    }

    private static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(3, RoundingMode.HALF_UP);
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(BigDecimal currentQuantity) { this.currentQuantity = scale(currentQuantity); }
    public BigDecimal getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = scale(reorderLevel); }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost == null ? null : unitCost.setScale(2, RoundingMode.HALF_UP);
    }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isLowStock() {
        return currentQuantity.compareTo(reorderLevel) <= 0;
    }

    public String getQuantityDisplay() {
        return currentQuantity.stripTrailingZeros().toPlainString() + " " + unit;
    }

    public String getReorderLevelDisplay() {
        return reorderLevel.stripTrailingZeros().toPlainString() + " " + unit;
    }

    public String getUnitCostDisplay() {
        return unitCost == null ? "—" : "LKR " + unitCost.toPlainString();
    }

    public String getLastUpdatedDisplay() {
        return lastUpdated == null ? "—" : lastUpdated.format(DISPLAY_FORMAT);
    }

    public String getStockStatusLabel() {
        return isLowStock() ? "Low stock" : "In stock";
    }

    public String getStockStatusCss() {
        return isLowStock() ? "pending" : "confirmed";
    }
}
