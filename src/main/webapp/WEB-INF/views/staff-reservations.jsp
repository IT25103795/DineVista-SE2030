<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.TableReservationRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Reservation Operations");
    request.setAttribute("activeNav", "staffReservations");
    List<TableReservationRecord> reservations = (List<TableReservationRecord>) request.getAttribute("staffReservations");
    String statusFilter = request.getAttribute("reservationFilterStatus") == null ? "" : request.getAttribute("reservationFilterStatus").toString();
    String dateFilter = request.getAttribute("reservationFilterDate") == null ? "" : request.getAttribute("reservationFilterDate").toString();
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <span class="eyebrow">Restaurant staff operations</span>
            <h1>Table reservation control centre.</h1>
            <p>Review requests, assign suitable tables, confirm or reject reservations, seat guests, complete visits, and prevent overlapping table allocations.</p>
        </div>
        <div class="hero-actions"><a class="btn btn-secondary" href="<%= ctx %>/dashboard">Operations dashboard</a></div>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
        <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div><% } %>

        <form class="operations-filter" method="get" action="<%= ctx %>/staff/reservations">
            <div class="form-group"><label for="status">Status</label><select class="form-control" id="status" name="status">
                <option value="">All statuses</option>
                <option value="PENDING" <%= HtmlUtil.selected("PENDING", statusFilter) %>>Pending</option>
                <option value="CONFIRMED" <%= HtmlUtil.selected("CONFIRMED", statusFilter) %>>Confirmed</option>
                <option value="SEATED" <%= HtmlUtil.selected("SEATED", statusFilter) %>>Seated</option>
                <option value="COMPLETED" <%= HtmlUtil.selected("COMPLETED", statusFilter) %>>Completed</option>
                <option value="CANCELLED" <%= HtmlUtil.selected("CANCELLED", statusFilter) %>>Cancelled</option>
                <option value="REJECTED" <%= HtmlUtil.selected("REJECTED", statusFilter) %>>Rejected</option>
                <option value="NO_SHOW" <%= HtmlUtil.selected("NO_SHOW", statusFilter) %>>No show</option>
            </select></div>
            <div class="form-group"><label for="date">Reservation date</label><input class="form-control" id="date" name="date" type="date" value="<%= HtmlUtil.escape(dateFilter) %>"></div>
            <button class="btn btn-dark" type="submit">Apply filters</button>
            <a class="btn btn-secondary" href="<%= ctx %>/staff/reservations">Clear</a>
        </form>

        <div class="operations-summary">
            <div><strong><%= reservations == null ? 0 : reservations.size() %></strong><span>Matching reservations</span></div>
            <div><strong><%= reservations == null ? 0 : reservations.stream().filter(item -> "PENDING".equals(item.getStatus())).count() %></strong><span>Awaiting review</span></div>
            <div><strong><%= reservations == null ? 0 : reservations.stream().filter(item -> "CONFIRMED".equals(item.getStatus())).count() %></strong><span>Confirmed</span></div>
            <div><strong>90 min</strong><span>Protected table slot</span></div>
        </div>

        <section class="panel operations-table-panel">
            <div class="panel-header"><div><h3>Reservation records</h3><span class="muted small">Open a record to assign a table or update its status.</span></div></div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead><tr><th>Reference</th><th>Guest</th><th>Date and time</th><th>Party</th><th>Preference</th><th>Table</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <% if (reservations == null || reservations.isEmpty()) { %>
                        <tr><td colspan="8"><div class="empty-table-message">No reservation records match the current filters.</div></td></tr>
                    <% } else { for (TableReservationRecord reservation : reservations) { %>
                        <tr>
                            <td><strong class="mono"><%= HtmlUtil.escape(reservation.getReference()) %></strong></td>
                            <td><strong><%= HtmlUtil.escape(reservation.getGuestName()) %></strong><span class="table-subtext"><%= HtmlUtil.escape(reservation.getPhone()) %></span></td>
                            <td><%= reservation.getDateDisplay() %><span class="table-subtext"><%= reservation.getTimeDisplay() %></span></td>
                            <td><%= reservation.getPartySize() %></td>
                            <td><%= HtmlUtil.escape(reservation.getSeatingPreferenceDisplay()) %></td>
                            <td><%= reservation.getTableCode() == null ? "Unassigned" : HtmlUtil.escape(reservation.getTableCode()) %></td>
                            <td><span class="status <%= reservation.getStatusCss() %>"><%= HtmlUtil.escape(reservation.getStatus().replace('_', ' ')) %></span></td>
                            <td><a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/reservations/view?reference=<%= reservation.getReference() %>">Manage</a></td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
