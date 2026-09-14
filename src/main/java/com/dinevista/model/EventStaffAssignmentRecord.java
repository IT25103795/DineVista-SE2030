package com.dinevista.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * One staff member assigned to work one event in a given role, owned by the Event
 * Resource and Staff Scheduling module (Wijesuriya W. A. T. D. / IT25103799). Mirrors
 * the {@code event_staff_assignment} table in database/schema.sql. Status is one of
 * ASSIGNED, CONFIRMED, COMPLETED, CANCELLED.
 */
public class EventStaffAssignmentRecord implements Serializable {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final long id;
    private final long staffId;
    private final String staffName;
    private final String eventLabel;
    private final String assignmentRole;
    private final LocalDate assignmentDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private String status;
    private final String notes;
    private final LocalDateTime createdAt;

    public EventStaffAssignmentRecord(long id, long staffId, String staffName, String eventLabel,
                                       String assignmentRole, LocalDate assignmentDate,
                                       LocalTime startTime, LocalTime endTime, String status,
                                       String notes, LocalDateTime createdAt) {
        this.id = id;
        this.staffId = staffId;
        this.staffName = staffName;
        this.eventLabel = eventLabel;
        this.assignmentRole = assignmentRole;
        this.assignmentDate = assignmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status == null ? "ASSIGNED" : status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public long getStaffId() { return staffId; }
    public String getStaffName() { return staffName; }
    public String getEventLabel() { return eventLabel; }
    public String getAssignmentRole() { return assignmentRole; }
    public LocalDate getAssignmentDate() { return assignmentDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    public boolean isActive() {
        return !"CANCELLED".equals(status);
    }

    public String getAssignmentDateDisplay() { return assignmentDate == null ? "—" : assignmentDate.format(DATE_FORMAT); }
    public String getStartTimeDisplay() { return startTime == null ? "—" : startTime.format(TIME_FORMAT); }
    public String getEndTimeDisplay() { return endTime == null ? "—" : endTime.format(TIME_FORMAT); }
    public String getTimeRangeDisplay() { return getStartTimeDisplay() + " – " + getEndTimeDisplay(); }
    public String getCreatedAtDisplay() { return createdAt == null ? "—" : createdAt.format(DATETIME_FORMAT); }

    public String getStatusCss() {
        switch (status) {
            case "CONFIRMED":
            case "COMPLETED":
                return "confirmed";
            case "CANCELLED":
                return "cancelled";
            default:
                return "pending";
        }
    }
}
