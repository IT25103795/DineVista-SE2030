package com.dinevista.model;

import java.io.Serializable;

public class EventBookingRecord implements Serializable {
    private final String reference;
    private final String customerName;
    private final String email;
    private final String phone;
    private final String eventType;
    private final String packageName;
    private final String venue;
    private final String eventDate;
    private final int guestCount;
    private final String status;

    public EventBookingRecord(String reference, String customerName, String email, String phone,
                              String eventType, String packageName, String venue,
                              String eventDate, int guestCount, String status) {
        this.reference = reference;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.eventType = eventType;
        this.packageName = packageName;
        this.venue = venue;
        this.eventDate = eventDate;
        this.guestCount = guestCount;
        this.status = status;
    }

    public String getReference() { return reference; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getEventType() { return eventType; }
    public String getPackageName() { return packageName; }
    public String getVenue() { return venue; }
    public String getEventDate() { return eventDate; }
    public int getGuestCount() { return guestCount; }
    public String getStatus() { return status; }
}
