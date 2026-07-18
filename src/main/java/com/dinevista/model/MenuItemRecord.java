package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MenuItemRecord implements Serializable {
    private final long id;
    private final String category;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final String imagePath;
    private final String dietaryType;
    private final String spiceLevel;
    private boolean available;

    public MenuItemRecord(long id, String category, String name, String description,
                          BigDecimal price, String imagePath, String dietaryType,
                          String spiceLevel, boolean available) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.imagePath = imagePath;
        this.dietaryType = dietaryType;
        this.spiceLevel = spiceLevel;
        this.available = available;
    }

    public long getId() { return id; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getImagePath() { return imagePath; }
    public String getDietaryType() { return dietaryType; }
    public String getSpiceLevel() { return spiceLevel; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getPriceDisplay() {
        return "LKR " + String.format("%,.0f", price);
    }
}
