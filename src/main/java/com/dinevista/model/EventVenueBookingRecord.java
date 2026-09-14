package com.dinevista.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A venue reserved for one event on one date, owned by the Event Resource and Staff
 * Scheduling module (Wijesuriya W. A. T. D. / IT25103799). Mirrors the
 * {@code event_venue_booking} table in database/schema.sql. Status is one of
 * REQUESTED, CONFIRMED, CANCELLED.
 */
public class EventVenueBookingRecord implements Serializable {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final long id;
    private final long venueId;
    private final String venueName;
    private final String eventLabel;
    private final LocalDate eventDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Integer guestCount;
    private String status;
    private final String notes;
    private final String createdByName;
    private final LocalDateTime createdAt;

    public EventVenueBookingRecord(long id, long venueId, String venueName, String eventLabel,
                                    LocalDate eventDate, LocalTime startTime, LocalTime endTime,
                                    Integer guestCount, String status, String notes,
                                    String createdByName, LocalDateTime createdAt) {
        this.id = id;
        this.venueId = venueId;
        this.venueName = venueName;
        this.eventLabel = eventLabel;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.guestCount = guestCount;
        this.status = status == null ? "REQUESTED" : status;
        this.notes = notes;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getVenueId() { return venueId; }
    public String getVenueName() { return venueName; }
    public String getEventLabel() { return eventLabel; }
    public LocalDate getEventDate() { return eventDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Integer getGuestCount() { return guestCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public String getCreatedByName() { return createdByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    public boolean isActive() {
        return !"CANCELLED".equals(status);
    }

    public String getEventDateDisplay() { return eventDate == null ? "—" : eventDate.format(DATE_FORMAT); }
    public String getStartTimeDisplay() { return startTime == null ? "—" : startTime.format(TIME_FORMAT); }
    public String getEndTimeDisplay() { return endTime == null ? "—" : endTime.format(TIME_FORMAT); }
    public String getTimeRangeDisplay() { return getStartTimeDisplay() + " – " + getEndTimeDisplay(); }
    public String getCreatedAtDisplay() { return createdAt == null ? "—" : createdAt.format(DATETIME_FORMAT); }

    public String getStatusCss() {
        switch (status) {
            case "CONFIRMED": return "confirmed";
            case "CANCELLED": return "cancelled";
            default: return "pending";
        }
    }
}
