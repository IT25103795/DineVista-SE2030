<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.EventVenueRecord" %>
<%@ page import="com.dinevista.model.EventResourceRecord" %>
<%@ page import="com.dinevista.model.EventVenueBookingRecord" %>
<%@ page import="com.dinevista.model.ResourceBookingRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Event Resource Management");
    request.setAttribute("activeNav", "staffEventResources");
    List<EventVenueRecord> venues = (List<EventVenueRecord>) request.getAttribute("venues");
    List<EventResourceRecord> resources = (List<EventResourceRecord>) request.getAttribute("resources");
    List<EventVenueBookingRecord> venueBookings = (List<EventVenueBookingRecord>) request.getAttribute("venueBookings");
    List<ResourceBookingRecord> resourceBookings = (List<ResourceBookingRecord>) request.getAttribute("resourceBookings");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <span class="eyebrow">Event Resource and Staff Scheduling</span>
            <h1>Venues &amp; equipment control centre.</h1>
            <p>Manage bookable venues and shared equipment, and reserve them against real-time capacity and availability so events never double-book a space or run short of gear.</p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/dashboard">Operations dashboard</a>
            <a class="btn btn-secondary" href="<%= ctx %>/staff/event-resources/resources/new">Add resource</a>
            <a class="btn btn-primary" href="<%= ctx %>/staff/event-resources/venues/new">Add venue</a>
        </div>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
        <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div><% } %>

        <div class="operations-summary">
            <div><strong><%= venues == null ? 0 : venues.size() %></strong><span>Venues</span></div>
            <div><strong><%= resources == null ? 0 : resources.size() %></strong><span>Resource types</span></div>
            <div><strong><%= venueBookings == null ? 0 : venueBookings.size() %></strong><span>Venue bookings</span></div>
            <div><strong><%= resourceBookings == null ? 0 : resourceBookings.size() %></strong><span>Resource bookings</span></div>
        </div>

        <section class="panel operations-table-panel">
            <div class="panel-header"><div><h3>Venues</h3><span class="muted small">Open a venue to book it or review its schedule.</span></div></div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead><tr><th>Venue</th><th>Type</th><th>Capacity</th><th>Base fee</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <% if (venues == null || venues.isEmpty()) { %>
                        <tr><td colspan="6"><div class="empty-table-message">No venues have been added yet.</div></td></tr>
                    <% } else { for (EventVenueRecord venue : venues) { %>
                        <tr>
                            <td><strong><%= HtmlUtil.escape(venue.getName()) %></strong></td>
                            <td><%= HtmlUtil.escape(venue.getVenueTypeLabel()) %></td>
                            <td><%= venue.getCapacity() %> guests</td>
                            <td><%= HtmlUtil.escape(venue.getBaseFeeDisplay()) %></td>
                            <td><span class="status <%= venue.getStatusCss() %>"><%= HtmlUtil.escape(venue.getAvailabilityStatus()) %></span></td>
                            <td><a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/event-resources/venues/view?id=<%= venue.getId() %>">Manage</a></td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="panel operations-table-panel" style="margin-top:24px">
            <div class="panel-header"><div><h3>Shared equipment</h3><span class="muted small">Open a resource to reserve units or review its booking history.</span></div></div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead><tr><th>Resource</th><th>Category</th><th>Available / Total</th><th>Unit cost</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <% if (resources == null || resources.isEmpty()) { %>
                        <tr><td colspan="6"><div class="empty-table-message">No resources have been added yet.</div></td></tr>
                    <% } else { for (EventResourceRecord resource : resources) { %>
                        <tr>
                            <td><strong><%= HtmlUtil.escape(resource.getName()) %></strong></td>
                            <td><%= HtmlUtil.escape(resource.getCategoryLabel()) %></td>
                            <td><%= resource.getAvailableQuantity() %> / <%= resource.getTotalQuantity() %></td>
                            <td><%= HtmlUtil.escape(resource.getUnitCostDisplay()) %></td>
                            <td><span class="status <%= resource.getStatusCss() %>"><%= HtmlUtil.escape(resource.getStatus()) %></span></td>
                            <td><a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/event-resources/resources/view?id=<%= resource.getId() %>">Manage</a></td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
