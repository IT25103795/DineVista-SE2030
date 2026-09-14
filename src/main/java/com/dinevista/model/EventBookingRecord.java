package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class EventBookingRecord implements Serializable {
    private final long id;
    private final String reference;
    private final long customerId;
    private final long packageId;
    private final long venueId;
    private final String customerName;
    private final String email;
    private final String phone;
    private final String eventType;
    private final String packageName;
    private final String venue;
    private final String eventDate;
    private final String eventTime;
    private final int guestCount;
    private final BigDecimal totalAmount;
    private final String status;
    private final String notes;

    public EventBookingRecord(long id, String reference, long customerId, long packageId, long venueId,
                              String customerName, String email, String phone, String eventType,
                              String packageName, String venue, String eventDate, String eventTime,
                              int guestCount, BigDecimal totalAmount, String status, String notes) {
        this.id = id;
        this.reference = reference;
        this.customerId = customerId;
        this.packageId = packageId;
        this.venueId = venueId;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.eventType = eventType;
        this.packageName = packageName;
        this.venue = venue;
        this.eventDate = eventDate;
        this.eventTime = eventTime == null ? "" : eventTime;
        this.guestCount = guestCount;
        this.totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        this.status = status;
        this.notes = notes == null ? "" : notes;
    }

    public EventBookingRecord(String reference, String customerName, String email, String phone,
                              String eventType, String packageName, String venue,
                              String eventDate, int guestCount, String status, String notes) {
        this(0, reference, 0, 0, 0, customerName, email, phone, eventType, packageName, venue, eventDate, "12:00:00", guestCount, BigDecimal.ZERO, status, notes);
    }

    public long getId() { return id; }
    public String getReference() { return reference; }
    public long getCustomerId() { return customerId; }
    public long getPackageId() { return packageId; }
    public long getVenueId() { return venueId; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getEventType() { return eventType; }
    public String getPackageName() { return packageName; }
    public String getVenue() { return venue; }
    public String getEventDate() { return eventDate; }
    public String getEventTime() { return eventTime; }
    public int getGuestCount() { return guestCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
}
