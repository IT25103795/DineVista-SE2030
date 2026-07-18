package com.dinevista.model;

import java.io.Serializable;

public class ReservationRecord implements Serializable {
    private final String reference;
    private final String guestName;
    private final String email;
    private final String phone;
    private final String date;
    private final String time;
    private final int partySize;
    private final String seatingArea;
    private final String occasion;
    private final String status;

    public ReservationRecord(String reference, String guestName, String email, String phone,
                             String date, String time, int partySize, String seatingArea,
                             String occasion, String status) {
        this.reference = reference;
        this.guestName = guestName;
        this.email = email;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.partySize = partySize;
        this.seatingArea = seatingArea;
        this.occasion = occasion;
        this.status = status;
    }

    public String getReference() { return reference; }
    public String getGuestName() { return guestName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getPartySize() { return partySize; }
    public String getSeatingArea() { return seatingArea; }
    public String getOccasion() { return occasion; }
    public String getStatus() { return status; }
}
