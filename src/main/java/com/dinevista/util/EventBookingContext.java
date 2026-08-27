package com.dinevista.util;

import com.dinevista.repository.EventBookingRepository;
import com.dinevista.repository.InMemoryEventBookingRepository;
import com.dinevista.repository.JdbcEventBookingRepository;

import javax.servlet.ServletContext;

/** Creates one event-booking repository for the web application. */
public final class EventBookingContext {
    private static final String REPOSITORY_KEY = EventBookingRepository.class.getName();

    private EventBookingContext() {}

    public static EventBookingRepository repository(ServletContext context) {
        synchronized (context) {
            EventBookingRepository repository =
                    (EventBookingRepository) context.getAttribute(REPOSITORY_KEY);
            if (repository == null) {
                DatabaseConfig config = DatabaseConfig.load();
                if (config.isMysqlEnabled()) {
                    try {
                        repository = new JdbcEventBookingRepository(config);
                        context.setAttribute("eventBookingStorageMode", "mysql");
                    } catch (Exception ex) {
                        throw new IllegalStateException(
                                "Event Booking requires MySQL, but persistence could not start.", ex);
                    }
                } else {
                    repository = new InMemoryEventBookingRepository();
                    context.setAttribute("eventBookingStorageMode", "memory");
                }
                context.setAttribute(REPOSITORY_KEY, repository);
            }
            return repository;
        }
    }
}
