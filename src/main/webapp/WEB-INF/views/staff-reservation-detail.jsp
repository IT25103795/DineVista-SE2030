<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.TableReservationRecord" %>
<%@ page import="com.dinevista.model.RestaurantTableRecord" %>
<%@ page import="com.dinevista.model.StatusHistoryRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Manage Reservation");
    request.setAttribute("activeNav", "staffReservations");
    TableReservationRecord reservation = (TableReservationRecord) request.getAttribute("reservation");
    List<RestaurantTableRecord> availableTables = (List<RestaurantTableRecord>) request.getAttribute("availableTables");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div><div class="breadcrumbs dark"><a href="<%= ctx %>/staff/reservations">Reservation operations</a><span>/</span><span><%= HtmlUtil.escape(reservation.getReference()) %></span></div><span class="eyebrow">Staff reservation review</span><h1><%= HtmlUtil.escape(reservation.getReference()) %></h1><p>Validate availability, assign a suitable table, record a decision, and maintain a complete status history.</p></div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/reservations">Back to reservations</a>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container detail-layout">
        <div class="detail-main">
            <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
            <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><div><strong>Unable to update the reservation:</strong><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div></div><% } %>

            <article class="detail-card">
                <div class="detail-card-header"><div><span class="record-reference">Customer reservation</span><h2><%= HtmlUtil.escape(reservation.getGuestName()) %></h2></div><span class="status <%= reservation.getStatusCss() %>"><%= HtmlUtil.escape(reservation.getStatus().replace('_', ' ')) %></span></div>
                <div class="detail-fact-grid">
                    <div><span>Date</span><strong><%= reservation.getDateDisplay() %></strong></div>
                    <div><span>Time</span><strong><%= reservation.getTimeDisplay() %></strong></div>
                    <div><span>Party size</span><strong><%= reservation.getPartySize() %> guests</strong></div>
                    <div><span>Seating preference</span><strong><%= HtmlUtil.escape(reservation.getSeatingPreferenceDisplay()) %></strong></div>
                    <div><span>Assigned table</span><strong><%= reservation.getTableCode() == null ? "Not assigned" : HtmlUtil.escape(reservation.getTableCode()) %></strong></div>
                    <div><span>Mobile</span><strong><%= HtmlUtil.escape(reservation.getPhone()) %></strong></div>
                </div>
                <% if (!reservation.getOccasionNotes().isEmpty()) { %><div class="note-box"><span>Customer request</span><p><%= HtmlUtil.escape(reservation.getOccasionNotes()) %></p></div><% } %>
            </article>

            <article class="detail-card">
                <span class="section-kicker">Staff action</span>
                <h3>Assign table and update status</h3>
                <p class="muted">Confirmation requires an available table. The system blocks tables that overlap another active reservation within 90 minutes.</p>
                <form method="post" action="<%= ctx %>/staff/reservations/update" data-confirm-form>
                    <input type="hidden" name="reference" value="<%= HtmlUtil.escape(reservation.getReference()) %>">
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="tableId">Available table</label>
                            <select class="form-control" id="tableId" name="tableId">
                                <option value="0">Keep current assignment</option>
                                <% if (availableTables != null) { for (RestaurantTableRecord table : availableTables) { %>
                                    <option value="<%= table.getId() %>">
                                        <%= HtmlUtil.escape(table.getCode()) %> — <%= HtmlUtil.escape(table.getSeatingAreaDisplay()) %> — <%= table.getCapacity() %> seats
                                    </option>
                                <% }} %>
                            </select>
                            <% if (availableTables == null || availableTables.isEmpty()) { %><span class="form-note danger-text">No suitable table is currently available for this slot.</span><% } %>
                        </div>
                        <div class="form-group">
                            <label for="status">New status</label>
                            <select class="form-control" id="status" name="status" required>
                                <option value="<%= HtmlUtil.escape(reservation.getStatus()) %>">Keep <%= HtmlUtil.escape(reservation.getStatus()) %></option>
                                <% if ("PENDING".equals(reservation.getStatus())) { %>
                                    <option value="CONFIRMED">Confirm reservation</option>
                                    <option value="REJECTED">Reject reservation</option>
                                    <option value="CANCELLED">Cancel reservation</option>
                                <% } else if ("CONFIRMED".equals(reservation.getStatus())) { %>
                                    <option value="SEATED">Mark guest as seated</option>
                                    <option value="NO_SHOW">Mark as no-show</option>
                                    <option value="CANCELLED">Cancel reservation</option>
                                <% } else if ("SEATED".equals(reservation.getStatus())) { %>
                                    <option value="COMPLETED">Complete dining visit</option>
                                <% } %>
                            </select>
                        </div>
                        <div class="form-group full"><label for="note">Staff note or decision reason</label><textarea class="form-control" id="note" name="note" maxlength="500" placeholder="Confirmation note, rejection reason, cancellation reason, or operational comment"></textarea></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary" type="submit">Save staff decision</button></div>
                </form>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Status history</h3><span class="muted small">Complete audit trail for this reservation.</span></div></div>
                <div class="timeline">
                    <% for (StatusHistoryRecord item : reservation.getHistory()) { %>
                        <div class="timeline-item"><span class="timeline-dot"></span><div><div class="timeline-top"><strong><%= HtmlUtil.escape(item.getStatus().replace('_', ' ')) %></strong><span><%= item.getChangedAtDisplay() %></span></div><p><%= HtmlUtil.escape(item.getNote()) %></p><small>By <%= HtmlUtil.escape(item.getChangedBy()) %></small></div></div>
                    <% } %>
                </div>
            </article>
        </div>

        <aside class="detail-sidebar">
            <article class="panel">
                <span class="section-kicker">Customer details</span>
                <div class="contact-stack"><div><span>Name</span><strong><%= HtmlUtil.escape(reservation.getGuestName()) %></strong></div><div><span>Email</span><strong><%= HtmlUtil.escape(reservation.getEmail()) %></strong></div><div><span>Phone</span><strong><%= HtmlUtil.escape(reservation.getPhone()) %></strong></div></div>
            </article>
            <article class="panel">
                <span class="section-kicker">Business checks</span>
                <ul class="check-list operational-checks"><li>Capacity must support the party size.</li><li>Table must be available for the 90-minute slot.</li><li>Reject or cancel actions require a reason.</li><li>Closed records cannot be changed.</li></ul>
            </article>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
