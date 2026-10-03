package com.dinevista.repository;

import com.dinevista.model.EventBookingRecord;
import com.dinevista.model.EventVenueRecord;
import com.dinevista.model.EventQuoteRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface EventBookingRepository {
    EventBookingRecord save(EventBookingRecord booking);
    EventBookingRecord update(EventBookingRecord booking);
    boolean cancel(String reference, String note);
    boolean delete(String reference);
    Optional<EventBookingRecord> findByReference(String reference);
    List<EventBookingRecord> findForCustomer(long customerId, String email);
    List<EventBookingRecord> findAll(String search);
    boolean hasConflict(long packageId,long venueId,LocalDate date,LocalTime time,int durationMinutes,String excludingReference);
    List<EventVenueRecord> findVenues();
    Long customerIdForUser(long userId);
    void addStatusHistory(String reference, String status, String note);
    List<EventQuoteRecord> findQuotes(String reference);
    EventQuoteRecord issueQuote(String reference, long packageId, long venueId, int guests,
                                String requirements, BigDecimal pricePerGuest,
                                BigDecimal venueFee, BigDecimal total);
    boolean acceptQuote(String reference, long quoteId, long customerUserId);
    long nextId();
}
