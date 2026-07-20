package com.dinevista.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationRecord {
    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("dd MMM, hh:mm a");

    private final long id;
    private final String recipientKey;
    private final String recipientRole;
    private final String type;
    private final String title;
    private final String message;
    private final String referenceType;
    private final String referenceCode;
    private final String actionPath;
    private final boolean read;
    private final LocalDateTime createdAt;

    public NotificationRecord(long id, String recipientKey, String recipientRole,
                              String type, String title, String message,
                              String referenceType, String referenceCode,
                              String actionPath, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.recipientKey = recipientKey;
        this.recipientRole = recipientRole;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceCode = referenceCode;
        this.actionPath = actionPath;
        this.read = read;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public NotificationRecord withId(long notificationId) {
        return new NotificationRecord(notificationId, recipientKey, recipientRole, type,
                title, message, referenceType, referenceCode, actionPath, read, createdAt);
    }

    public NotificationRecord asRead() {
        if (read) return this;
        return new NotificationRecord(id, recipientKey, recipientRole, type,
                title, message, referenceType, referenceCode, actionPath, true, createdAt);
    }

    public long getId() { return id; }
    public String getRecipientKey() { return recipientKey; }
    public String getRecipientRole() { return recipientRole; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getReferenceType() { return referenceType; }
    public String getReferenceCode() { return referenceCode; }
    public String getActionPath() { return actionPath; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedAtDisplay() { return createdAt.format(DISPLAY_TIME); }
}
