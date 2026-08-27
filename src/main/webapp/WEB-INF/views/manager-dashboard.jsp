<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.TableReservationRecord" %>
<%@ page import="com.dinevista.model.FoodOrderRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Operations Dashboard");
    request.setAttribute("activeNav", "");
    List<TableReservationRecord> managerReservations = (List<TableReservationRecord>) request.getAttribute("managerReservations");
    List<FoodOrderRecord> managerOrders = (List<FoodOrderRecord>) request.getAttribute("managerOrders");
    long activeReservationCount = request.getAttribute("activeReservationCount") == null ? 0L : (Long) request.getAttribute("activeReservationCount");
    long activeOrderCount = request.getAttribute("activeOrderCount") == null ? 0L : (Long) request.getAttribute("activeOrderCount");
%>
<%@ include file="fragments/header.jspf" %>
<section class="dashboard-page">
    <div class="container">
        <div class="dashboard-header">
            <div><span class="eyebrow">Operations centre</span><h1>DineVista restaurant overview</h1><p>Monitor table reservations, food-order demand, kitchen status, and operational priorities.</p></div>
            <div class="hero-actions" style="margin:0"><a class="btn btn-secondary" href="<%= ctx %>/logout">Sign out</a></div>
        </div>
        <div class="kpi-grid">
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg></span><span class="trend">Live module</span></div><strong><%= activeReservationCount %></strong><span>Active reservations</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 4h2l2 10h10l2-7H7"/></svg></span><span class="trend">Kitchen queue</span></div><strong><%= activeOrderCount %></strong><span>Active food orders</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3v18M3 12h18"/></svg></span><span class="trend">Validation</span></div><strong>90m</strong><span>Reservation slot protection</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19V9M10 19V5M16 19v-7M22 19H2"/></svg></span><span class="trend">Complete flow</span></div><strong>CRUD</strong><span>Reservation and order management</span></article>
        </div>

        <div class="dashboard-grid">
            <section class="panel">
                <div class="panel-header"><div><h3>Restaurant operations</h3><span class="muted small">Open the complete management workspaces.</span></div></div>
                <div class="operations-launch-grid">
                    <a class="operations-launch" href="<%= ctx %>/staff/reservations"><span class="launch-icon">R</span><div><strong>Reservation operations</strong><p>Assign tables, confirm requests, seat guests, complete visits, or reject invalid requests.</p></div><span>Open</span></a>
                    <a class="operations-launch" href="<%= ctx %>/staff/orders"><span class="launch-icon green">O</span><div><strong>Kitchen food orders</strong><p>Accept orders, start preparation, mark ready, serve, complete, or reject with a reason.</p></div><span>Open</span></a>
                    <a class="operations-launch" href="<%= ctx %>/staff/inventory"><span class="launch-icon">I</span><div><strong>Inventory management</strong><p>Track ingredients, reorder levels, and an auditable purchase, usage, and waste history.</p></div><span>Open</span></a>
                </div>
            </section>
            <aside class="panel">
                <div class="panel-header"><h3>Business rules active</h3></div>
                <ul class="check-list operational-checks">
                    <li>Prevents overlapping table assignments.</li>
                    <li>Validates party size and table capacity.</li>
                    <li>Blocks invalid item quantities.</li>
                    <li>Enforces reservation and order status transitions.</li>
                    <li>Stores a full status history for audit.</li>
                </ul>
            </aside>
        </div>

        <div class="dashboard-grid" style="margin-top:22px">
            <section class="panel">
                <div class="panel-header"><h3>Upcoming reservation schedule</h3><a class="btn btn-ghost btn-sm" href="<%= ctx %>/staff/reservations">Manage all</a></div>
                <div class="table-wrap"><table class="data-table"><thead><tr><th>Reference</th><th>Date and time</th><th>Guest</th><th>Party</th><th>Table</th><th>Status</th></tr></thead><tbody>
                    <% if (managerReservations == null || managerReservations.isEmpty()) { %><tr><td colspan="6">No reservations available.</td></tr>
                    <% } else { int shown = 0; for (TableReservationRecord item : managerReservations) { if (shown++ >= 6) break; %>
                        <tr><td><a class="table-link" href="<%= ctx %>/staff/reservations/view?reference=<%= item.getReference() %>"><%= HtmlUtil.escape(item.getReference()) %></a></td><td><%= item.getDateDisplay() %><span class="table-subtext"><%= item.getTimeDisplay() %></span></td><td><%= HtmlUtil.escape(item.getGuestName()) %></td><td><%= item.getPartySize() %></td><td><%= item.getTableCode() == null ? "Unassigned" : HtmlUtil.escape(item.getTableCode()) %></td><td><span class="status <%= item.getStatusCss() %>"><%= HtmlUtil.escape(item.getStatus()) %></span></td></tr>
                    <% }} %>
                </tbody></table></div>
            </section>
            <aside class="panel">
                <div class="panel-header"><h3>Current kitchen queue</h3><a class="btn btn-ghost btn-sm" href="<%= ctx %>/staff/orders">Manage all</a></div>
                <div class="activity-list">
                    <% if (managerOrders == null || managerOrders.isEmpty()) { %><p class="muted small">No food orders available.</p>
                    <% } else { int shown = 0; for (FoodOrderRecord order : managerOrders) { if (shown++ >= 5) break; %>
                        <a class="activity-item" href="<%= ctx %>/staff/orders/view?reference=<%= order.getReference() %>"><span class="activity-dot"></span><span><strong><%= HtmlUtil.escape(order.getReference()) %> — <%= order.getStatus() %></strong><span><%= HtmlUtil.escape(order.getCustomerName()) %> · <%= order.getTotalQuantity() %> item(s)</span></span></a>
                    <% }} %>
                </div>
            </aside>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
