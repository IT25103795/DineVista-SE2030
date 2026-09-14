<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<%@ page import="com.dinevista.model.EventPackageRecord" %>
<%@ page import="com.dinevista.model.EventVenueRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Arrays" %>
<%
    request.setAttribute("pageTitle", "Manage Event Booking");
    request.setAttribute("activeNav", "staffEventBookings");
    EventBookingRecord b = (EventBookingRecord) request.getAttribute("booking");
    List<EventPackageRecord> packages = (List<EventPackageRecord>) request.getAttribute("packages");
    List<EventVenueRecord> venues = (List<EventVenueRecord>) request.getAttribute("venues");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <div class="breadcrumbs" style="margin-bottom:8px;">
                <a href="<%= ctx %>/staff/event-bookings">Event bookings</a>
                <span>/</span>
                <span>Update <%= b != null ? HtmlUtil.escape(b.getReference()) : "" %></span>
            </div>
            <span class="eyebrow">Event operations</span>
            <h1>Update <%= b != null ? HtmlUtil.escape(b.getReference()) : "booking" %></h1>
            <p>Review customer details, modify package selection, update venue allocation, or advance booking status.</p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/staff/event-bookings/view?reference=<%= b != null ? b.getReference() : "" %>">&larr; Return to review</a>
        </div>
    </div>
</section>

<div class="container operations-container">
    <% List<String> errors = (List<String>) request.getAttribute("errors");
       if (errors != null && !errors.isEmpty()) { %>
        <div class="alert alert-error" style="margin-bottom:20px;">
            <ul style="margin:0;padding-left:18px;">
                <% for (String e : errors) { %>
                    <li><%= HtmlUtil.escape(e) %></li>
                <% } %>
            </ul>
        </div>
    <% } %>

    <% if (b == null) { %>
        <div class="alert alert-error">Event booking not found.</div>
    <% } else { %>
        <section class="panel">
            <div class="panel-header">
                <h3>Booking details &amp; status transition</h3>
                <p class="muted small">Changes will update the live booking record and notify the coordinator team.</p>
            </div>

            <form method="post" action="<%= ctx %>/staff/event-bookings/update">
                <input type="hidden" name="reference" value="<%= b.getReference() %>">

                <div class="form-grid" style="display:grid;grid-template-columns:repeat(auto-fit, minmax(280px, 1fr));gap:18px;">
                    <div class="form-group">
                        <label>Contact name <span style="color:#c53030">*</span></label>
                        <input class="form-control" name="customerName" required value="<%= HtmlUtil.escape(b.getCustomerName()) %>">
                    </div>

                    <div class="form-group">
                        <label>Email <span style="color:#c53030">*</span></label>
                        <input class="form-control" name="email" type="email" required value="<%= HtmlUtil.escape(b.getEmail()) %>">
                    </div>

                    <div class="form-group">
                        <label>Phone <span style="color:#c53030">*</span></label>
                        <input class="form-control" name="phone" required value="<%= HtmlUtil.escape(b.getPhone()) %>">
                    </div>

                    <div class="form-group">
                        <label>Event type <span style="color:#c53030">*</span></label>
                        <input class="form-control" name="eventType" required value="<%= HtmlUtil.escape(b.getEventType()) %>">
                    </div>

                    <div class="form-group">
                        <label>Event package <span style="color:#c53030">*</span></label>
                        <select class="form-control" name="packageId" required>
                            <% if (packages != null) {
                                for (EventPackageRecord p : packages) { %>
                                <option value="<%= p.getId() %>" <%= p.getId() == b.getPackageId() ? "selected" : "" %>>
                                    <%= HtmlUtil.escape(p.getName()) %> &mdash; LKR <%= String.format("%,.2f", p.getPricePerGuest()) %>
                                </option>
                            <%  }
                            } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Venue allocation <span style="color:#c53030">*</span></label>
                        <select class="form-control" name="venueId" required>
                            <% if (venues != null) {
                                for (EventVenueRecord v : venues) { %>
                                <option value="<%= v.getId() %>" <%= v.getId() == b.getVenueId() ? "selected" : "" %>>
                                    <%= HtmlUtil.escape(v.getName()) %> (up to <%= v.getCapacity() %> guests)
                                </option>
                            <%  }
                            } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Event date <span style="color:#c53030">*</span></label>
                        <input class="form-control" type="date" name="eventDate" required value="<%= b.getEventDate() %>">
                    </div>

                    <div class="form-group">
                        <label>Start time <span style="color:#c53030">*</span></label>
                        <input class="form-control" type="time" name="eventTime" required value="<%= b.getEventTime() %>">
                    </div>

                    <div class="form-group">
                        <label>Expected guests <span style="color:#c53030">*</span></label>
                        <input class="form-control" type="number" min="1" name="guestCount" required value="<%= b.getGuestCount() %>">
                    </div>

                    <div class="form-group">
                        <label>Booking status <span style="color:#c53030">*</span></label>
                        <select class="form-control" name="status" required>
                            <% for (String st : Arrays.asList("INQUIRY", "CONSULTATION", "QUOTED", "CONFIRMED", "COMPLETED", "CANCELLED")) { %>
                                <option value="<%= st %>" <%= st.equals(b.getStatus()) ? "selected" : "" %>><%= st %></option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group full" style="grid-column:1/-1;">
                        <label>Requirements, notes &amp; cancellation reason</label>
                        <textarea class="form-control" name="notes" maxlength="5000" rows="4"><%= HtmlUtil.escape(b.getNotes()) %></textarea>
                    </div>
                </div>

                <div class="form-actions" style="margin-top:24px;display:flex;gap:12px;">
                    <button class="btn btn-primary" type="submit">Save booking changes</button>
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/event-bookings/view?reference=<%= b.getReference() %>">Cancel</a>
                </div>
            </form>
        </section>
    <% } %>
</div>

<%@ include file="fragments/footer.jspf" %>
