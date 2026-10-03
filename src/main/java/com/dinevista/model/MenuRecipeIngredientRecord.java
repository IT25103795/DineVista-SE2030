package com.dinevista.model;

import java.math.BigDecimal;

/** Ingredient amount required for one portion of a menu item. */
public final class MenuRecipeIngredientRecord {
    private final long ingredientId;
    private final String ingredientName;
    private final String unit;
    private final BigDecimal quantityRequired;
    private final BigDecimal currentQuantity;

    public MenuRecipeIngredientRecord(long ingredientId, String ingredientName, String unit,
                                      BigDecimal quantityRequired, BigDecimal currentQuantity) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.quantityRequired = quantityRequired;
        this.currentQuantity = currentQuantity;
    }

    public long getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public String getUnit() { return unit; }
    public BigDecimal getQuantityRequired() { return quantityRequired; }
    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public boolean isInStock() { return currentQuantity.compareTo(quantityRequired) >= 0; }
}
