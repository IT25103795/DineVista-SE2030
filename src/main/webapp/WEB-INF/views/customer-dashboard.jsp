<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.TableReservationRecord" %>
<%@ page import="com.dinevista.model.FoodOrderRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Customer Dashboard");
    request.setAttribute("activeNav", "");
    List<TableReservationRecord> customerReservations = (List<TableReservationRecord>) request.getAttribute("customerReservations");
    List<FoodOrderRecord> customerOrders = (List<FoodOrderRecord>) request.getAttribute("customerOrders");
    int reservationCount = customerReservations == null ? 0 : customerReservations.size();
    int orderCount = customerOrders == null ? 0 : customerOrders.size();
%>
<%@ include file="fragments/header.jspf" %>
<section class="dashboard-page">
    <div class="container">
        <div class="dashboard-header">
            <div>
                <span class="eyebrow">Customer portal</span>
                <h1>Hello, <%= HtmlUtil.escape(session.getAttribute("displayName") == null ? "DineVista Guest" : session.getAttribute("displayName")) %>.</h1>
                <p>Manage table reservations, food orders, and your next dining experience.</p>
            </div>
            <a class="btn btn-secondary" href="<%= ctx %>/logout">Sign out</a>
        </div>

        <div class="kpi-grid">
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg></span><span class="trend">My visits</span></div><strong><%= reservationCount %></strong><span>Table reservations</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 4h2l2 10h10l2-7H7"/></svg></span><span class="trend">My favourites</span></div><strong><%= orderCount %></strong><span>Food orders</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3l2.5 5.1L20 9l-4 4 1 5.6-5-2.6-5 2.6 1-5.6-4-4 5.5-.9z"/></svg></span><span class="trend">DineVista rewards</span></div><strong>Silver</strong><span>Loyalty membership</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10"/></svg></span><span class="trend">Celebrations</span></div><strong>0</strong><span>Event inquiries</span></article>
        </div>

        <div class="dashboard-grid">
            <section class="panel">
                <div class="panel-header"><h3>Recent reservations</h3><a class="btn btn-ghost btn-sm" href="<%= ctx %>/reservations">Manage reservations</a></div>
                <div class="table-wrap">
                    <table class="data-table"><thead><tr><th>Reference</th><th>Date and time</th><th>Guests</th><th>Table</th><th>Status</th></tr></thead><tbody>
                    <% if (customerReservations == null || customerReservations.isEmpty()) { %>
                        <tr><td colspan="5">No reservation requests yet.</td></tr>
                    <% } else { int shown = 0; for (TableReservationRecord item : customerReservations) { if (shown++ >= 5) break; %>
                        <tr><td><a class="table-link" href="<%= ctx %>/reservations/view?reference=<%= item.getReference() %>"><%= HtmlUtil.escape(item.getReference()) %></a></td><td><%= item.getDateDisplay() %><span class="table-subtext"><%= item.getTimeDisplay() %></span></td><td><%= item.getPartySize() %></td><td><%= item.getTableCode() == null ? "Pending" : HtmlUtil.escape(item.getTableCode()) %></td><td><span class="status <%= item.getStatusCss() %>"><%= HtmlUtil.escape(item.getStatus()) %></span></td></tr>
                    <% }} %>
                    </tbody></table>
                </div>
            </section>
            <aside class="panel">
                <div class="panel-header"><h3>Quick actions</h3></div>
                <div class="quick-actions">
                    <a class="quick-action" href="<%= ctx %>/orders"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 4h2l2 10h10l2-7H7"/></svg><span><strong>Order food</strong><span>Create dine-in, takeaway, or pre-order</span></span></a>
                    <a class="quick-action" href="<%= ctx %>/reservations"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/></svg><span><strong>Reserve a table</strong><span>Check availability and submit</span></span></a>
                    <a class="quick-action" href="<%= ctx %>/events"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10"/></svg><span><strong>Plan an event</strong><span>Explore event packages</span></span></a>
                    <a class="quick-action" href="<%= ctx %>/menu"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 4h14v16H5zM8 8h8M8 12h8"/></svg><span><strong>Browse menu</strong><span>Review available dishes</span></span></a>
                </div>
            </aside>
        </div>

        <section class="panel" style="margin-top:22px">
            <div class="panel-header"><h3>Recent food orders</h3><a class="btn btn-ghost btn-sm" href="<%= ctx %>/orders#my-orders">View all orders</a></div>
            <div class="table-wrap"><table class="data-table"><thead><tr><th>Reference</th><th>Type</th><th>Requested for</th><th>Items</th><th>Total</th><th>Status</th></tr></thead><tbody>
                <% if (customerOrders == null || customerOrders.isEmpty()) { %><tr><td colspan="6">No food orders yet.</td></tr>
                <% } else { int shown = 0; for (FoodOrderRecord item : customerOrders) { if (shown++ >= 5) break; %>
                    <tr><td><a class="table-link" href="<%= ctx %>/orders/view?reference=<%= item.getReference() %>"><%= HtmlUtil.escape(item.getReference()) %></a></td><td><%= HtmlUtil.escape(item.getOrderTypeDisplay()) %></td><td><%= HtmlUtil.escape(item.getRequestedForDisplay()) %></td><td><%= item.getTotalQuantity() %></td><td><%= item.getTotalAmountDisplay() %></td><td><span class="status <%= item.getStatusCss() %>"><%= HtmlUtil.escape(item.getStatus()) %></span></td></tr>
                <% }} %>
            </tbody></table></div>
        </section>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
