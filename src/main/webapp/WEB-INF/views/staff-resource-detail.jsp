<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.EventResourceRecord" %>
<%@ page import="com.dinevista.model.ResourceBookingRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    EventResourceRecord resource = (EventResourceRecord) request.getAttribute("resource");
    List<ResourceBookingRecord> bookings = (List<ResourceBookingRecord>) request.getAttribute("resourceBookings");
    request.setAttribute("pageTitle", resource.getName());
    request.setAttribute("activeNav", "staffEventResources");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/event-resources">Event Resources</a><span>/</span><span><%= HtmlUtil.escape(resource.getName()) %></span></div>
            <span class="eyebrow">Event Resource Management</span>
            <h1><%= HtmlUtil.escape(resource.getName()) %></h1>
            <p>Reserve units for an event and review its full booking history. A request that would exceed capacity on that date is rejected automatically.</p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/event-resources">Back to overview</a>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container detail-layout">
        <div class="detail-main">
            <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
            <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><div><strong>Unable to complete that action:</strong><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div></div><% } %>

            <article class="detail-card">
                <div class="detail-card-header"><div><span class="record-reference">Resource record</span><h2><%= HtmlUtil.escape(resource.getName()) %></h2></div><span class="status <%= resource.getStatusCss() %>"><%= HtmlUtil.escape(resource.getStatus()) %></span></div>
                <div class="detail-fact-grid">
                    <div><span>Category</span><strong><%= HtmlUtil.escape(resource.getCategoryLabel()) %></strong></div>
                    <div><span>Usable / total</span><strong><%= resource.getAvailableQuantity() %> / <%= resource.getTotalQuantity() %></strong></div>
                    <div><span>Unit cost</span><strong><%= HtmlUtil.escape(resource.getUnitCostDisplay()) %></strong></div>
                </div>
            </article>

            <article class="detail-card">
                <span class="section-kicker">Coordinator action</span>
                <h3>Reserve units for an event</h3>
                <p class="muted">Enter the event name, date, and how many units you need. A request that would exceed the usable quantity on that date is rejected.</p>
                <form method="post" action="<%= ctx %>/staff/event-resources/resources/book">
                    <input type="hidden" name="resourceId" value="<%= resource.getId() %>">
                    <div class="form-grid">
                        <div class="form-group full">
                            <label for="eventLabel">Event name / reference</label>
                            <input class="form-control" id="eventLabel" name="eventLabel" type="text" required maxlength="180" placeholder="e.g. Perera Wedding Reception">
                        </div>
                        <div class="form-group">
                            <label for="eventDate">Event date</label>
                            <input class="form-control" id="eventDate" name="eventDate" type="date" required>
                        </div>
                        <div class="form-group">
                            <label for="quantity">Quantity needed</label>
                            <input class="form-control" id="quantity" name="quantity" type="number" min="1" max="<%= resource.getAvailableQuantity() %>" required>
                        </div>
                        <div class="form-group full"><label for="notes">Notes (optional)</label><input class="form-control" id="notes" name="notes" type="text" maxlength="500"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary" type="submit" <%= resource.isBookable() ? "" : "disabled" %>>Request booking</button></div>
                </form>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Booking history</h3><span class="muted small">Allocate a requested booking, mark units returned, or cancel — history stays on record.</span></div></div>
                <div class="table-wrap">
                    <table class="data-table operations-table">
                        <thead><tr><th>Event</th><th>Date</th><th>Qty</th><th>Status</th><th></th></tr></thead>
                        <tbody>
                        <% if (bookings == null || bookings.isEmpty()) { %>
                            <tr><td colspan="5"><div class="empty-table-message">No bookings recorded yet.</div></td></tr>
                        <% } else { for (ResourceBookingRecord booking : bookings) { %>
                            <tr>
                                <td><strong><%= HtmlUtil.escape(booking.getEventLabel()) %></strong></td>
                                <td><%= HtmlUtil.escape(booking.getEventDateDisplay()) %></td>
                                <td><%= booking.getQuantityReserved() %></td>
                                <td><span class="status <%= booking.getStatusCss() %>"><%= HtmlUtil.escape(booking.getStatus()) %></span></td>
                                <td>
                                    <% if ("REQUESTED".equals(booking.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/event-resources/resources/booking-status" style="display:inline">
                                            <input type="hidden" name="resourceId" value="<%= resource.getId() %>">
                                            <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
                                            <input type="hidden" name="status" value="ALLOCATED">
                                            <button class="btn btn-secondary btn-sm" type="submit">Allocate</button>
                                        </form>
                                    <% } %>
                                    <% if ("ALLOCATED".equals(booking.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/event-resources/resources/booking-status" style="display:inline">
                                            <input type="hidden" name="resourceId" value="<%= resource.getId() %>">
                                            <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
                                            <input type="hidden" name="status" value="RETURNED">
                                            <button class="btn btn-secondary btn-sm" type="submit">Mark returned</button>
                                        </form>
                                    <% } %>
                                    <% if (!"CANCELLED".equals(booking.getStatus()) && !"RETURNED".equals(booking.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/event-resources/resources/booking-status" style="display:inline"
                                              onsubmit="return confirm('Cancel this booking?');">
                                            <input type="hidden" name="resourceId" value="<%= resource.getId() %>">
                                            <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
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
                <span class="section-kicker">Manage this record</span>
                <a class="btn btn-secondary btn-sm" style="margin-top:12px;display:inline-flex" href="<%= ctx %>/staff/event-resources/resources/edit?id=<%= resource.getId() %>">Edit details</a>
                <form method="post" action="<%= ctx %>/staff/event-resources/resources/delete" style="margin-top:10px"
                      onsubmit="return confirm('Delete this resource? Only possible while it has no booking history.');">
                    <input type="hidden" name="id" value="<%= resource.getId() %>">
                    <button class="btn btn-danger btn-sm" type="submit">Delete resource</button>
                </form>
            </article>
            <article class="panel">
                <span class="section-kicker">Business checks</span>
                <ul class="check-list operational-checks"><li>Reserved quantities on the same date can never exceed the usable total.</li><li>Resources with recorded bookings cannot be deleted.</li><li>Every allocation, return, and cancellation stays on record.</li></ul>
            </article>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
