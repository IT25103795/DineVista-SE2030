package com.dinevista.controller;

import com.dinevista.model.RestaurantTableRecord;
import com.dinevista.model.TableReservationRecord;
import com.dinevista.service.OperationResult;
import com.dinevista.service.ReservationOrderService;
import com.dinevista.util.FlashUtil;
import com.dinevista.util.RequestUtil;
import com.dinevista.util.ReservationOrderContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@WebServlet(urlPatterns = {"/reservations", "/reservations/*"})
public class ReservationServlet extends HttpServlet {
    private ReservationOrderService service;

    @Override
    public void init() {
        service = ReservationOrderContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        FlashUtil.expose(request);
        String path = path(request);

        if ("/view".equals(path)) {
            showDetails(request, response);
            return;
        }
        if ("/edit".equals(path)) {
            showEdit(request, response);
            return;
        }
        renderMain(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = path(request);
        if ("/update".equals(path)) {
            update(request, response);
        } else if ("/cancel".equals(path)) {
            cancel(request, response);
        } else {
            create(request, response);
        }
    }

    private void renderMain(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String customerKey = ReservationOrderContext.customerKey(request);
        request.setAttribute("customerReservations", service.reservationsForCustomer(customerKey));
        request.setAttribute("restaurantTables", service.tables());

        LocalDate availabilityDate = RequestUtil.date(request, "availabilityDate");
        LocalTime availabilityTime = RequestUtil.time(request, "availabilityTime");
        int availabilityParty = RequestUtil.integer(request, "availabilityPartySize", 0);
        String availabilityArea = RequestUtil.clean(request, "availabilityArea");

        if (availabilityDate != null && availabilityTime != null && availabilityParty > 0) {
            List<RestaurantTableRecord> available = service.findAvailableTables(
                    availabilityDate, availabilityTime, availabilityParty,
                    availabilityArea, null);
            request.setAttribute("availableTables", available);
            request.setAttribute("availabilitySearched", true);
        }

        request.getRequestDispatcher("/WEB-INF/views/reservations.jsp").forward(request, response);
    }

    private void create(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String customerKey = ReservationOrderContext.customerKey(request);
        OperationResult<TableReservationRecord> result = service.createReservation(
                customerKey,
                RequestUtil.clean(request, "guestName"),
                RequestUtil.clean(request, "email"),
                RequestUtil.clean(request, "phone"),
                RequestUtil.date(request, "date"),
                RequestUtil.time(request, "time"),
                RequestUtil.integer(request, "partySize", 0),
                RequestUtil.clean(request, "seatingArea"),
                RequestUtil.clean(request, "occasion"));

        if (!result.isSuccess()) {
            request.setAttribute("errors", result.getErrors());
            copyReservationForm(request);
            renderMain(request, response);
            return;
        }

        FlashUtil.success(request, "Reservation " + result.getValue().getReference()
                + " was submitted successfully and is awaiting staff confirmation.");
        response.sendRedirect(request.getContextPath() + "/reservations?created="
                + result.getValue().getReference());
    }

    private void showEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String reference = RequestUtil.clean(request, "reference");
        String customerKey = ReservationOrderContext.customerKey(request);
        Optional<TableReservationRecord> record = service.reservation(reference);
        if (record.isEmpty() || !record.get().getCustomerKey().equals(customerKey)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("editReservation", record.get());
        request.setAttribute("restaurantTables", service.tables());
        request.getRequestDispatcher("/WEB-INF/views/reservation-edit.jsp").forward(request, response);
    }

    private void update(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String customerKey = ReservationOrderContext.customerKey(request);
        String reference = RequestUtil.clean(request, "reference");
        OperationResult<TableReservationRecord> result = service.updateReservation(
                customerKey, reference,
                RequestUtil.clean(request, "guestName"),
                RequestUtil.clean(request, "email"),
                RequestUtil.clean(request, "phone"),
                RequestUtil.date(request, "date"),
                RequestUtil.time(request, "time"),
                RequestUtil.integer(request, "partySize", 0),
                RequestUtil.clean(request, "seatingArea"),
                RequestUtil.clean(request, "occasion"));

        if (!result.isSuccess()) {
            request.setAttribute("errors", result.getErrors());
            request.setAttribute("editReservation", service.reservation(reference).orElse(null));
            copyReservationForm(request);
            request.getRequestDispatcher("/WEB-INF/views/reservation-edit.jsp").forward(request, response);
            return;
        }

        FlashUtil.success(request, "Reservation " + reference + " was updated successfully.");
        response.sendRedirect(request.getContextPath() + "/reservations/view?reference=" + reference);
    }

    private void cancel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String customerKey = ReservationOrderContext.customerKey(request);
        String reference = RequestUtil.clean(request, "reference");
        OperationResult<TableReservationRecord> result = service.cancelReservation(
                customerKey, reference, RequestUtil.clean(request, "reason"));

        if (result.isSuccess()) {
            FlashUtil.success(request, "Reservation " + reference + " was cancelled.");
        } else {
            FlashUtil.errors(request, result.getErrors());
        }
        response.sendRedirect(request.getContextPath() + "/reservations/view?reference=" + reference);
    }

    private void showDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String reference = RequestUtil.clean(request, "reference");
        Optional<TableReservationRecord> record = service.reservation(reference);
        boolean manager = ReservationOrderContext.isManager(request);
        String customerKey = ReservationOrderContext.customerKey(request);

        if (record.isEmpty()
                || (!manager && !record.get().getCustomerKey().equals(customerKey))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("reservation", record.get());
        request.getRequestDispatcher("/WEB-INF/views/reservation-detail.jsp").forward(request, response);
    }

    private void copyReservationForm(HttpServletRequest request) {
        request.setAttribute("formGuestName", RequestUtil.clean(request, "guestName"));
        request.setAttribute("formEmail", RequestUtil.clean(request, "email"));
        request.setAttribute("formPhone", RequestUtil.clean(request, "phone"));
        request.setAttribute("formDate", RequestUtil.clean(request, "date"));
        request.setAttribute("formTime", RequestUtil.clean(request, "time"));
        request.setAttribute("formPartySize", RequestUtil.clean(request, "partySize"));
        request.setAttribute("formSeatingArea", RequestUtil.clean(request, "seatingArea"));
        request.setAttribute("formOccasion", RequestUtil.clean(request, "occasion"));
    }

    private String path(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || "/".equals(path) ? "" : path;
    }
}
