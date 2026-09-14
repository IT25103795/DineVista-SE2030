package com.dinevista.util;

import com.dinevista.repository.InMemoryMenuRepository;
import com.dinevista.repository.JdbcMenuRepository;
import com.dinevista.repository.MenuRepository;
import com.dinevista.service.MenuService;

import javax.servlet.ServletContext;

/**
 * Creates and shares a single {@link MenuService} per application context,
 * mirroring {@link InventoryContext} and {@link BillingContext}. Reads the same
 * DINEVISTA_STORAGE_MODE config: when set to "mysql" it persists categories and
 * dishes to the database via {@link JdbcMenuRepository}; otherwise (or if MySQL
 * is unreachable) it falls back to an in-memory repository so the module still
 * runs smoothly for a live demo.
 */
public final class MenuContext {
    private static final String SERVICE_KEY = MenuService.class.getName();

    private MenuContext() {}

    public static MenuService service(ServletContext context) {
        synchronized (context) {
            MenuService service = (MenuService) context.getAttribute(SERVICE_KEY);
            if (service == null) {
                MenuRepository repository;
                DatabaseConfig config = DatabaseConfig.load();
                if (config.isMysqlEnabled()) {
                    try {
                        repository = new JdbcMenuRepository(config);
                        context.setAttribute("menuStorageMode", "mysql");
                    } catch (Exception ex) {
                        repository = new InMemoryMenuRepository();
                        context.setAttribute("menuStorageMode", "memory");
                    }
                } else {
                    repository = new InMemoryMenuRepository();
                    context.setAttribute("menuStorageMode", "memory");
                }
                service = new MenuService(repository);
                context.setAttribute(SERVICE_KEY, service);
            }
            return service;
        }
    }
}
