<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.TableReservationRecord" %>
<%@ page import="com.dinevista.model.FoodOrderRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Operations Dashboard");
    request.setAttribute("activeNav", "managerDashboard");
    List<TableReservationRecord> managerReservations = (List<TableReservationRecord>) request.getAttribute("managerReservations");
    List<FoodOrderRecord> managerOrders = (List<FoodOrderRecord>) request.getAttribute("managerOrders");
    long activeReservationCount = request.getAttribute("activeReservationCount") == null ? 0L : (Long) request.getAttribute("activeReservationCount");
    long activeOrderCount = request.getAttribute("activeOrderCount") == null ? 0L : (Long) request.getAttribute("activeOrderCount");
%>
<%@ include file="fragments/header.jspf" %>
<section class="dashboard-page manager-dashboard-page">
    <div class="container">
        <div class="dashboard-header">
            <div><span class="eyebrow">Operations centre</span><h1>DineVista restaurant overview</h1><p>Monitor table reservations, food-order demand, kitchen status, and operational priorities.</p></div>
        </div>
        <div class="kpi-grid">
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg></span><span class="trend">Live operations</span></div><strong><%= activeReservationCount %></strong><span>Active reservations</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 4h2l2 10h10l2-7H7"/></svg></span><span class="trend">Kitchen queue</span></div><strong><%= activeOrderCount %></strong><span>Active food orders</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3v18M3 12h18"/></svg></span><span class="trend">Validation</span></div><strong>90m</strong><span>Reservation slot protection</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19V9M10 19V5M16 19v-7M22 19H2"/></svg></span><span class="trend">Connected control</span></div><strong>5</strong><span>Operational workspaces</span></article>
        </div>

        <div class="dashboard-grid">
            <section class="panel">
                <div class="panel-header"><div><h3>Restaurant operations</h3><span class="muted small">Open the complete management workspaces.</span></div></div>
                <div class="operations-launch-grid">
                    <a class="operations-launch" href="<%= ctx %>/staff/menu">
                        <span class="launch-icon green" title="Menu management">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                                <line x1="9" y1="7" x2="15" y2="7"/>
                                <line x1="9" y1="11" x2="15" y2="11"/>
                            </svg>
                        </span>
                        <div>
                            <strong>Menu management</strong>
                            <p>Maintain dishes, categories, pricing, dietary tags, and live order availability.</p>
                        </div>
                        <span>Open</span>
                    </a>
                    <a class="operations-launch" href="<%= ctx %>/staff/reservations">
                        <span class="launch-icon" title="Reservation operations">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                                <line x1="16" y1="2" x2="16" y2="6"/>
                                <line x1="8" y1="2" x2="8" y2="6"/>
                                <line x1="3" y1="10" x2="21" y2="10"/>
                            </svg>
                        </span>
                        <div>
                            <strong>Reservation operations</strong>
                            <p>Assign tables, confirm requests, seat guests, complete visits, or reject invalid requests.</p>
                        </div>
                        <span>Open</span>
                    </a>
                    <a class="operations-launch" href="<%= ctx %>/staff/orders">
                        <span class="launch-icon green" title="Kitchen food orders">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <path d="M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8zM6 1v3M10 1v3M14 1v3"/>
                            </svg>
                        </span>
                        <div>
                            <strong>Kitchen food orders</strong>
                            <p>Accept orders, start preparation, mark ready, serve, complete, or reject with a reason.</p>
                        </div>
                        <span>Open</span>
                    </a>
                    <a class="operations-launch" href="<%= ctx %>/staff/inventory">
                        <span class="launch-icon" title="Inventory management">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                                <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
                                <line x1="12" y1="22.08" x2="12" y2="12"/>
                            </svg>
                        </span>
                        <div>
                            <strong>Inventory management</strong>
                            <p>Track ingredients, reorder levels, and an auditable purchase, usage, and waste history.</p>
                        </div>
                        <span>Open</span>
                    </a>
                    <a class="operations-launch" href="<%= ctx %>/staff/billing">
                        <span class="launch-icon" title="Billing & promotions">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                                <path d="M4 2v20l2-1 2 1 2-1 2 1 2-1 2 1 2-1 2 1V2l-2 1-2-1-2 1-2-1-2 1-2-1-2 1-2-1z"/>
                                <line x1="8" y1="7" x2="16" y2="7"/>
                                <line x1="8" y1="11" x2="16" y2="11"/>
                                <line x1="8" y1="15" x2="13" y2="15"/>
                            </svg>
                        </span>
                        <div>
                            <strong>Billing &amp; promotions</strong>
                            <p>Generate invoices, apply discounts and promotions, and record payments.</p>
                        </div>
                        <span>Open</span>
                    </a>
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
                <div class="table-wrap scrollable-dashboard-table"><table class="data-table"><thead><tr><th>Reference</th><th>Date and time</th><th>Guest</th><th>Party</th><th>Table</th><th>Status</th></tr></thead><tbody>
                    <% if (managerReservations == null || managerReservations.isEmpty()) { %><tr><td colspan="6">No reservations available.</td></tr>
                    <% } else { for (TableReservationRecord item : managerReservations) { %>
                        <tr><td><a class="table-link" href="<%= ctx %>/staff/reservations/view?reference=<%= item.getReference() %>"><%= HtmlUtil.escape(item.getReference()) %></a></td><td><%= item.getDateDisplay() %><span class="table-subtext"><%= item.getTimeDisplay() %></span></td><td><%= HtmlUtil.escape(item.getGuestName()) %></td><td><%= item.getPartySize() %></td><td><%= item.getTableCode() == null ? "Unassigned" : HtmlUtil.escape(item.getTableCode()) %></td><td><span class="status <%= item.getStatusCss() %>"><%= HtmlUtil.escape(item.getStatus()) %></span></td></tr>
                    <% }} %>
                </tbody></table></div>
            </section>
            <aside class="panel">
                <div class="panel-header"><h3>Current kitchen queue</h3><a class="btn btn-ghost btn-sm" href="<%= ctx %>/staff/orders">Manage all</a></div>
                <div class="activity-list scrollable-dashboard-queue">
                    <% if (managerOrders == null || managerOrders.isEmpty()) { %><p class="muted small">No food orders available.</p>
                    <% } else { for (FoodOrderRecord order : managerOrders) { %>
                        <a class="activity-item" href="<%= ctx %>/staff/orders/view?reference=<%= order.getReference() %>"><span class="activity-dot"></span><span><strong><%= HtmlUtil.escape(order.getReference()) %> &mdash; <%= order.getStatus() %></strong><span><%= HtmlUtil.escape(order.getCustomerName()) %> &middot; <%= order.getTotalQuantity() %> item(s)</span></span></a>
                    <% }} %>
                </div>
            </aside>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>

