<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.EventPackageRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("pageTitle", "Event Package");
    request.setAttribute("activeNav", "staffEventPackages");
    String mode = String.valueOf(request.getAttribute("packageFormMode"));
    EventPackageRecord p = (EventPackageRecord) request.getAttribute("eventPackage");
    boolean edit = "edit".equals(mode);
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <div class="breadcrumbs" style="margin-bottom:8px;">
                <a href="<%= ctx %>/staff/event-packages">Event packages</a>
                <span>/</span>
                <span><%= edit ? "Edit package" : "Add package" %></span>
            </div>
            <span class="eyebrow">Package configuration</span>
            <h1><%= edit ? "Edit " + (p != null ? HtmlUtil.escape(p.getName()) : "package") : "Create new event package" %></h1>
            <p><%= edit ? "Update package pricing, guest capacities, duration, and inclusions." : "Define a new event package for customer inquiries and coordinator quoting." %></p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/staff/event-packages">&larr; Cancel and return</a>
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

    <section class="panel">
        <div class="panel-header">
            <h3>Package parameters</h3>
            <p class="muted small">All fields are validated against standard event planning thresholds.</p>
        </div>

        <form method="post" action="<%= ctx %>/staff/event-packages/<%= edit ? "update" : "create" %>">
            <% if (edit && p != null) { %>
                <input type="hidden" name="id" value="<%= p.getId() %>">
            <% } %>

            <div class="form-grid" style="display:grid;grid-template-columns:repeat(auto-fit, minmax(280px, 1fr));gap:18px;">
                <div class="form-group">
                    <label>Package name <span style="color:#c53030">*</span></label>
                    <input class="form-control" name="name" required maxlength="140" placeholder="e.g. Joyful Gatherings"
                           value="<%= edit && p != null ? HtmlUtil.escape(p.getName()) : HtmlUtil.escape(String.valueOf(request.getAttribute("formName") == null ? "" : request.getAttribute("formName"))) %>">
                </div>

                <div class="form-group">
                    <label>Category <span style="color:#c53030">*</span></label>
                    <select class="form-control" name="category" required>
                        <% for (String c : com.dinevista.service.EventPackageService.CATEGORIES) {
                            boolean sel = (edit && p != null && c.equals(p.getCategory()))
                                    || (request.getAttribute("formCategory") != null && c.equals(request.getAttribute("formCategory")));
                        %>
                            <option value="<%= c %>" <%= sel ? "selected" : "" %>><%= c %></option>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label>Base price per guest (LKR) <span style="color:#c53030">*</span></label>
                    <input class="form-control" type="number" step="0.01" min="0.01" name="price" required placeholder="4500.00"
                           value="<%= edit && p != null ? p.getPricePerGuest() : (request.getAttribute("formPrice") == null ? "" : request.getAttribute("formPrice")) %>">
                </div>

                <div class="form-group">
                    <label>Duration (minutes) <span style="color:#c53030">*</span></label>
                    <input class="form-control" type="number" min="30" max="1440" name="durationMinutes" required placeholder="240"
                           value="<%= edit && p != null ? p.getDurationMinutes() : (request.getAttribute("formDurationMinutes") == null ? "240" : request.getAttribute("formDurationMinutes")) %>">
                    <span class="muted small">e.g. 240 mins = 4 hours</span>
                </div>

                <div class="form-group">
                    <label>Minimum guests <span style="color:#c53030">*</span></label>
                    <input class="form-control" type="number" min="1" name="minimumGuests" required placeholder="20"
                           value="<%= edit && p != null ? p.getMinimumGuests() : (request.getAttribute("formMinimumGuests") == null ? "20" : request.getAttribute("formMinimumGuests")) %>">
                </div>

                <div class="form-group">
                    <label>Maximum guests <span style="color:#c53030">*</span></label>
                    <input class="form-control" type="number" min="1" name="maximumGuests" required placeholder="200"
                           value="<%= edit && p != null ? p.getMaximumGuests() : (request.getAttribute("formMaximumGuests") == null ? "200" : request.getAttribute("formMaximumGuests")) %>">
                </div>

                <div class="form-group full" style="grid-column:1/-1;">
                    <label>Description</label>
                    <textarea class="form-control" name="description" maxlength="800" rows="3" placeholder="Brief overview of the package target and experience..."><%= edit && p != null ? HtmlUtil.escape(p.getDescription()) : HtmlUtil.escape(String.valueOf(request.getAttribute("formDescription") == null ? "" : request.getAttribute("formDescription"))) %></textarea>
                </div>

                <div class="form-group full" style="grid-column:1/-1;">
                    <label>Inclusions &amp; services (comma-separated)</label>
                    <textarea class="form-control" name="inclusions" maxlength="5000" rows="4" placeholder="Buffet menu, basic styling, welcome beverage, sound system, service staff"><%= edit && p != null ? HtmlUtil.escape(p.getInclusions()) : HtmlUtil.escape(String.valueOf(request.getAttribute("formInclusions") == null ? "" : request.getAttribute("formInclusions"))) %></textarea>
                </div>

                <% if (edit && p != null) { %>
                    <div class="form-group full" style="grid-column:1/-1;">
                        <label style="display:flex;align-items:center;gap:8px;cursor:pointer;">
                            <input type="checkbox" name="active" value="true" <%= p.isActive() ? "checked" : "" %>>
                            <strong>Active package (visible to customers during online booking)</strong>
                        </label>
                    </div>
                <% } %>
            </div>

            <div class="form-actions" style="margin-top:24px;display:flex;gap:12px;">
                <button class="btn btn-primary" type="submit"><%= edit ? "Save changes" : "Create package" %></button>
                <a class="btn btn-secondary" href="<%= ctx %>/staff/event-packages">Cancel</a>
            </div>
        </form>
    </section>
</div>

<%@ include file="fragments/footer.jspf" %>
