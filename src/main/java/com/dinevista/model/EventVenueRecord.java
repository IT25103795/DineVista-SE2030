package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A bookable event space owned by the Event Resource and Staff Scheduling module
 * (Wijesuriya W. A. T. D. / IT25103799). Mirrors the {@code event_venue} table in
 * database/schema.sql. This table is also read (by name) by the Event Package and
 * Booking Management module's estimate calculation, so its column shape is kept intact.
 */
public class EventVenueRecord implements Serializable {
    private final long id;
    private String name;
    private String venueType;
    private int capacity;
    private BigDecimal baseFee;
    private String description;
    private String availabilityStatus;

    public EventVenueRecord(long id, String name, String venueType, int capacity,
                             BigDecimal baseFee, String description, String availabilityStatus) {
        this.id = id;
        this.name = name;
        this.venueType = venueType;
        this.capacity = capacity;
        this.baseFee = baseFee == null ? BigDecimal.ZERO : baseFee.setScale(2, RoundingMode.HALF_UP);
        this.description = description;
        this.availabilityStatus = availabilityStatus == null ? "AVAILABLE" : availabilityStatus;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVenueType() { return venueType; }
    public void setVenueType(String venueType) { this.venueType = venueType; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public BigDecimal getBaseFee() { return baseFee; }
    public void setBaseFee(BigDecimal baseFee) {
        this.baseFee = baseFee == null ? BigDecimal.ZERO : baseFee.setScale(2, RoundingMode.HALF_UP);
    }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public boolean isBookable() {
        return "AVAILABLE".equals(availabilityStatus);
    }

    public String getVenueTypeLabel() {
        return venueType == null ? "—" : venueType.replace('_', ' ');
    }

    public String getBaseFeeDisplay() {
        return "LKR " + baseFee.toPlainString();
    }

    public String getStatusCss() {
        switch (availabilityStatus) {
            case "AVAILABLE": return "confirmed";
            case "MAINTENANCE": return "pending";
            default: return "cancelled";
        }
    }
}
