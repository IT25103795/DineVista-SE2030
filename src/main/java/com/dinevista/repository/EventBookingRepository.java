package com.dinevista.repository;

import com.dinevista.model.EventBookingRecord;
import com.dinevista.model.EventVenueRecord;
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
    long nextId();
}
