package com.dinevista.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatusHistoryRecord implements Serializable {
    private final String status;
    private final String note;
    private final String changedBy;
    private final LocalDateTime changedAt;

    public StatusHistoryRecord(String status, String note, String changedBy, LocalDateTime changedAt) {
        this.status = status;
        this.note = note == null ? "" : note;
        this.changedBy = changedBy == null ? "System" : changedBy;
        this.changedAt = changedAt == null ? LocalDateTime.now() : changedAt;
    }

    public String getStatus() { return status; }
    public String getNote() { return note; }
    public String getChangedBy() { return changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }

    public String getChangedAtDisplay() {
        return changedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }
}
