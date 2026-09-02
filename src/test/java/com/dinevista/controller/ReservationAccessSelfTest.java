package com.dinevista.controller;

import com.dinevista.repository.InMemoryReservationOrderRepository;
import com.dinevista.service.ReservationOrderService;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** Dependency-free servlet regression tests; no database or Tomcat required. */
public final class ReservationAccessSelfTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        ReservationServlet servlet = new ReservationServlet();
        ReservationOrderService service = new ReservationOrderService(
                new InMemoryReservationOrderRepository());
        Field field = ReservationServlet.class.getDeclaredField("service");
        field.setAccessible(true);
        field.set(servlet, service);

        Exchange managerPage = new Exchange("manager", null);
        servlet.doGet(managerPage.request, managerPage.response);
        check("/DineVista/staff/reservations".equals(managerPage.redirect),
                "manager opening customer form returns to staff operations");
        check(managerPage.forward == null, "manager never receives customer creation form");

        for (String role : new String[]{"manager", "MANAGER", "staff", "unexpected-role"}) {
            Exchange attempt = new Exchange(role, "/create");
            attempt.validReservation();
            servlet.doPost(attempt.request, attempt.response);
            check(attempt.status == 403, role + " direct creation POST is forbidden");
            check(service.reservationsForCustomer(attempt.email).isEmpty(),
                    role + " attempt does not create any reservation");
        }

        Exchange guest = new Exchange(null, "/create");
        servlet.doPost(guest.request, guest.response);
        check("/DineVista/login".equals(guest.redirect), "guest must sign in");

        Exchange customerPage = new Exchange("customer", null);
        servlet.doGet(customerPage.request, customerPage.response);
        check("/WEB-INF/views/reservations.jsp".equals(customerPage.forward),
                "customer can still access reservation form");

        Exchange customer = new Exchange("customer", "/create");
        customer.validReservation();
        servlet.doPost(customer.request, customer.response);
        check(customer.redirect != null
                        && customer.redirect.startsWith("/DineVista/reservations?created="),
                "customer can still submit a reservation");
        check(service.reservationsForCustomer(customer.email).size() == 1,
                "customer reservation is saved");

        System.out.println("DineVista reservation access self-test passed: " + checks + " checks.");
    }

    private static void check(boolean result, String message) {
        checks++;
        if (!result) throw new AssertionError(message);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static final class Exchange {
        final Map<String, Object> sessionValues = new HashMap<>();
        final Map<String, Object> attributes = new HashMap<>();
        final Map<String, String> parameters = new HashMap<>();
        final String email;
        final HttpServletRequest request;
        final HttpServletResponse response;
        int status = 200;
        String redirect;
        String forward;

        Exchange(String role, String path) {
            email = (role == null ? "guest" : role.toLowerCase(java.util.Locale.ROOT))
                    + "@example.com";
            if (role != null) {
                sessionValues.put("demoRole", role);
                sessionValues.put("demoEmail", email);
                sessionValues.put("userId", 1L);
            }
            HttpSession session = proxy(HttpSession.class, (p, method, args) -> {
                switch (method.getName()) {
                    case "getAttribute": return sessionValues.get(args[0]);
                    case "setAttribute": sessionValues.put((String) args[0], args[1]); return null;
                    case "removeAttribute": sessionValues.remove(args[0]); return null;
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
            request = proxy(HttpServletRequest.class, (p, method, args) -> {
                switch (method.getName()) {
                    case "getSession": return role == null ? null : session;
                    case "getContextPath": return "/DineVista";
                    case "getPathInfo": return path;
                    case "getParameter": return parameters.get(args[0]);
                    case "getAttribute": return attributes.get(args[0]);
                    case "setAttribute": attributes.put((String) args[0], args[1]); return null;
                    case "getRequestDispatcher":
                        String destination = (String) args[0];
                        return proxy(RequestDispatcher.class, (d, operation, values) -> {
                            if (!"forward".equals(operation.getName())) {
                                throw new UnsupportedOperationException(operation.getName());
                            }
                            forward = destination;
                            return null;
                        });
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
            response = proxy(HttpServletResponse.class, (p, method, args) -> {
                switch (method.getName()) {
                    case "sendError": status = (Integer) args[0]; return null;
                    case "sendRedirect": status = 302; redirect = (String) args[0]; return null;
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
        }

        void validReservation() {
            parameters.put("guestName", "Test Customer");
            parameters.put("email", email);
            parameters.put("phone", "0771234567");
            parameters.put("date", LocalDate.now().plusDays(10).toString());
            parameters.put("time", "18:30");
            parameters.put("partySize", "2");
            parameters.put("seatingArea", "INDOOR");
        }
    }
}
