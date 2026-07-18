package com.dinevista.controller;

import com.dinevista.model.ReservationRecord;

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

@WebServlet("/reservations")
public class ReservationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/reservations.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String guestName = clean(request.getParameter("guestName"));
        String email = clean(request.getParameter("email"));
        String phone = clean(request.getParameter("phone"));
        String date = clean(request.getParameter("date"));
        String time = clean(request.getParameter("time"));
        String partySizeValue = clean(request.getParameter("partySize"));
        String seatingArea = clean(request.getParameter("seatingArea"));
        String occasion = clean(request.getParameter("occasion"));

        List<String> errors = new ArrayList<>();
        int partySize = parseInt(partySizeValue, 0);

        if (guestName.length() < 2) errors.add("Enter the guest name.");
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) errors.add("Enter a valid email address.");
        if (!phone.matches("^(?:\\+94|0)7\\d{8}$")) errors.add("Enter a valid Sri Lankan mobile number.");
        if (partySize < 1 || partySize > 20) errors.add("Party size must be between 1 and 20 guests.");
        if (time.isEmpty()) errors.add("Select a reservation time.");
        if (seatingArea.isEmpty()) errors.add("Select a seating area.");

        try {
            if (date.isEmpty() || LocalDate.parse(date).isBefore(LocalDate.now())) {
                errors.add("Select today or a future date.");
            }
        } catch (Exception ex) {
            errors.add("Select a valid reservation date.");
        }

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("formGuestName", guestName);
            request.setAttribute("formEmail", email);
            request.setAttribute("formPhone", phone);
            request.setAttribute("formDate", date);
            request.setAttribute("formTime", time);
            request.setAttribute("formPartySize", partySizeValue);
            request.setAttribute("formSeatingArea", seatingArea);
            request.setAttribute("formOccasion", occasion);
            request.getRequestDispatcher("/WEB-INF/views/reservations.jsp").forward(request, response);
            return;
        }

        String reference = "DV-R-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        ReservationRecord record = new ReservationRecord(reference, guestName, email, phone,
                date, time, partySize, seatingArea, occasion.isEmpty() ? "Casual dining" : occasion,
                "Pending confirmation");

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<ReservationRecord> reservations = (List<ReservationRecord>) session.getAttribute("reservations");
        if (reservations == null) {
            reservations = new ArrayList<>();
            session.setAttribute("reservations", reservations);
        }
        reservations.add(0, record);

        request.setAttribute("latestReservation", record);
        request.setAttribute("successMessage", "Your table request was submitted successfully.");
        request.getRequestDispatcher("/WEB-INF/views/reservations.jsp").forward(request, response);
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
