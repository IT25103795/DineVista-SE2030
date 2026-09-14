package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class EventPackageRecord implements Serializable {
    private final long id;
    private final String name;
    private final String category;
    private final String description;
    private final BigDecimal pricePerGuest;
    private final int minimumGuests;
    private final int maximumGuests;
    private final int durationMinutes;
    private final String inclusions;
    private final boolean active;

    public EventPackageRecord(long id, String name, String category, String description,
                              BigDecimal pricePerGuest, int minimumGuests, int maximumGuests,
                              int durationMinutes, String inclusions, boolean active) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description == null ? "" : description;
        this.pricePerGuest = pricePerGuest;
        this.minimumGuests = minimumGuests;
        this.maximumGuests = maximumGuests;
        this.durationMinutes = durationMinutes;
        this.inclusions = inclusions == null ? "" : inclusions;
        this.active = active;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public BigDecimal getPricePerGuest() { return pricePerGuest; }
    public int getMinimumGuests() { return minimumGuests; }
    public int getMaximumGuests() { return maximumGuests; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getInclusions() { return inclusions; }
    public boolean isActive() { return active; }
}
