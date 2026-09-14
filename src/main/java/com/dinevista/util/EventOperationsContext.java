package com.dinevista.util;

import com.dinevista.repository.EventOperationsRepository;
import com.dinevista.repository.InMemoryEventOperationsRepository;
import com.dinevista.repository.JdbcEventOperationsRepository;
import com.dinevista.service.EventOperationsService;

import javax.servlet.ServletContext;

/**
 * Creates and shares a single {@link EventOperationsService} per application context,
 * mirroring {@link InventoryContext}. Reads the same DINEVISTA_STORAGE_MODE config:
 * when set to "mysql" it persists venues, resources, bookings, and staff scheduling
 * to the database via {@link JdbcEventOperationsRepository}; otherwise it uses an
 * in-memory repository so the Event Resource and Staff Scheduling Management module
 * (Wijesuriya W. A. T. D. / IT25103799) still runs for a demo.
 */
public final class EventOperationsContext {
    private static final String SERVICE_KEY = EventOperationsService.class.getName();

    private EventOperationsContext() {}

    public static EventOperationsService service(ServletContext context) {
        synchronized (context) {
            EventOperationsService service = (EventOperationsService) context.getAttribute(SERVICE_KEY);
            if (service == null) {
                EventOperationsRepository repository;
                DatabaseConfig config = DatabaseConfig.load();
                if (config.isMysqlEnabled()) {
                    try {
                        repository = new JdbcEventOperationsRepository(config);
                        context.setAttribute("eventOperationsStorageMode", "mysql");
                    } catch (Exception ex) {
                        throw new IllegalStateException(
                                "Event resource and staff scheduling requires MySQL, but persistence could not start.", ex);
                    }
                } else {
                    repository = new InMemoryEventOperationsRepository();
                    context.setAttribute("eventOperationsStorageMode", "memory");
                }
                service = new EventOperationsService(repository);
                context.setAttribute(SERVICE_KEY, service);
            }
            return service;
        }
    }
}
