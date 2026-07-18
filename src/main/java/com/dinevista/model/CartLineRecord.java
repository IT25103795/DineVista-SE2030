package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartLineRecord implements Serializable {
    private final MenuItemRecord menuItem;
    private final int quantity;

    public CartLineRecord(MenuItemRecord menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItemRecord getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() {
        return menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
    public String getLineTotalDisplay() {
        return "LKR " + String.format("%,.0f", getLineTotal());
    }
}
