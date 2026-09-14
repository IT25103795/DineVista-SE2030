package com.dinevista.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A quantity of one {@link EventResourceRecord} reserved for one event on one date,
 * owned by the Event Resource and Staff Scheduling module
 * (Wijesuriya W. A. T. D. / IT25103799). Mirrors the {@code resource_booking} table
 * in database/schema.sql. Status is one of REQUESTED, ALLOCATED, RETURNED, CANCELLED.
 */
public class ResourceBookingRecord implements Serializable {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final long id;
    private final long resourceId;
    private final String resourceName;
    private final String eventLabel;
    private final LocalDate eventDate;
    private final int quantityReserved;
    private String status;
    private final String requestedByName;
    private final String notes;
    private final LocalDateTime createdAt;

    public ResourceBookingRecord(long id, long resourceId, String resourceName, String eventLabel,
                                  LocalDate eventDate, int quantityReserved, String status,
                                  String requestedByName, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.eventLabel = eventLabel;
        this.eventDate = eventDate;
        this.quantityReserved = quantityReserved;
        this.status = status == null ? "REQUESTED" : status;
        this.requestedByName = requestedByName;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getResourceId() { return resourceId; }
    public String getResourceName() { return resourceName; }
    public String getEventLabel() { return eventLabel; }
    public LocalDate getEventDate() { return eventDate; }
    public int getQuantityReserved() { return quantityReserved; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRequestedByName() { return requestedByName; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** Counts toward capacity while requested or allocated; excludes returned/cancelled. */
    public boolean holdsCapacity() {
        return "REQUESTED".equals(status) || "ALLOCATED".equals(status);
    }

    public String getEventDateDisplay() { return eventDate == null ? "—" : eventDate.format(DATE_FORMAT); }
    public String getCreatedAtDisplay() { return createdAt == null ? "—" : createdAt.format(DATETIME_FORMAT); }

    public String getStatusCss() {
        switch (status) {
            case "ALLOCATED": return "confirmed";
            case "RETURNED": return "processing";
            case "CANCELLED": return "cancelled";
            default: return "pending";
        }
    }
}
