package com.dinevista.model;

import java.io.Serializable;

/**
 * A menu category owned by the Menu Management module.
 * Mirrors the {@code menu_category} table in database/schema.sql.
 */
public class MenuCategoryRecord implements Serializable {
    private final long id;
    private String name;
    private String description;
    private int displayOrder;
    private boolean active;

    public MenuCategoryRecord(long id, String name, String description,
                               int displayOrder, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getStatusLabel() {
        return active ? "Active" : "Hidden";
    }

    public String getStatusCss() {
        return active ? "confirmed" : "pending";
    }
}
