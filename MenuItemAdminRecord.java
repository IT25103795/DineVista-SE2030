package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Full admin-facing view of a menu item, owned by the Menu Management module.
 * Mirrors the {@code menu_item} table in database/schema.sql, including fields
 * (category id, preparation time, the three-state availability status) that the
 * customer-facing {@link MenuItemRecord} intentionally does not expose.
 *
 * This is a separate class from {@link MenuItemRecord} on purpose: that record is
 * shared with the Reservation/Order module (cart, checkout, order history) and is
 * left completely untouched here to avoid merge conflicts. Both classes read and
 * write the same {@code menu_item} table, so admin changes made through this
 * record are immediately visible on the public menu.
 */
public class MenuItemAdminRecord implements Serializable {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final long id;
    private long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private String imagePath;
    private int preparationMinutes;
    private String dietaryType;
    private String spiceLevel;
    private String availabilityStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MenuItemAdminRecord(long id, long categoryId, String categoryName, String name,
                                String description, BigDecimal price, String imagePath,
                                int preparationMinutes, String dietaryType, String spiceLevel,
                                String availabilityStatus, LocalDateTime createdAt,
                                LocalDateTime updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.price = price == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : price.setScale(2, RoundingMode.HALF_UP);
        this.imagePath = imagePath;
        this.preparationMinutes = preparationMinutes;
        this.dietaryType = dietaryType;
        this.spiceLevel = spiceLevel;
        this.availabilityStatus = availabilityStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price.setScale(2, RoundingMode.HALF_UP); }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public int getPreparationMinutes() { return preparationMinutes; }
    public void setPreparationMinutes(int preparationMinutes) { this.preparationMinutes = preparationMinutes; }
    public String getDietaryType() { return dietaryType; }
    public void setDietaryType(String dietaryType) { this.dietaryType = dietaryType; }
    public String getSpiceLevel() { return spiceLevel; }
    public void setSpiceLevel(String spiceLevel) { this.spiceLevel = spiceLevel; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isAvailable() {
        return "AVAILABLE".equals(availabilityStatus);
    }

    public String getPriceDisplay() {
        return "LKR " + String.format("%,.0f", price);
    }

    public String getStatusLabel() {
        switch (availabilityStatus) {
            case "AVAILABLE": return "Available";
            case "SOLD_OUT": return "Sold out today";
            default: return "Unavailable";
        }
    }

    public String getStatusCss() {
        switch (availabilityStatus) {
            case "AVAILABLE": return "confirmed";
            case "SOLD_OUT": return "pending";
            default: return "cancelled";
        }
    }

    public String getUpdatedAtDisplay() {
        return updatedAt == null ? "—" : updatedAt.format(DISPLAY_FORMAT);
    }
}
