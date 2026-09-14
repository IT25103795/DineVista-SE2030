package com.dinevista.controller;

import com.dinevista.model.EventResourceRecord;
import com.dinevista.model.EventVenueRecord;
import com.dinevista.service.EventOperationsService;
import com.dinevista.service.OperationResult;
import com.dinevista.util.EventOperationsContext;
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

/**
 * Owner: Wijesuriya W. A. T. D. (IT25103799) — Event Resource and Staff Scheduling
 * Management (venue and equipment half). Staff-only CRUD for event venues and shared
 * equipment, plus conflict-free venue and resource booking against capacity and
 * availability. Staff scheduling itself lives in {@link StaffSchedulingServlet}.
 * Routes:
 *   GET  /staff/event-resources                       overview: venues + resources
 *   GET  /staff/event-resources/venues/new             blank venue form
 *   GET  /staff/event-resources/venues/edit             edit venue form (?id=)
 *   GET  /staff/event-resources/venues/view              venue detail + booking history (?id=)
 *   POST /staff/event-resources/venues/save             create or update a venue
 *   POST /staff/event-resources/venues/delete            delete (blocked once booked)
 *   POST /staff/event-resources/venues/book              book the venue for an event
 *   POST /staff/event-resources/venues/booking-status    confirm/cancel a venue booking
 *   GET  /staff/event-resources/resources/new           blank resource form
 *   GET  /staff/event-resources/resources/edit           edit resource form (?id=)
 *   GET  /staff/event-resources/resources/view            resource detail + booking history (?id=)
 *   POST /staff/event-resources/resources/save           create or update a resource
 *   POST /staff/event-resources/resources/delete          delete (blocked once booked)
 *   POST /staff/event-resources/resources/book            reserve units for an event
 *   POST /staff/event-resources/resources/booking-status  allocate/return/cancel a booking
 */
@WebServlet(urlPatterns = {"/staff/event-resources", "/staff/event-resources/*"})
public class EventResourceServlet extends HttpServlet {
    private EventOperationsService service;

    @Override
    public void init() {
        service = EventOperationsContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        FlashUtil.expose(request);
        String path = path(request);

        switch (path) {
            case "": {
                request.setAttribute("venues", service.allVenues());
                request.setAttribute("resources", service.allResources());
                request.setAttribute("venueBookings", service.allVenueBookings());
                request.setAttribute("resourceBookings", service.allResourceBookings());
                request.getRequestDispatcher("/WEB-INF/views/staff-event-resources.jsp")
                        .forward(request, response);
                return;
            }
            case "/venues/new":
                request.getRequestDispatcher("/WEB-INF/views/staff-venue-form.jsp")
                        .forward(request, response);
                return;
            case "/venues/edit": {
                Optional<EventVenueRecord> venue = service.venue(RequestUtil.longValue(request, "id", 0));
                if (venue.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("venue", venue.get());
                request.getRequestDispatcher("/WEB-INF/views/staff-venue-form.jsp")
                        .forward(request, response);
                return;
            }
            case "/venues/view": {
                long id = RequestUtil.longValue(request, "id", 0);
                Optional<EventVenueRecord> venue = service.venue(id);
                if (venue.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("venue", venue.get());
                request.setAttribute("venueBookings", service.venueBookings(id));
                request.getRequestDispatcher("/WEB-INF/views/staff-venue-detail.jsp")
                        .forward(request, response);
                return;
            }
            case "/resources/new":
                request.getRequestDispatcher("/WEB-INF/views/staff-resource-form.jsp")
                        .forward(request, response);
                return;
            case "/resources/edit": {
                Optional<EventResourceRecord> resource = service.resource(RequestUtil.longValue(request, "id", 0));
                if (resource.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("resource", resource.get());
                request.getRequestDispatcher("/WEB-INF/views/staff-resource-form.jsp")
                        .forward(request, response);
                return;
            }
            case "/resources/view": {
                long id = RequestUtil.longValue(request, "id", 0);
                Optional<EventResourceRecord> resource = service.resource(id);
                if (resource.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("resource", resource.get());
                request.setAttribute("resourceBookings", service.resourceBookings(id));
                request.getRequestDispatcher("/WEB-INF/views/staff-resource-detail.jsp")
                        .forward(request, response);
                return;
            }
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireManager(request, response)) return;
        String path = path(request);

        switch (path) {
            case "/venues/save": {
                long id = RequestUtil.longValue(request, "id", 0);
                OperationResult<EventVenueRecord> result = service.saveVenue(
                        id,
                        RequestUtil.clean(request, "name"),
                        RequestUtil.clean(request, "venueType"),
                        RequestUtil.clean(request, "capacity"),
                        RequestUtil.clean(request, "baseFee"),
                        RequestUtil.clean(request, "description"),
                        RequestUtil.clean(request, "availabilityStatus"));
                if (result.isSuccess()) {
                    FlashUtil.success(request, id <= 0
                            ? "Venue \"" + result.getValue().getName() + "\" was added."
                            : "Venue \"" + result.getValue().getName() + "\" was updated.");
                    response.sendRedirect(request.getContextPath()
                            + "/staff/event-resources/venues/view?id=" + result.getValue().getId());
                } else {
                    FlashUtil.errors(request, result.getErrors());
                    response.sendRedirect(request.getContextPath() + (id <= 0
                            ? "/staff/event-resources/venues/new"
                            : "/staff/event-resources/venues/edit?id=" + id));
                }
                return;
            }
            case "/venues/delete": {
                long id = RequestUtil.longValue(request, "id", 0);
                OperationResult<Void> result = service.deleteVenue(id);
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Venue was deleted.");
                    response.sendRedirect(request.getContextPath() + "/staff/event-resources");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                    response.sendRedirect(request.getContextPath() + "/staff/event-resources/venues/view?id=" + id);
                }
                return;
            }
            case "/venues/book": {
                long venueId = RequestUtil.longValue(request, "venueId", 0);
                OperationResult<?> result = service.bookVenue(
                        venueId,
                        RequestUtil.clean(request, "eventLabel"),
                        RequestUtil.clean(request, "eventDate"),
                        RequestUtil.clean(request, "startTime"),
                        RequestUtil.clean(request, "endTime"),
                        RequestUtil.clean(request, "guestCount"),
                        RequestUtil.clean(request, "notes"),
                        ReservationOrderContext.displayName(request));
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Venue booking was requested.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/event-resources/venues/view?id=" + venueId);
                return;
            }
            case "/venues/booking-status": {
                long venueId = RequestUtil.longValue(request, "venueId", 0);
                OperationResult<?> result = service.updateVenueBookingStatus(
                        RequestUtil.longValue(request, "bookingId", 0),
                        RequestUtil.clean(request, "status"));
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Booking status updated.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/event-resources/venues/view?id=" + venueId);
                return;
            }
            case "/resources/save": {
                long id = RequestUtil.longValue(request, "id", 0);
                OperationResult<EventResourceRecord> result = service.saveResource(
                        id,
                        RequestUtil.clean(request, "name"),
                        RequestUtil.clean(request, "category"),
                        RequestUtil.clean(request, "totalQuantity"),
                        RequestUtil.clean(request, "availableQuantity"),
                        RequestUtil.clean(request, "unitCost"),
                        RequestUtil.clean(request, "status"));
                if (result.isSuccess()) {
                    FlashUtil.success(request, id <= 0
                            ? "Resource \"" + result.getValue().getName() + "\" was added."
                            : "Resource \"" + result.getValue().getName() + "\" was updated.");
                    response.sendRedirect(request.getContextPath()
                            + "/staff/event-resources/resources/view?id=" + result.getValue().getId());
                } else {
                    FlashUtil.errors(request, result.getErrors());
                    response.sendRedirect(request.getContextPath() + (id <= 0
                            ? "/staff/event-resources/resources/new"
                            : "/staff/event-resources/resources/edit?id=" + id));
                }
                return;
            }
            case "/resources/delete": {
                long id = RequestUtil.longValue(request, "id", 0);
                OperationResult<Void> result = service.deleteResource(id);
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Resource was deleted.");
                    response.sendRedirect(request.getContextPath() + "/staff/event-resources");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                    response.sendRedirect(request.getContextPath() + "/staff/event-resources/resources/view?id=" + id);
                }
                return;
            }
            case "/resources/book": {
                long resourceId = RequestUtil.longValue(request, "resourceId", 0);
                OperationResult<?> result = service.bookResource(
                        resourceId,
                        RequestUtil.clean(request, "eventLabel"),
                        RequestUtil.clean(request, "eventDate"),
                        RequestUtil.clean(request, "quantity"),
                        RequestUtil.clean(request, "notes"),
                        ReservationOrderContext.displayName(request));
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Resource booking was requested.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/event-resources/resources/view?id=" + resourceId);
                return;
            }
            case "/resources/booking-status": {
                long resourceId = RequestUtil.longValue(request, "resourceId", 0);
                OperationResult<?> result = service.updateResourceBookingStatus(
                        RequestUtil.longValue(request, "bookingId", 0),
                        RequestUtil.clean(request, "status"));
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Booking status updated.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/event-resources/resources/view?id=" + resourceId);
                return;
            }
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
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
