package com.dinevista.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Immutable priced requirement snapshot for one version of an event quote. */
public final class EventQuoteRecord {
    private final long id;
    private final String bookingReference;
    private final int version;
    private final long packageId;
    private final long venueId;
    private final int guestCount;
    private final String requirements;
    private final BigDecimal pricePerGuest;
    private final BigDecimal venueFee;
    private final BigDecimal total;
    private final LocalDateTime issuedAt;
    private final LocalDateTime acceptedAt;

    public EventQuoteRecord(long id, String bookingReference, int version, long packageId, long venueId,
                            int guestCount, String requirements, BigDecimal pricePerGuest,
                            BigDecimal venueFee, BigDecimal total, LocalDateTime issuedAt,
                            LocalDateTime acceptedAt) {
        this.id = id;
        this.bookingReference = bookingReference;
        this.version = version;
        this.packageId = packageId;
        this.venueId = venueId;
        this.guestCount = guestCount;
        this.requirements = requirements == null ? "" : requirements;
        this.pricePerGuest = pricePerGuest;
        this.venueFee = venueFee;
        this.total = total;
        this.issuedAt = issuedAt;
        this.acceptedAt = acceptedAt;
    }

    public long getId() { return id; }
    public String getBookingReference() { return bookingReference; }
    public int getVersion() { return version; }
    public long getPackageId() { return packageId; }
    public long getVenueId() { return venueId; }
    public int getGuestCount() { return guestCount; }
    public String getRequirements() { return requirements; }
    public BigDecimal getPricePerGuest() { return pricePerGuest; }
    public BigDecimal getVenueFee() { return venueFee; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public boolean isAccepted() { return acceptedAt != null; }
}
