<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("pageTitle", "Event Bookings");
    request.setAttribute("activeNav", "staffEventBookings");
    List<EventBookingRecord> bs = (List<EventBookingRecord>) request.getAttribute("eventBookings");
    String searchQuery = request.getParameter("search") == null ? "" : request.getParameter("search").trim();
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <span class="eyebrow">Event Booking Management</span>
            <h1>Event bookings &amp; inquiries.</h1>
            <p>Review customer booking inquiries, verify capacity and package options, update operational status, and manage event schedules.</p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/dashboard">Operations dashboard</a>
            <a class="btn btn-secondary" href="<%= ctx %>/staff/event-packages">Manage packages</a>
        </div>
    </div>
</section>

<div class="container operations-container">
    <% if (request.getParameter("updated") != null) { %>
        <div class="alert alert-success" style="margin-bottom:20px;">Event booking updated successfully.</div>
    <% } else if (request.getParameter("deleted") != null) { %>
        <div class="alert alert-success" style="margin-bottom:20px;">Event booking was permanently removed.</div>
    <% } %>

    <section class="panel">
        <div class="panel-header" style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:14px;">
            <div>
                <h3>All event bookings (<%= bs == null ? 0 : bs.size() %>)</h3>
                <p class="muted small">Live customer event requests, consultations, and confirmed bookings.</p>
            </div>
            <form method="get" action="<%= ctx %>/staff/event-bookings" class="search-filter-grid" style="margin:0;">
                <input class="form-control" name="search" placeholder="Search reference, customer, or package..." value="<%= HtmlUtil.escape(searchQuery) %>">
                <button class="btn btn-secondary" type="submit">Search</button>
                <% if (!searchQuery.isEmpty()) { %>
                    <a class="btn btn-ghost" href="<%= ctx %>/staff/event-bookings">Clear</a>
                <% } %>
            </form>
        </div>

        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Reference</th>
                        <th>Customer</th>
                        <th>Package &amp; Venue</th>
                        <th>Date &amp; Time</th>
                        <th>Guests</th>
                        <th>Estimated Total</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                <% if (bs == null || bs.isEmpty()) { %>
                    <tr>
                        <td colspan="8" style="text-align:center;padding:32px 16px;">
                            <p class="muted" style="margin-bottom:0;">No event bookings found matching your search.</p>
                        </td>
                    </tr>
                <% } else {
                    for (EventBookingRecord b : bs) { %>
                    <tr>
                        <td>
                            <a class="table-link" href="<%= ctx %>/staff/event-bookings/view?reference=<%= b.getReference() %>">
                                <strong><%= HtmlUtil.escape(b.getReference()) %></strong>
                            </a>
                            <span class="table-subtext"><%= HtmlUtil.escape(b.getEventType()) %></span>
                        </td>
                        <td>
                            <strong><%= HtmlUtil.escape(b.getCustomerName()) %></strong>
                            <span class="table-subtext"><%= HtmlUtil.escape(b.getEmail()) %> &middot; <%= HtmlUtil.escape(b.getPhone()) %></span>
                        </td>
                        <td>
                            <strong><%= HtmlUtil.escape(b.getPackageName()) %></strong>
                            <span class="table-subtext"><%= HtmlUtil.escape(b.getVenue()) %></span>
                        </td>
                        <td>
                            <strong><%= b.getEventDate() %></strong>
                            <span class="table-subtext"><%= b.getEventTime() %></span>
                        </td>
                        <td><%= b.getGuestCount() %> guests</td>
                        <td><strong>LKR <%= String.format("%,.2f", b.getTotalAmount()) %></strong></td>
                        <td>
                            <%
                                String statusClass = "status-pending";
                                if ("CONFIRMED".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus())) statusClass = "status-confirmed";
                                else if ("CANCELLED".equalsIgnoreCase(b.getStatus())) statusClass = "status-cancelled";
                            %>
                            <span class="status <%= statusClass %>"><%= HtmlUtil.escape(b.getStatus()) %></span>
                        </td>
                        <td>
                            <a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/event-bookings/view?reference=<%= b.getReference() %>">Review</a>
                        </td>
                    </tr>
                <%  }
                } %>
                </tbody>
            </table>
        </div>
    </section>
</div>

<%@ include file="fragments/footer.jspf" %>
