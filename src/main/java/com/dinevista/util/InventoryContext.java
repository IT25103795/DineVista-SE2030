package com.dinevista.util;

import com.dinevista.repository.InMemoryInventoryRepository;
import com.dinevista.repository.InventoryRepository;
import com.dinevista.repository.JdbcInventoryRepository;
import com.dinevista.service.InventoryService;

import javax.servlet.ServletContext;

/**
 * Creates and shares a single {@link InventoryService} per application context,
 * mirroring {@link ReservationOrderContext}. Reads the same DINEVISTA_STORAGE_MODE
 * config: when set to "mysql" it persists ingredients and stock transactions to
 * the database via {@link JdbcInventoryRepository}; otherwise (or if the database
 * is unreachable) it falls back to an in-memory repository so the module still
 * runs for a demo.
 */
public final class InventoryContext {
    private static final String SERVICE_KEY = InventoryService.class.getName();

    private InventoryContext() {}

    public static InventoryService service(ServletContext context) {
        synchronized (context) {
            InventoryService service = (InventoryService) context.getAttribute(SERVICE_KEY);
            if (service == null) {
                InventoryRepository repository;
                DatabaseConfig config = DatabaseConfig.load();
                if (config.isMysqlEnabled()) {
                    try {
                        repository = new JdbcInventoryRepository(config);
                        context.setAttribute("inventoryStorageMode", "mysql");
                    } catch (Exception ex) {
                        repository = new InMemoryInventoryRepository();
                        context.setAttribute("inventoryStorageMode", "memory-fallback");
                        context.setAttribute("inventoryStorageWarning",
                                "MySQL was unavailable, so Inventory started in memory mode: " + ex.getMessage());
                    }
                } else {
                    repository = new InMemoryInventoryRepository();
                    context.setAttribute("inventoryStorageMode", "memory");
                }
                service = new InventoryService(repository);
                context.setAttribute(SERVICE_KEY, service);
            }
            return service;
        }
    }
}
