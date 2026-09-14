<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.EventPackageRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("pageTitle", "Event Packages");
    request.setAttribute("activeNav", "staffEventPackages");
    List<EventPackageRecord> ps = (List<EventPackageRecord>) request.getAttribute("eventPackages");
    String searchQuery = request.getParameter("search") == null ? "" : request.getParameter("search").trim();
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <span class="eyebrow">Event Package Management</span>
            <h1>Event packages &amp; offerings.</h1>
            <p>Configure pricing, guest capacity thresholds, event durations, inclusions, and package catalog availability.</p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/dashboard">Operations dashboard</a>
            <a class="btn btn-primary" href="<%= ctx %>/staff/event-packages/new">+ Add new package</a>
        </div>
    </div>
</section>

<div class="container operations-container">
    <% if (request.getParameter("deleted") != null) { %>
        <div class="alert alert-success" style="margin-bottom:20px;">Event package was permanently removed.</div>
    <% } else if (request.getParameter("deactivated") != null) { %>
        <div class="alert alert-success" style="margin-bottom:20px;">Event package status was updated to inactive.</div>
    <% } else if (request.getParameter("error") != null) { %>
        <div class="alert alert-error" style="margin-bottom:20px;"><%= HtmlUtil.escape(request.getParameter("error")) %></div>
    <% } %>

    <section class="panel">
        <div class="panel-header" style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:14px;">
            <div>
                <h3>Package catalog (<%= ps == null ? 0 : ps.size() %>)</h3>
                <p class="muted small">Configured event packages available for customer inquiries and quotations.</p>
            </div>
            <form method="get" action="<%= ctx %>/staff/event-packages" class="search-filter-grid" style="margin:0;">
                <input class="form-control" name="search" placeholder="Search package name or category..." value="<%= HtmlUtil.escape(searchQuery) %>">
                <button class="btn btn-secondary" type="submit">Search</button>
                <% if (!searchQuery.isEmpty()) { %>
                    <a class="btn btn-ghost" href="<%= ctx %>/staff/event-packages">Clear</a>
                <% } %>
            </form>
        </div>

        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Package</th>
                        <th>Category</th>
                        <th>Price / Guest</th>
                        <th>Guest Capacity</th>
                        <th>Duration</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <% if (ps == null || ps.isEmpty()) { %>
                    <tr>
                        <td colspan="7" style="text-align:center;padding:32px 16px;">
                            <p class="muted" style="margin-bottom:12px;">No event packages match your criteria.</p>
                            <a class="btn btn-sm btn-primary" href="<%= ctx %>/staff/event-packages/new">Create first package</a>
                        </td>
                    </tr>
                <% } else {
                    for (EventPackageRecord p : ps) { %>
                    <tr>
                        <td>
                            <a class="table-link" href="<%= ctx %>/staff/event-packages/view?id=<%= p.getId() %>">
                                <strong><%= HtmlUtil.escape(p.getName()) %></strong>
                            </a>
                            <% if (p.getDescription() != null && !p.getDescription().isBlank()) { %>
                                <span class="table-subtext"><%= HtmlUtil.escape(p.getDescription()) %></span>
                            <% } %>
                        </td>
                        <td><span class="badge"><%= HtmlUtil.escape(p.getCategory()) %></span></td>
                        <td><strong>LKR <%= String.format("%,.2f", p.getPricePerGuest()) %></strong></td>
                        <td><%= p.getMinimumGuests() %> &ndash; <%= p.getMaximumGuests() %> guests</td>
                        <td><%= p.getDurationMinutes() / 60 %>h <%= p.getDurationMinutes() % 60 > 0 ? (p.getDurationMinutes() % 60) + "m" : "" %></td>
                        <td>
                            <span class="status <%= p.isActive() ? "status-confirmed" : "status-cancelled" %>">
                                <%= p.isActive() ? "ACTIVE" : "INACTIVE" %>
                            </span>
                        </td>
                        <td>
                            <div style="display:flex;gap:6px;">
                                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/staff/event-packages/view?id=<%= p.getId() %>">View</a>
                                <a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/event-packages/edit?id=<%= p.getId() %>">Edit</a>
                            </div>
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
