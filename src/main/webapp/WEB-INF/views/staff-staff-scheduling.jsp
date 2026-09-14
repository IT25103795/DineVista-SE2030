<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.StaffMemberRecord" %>
<%@ page import="com.dinevista.model.StaffScheduleRecord" %>
<%@ page import="com.dinevista.model.EventStaffAssignmentRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Staff Scheduling");
    request.setAttribute("activeNav", "staffStaffScheduling");
    List<StaffMemberRecord> staffList = (List<StaffMemberRecord>) request.getAttribute("staffList");
    List<StaffScheduleRecord> schedules = (List<StaffScheduleRecord>) request.getAttribute("schedules");
    List<EventStaffAssignmentRecord> assignments = (List<EventStaffAssignmentRecord>) request.getAttribute("assignments");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <span class="eyebrow">Event Resource and Staff Scheduling</span>
            <h1>Staff scheduling control centre.</h1>
            <p>Review the operational roster, keep availability status current, and schedule shifts and event assignments without ever double-booking a staff member.</p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/dashboard">Operations dashboard</a>
            <a class="btn btn-secondary" href="<%= ctx %>/staff/event-resources">Event resources</a>
        </div>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
        <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div><% } %>

        <div class="operations-summary">
            <div><strong><%= staffList == null ? 0 : staffList.size() %></strong><span>Operational staff</span></div>
            <div><strong><%= schedules == null ? 0 : schedules.size() %></strong><span>Scheduled shifts</span></div>
            <div><strong><%= assignments == null ? 0 : assignments.size() %></strong><span>Event assignments</span></div>
        </div>

        <section class="panel operations-table-panel">
            <div class="panel-header"><div><h3>Operational staff roster</h3><span class="muted small">Open a staff member to update availability, or schedule shifts and event assignments.</span></div></div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead><tr><th>Staff member</th><th>Role</th><th>Department</th><th>Availability</th><th></th></tr></thead>
                    <tbody>
                    <% if (staffList == null || staffList.isEmpty()) { %>
                        <tr><td colspan="5"><div class="empty-table-message">No operational staff registered yet. Staff appear here once registered as managers/coordinators through account sign-up.</div></td></tr>
                    <% } else { for (StaffMemberRecord staff : staffList) { %>
                        <tr>
                            <td><strong><%= HtmlUtil.escape(staff.getFullName()) %></strong></td>
                            <td><%= HtmlUtil.escape(staff.getJobTitle() == null ? "—" : staff.getJobTitle()) %></td>
                            <td><%= HtmlUtil.escape(staff.getDepartment() == null ? "—" : staff.getDepartment()) %></td>
                            <td><span class="status <%= staff.getStatusCss() %>"><%= HtmlUtil.escape(staff.getAvailabilityStatus()) %></span></td>
                            <td><a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/staff-scheduling/view?id=<%= staff.getId() %>">Manage</a></td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="panel operations-table-panel" style="margin-top:24px">
            <div class="panel-header"><div><h3>Upcoming &amp; recent shifts</h3></div></div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead><tr><th>Staff member</th><th>Date</th><th>Time</th><th>Type</th><th>Status</th></tr></thead>
                    <tbody>
                    <% if (schedules == null || schedules.isEmpty()) { %>
                        <tr><td colspan="5"><div class="empty-table-message">No shifts scheduled yet.</div></td></tr>
                    <% } else { for (StaffScheduleRecord schedule : schedules) { %>
                        <tr>
                            <td><a href="<%= ctx %>/staff/staff-scheduling/view?id=<%= schedule.getStaffId() %>"><%= HtmlUtil.escape(schedule.getStaffName()) %></a></td>
                            <td><%= HtmlUtil.escape(schedule.getShiftDateDisplay()) %></td>
                            <td><%= HtmlUtil.escape(schedule.getTimeRangeDisplay()) %></td>
                            <td><%= HtmlUtil.escape(schedule.getShiftTypeLabel()) %></td>
                            <td><span class="status <%= schedule.getStatusCss() %>"><%= HtmlUtil.escape(schedule.getStatus()) %></span></td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="panel operations-table-panel" style="margin-top:24px">
            <div class="panel-header"><div><h3>Upcoming &amp; recent event assignments</h3></div></div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead><tr><th>Staff member</th><th>Event</th><th>Role</th><th>Date</th><th>Time</th><th>Status</th></tr></thead>
                    <tbody>
                    <% if (assignments == null || assignments.isEmpty()) { %>
                        <tr><td colspan="6"><div class="empty-table-message">No event assignments recorded yet.</div></td></tr>
                    <% } else { for (EventStaffAssignmentRecord assignment : assignments) { %>
                        <tr>
                            <td><a href="<%= ctx %>/staff/staff-scheduling/view?id=<%= assignment.getStaffId() %>"><%= HtmlUtil.escape(assignment.getStaffName()) %></a></td>
                            <td><%= HtmlUtil.escape(assignment.getEventLabel()) %></td>
                            <td><%= HtmlUtil.escape(assignment.getAssignmentRole()) %></td>
                            <td><%= HtmlUtil.escape(assignment.getAssignmentDateDisplay()) %></td>
                            <td><%= HtmlUtil.escape(assignment.getTimeRangeDisplay()) %></td>
                            <td><span class="status <%= assignment.getStatusCss() %>"><%= HtmlUtil.escape(assignment.getStatus()) %></span></td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
