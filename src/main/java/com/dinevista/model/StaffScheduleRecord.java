package com.dinevista.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A single operational shift for one staff member, owned by the Event Resource and
 * Staff Scheduling module (Wijesuriya W. A. T. D. / IT25103799). Mirrors the
 * {@code staff_schedule} table in database/schema.sql. Status is one of SCHEDULED,
 * CONFIRMED, COMPLETED, ABSENT, CANCELLED.
 */
public class StaffScheduleRecord implements Serializable {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private final long id;
    private final long staffId;
    private final String staffName;
    private final LocalDate shiftDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String shiftType;
    private String status;
    private final String notes;

    public StaffScheduleRecord(long id, long staffId, String staffName, LocalDate shiftDate,
                                LocalTime startTime, LocalTime endTime, String shiftType,
                                String status, String notes) {
        this.id = id;
        this.staffId = staffId;
        this.staffName = staffName;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftType = shiftType;
        this.status = status == null ? "SCHEDULED" : status;
        this.notes = notes;
    }

    public long getId() { return id; }
    public long getStaffId() { return staffId; }
    public String getStaffName() { return staffName; }
    public LocalDate getShiftDate() { return shiftDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getShiftType() { return shiftType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }

    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        return startTime.isBefore(otherEnd) && otherStart.isBefore(endTime);
    }

    public boolean isActive() {
        return !"CANCELLED".equals(status);
    }

    public String getShiftDateDisplay() { return shiftDate == null ? "—" : shiftDate.format(DATE_FORMAT); }
    public String getStartTimeDisplay() { return startTime == null ? "—" : startTime.format(TIME_FORMAT); }
    public String getEndTimeDisplay() { return endTime == null ? "—" : endTime.format(TIME_FORMAT); }
    public String getTimeRangeDisplay() { return getStartTimeDisplay() + " – " + getEndTimeDisplay(); }
    public String getShiftTypeLabel() { return shiftType == null ? "—" : shiftType.replace('_', ' '); }

    public String getStatusCss() {
        switch (status) {
            case "CONFIRMED":
            case "COMPLETED":
                return "confirmed";
            case "ABSENT":
            case "CANCELLED":
                return "cancelled";
            default:
                return "pending";
        }
    }
}
