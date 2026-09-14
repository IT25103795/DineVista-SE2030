<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.EventVenueRecord" %>
<%@ page import="com.dinevista.model.EventVenueBookingRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    EventVenueRecord venue = (EventVenueRecord) request.getAttribute("venue");
    List<EventVenueBookingRecord> bookings = (List<EventVenueBookingRecord>) request.getAttribute("venueBookings");
    request.setAttribute("pageTitle", venue.getName());
    request.setAttribute("activeNav", "staffEventResources");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/event-resources">Event Resources</a><span>/</span><span><%= HtmlUtil.escape(venue.getName()) %></span></div>
            <span class="eyebrow">Event Resource Management</span>
            <h1><%= HtmlUtil.escape(venue.getName()) %></h1>
            <p>Book this venue for an event and review its full booking history. Overlapping bookings on the same date are rejected automatically.</p>
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
                <div class="detail-card-header"><div><span class="record-reference">Venue record</span><h2><%= HtmlUtil.escape(venue.getName()) %></h2></div><span class="status <%= venue.getStatusCss() %>"><%= HtmlUtil.escape(venue.getAvailabilityStatus()) %></span></div>
                <div class="detail-fact-grid">
                    <div><span>Type</span><strong><%= HtmlUtil.escape(venue.getVenueTypeLabel()) %></strong></div>
                    <div><span>Capacity</span><strong><%= venue.getCapacity() %> guests</strong></div>
                    <div><span>Base fee</span><strong><%= HtmlUtil.escape(venue.getBaseFeeDisplay()) %></strong></div>
                    <div><span>Description</span><strong><%= HtmlUtil.escape(venue.getDescription() == null ? "Not set" : venue.getDescription()) %></strong></div>
                </div>
            </article>

            <article class="detail-card">
                <span class="section-kicker">Coordinator action</span>
                <h3>Book this venue for an event</h3>
                <p class="muted">Enter the event name, date, and time window. A booking that overlaps another active booking for this venue on the same date is rejected.</p>
                <form method="post" action="<%= ctx %>/staff/event-resources/venues/book">
                    <input type="hidden" name="venueId" value="<%= venue.getId() %>">
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
                            <label for="guestCount">Expected guests (optional)</label>
                            <input class="form-control" id="guestCount" name="guestCount" type="number" min="1" max="<%= venue.getCapacity() %>">
                        </div>
                        <div class="form-group">
                            <label for="startTime">Start time</label>
                            <input class="form-control" id="startTime" name="startTime" type="time" required>
                        </div>
                        <div class="form-group">
                            <label for="endTime">End time</label>
                            <input class="form-control" id="endTime" name="endTime" type="time" required>
                        </div>
                        <div class="form-group full"><label for="notes">Notes (optional)</label><input class="form-control" id="notes" name="notes" type="text" maxlength="500"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary" type="submit" <%= venue.isBookable() ? "" : "disabled" %>>Request booking</button></div>
                </form>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Booking history</h3><span class="muted small">Confirm a requested booking or cancel it — cancelled bookings stay on record.</span></div></div>
                <div class="table-wrap">
                    <table class="data-table operations-table">
                        <thead><tr><th>Event</th><th>Date</th><th>Time</th><th>Guests</th><th>Status</th><th></th></tr></thead>
                        <tbody>
                        <% if (bookings == null || bookings.isEmpty()) { %>
                            <tr><td colspan="6"><div class="empty-table-message">No bookings recorded yet.</div></td></tr>
                        <% } else { for (EventVenueBookingRecord booking : bookings) { %>
                            <tr>
                                <td><strong><%= HtmlUtil.escape(booking.getEventLabel()) %></strong></td>
                                <td><%= HtmlUtil.escape(booking.getEventDateDisplay()) %></td>
                                <td><%= HtmlUtil.escape(booking.getTimeRangeDisplay()) %></td>
                                <td><%= booking.getGuestCount() == null ? "—" : booking.getGuestCount() %></td>
                                <td><span class="status <%= booking.getStatusCss() %>"><%= HtmlUtil.escape(booking.getStatus()) %></span></td>
                                <td>
                                    <% if ("REQUESTED".equals(booking.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/event-resources/venues/booking-status" style="display:inline">
                                            <input type="hidden" name="venueId" value="<%= venue.getId() %>">
                                            <input type="hidden" name="bookingId" value="<%= booking.getId() %>">
                                            <input type="hidden" name="status" value="CONFIRMED">
                                            <button class="btn btn-secondary btn-sm" type="submit">Confirm</button>
                                        </form>
                                    <% } %>
                                    <% if (!"CANCELLED".equals(booking.getStatus())) { %>
                                        <form method="post" action="<%= ctx %>/staff/event-resources/venues/booking-status" style="display:inline"
                                              onsubmit="return confirm('Cancel this booking?');">
                                            <input type="hidden" name="venueId" value="<%= venue.getId() %>">
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
                <a class="btn btn-secondary btn-sm" style="margin-top:12px;display:inline-flex" href="<%= ctx %>/staff/event-resources/venues/edit?id=<%= venue.getId() %>">Edit details</a>
                <form method="post" action="<%= ctx %>/staff/event-resources/venues/delete" style="margin-top:10px"
                      onsubmit="return confirm('Delete this venue? Only possible while it has no booking history.');">
                    <input type="hidden" name="id" value="<%= venue.getId() %>">
                    <button class="btn btn-danger btn-sm" type="submit">Delete venue</button>
                </form>
            </article>
            <article class="panel">
                <span class="section-kicker">Business checks</span>
                <ul class="check-list operational-checks"><li>No two active bookings for a venue may overlap on the same date.</li><li>Guest count cannot exceed the venue's capacity.</li><li>Venues with recorded bookings cannot be deleted.</li><li>Cancelled bookings remain visible in the history.</li></ul>
            </article>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
