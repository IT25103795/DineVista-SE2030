package com.dinevista.util;

import com.dinevista.repository.AccountRepository;
import com.dinevista.repository.JdbcAccountRepository;
import com.dinevista.service.AccountService;

import javax.servlet.ServletContext;

public final class AccountContext {
    private static final String SERVICE_KEY = AccountService.class.getName();

    private AccountContext() {}

    public static AccountService service(ServletContext context) {
        synchronized (context) {
            AccountService service = (AccountService) context.getAttribute(SERVICE_KEY);
            if (service == null) {
                DatabaseConfig config = DatabaseConfig.load();
                if (!config.isMysqlEnabled()) {
                    throw new IllegalStateException(
                            "Secure account registration and login require MySQL storage mode.");
                }
                try {
                    AccountRepository repository = new JdbcAccountRepository(config);
                    service = new AccountService(repository);
                    context.setAttribute(SERVICE_KEY, service);
                } catch (Exception ex) {
                    throw new IllegalStateException("Account persistence could not start.", ex);
                }
            }
            return service;
        }
    }
}
