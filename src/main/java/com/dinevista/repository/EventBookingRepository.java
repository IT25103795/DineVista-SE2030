package com.dinevista.repository;

import com.dinevista.model.EventBookingRecord;

/** Persistence contract for event consultation requests. */
public interface EventBookingRepository {
    EventBookingRecord save(EventBookingRecord booking);
}
