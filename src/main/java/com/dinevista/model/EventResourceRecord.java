package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A pool of shared equipment (furniture, audio-visual gear, decor, etc.) owned by the
 * Event Resource and Staff Scheduling module (Wijesuriya W. A. T. D. / IT25103799).
 * Mirrors the {@code event_resource} table in database/schema.sql.
 *
 * {@code availableQuantity} tracks how many of {@code totalQuantity} units are currently
 * usable (that is, not out for maintenance or retired). Day-by-day capacity for a
 * specific event date is computed separately by summing active {@link ResourceBookingRecord}s.
 */
public class EventResourceRecord implements Serializable {
    private final long id;
    private String name;
    private String category;
    private int totalQuantity;
    private int availableQuantity;
    private BigDecimal unitCost;
    private String status;

    public EventResourceRecord(long id, String name, String category, int totalQuantity,
                                int availableQuantity, BigDecimal unitCost, String status) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.unitCost = unitCost == null ? null : unitCost.setScale(2, RoundingMode.HALF_UP);
        this.status = status == null ? "AVAILABLE" : status;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost == null ? null : unitCost.setScale(2, RoundingMode.HALF_UP);
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isBookable() {
        return "AVAILABLE".equals(status) && availableQuantity > 0;
    }

    public String getCategoryLabel() {
        return category == null ? "—" : category.replace('_', ' ');
    }

    public String getUnitCostDisplay() {
        return unitCost == null ? "—" : "LKR " + unitCost.toPlainString();
    }

    public String getStatusCss() {
        switch (status) {
            case "AVAILABLE": return availableQuantity > 0 ? "confirmed" : "pending";
            case "MAINTENANCE": return "pending";
            default: return "cancelled";
        }
    }
}
