<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.EventResourceRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    EventResourceRecord resource = (EventResourceRecord) request.getAttribute("resource");
    boolean editing = resource != null;
    request.setAttribute("pageTitle", editing ? "Edit Resource" : "Add Resource");
    request.setAttribute("activeNav", "staffEventResources");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/event-resources">Event Resources</a><span>/</span><span><%= editing ? "Edit" : "Add" %></span></div>
            <span class="eyebrow">Event Resource Management</span>
            <h1><%= editing ? "Edit resource." : "Add a new resource." %></h1>
            <p><%= editing ? "Update details for " + HtmlUtil.escape(resource.getName()) + "." : "Register a new pool of shared equipment." %></p>
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
            <form method="post" action="<%= ctx %>/staff/event-resources/resources/save" novalidate>
                <input type="hidden" name="id" value="<%= editing ? resource.getId() : 0 %>">
                <div class="form-grid">
                    <div class="form-group full">
                        <label for="name">Resource name</label>
                        <input class="form-control" id="name" name="name" type="text" required maxlength="160"
                               value="<%= editing ? HtmlUtil.escape(resource.getName()) : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="category">Category</label>
                        <select class="form-control" id="category" name="category" required>
                            <%
                                String currentCategory = editing ? resource.getCategory() : "FURNITURE";
                                String[] categories = {"FURNITURE","AUDIO_VISUAL","DECOR","LIGHTING","KITCHEN","TRANSPORT","OTHER"};
                                for (String c : categories) {
                            %>
                                <option value="<%= c %>" <%= c.equals(currentCategory) ? "selected" : "" %>><%= c.replace('_',' ') %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="status">Status</label>
                        <select class="form-control" id="status" name="status" required>
                            <%
                                String currentStatus = editing ? resource.getStatus() : "AVAILABLE";
                                String[] statuses = {"AVAILABLE","MAINTENANCE","RETIRED"};
                                for (String s : statuses) {
                            %>
                                <option value="<%= s %>" <%= s.equals(currentStatus) ? "selected" : "" %>><%= s %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="totalQuantity">Total quantity owned</label>
                        <input class="form-control" id="totalQuantity" name="totalQuantity" type="number" min="1" required
                               value="<%= editing ? resource.getTotalQuantity() : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="availableQuantity">Currently usable quantity</label>
                        <input class="form-control" id="availableQuantity" name="availableQuantity" type="number" min="0" required
                               value="<%= editing ? resource.getAvailableQuantity() : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="unitCost">Unit cost (LKR, optional)</label>
                        <input class="form-control" id="unitCost" name="unitCost" type="number" step="0.01" min="0"
                               value="<%= editing && resource.getUnitCost() != null ? resource.getUnitCost().toPlainString() : "" %>">
                    </div>
                </div>
                <p class="muted small">Usable quantity is what's not currently out for maintenance or retired — it caps how many units can be booked for any one event date.</p>
                <div class="form-actions">
                    <button class="btn btn-primary" type="submit"><%= editing ? "Save changes" : "Add resource" %></button>
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/event-resources">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
