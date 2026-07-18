package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderItemRecord implements Serializable {
    private final long menuItemId;
    private final String itemName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final String itemNotes;

    public OrderItemRecord(long menuItemId, String itemName, int quantity,
                           BigDecimal unitPrice, String itemNotes) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.itemNotes = itemNotes == null ? "" : itemNotes;
    }

    public long getMenuItemId() { return menuItemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getItemNotes() { return itemNotes; }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public String getUnitPriceDisplay() {
        return "LKR " + String.format("%,.0f", unitPrice);
    }

    public String getLineTotalDisplay() {
        return "LKR " + String.format("%,.0f", getLineTotal());
    }
}
