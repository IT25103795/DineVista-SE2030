package com.dinevista.util;

import com.dinevista.repository.BillingRepository;
import com.dinevista.repository.InMemoryBillingRepository;
import com.dinevista.repository.JdbcBillingRepository;
import com.dinevista.service.BillingService;

import javax.servlet.ServletContext;

/**
 * Creates and shares a single {@link BillingService} per application context,
 * mirroring {@link InventoryContext}. Reads the same DINEVISTA_STORAGE_MODE
 * config: when set to "mysql" it persists invoices, payments and promotions
 * to the database via {@link JdbcBillingRepository}; otherwise (or if the
 * database is unreachable) it falls back to an in-memory repository so the
 * Billing, Promotions & Discounts module still runs for a demo.
 */
public final class BillingContext {
    private static final String SERVICE_KEY = BillingService.class.getName();

    private BillingContext() {}

    public static BillingService service(ServletContext context) {
        synchronized (context) {
            BillingService service = (BillingService) context.getAttribute(SERVICE_KEY);
            if (service == null) {
                BillingRepository repository;
                DatabaseConfig config = DatabaseConfig.load();
                if (config.isMysqlEnabled()) {
                    try {
                        repository = new JdbcBillingRepository(config);
                        context.setAttribute("billingStorageMode", "mysql");
                    } catch (Exception ex) {
                        throw new IllegalStateException(
                                "Billing requires MySQL, but persistence could not start.", ex);
                    }
                } else {
                    repository = new InMemoryBillingRepository();
                    context.setAttribute("billingStorageMode", "memory");
                }
                service = new BillingService(repository);
                context.setAttribute(SERVICE_KEY, service);
            }
            return service;
        }
    }
}
