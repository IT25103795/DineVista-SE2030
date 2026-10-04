<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.StaffMemberRecord" %>
<%@ page import="com.dinevista.model.StaffScheduleRecord" %>
<%@ page import="com.dinevista.model.EventStaffAssignmentRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "My Schedule");
    request.setAttribute("activeNav", "staffStaffScheduling");
    StaffMemberRecord member = (StaffMemberRecord) request.getAttribute("staffMember");
    List<StaffScheduleRecord> shifts = (List<StaffScheduleRecord>) request.getAttribute("schedules");
    List<EventStaffAssignmentRecord> assignments = (List<EventStaffAssignmentRecord>) request.getAttribute("assignments");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero"><div class="container"><div>
    <span class="eyebrow">Staff Scheduling</span>
    <h1>My schedule</h1>
    <p>Shifts and event assignments for <%= HtmlUtil.escape(member.getFullName()) %>.</p>
</div><div class="hero-actions"><a class="btn btn-secondary" href="<%= ctx %>/staff/staff-scheduling">Back to scheduling</a></div></div></section>
<section class="section-sm operations-section"><div class="container">
    <section class="panel operations-table-panel"><div class="panel-header"><h3>My shifts</h3></div><div class="table-wrap">
    <table class="data-table operations-table"><thead><tr><th>Date</th><th>Time</th><th>Type</th><th>Status</th></tr></thead><tbody>
    <% if (shifts == null || shifts.isEmpty()) { %><tr><td colspan="4">No shifts are scheduled.</td></tr>
    <% } else { for (StaffScheduleRecord shift : shifts) { %><tr>
        <td><%= HtmlUtil.escape(shift.getShiftDateDisplay()) %></td><td><%= HtmlUtil.escape(shift.getTimeRangeDisplay()) %></td>
        <td><%= HtmlUtil.escape(shift.getShiftTypeLabel()) %></td><td><%= HtmlUtil.escape(shift.getStatus()) %></td>
    </tr><% }} %></tbody></table></div></section>
    <section class="panel operations-table-panel" style="margin-top:24px"><div class="panel-header"><h3>My event assignments</h3></div><div class="table-wrap">
    <table class="data-table operations-table"><thead><tr><th>Event</th><th>Role</th><th>Date</th><th>Time</th><th>Status</th></tr></thead><tbody>
    <% if (assignments == null || assignments.isEmpty()) { %><tr><td colspan="5">No event assignments are scheduled.</td></tr>
    <% } else { for (EventStaffAssignmentRecord assignment : assignments) { %><tr>
        <td><%= HtmlUtil.escape(assignment.getEventLabel()) %></td><td><%= HtmlUtil.escape(assignment.getAssignmentRole()) %></td>
        <td><%= HtmlUtil.escape(assignment.getAssignmentDateDisplay()) %></td><td><%= HtmlUtil.escape(assignment.getTimeRangeDisplay()) %></td>
        <td><%= HtmlUtil.escape(assignment.getStatus()) %></td>
    </tr><% }} %></tbody></table></div></section>
</div></section>
<%@ include file="fragments/footer.jspf" %>
