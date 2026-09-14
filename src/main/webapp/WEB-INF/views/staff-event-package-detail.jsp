<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.EventPackageRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Event Package Details");
    request.setAttribute("activeNav", "staffEventPackages");
    EventPackageRecord p = (EventPackageRecord) request.getAttribute("eventPackage");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <div class="breadcrumbs" style="margin-bottom:8px;">
                <a href="<%= ctx %>/staff/event-packages">Event packages</a>
                <span>/</span>
                <span><%= p != null ? HtmlUtil.escape(p.getName()) : "Details" %></span>
            </div>
            <span class="eyebrow">Event Package Details</span>
            <h1><%= p != null ? HtmlUtil.escape(p.getName()) : "Package Not Found" %></h1>
            <p><%= p != null ? HtmlUtil.escape(p.getDescription()) : "" %></p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/staff/event-packages">&larr; Back to packages</a>
            <% if (p != null) { %>
                <a class="btn btn-primary" href="<%= ctx %>/staff/event-packages/edit?id=<%= p.getId() %>">Edit package</a>
            <% } %>
        </div>
    </div>
</section>

<div class="container operations-container">
    <% if (p == null) { %>
        <div class="alert alert-error">Event package not found.</div>
    <% } else { %>
        <div class="dashboard-grid">
            <section class="panel">
                <div class="panel-header">
                    <h3>Package configuration</h3>
                    <span class="status <%= p.isActive() ? "status-confirmed" : "status-cancelled" %>">
                        <%= p.isActive() ? "ACTIVE" : "INACTIVE" %>
                    </span>
                </div>
                <div class="detail-grid" style="display:grid;grid-template-columns:repeat(auto-fit, minmax(200px, 1fr));gap:20px;margin-bottom:24px;">
                    <div>
                        <span class="muted small" style="display:block;">Category</span>
                        <strong><span class="badge"><%= HtmlUtil.escape(p.getCategory()) %></span></strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Base Price</span>
                        <strong style="font-size:1.15rem;color:var(--brand-dark, #2b5138);">LKR <%= String.format("%,.2f", p.getPricePerGuest()) %> <span class="muted small">/ guest</span></strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Capacity Window</span>
                        <strong><%= p.getMinimumGuests() %> &ndash; <%= p.getMaximumGuests() %> guests</strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Standard Duration</span>
                        <strong><%= p.getDurationMinutes() / 60 %>h <%= p.getDurationMinutes() % 60 > 0 ? (p.getDurationMinutes() % 60) + "m" : "" %> (<%= p.getDurationMinutes() %> min)</strong>
                    </div>
                </div>

                <hr style="border:0;border-top:1px solid var(--line, #e6decb);margin:20px 0;">

                <div>
                    <h4>Inclusions &amp; services provided</h4>
                    <p style="white-space:pre-wrap;margin-top:8px;line-height:1.6;"><%= HtmlUtil.escape(p.getInclusions()) %></p>
                </div>
            </section>

            <aside class="panel">
                <div class="panel-header">
                    <h3>Package actions</h3>
                </div>
                <div style="display:flex;flex-direction:column;gap:12px;">
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/event-packages/edit?id=<%= p.getId() %>">Edit package details</a>
                    <% if (p.isActive()) { %>
                        <form method="post" action="<%= ctx %>/staff/event-packages/deactivate" onsubmit="return confirm('Deactivate this package? It will no longer be available for new customer inquiries.');">
                            <input type="hidden" name="id" value="<%= p.getId() %>">
                            <button class="btn btn-ghost" style="width:100%;color:#c53030;" type="submit">Deactivate package</button>
                        </form>
                    <% } else { %>
                        <form method="post" action="<%= ctx %>/staff/event-packages/delete" onsubmit="return confirm('Permanently delete this package? If existing bookings reference it, deletion will be blocked.');">
                            <input type="hidden" name="id" value="<%= p.getId() %>">
                            <button class="btn btn-ghost" style="width:100%;color:#c53030;" type="submit">Delete permanently</button>
                        </form>
                    <% } %>
                </div>
            </aside>
        </div>
    <% } %>
</div>

<%@ include file="fragments/footer.jspf" %>
