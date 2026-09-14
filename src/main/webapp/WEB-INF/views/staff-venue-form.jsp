<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.EventVenueRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    EventVenueRecord venue = (EventVenueRecord) request.getAttribute("venue");
    boolean editing = venue != null;
    request.setAttribute("pageTitle", editing ? "Edit Venue" : "Add Venue");
    request.setAttribute("activeNav", "staffEventResources");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/event-resources">Event Resources</a><span>/</span><span><%= editing ? "Edit" : "Add" %></span></div>
            <span class="eyebrow">Event Resource Management</span>
            <h1><%= editing ? "Edit venue." : "Add a new venue." %></h1>
            <p><%= editing ? "Update details for " + HtmlUtil.escape(venue.getName()) + "." : "Register a new bookable event space." %></p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/event-resources">Back to overview</a>
    </div>
</section>

<section class="section-sm">
    <div class="container form-layout">
        <div class="form-card">
            <% if (request.getAttribute("errors") != null) { %>
                <div class="alert alert-danger"><div><strong>Please correct the following:</strong><ul>
                    <% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %>
                </ul></div></div>
            <% } %>
            <form method="post" action="<%= ctx %>/staff/event-resources/venues/save" novalidate>
                <input type="hidden" name="id" value="<%= editing ? venue.getId() : 0 %>">
                <div class="form-grid">
                    <div class="form-group full">
                        <label for="name">Venue name</label>
                        <input class="form-control" id="name" name="name" type="text" required maxlength="160"
                               value="<%= editing ? HtmlUtil.escape(venue.getName()) : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="venueType">Venue type</label>
                        <select class="form-control" id="venueType" name="venueType" required>
                            <%
                                String currentType = editing ? venue.getVenueType() : "INDOOR";
                                String[][] types = {{"INDOOR","Indoor"},{"OUTDOOR","Outdoor"},{"PRIVATE_ROOM","Private room"},{"OFF_SITE","Off-site"}};
                                for (String[] type : types) {
                            %>
                                <option value="<%= type[0] %>" <%= type[0].equals(currentType) ? "selected" : "" %>><%= type[1] %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="availabilityStatus">Availability status</label>
                        <select class="form-control" id="availabilityStatus" name="availabilityStatus" required>
                            <%
                                String currentStatus = editing ? venue.getAvailabilityStatus() : "AVAILABLE";
                                String[] statuses = {"AVAILABLE","UNAVAILABLE","MAINTENANCE"};
                                for (String s : statuses) {
                            %>
                                <option value="<%= s %>" <%= s.equals(currentStatus) ? "selected" : "" %>><%= s %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="capacity">Capacity (guests)</label>
                        <input class="form-control" id="capacity" name="capacity" type="number" min="1" required
                               value="<%= editing ? venue.getCapacity() : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="baseFee">Base fee (LKR)</label>
                        <input class="form-control" id="baseFee" name="baseFee" type="number" step="0.01" min="0" required
                               value="<%= editing ? venue.getBaseFee().toPlainString() : "" %>">
                    </div>
                    <div class="form-group full">
                        <label for="description">Description (optional)</label>
                        <input class="form-control" id="description" name="description" type="text" maxlength="500"
                               value="<%= editing && venue.getDescription() != null ? HtmlUtil.escape(venue.getDescription()) : "" %>">
                    </div>
                </div>
                <div class="form-actions">
                    <button class="btn btn-primary" type="submit"><%= editing ? "Save changes" : "Add venue" %></button>
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/event-resources">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
