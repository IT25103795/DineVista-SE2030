package com.dinevista.repository;

import com.dinevista.model.EventBookingRecord;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Zero-setup storage used only when the application is explicitly in memory mode. */
public class InMemoryEventBookingRepository implements EventBookingRepository {
    private final List<EventBookingRecord> bookings = new CopyOnWriteArrayList<>();

    @Override
    public EventBookingRecord save(EventBookingRecord booking) {
        bookings.add(0, booking);
        return booking;
    }
}
