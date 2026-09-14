package com.dinevista.controller;

import com.dinevista.model.StaffMemberRecord;
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
 * Management (staff half). Staff-only view of the operational roster plus
 * conflict-free shift scheduling and per-event staff assignment. Venue and equipment
 * booking lives in {@link EventResourceServlet}.
 * Routes:
 *   GET  /staff/staff-scheduling                    roster + all shifts + all assignments
 *   GET  /staff/staff-scheduling/view                 staff detail, shifts, assignments (?id=)
 *   POST /staff/staff-scheduling/status                update a staff member's availability
 *   POST /staff/staff-scheduling/shifts/save           schedule a shift (conflict-checked)
 *   POST /staff/staff-scheduling/shifts/status          confirm/complete/mark absent/cancel a shift
 *   POST /staff/staff-scheduling/assignments/save       assign staff to an event (conflict-checked)
 *   POST /staff/staff-scheduling/assignments/status      confirm/complete/cancel an assignment
 */
@WebServlet(urlPatterns = {"/staff/staff-scheduling", "/staff/staff-scheduling/*"})
public class StaffSchedulingServlet extends HttpServlet {
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
                request.setAttribute("staffList", service.allStaff());
                request.setAttribute("schedules", service.allSchedules());
                request.setAttribute("assignments", service.allAssignments());
                request.getRequestDispatcher("/WEB-INF/views/staff-staff-scheduling.jsp")
                        .forward(request, response);
                return;
            }
            case "/view": {
                long id = RequestUtil.longValue(request, "id", 0);
                Optional<StaffMemberRecord> staff = service.staff(id);
                if (staff.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("staffMember", staff.get());
                request.setAttribute("schedules", service.schedulesForStaff(id));
                request.setAttribute("assignments", service.assignmentsForStaff(id));
                request.getRequestDispatcher("/WEB-INF/views/staff-staff-detail.jsp")
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
            case "/status": {
                long staffId = RequestUtil.longValue(request, "staffId", 0);
                OperationResult<Void> result = service.updateStaffAvailability(
                        staffId, RequestUtil.clean(request, "availabilityStatus"));
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Availability status updated.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/staff-scheduling/view?id=" + staffId);
                return;
            }
            case "/shifts/save": {
                long staffId = RequestUtil.longValue(request, "staffId", 0);
                OperationResult<?> result = service.scheduleShift(
                        staffId,
                        RequestUtil.clean(request, "shiftDate"),
                        RequestUtil.clean(request, "startTime"),
                        RequestUtil.clean(request, "endTime"),
                        RequestUtil.clean(request, "shiftType"),
                        RequestUtil.clean(request, "notes"));
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Shift scheduled.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/staff-scheduling/view?id=" + staffId);
                return;
            }
            case "/shifts/status": {
                long staffId = RequestUtil.longValue(request, "staffId", 0);
                OperationResult<?> result = service.updateScheduleStatus(
                        RequestUtil.longValue(request, "scheduleId", 0),
                        RequestUtil.clean(request, "status"));
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Shift status updated.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/staff-scheduling/view?id=" + staffId);
                return;
            }
            case "/assignments/save": {
                long staffId = RequestUtil.longValue(request, "staffId", 0);
                OperationResult<?> result = service.assignStaffToEvent(
                        staffId,
                        RequestUtil.clean(request, "eventLabel"),
                        RequestUtil.clean(request, "assignmentRole"),
                        RequestUtil.clean(request, "assignmentDate"),
                        RequestUtil.clean(request, "startTime"),
                        RequestUtil.clean(request, "endTime"),
                        RequestUtil.clean(request, "notes"));
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Staff member assigned to event.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/staff-scheduling/view?id=" + staffId);
                return;
            }
            case "/assignments/status": {
                long staffId = RequestUtil.longValue(request, "staffId", 0);
                OperationResult<?> result = service.updateAssignmentStatus(
                        RequestUtil.longValue(request, "assignmentId", 0),
                        RequestUtil.clean(request, "status"));
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Assignment status updated.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/staff-scheduling/view?id=" + staffId);
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
