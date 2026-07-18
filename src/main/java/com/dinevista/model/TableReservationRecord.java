package com.dinevista.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableReservationRecord implements Serializable {
    private final long id;
    private final String reference;
    private final String customerKey;
    private String guestName;
    private String email;
    private String phone;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private int partySize;
    private String seatingPreference;
    private String occasionNotes;
    private String status;
    private Long tableId;
    private String tableCode;
    private String staffNote;
    private String cancellationReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<StatusHistoryRecord> history = new ArrayList<>();

    public TableReservationRecord(long id, String reference, String customerKey,
                                  String guestName, String email, String phone,
                                  LocalDate reservationDate, LocalTime reservationTime,
                                  int partySize, String seatingPreference, String occasionNotes) {
        this.id = id;
        this.reference = reference;
        this.customerKey = customerKey;
        this.guestName = guestName;
        this.email = email;
        this.phone = phone;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.seatingPreference = seatingPreference;
        this.occasionNotes = occasionNotes;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
        addHistory("PENDING", "Reservation request created.", guestName);
    }


    public TableReservationRecord(long id, String reference, String customerKey,
                                  String guestName, String email, String phone,
                                  LocalDate reservationDate, LocalTime reservationTime,
                                  int partySize, String seatingPreference, String occasionNotes,
                                  String status, Long tableId, String tableCode,
                                  String staffNote, String cancellationReason,
                                  LocalDateTime createdAt, LocalDateTime updatedAt,
                                  List<StatusHistoryRecord> restoredHistory) {
        this.id = id;
        this.reference = reference;
        this.customerKey = customerKey;
        this.guestName = guestName;
        this.email = email;
        this.phone = phone;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.seatingPreference = seatingPreference;
        this.occasionNotes = occasionNotes;
        this.status = status;
        this.tableId = tableId;
        this.tableCode = tableCode;
        this.staffNote = staffNote == null ? "" : staffNote;
        this.cancellationReason = cancellationReason == null ? "" : cancellationReason;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        if (restoredHistory != null) this.history.addAll(restoredHistory);
    }

    public long getId() { return id; }
    public String getReference() { return reference; }
    public String getCustomerKey() { return customerKey; }
    public String getGuestName() { return guestName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getReservationDate() { return reservationDate; }
    public LocalTime getReservationTime() { return reservationTime; }
    public int getPartySize() { return partySize; }
    public String getSeatingPreference() { return seatingPreference; }
    public String getOccasionNotes() { return occasionNotes; }
    public String getStatus() { return status; }
    public Long getTableId() { return tableId; }
    public String getTableCode() { return tableCode; }
    public String getStaffNote() { return staffNote; }
    public String getCancellationReason() { return cancellationReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<StatusHistoryRecord> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void updateCustomerDetails(String guestName, String email, String phone,
                                      LocalDate reservationDate, LocalTime reservationTime,
                                      int partySize, String seatingPreference, String occasionNotes) {
        this.guestName = guestName;
        this.email = email;
        this.phone = phone;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.seatingPreference = seatingPreference;
        this.occasionNotes = occasionNotes;
        touch();
        addHistory(status, "Customer updated reservation details.", guestName);
    }

    public void assignTable(Long tableId, String tableCode, String changedBy) {
        this.tableId = tableId;
        this.tableCode = tableCode;
        touch();
        addHistory(status, "Assigned table " + tableCode + ".", changedBy);
    }

    public void changeStatus(String newStatus, String note, String changedBy) {
        this.status = newStatus;
        this.staffNote = note == null ? "" : note;
        if ("CANCELLED".equals(newStatus) || "REJECTED".equals(newStatus)) {
            this.cancellationReason = this.staffNote;
        }
        touch();
        addHistory(newStatus, note, changedBy);
    }

    private void addHistory(String status, String note, String changedBy) {
        history.add(0, new StatusHistoryRecord(status, note, changedBy, LocalDateTime.now()));
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
    }

    public String getDateDisplay() {
        return reservationDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    public String getTimeDisplay() {
        return reservationTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public String getDateInputValue() {
        return reservationDate.toString();
    }

    public String getTimeInputValue() {
        return reservationTime.toString();
    }

    public String getSeatingPreferenceDisplay() {
        return seatingPreference == null ? "Any available area" : seatingPreference.replace('_', ' ');
    }

    public String getStatusCss() {
        String value = status == null ? "" : status.toLowerCase();
        if ("confirmed".equals(value) || "completed".equals(value) || "seated".equals(value)) return "confirmed";
        if ("cancelled".equals(value) || "rejected".equals(value) || "no_show".equals(value)) return "cancelled";
        if ("pending".equals(value)) return "pending";
        return "processing";
    }
}
