package com.dinevista.controller;

import com.dinevista.model.EventBookingRecord;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@WebServlet("/event-booking")
public class EventBookingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/event-booking.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String customerName = clean(request.getParameter("customerName"));
        String email = clean(request.getParameter("email"));
        String phone = clean(request.getParameter("phone"));
        String eventType = clean(request.getParameter("eventType"));
        String packageName = clean(request.getParameter("packageName"));
        String venue = clean(request.getParameter("venue"));
        String eventDate = clean(request.getParameter("eventDate"));
        String guestCountValue = clean(request.getParameter("guestCount"));
        int guestCount = parseInt(guestCountValue, 0);

        List<String> errors = new ArrayList<>();
        if (customerName.length() < 2) errors.add("Enter the contact person's name.");
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) errors.add("Enter a valid email address.");
        if (!phone.matches("^(?:\\+94|0)7\\d{8}$")) errors.add("Enter a valid Sri Lankan mobile number.");
        if (eventType.isEmpty()) errors.add("Select an event type.");
        if (packageName.isEmpty()) errors.add("Select an event package.");
        if (venue.isEmpty()) errors.add("Select a venue preference.");
        if (guestCount < 10 || guestCount > 500) errors.add("Guest count must be between 10 and 500.");

        try {
            if (eventDate.isEmpty() || LocalDate.parse(eventDate).isBefore(LocalDate.now().plusDays(7))) {
                errors.add("Event bookings must be requested at least 7 days in advance.");
            }
        } catch (Exception ex) {
            errors.add("Select a valid event date.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("formCustomerName", customerName);
            request.setAttribute("formEmail", email);
            request.setAttribute("formPhone", phone);
            request.setAttribute("formEventType", eventType);
            request.setAttribute("formPackageName", packageName);
            request.setAttribute("formVenue", venue);
            request.setAttribute("formEventDate", eventDate);
            request.setAttribute("formGuestCount", guestCountValue);
            request.getRequestDispatcher("/WEB-INF/views/event-booking.jsp").forward(request, response);
            return;
        }

        String reference = "DV-E-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        EventBookingRecord record = new EventBookingRecord(reference, customerName, email, phone,
                eventType, packageName, venue, eventDate, guestCount, "Consultation requested");

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<EventBookingRecord> eventBookings = (List<EventBookingRecord>) session.getAttribute("eventBookings");
        if (eventBookings == null) {
            eventBookings = new ArrayList<>();
            session.setAttribute("eventBookings", eventBookings);
        }
        eventBookings.add(0, record);

        request.setAttribute("latestEventBooking", record);
        request.setAttribute("successMessage", "Your event consultation request was submitted successfully.");
        request.getRequestDispatcher("/WEB-INF/views/event-booking.jsp").forward(request, response);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
