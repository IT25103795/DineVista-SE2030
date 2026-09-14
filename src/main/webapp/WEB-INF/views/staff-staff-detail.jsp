<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.StaffMemberRecord" %>
<%@ page import="com.dinevista.model.StaffScheduleRecord" %>
<%@ page import="com.dinevista.model.EventStaffAssignmentRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    StaffMemberRecord staff = (StaffMemberRecord) request.getAttribute("staffMember");
    List<StaffScheduleRecord> schedules = (List<StaffScheduleRecord>) request.getAttribute("schedules");
    List<EventStaffAssignmentRecord> assignments = (List<EventStaffAssignmentRecord>) request.getAttribute("assignments");
    request.setAttribute("pageTitle", staff.getFullName());
    request.setAttribute("activeNav", "staffStaffScheduling");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/staff-scheduling">Staff Scheduling</a><span>/</span><span><%= HtmlUtil.escape(staff.getFullName()) %></span></div>
            <span class="eyebrow">Staff Scheduling</span>
            <h1><%= HtmlUtil.escape(staff.getFullName()) %></h1>
            <p>Update availability, schedule shifts, and assign this staff member to events. Any overlapping shift or assignment is rejected automatically.</p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/staff-scheduling">Back to roster</a>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container detail-layout">
        <div class="detail-main">
            <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
            <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><div><strong>Unable to complete that action:</strong><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div></div><% } %>

            <article class="detail-card">
                <div class="detail-card-header"><div><span class="record-reference">Staff record</span><h2><%= HtmlUtil.escape(staff.getFullName()) %></h2></div><span class="status <%= staff.getStatusCss() %>"><%= HtmlUtil.escape(staff.getAvailabilityStatus()) %></span></div>
                <div class="detail-fact-grid">
                    <div><span>Role</span><strong><%= HtmlUtil.escape(staff.getJobTitle() == null ? "Not set" : staff.getJobTitle()) %></strong></div>
                    <div><span>Department</span><strong><%= HtmlUtil.escape(staff.getDepartment() == null ? "Not set" : staff.getDepartment()) %></strong></div>
                    <div><span>Employee code</span><strong><%= HtmlUtil.escape(staff.getEmployeeCode() == null ? "Not set" : staff.getEmployeeCode()) %></strong></div>
                </div>
            </article>

            <article class="detail-card">
                <span class="section-kicker">Coordinator action</span>
                <h3>Update availability status</h3>
                <form method="post" action="<%= ctx %>/staff/staff-scheduling/status" class="availability-form">
                    <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="availabilityStatus">Availability</label>
                            <select class="form-control" id="availabilityStatus" name="availabilityStatus" required>
                                <%
                                    String[] avail = {"AVAILABLE","UNAVAILABLE","ON_LEAVE"};
                                    for (String a : avail) {
                                %>
                                    <option value="<%= a %>" <%= a.equals(staff.getAvailabilityStatus()) ? "selected" : "" %>><%= a.replace('_',' ') %></option>
                                <% } %>
                            </select>
                        </div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary" type="submit">Update status</button></div>
                </form>
            </article>

            <article class="detail-card">
                <span class="section-kicker">Coordinator action</span>
                <h3>Schedule a shift</h3>
                <p class="muted">A shift that overlaps another active shift or event assignment for this staff member on the same date is rejected.</p>
                <form method="post" action="<%= ctx %>/staff/staff-scheduling/shifts/save">
                    <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="shiftDate">Shift date</label>
                            <input class="form-control" id="shiftDate" name="shiftDate" type="date" required>
                        </div>
                        <div class="form-group">
                            <label for="shiftType">Shift type</label>
                            <select class="form-control" id="shiftType" name="shiftType" required>
                                <% for (String t : new String[]{"RESTAURANT","KITCHEN","EVENT","DELIVERY","ADMIN"}) { %>
                                    <option value="<%= t %>"><%= t %></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="startTime">Start time</label>
                            <input class="form-control" id="startTime" name="startTime" type="time" required>
                        </div>
                        <div class="form-group">
                            <label for="endTime">End time</label>
                            <input class="form-control" id="endTime" name="endTime" type="time" required>
                        </div>
                        <div class="form-group full"><label for="notes">Notes (optional)</label><input class="form-control" id="notes" name="notes" type="text" maxlength="255"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary" type="submit">Schedule shift</button></div>
                </form>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Shift history</h3></div></div>
                <div class="table-wrap">
                    <table class="data-table operations-table">
                        <thead><tr><th>Date</th><th>Time</th><th>Type</th><th>Status</th><th></th></tr></thead>
                        <tbody>
                        <% if (schedules == null || schedules.isEmpty()) { %>
                            <tr><td colspan="5"><div class="empty-table-message">No shifts scheduled yet.</div></td></tr>
                        <% } else { for (StaffScheduleRecord schedule : schedules) { %>
                            <tr>
                                <td><%= HtmlUtil.escape(schedule.getShiftDateDisplay()) %></td>
                                <td><%= HtmlUtil.escape(schedule.getTimeRangeDisplay()) %></td>
                                <td><%= HtmlUtil.escape(schedule.getShiftTypeLabel()) %></td>
                                <td><span class="status <%= schedule.getStatusCss() %>"><%= HtmlUtil.escape(schedule.getStatus()) %></span></td>
                                <td>
                                    <% if ("SCHEDULED".equals(schedule.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/staff-scheduling/shifts/status" style="display:inline">
                                            <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                                            <input type="hidden" name="scheduleId" value="<%= schedule.getId() %>">
                                            <input type="hidden" name="status" value="CONFIRMED">
                                            <button class="btn btn-secondary btn-sm" type="submit">Confirm</button>
                                        </form>
                                    <% } %>
                                    <% if ("CONFIRMED".equals(schedule.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/staff-scheduling/shifts/status" style="display:inline">
                                            <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                                            <input type="hidden" name="scheduleId" value="<%= schedule.getId() %>">
                                            <input type="hidden" name="status" value="COMPLETED">
                                            <button class="btn btn-secondary btn-sm" type="submit">Mark completed</button>
                                        </form>
                                        <form method="post" action="<%= ctx %>/staff/staff-scheduling/shifts/status" style="display:inline">
                                            <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                                            <input type="hidden" name="scheduleId" value="<%= schedule.getId() %>">
                                            <input type="hidden" name="status" value="ABSENT">
                                            <button class="btn btn-secondary btn-sm" type="submit">Mark absent</button>
                                        </form>
                                    <% } %>
                                    <% if (!"CANCELLED".equals(schedule.getStatus()) && !"COMPLETED".equals(schedule.getStatus()) && !"ABSENT".equals(schedule.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/staff-scheduling/shifts/status" style="display:inline"
                                              onsubmit="return confirm('Cancel this shift?');">
                                            <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                                            <input type="hidden" name="scheduleId" value="<%= schedule.getId() %>">
                                            <input type="hidden" name="status" value="CANCELLED">
                                            <button class="btn btn-danger btn-sm" type="submit">Cancel</button>
                                        </form>
                                    <% } %>
                                </td>
                            </tr>
                        <% }} %>
                        </tbody>
                    </table>
                </div>
            </article>

            <article class="detail-card">
                <span class="section-kicker">Coordinator action</span>
                <h3>Assign to an event</h3>
                <p class="muted">An assignment that overlaps another active assignment or shift for this staff member on the same date is rejected.</p>
                <form method="post" action="<%= ctx %>/staff/staff-scheduling/assignments/save">
                    <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                    <div class="form-grid">
                        <div class="form-group full">
                            <label for="eventLabel">Event name / reference</label>
                            <input class="form-control" id="eventLabel" name="eventLabel" type="text" required maxlength="180" placeholder="e.g. Perera Wedding Reception">
                        </div>
                        <div class="form-group">
                            <label for="assignmentRole">Role at this event</label>
                            <input class="form-control" id="assignmentRole" name="assignmentRole" type="text" required maxlength="100" placeholder="e.g. Banquet supervisor">
                        </div>
                        <div class="form-group">
                            <label for="assignmentDate">Event date</label>
                            <input class="form-control" id="assignmentDate" name="assignmentDate" type="date" required>
                        </div>
                        <div class="form-group">
                            <label for="startTime2">Start time</label>
                            <input class="form-control" id="startTime2" name="startTime" type="time" required>
                        </div>
                        <div class="form-group">
                            <label for="endTime2">End time</label>
                            <input class="form-control" id="endTime2" name="endTime" type="time" required>
                        </div>
                        <div class="form-group full"><label for="notes2">Notes (optional)</label><input class="form-control" id="notes2" name="notes" type="text" maxlength="500"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary" type="submit">Assign to event</button></div>
                </form>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Event assignment history</h3></div></div>
                <div class="table-wrap">
                    <table class="data-table operations-table">
                        <thead><tr><th>Event</th><th>Role</th><th>Date</th><th>Time</th><th>Status</th><th></th></tr></thead>
                        <tbody>
                        <% if (assignments == null || assignments.isEmpty()) { %>
                            <tr><td colspan="6"><div class="empty-table-message">No event assignments yet.</div></td></tr>
                        <% } else { for (EventStaffAssignmentRecord assignment : assignments) { %>
                            <tr>
                                <td><strong><%= HtmlUtil.escape(assignment.getEventLabel()) %></strong></td>
                                <td><%= HtmlUtil.escape(assignment.getAssignmentRole()) %></td>
                                <td><%= HtmlUtil.escape(assignment.getAssignmentDateDisplay()) %></td>
                                <td><%= HtmlUtil.escape(assignment.getTimeRangeDisplay()) %></td>
                                <td><span class="status <%= assignment.getStatusCss() %>"><%= HtmlUtil.escape(assignment.getStatus()) %></span></td>
                                <td>
                                    <% if ("ASSIGNED".equals(assignment.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/staff-scheduling/assignments/status" style="display:inline">
                                            <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                                            <input type="hidden" name="assignmentId" value="<%= assignment.getId() %>">
                                            <input type="hidden" name="status" value="CONFIRMED">
                                            <button class="btn btn-secondary btn-sm" type="submit">Confirm</button>
                                        </form>
                                    <% } %>
                                    <% if ("CONFIRMED".equals(assignment.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/staff-scheduling/assignments/status" style="display:inline">
                                            <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                                            <input type="hidden" name="assignmentId" value="<%= assignment.getId() %>">
                                            <input type="hidden" name="status" value="COMPLETED">
                                            <button class="btn btn-secondary btn-sm" type="submit">Mark completed</button>
                                        </form>
                                    <% } %>
                                    <% if (!"CANCELLED".equals(assignment.getStatus()) && !"COMPLETED".equals(assignment.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/staff-scheduling/assignments/status" style="display:inline"
                                              onsubmit="return confirm('Cancel this assignment?');">
                                            <input type="hidden" name="staffId" value="<%= staff.getId() %>">
                                            <input type="hidden" name="assignmentId" value="<%= assignment.getId() %>">
                                            <input type="hidden" name="status" value="CANCELLED">
                                            <button class="btn btn-danger btn-sm" type="submit">Cancel</button>
                                        </form>
                                    <% } %>
                                </td>
                            </tr>
                        <% }} %>
                        </tbody>
                    </table>
                </div>
            </article>
        </div>

        <aside class="detail-sidebar">
            <article class="panel">
                <span class="section-kicker">Business checks</span>
                <ul class="check-list operational-checks"><li>No two active shifts or assignments for one staff member may overlap.</li><li>Unavailable staff cannot be newly scheduled or assigned.</li><li>Every status change stays on record — nothing is deleted.</li></ul>
            </article>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
