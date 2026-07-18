package com.dinevista.util;

import com.dinevista.repository.InMemoryReservationOrderRepository;
import com.dinevista.repository.JdbcReservationOrderRepository;
import com.dinevista.repository.ReservationOrderRepository;
import com.dinevista.service.ReservationOrderService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class ReservationOrderContext {
    private static final String REPOSITORY_KEY = ReservationOrderRepository.class.getName();
    private static final String SERVICE_KEY = ReservationOrderService.class.getName();
    private static final String CUSTOMER_KEY = "reservationOrderCustomerKey";
    private static final String CART_KEY = "reservationOrderCart";

    private ReservationOrderContext() {}

    public static ReservationOrderService service(ServletContext context) {
        synchronized (context) {
            ReservationOrderService service = (ReservationOrderService) context.getAttribute(SERVICE_KEY);
            if (service == null) {
                ReservationOrderRepository repository;
                DatabaseConfig config = DatabaseConfig.load();
                if (config.isMysqlEnabled()) {
                    try {
                        repository = new JdbcReservationOrderRepository(config);
                        context.setAttribute("reservationOrderStorageMode", "mysql");
                    } catch (Exception ex) {
                        repository = new InMemoryReservationOrderRepository();
                        context.setAttribute("reservationOrderStorageMode", "memory-fallback");
                        context.setAttribute("reservationOrderStorageWarning",
                                "MySQL was unavailable, so the module started in memory mode: " + ex.getMessage());
                    }
                } else {
                    repository = new InMemoryReservationOrderRepository();
                    context.setAttribute("reservationOrderStorageMode", "memory");
                }
                context.setAttribute(REPOSITORY_KEY, repository);
                service = new ReservationOrderService(repository);
                context.setAttribute(SERVICE_KEY, service);
            }
            return service;
        }
    }

    public static String customerKey(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String loginEmail = string(session.getAttribute("demoEmail"));
        if (!loginEmail.isEmpty()) {
            session.setAttribute(CUSTOMER_KEY, loginEmail.toLowerCase());
            return loginEmail.toLowerCase();
        }
        String key = string(session.getAttribute(CUSTOMER_KEY));
        if (key.isEmpty()) {
            key = "guest-" + UUID.randomUUID().toString();
            session.setAttribute(CUSTOMER_KEY, key);
        }
        return key;
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, Integer> cart(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(CART_KEY);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }

    public static int cartQuantity(HttpServletRequest request) {
        return cart(request).values().stream().mapToInt(Integer::intValue).sum();
    }

    public static boolean isManager(HttpServletRequest request) {
        return "manager".equalsIgnoreCase(string(request.getSession().getAttribute("demoRole")));
    }

    public static String displayName(HttpServletRequest request) {
        String name = string(request.getSession().getAttribute("displayName"));
        return name.isEmpty() ? "DineVista Guest" : name;
    }

    private static String string(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
