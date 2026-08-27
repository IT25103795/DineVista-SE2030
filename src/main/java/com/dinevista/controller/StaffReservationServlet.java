package com.dinevista.controller;

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
import java.util.Optional;

@WebServlet(urlPatterns = {"/staff/reservations", "/staff/reservations/*"})
public class StaffReservationServlet extends HttpServlet {
    private ReservationOrderService service;

    @Override
    public void init() {
        service = ReservationOrderContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        FlashUtil.expose(request);

        if ("/view".equals(path(request))) {
            String reference = RequestUtil.clean(request, "reference");
            Optional<TableReservationRecord> reservation = service.reservation(reference);
            if (reservation.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("reservation", reservation.get());
            request.setAttribute("restaurantTables", service.tables());
            request.setAttribute("availableTables", service.findAvailableTables(
                    reservation.get().getReservationDate(),
                    reservation.get().getReservationTime(),
                    reservation.get().getPartySize(),
                    reservation.get().getSeatingPreference(),
                    reservation.get().getReference()));
            request.getRequestDispatcher("/WEB-INF/views/staff-reservation-detail.jsp")
                    .forward(request, response);
            return;
        }

        if (path(request).isEmpty()) {
            String status = RequestUtil.clean(request, "status");
            String date = RequestUtil.clean(request, "date");
            request.setAttribute("reservationFilterStatus", status);
            request.setAttribute("reservationFilterDate", date);
            request.setAttribute("staffReservations", service.allReservations(status, date));
            request.getRequestDispatcher("/WEB-INF/views/staff-reservations.jsp")
                    .forward(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!requireManager(request, response)) return;
        if (!"/update".equals(path(request))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String reference = RequestUtil.clean(request, "reference");
        OperationResult<TableReservationRecord> result = service.staffUpdateReservation(
                reference,
                RequestUtil.longValue(request, "tableId", 0),
                RequestUtil.clean(request, "status"),
                RequestUtil.clean(request, "note"),
                ReservationOrderContext.displayName(request));

        if (result.isSuccess()) {
            FlashUtil.success(request, "Reservation " + reference + " was updated.");
        } else {
            FlashUtil.errors(request, result.getErrors());
        }
        response.sendRedirect(request.getContextPath()
                + "/staff/reservations/view?reference=" + reference);
    }

    private boolean requireManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (ReservationOrderContext.isManager(request)) return true;
        response.sendRedirect(request.getContextPath() + "/manager/login?required=manager");
        return false;
    }

    private String path(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || "/".equals(path) ? "" : path;
    }
}
