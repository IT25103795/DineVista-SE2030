package com.dinevista.controller;

import com.dinevista.repository.InMemoryReservationOrderRepository;
import com.dinevista.repository.InMemoryEventBookingRepository;
import com.dinevista.repository.InMemoryEventPackageRepository;
import com.dinevista.service.EventBookingService;
import com.dinevista.service.ReservationOrderService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/** Checks stale detail/edit URLs without Tomcat or a database. */
public final class MissingRecordRedirectSelfTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        ReservationOrderService service = new ReservationOrderService(
                new InMemoryReservationOrderRepository());
        ReservationServlet reservations = new ReservationServlet();
        FoodOrderServlet orders = new FoodOrderServlet();
        inject(reservations, service);
        inject(orders, service);

        StaffReservationServlet staffReservations = new StaffReservationServlet();
        StaffOrderServlet staffOrders = new StaffOrderServlet();
        inject(staffReservations, service);
        inject(staffOrders, service);

        Exchange staffReservation = new Exchange("manager", "/view");
        staffReservations.doGet(staffReservation.request, staffReservation.response);
        assertRedirect(staffReservation, "staff reservation detail");

        Exchange staffOrder = new Exchange("manager", "/view");
        staffOrders.doGet(staffOrder.request, staffOrder.response);
        assertRedirect(staffOrder, "staff order detail");

        for (String role : new String[]{"manager", "customer"}) {
            Exchange reservation = new Exchange(role, "/view");
            reservations.doGet(reservation.request, reservation.response);
            assertRedirect(reservation, role + " reservation detail");

            Exchange order = new Exchange(role, "/view");
            orders.doGet(order.request, order.response);
            assertRedirect(order, role + " order detail");
        }

        Exchange reservationEdit = new Exchange("customer", "/edit");
        reservations.doGet(reservationEdit.request, reservationEdit.response);
        assertRedirect(reservationEdit, "customer reservation edit");

        Exchange staleUpdate = new Exchange("customer", "/update");
        reservations.doPost(staleUpdate.request, staleUpdate.response);
        assertRedirect(staleUpdate, "customer reservation update after deletion");

        EventBookingServlet events = new EventBookingServlet();
        Field eventField = EventBookingServlet.class.getDeclaredField("service");
        eventField.setAccessible(true);
        eventField.set(events, new EventBookingService(
                new InMemoryEventBookingRepository(), new InMemoryEventPackageRepository()));
        for (String role : new String[]{"manager", "customer"}) {
            Exchange eventView = new Exchange(role, "/view");
            events.doGet(eventView.request, eventView.response);
            assertRedirect(eventView, role + " event booking detail");

            Exchange eventEdit = new Exchange(role, "/edit");
            events.doGet(eventEdit.request, eventEdit.response);
            assertRedirect(eventEdit, role + " event booking edit");
        }

        Exchange unknown = new Exchange("customer", "/unknown");
        reservations.doGet(unknown.request, unknown.response);
        check(unknown.status == 404, "unknown routes stay 404");

        System.out.println("Missing-record redirect self-test passed: " + checks + " checks.");
    }

    private static void inject(Object servlet, ReservationOrderService service) throws Exception {
        Field field = servlet.getClass().getDeclaredField("service");
        field.setAccessible(true);
        field.set(servlet, service);
    }

    private static void assertRedirect(Exchange exchange, String label) {
        check(exchange.status == 302 && "/DineVista/dashboard".equals(exchange.redirect),
                label + " returns to dashboard");
        check(exchange.sessionValues.containsKey("flashErrors"),
                label + " explains that the record is unavailable");
    }

    private static void check(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError(label);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static final class Exchange {
        final Map<String, Object> sessionValues = new HashMap<>();
        final Map<String, Object> requestValues = new HashMap<>();
        final HttpServletRequest request;
        final HttpServletResponse response;
        int status = 200;
        String redirect;

        Exchange(String role, String path) {
            sessionValues.put("demoRole", role);
            sessionValues.put("demoEmail", role + "@example.com");
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
                    case "getSession": return session;
                    case "getContextPath": return "/DineVista";
                    case "getPathInfo": return path;
                    case "getServletPath": return "manager".equals(role)
                            ? "/staff/event-bookings" : "/event-booking";
                    case "getParameter": return "reference".equals(args[0]) ? "DV-DELETED" : null;
                    case "setAttribute": requestValues.put((String) args[0], args[1]); return null;
                    case "getAttribute": return requestValues.get(args[0]);
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
            response = proxy(HttpServletResponse.class, (p, method, args) -> {
                switch (method.getName()) {
                    case "sendRedirect": status = 302; redirect = (String) args[0]; return null;
                    case "sendError": status = (Integer) args[0]; return null;
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
        }
    }
}
