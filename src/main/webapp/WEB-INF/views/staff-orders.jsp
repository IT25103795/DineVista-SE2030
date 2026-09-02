<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.FoodOrderRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Kitchen Order Operations");
    request.setAttribute("activeNav", "staffOrders");
    List<FoodOrderRecord> orders = (List<FoodOrderRecord>) request.getAttribute("staffOrders");
    String statusFilter = request.getAttribute("orderFilterStatus") == null ? "" : request.getAttribute("orderFilterStatus").toString();
    String typeFilter = request.getAttribute("orderFilterType") == null ? "" : request.getAttribute("orderFilterType").toString();
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div><span class="eyebrow">Kitchen and order operations</span><h1>Food order control centre.</h1><p>Accept customer orders, move them into the kitchen queue, update preparation status, and maintain a complete service timeline.</p></div>
        <div class="hero-actions"><a class="btn btn-secondary" href="<%= ctx %>/dashboard">Operations dashboard</a></div>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
        <form class="operations-filter" method="get" action="<%= ctx %>/staff/orders">
            <div class="form-group"><label for="status">Status</label><select class="form-control" id="status" name="status">
                <option value="">All statuses</option>
                <option value="PENDING" <%= HtmlUtil.selected("PENDING", statusFilter) %>>Pending</option>
                <option value="CONFIRMED" <%= HtmlUtil.selected("CONFIRMED", statusFilter) %>>Confirmed</option>
                <option value="PREPARING" <%= HtmlUtil.selected("PREPARING", statusFilter) %>>Preparing</option>
                <option value="READY" <%= HtmlUtil.selected("READY", statusFilter) %>>Ready</option>
                <option value="SERVED" <%= HtmlUtil.selected("SERVED", statusFilter) %>>Served</option>
                <option value="COMPLETED" <%= HtmlUtil.selected("COMPLETED", statusFilter) %>>Completed</option>
                <option value="CANCELLED" <%= HtmlUtil.selected("CANCELLED", statusFilter) %>>Cancelled</option>
                <option value="REJECTED" <%= HtmlUtil.selected("REJECTED", statusFilter) %>>Rejected</option>
            </select></div>
            <div class="form-group"><label for="type">Order type</label><select class="form-control" id="type" name="type">
                <option value="">All types</option>
                <option value="DINE_IN" <%= HtmlUtil.selected("DINE_IN", typeFilter) %>>Dine-in</option>
                <option value="TAKEAWAY" <%= HtmlUtil.selected("TAKEAWAY", typeFilter) %>>Takeaway</option>
                <option value="PRE_ORDER" <%= HtmlUtil.selected("PRE_ORDER", typeFilter) %>>Pre-order</option>
            </select></div>
            <button class="btn btn-dark" type="submit">Apply filters</button><a class="btn btn-secondary" href="<%= ctx %>/staff/orders">Clear</a>
        </form>

        <div class="operations-summary">
            <div><strong><%= orders == null ? 0 : orders.size() %></strong><span>Matching orders</span></div>
            <div><strong><%= orders == null ? 0 : orders.stream().filter(item -> "PENDING".equals(item.getStatus())).count() %></strong><span>Awaiting acceptance</span></div>
            <div><strong><%= orders == null ? 0 : orders.stream().filter(item -> "PREPARING".equals(item.getStatus())).count() %></strong><span>In kitchen</span></div>
            <div><strong><%= orders == null ? 0 : orders.stream().filter(item -> "READY".equals(item.getStatus())).count() %></strong><span>Ready to serve</span></div>
        </div>

        <section class="panel operations-table-panel">
            <div class="panel-header"><div><h3>Food order records</h3><span class="muted small">Open an order to update kitchen and fulfilment status.</span></div></div>
            <div class="table-wrap"><table class="data-table operations-table"><thead><tr><th>Reference</th><th>Customer</th><th>Type</th><th>Requested for</th><th>Items</th><th>Total</th><th>Status</th><th></th></tr></thead><tbody>
            <% if (orders == null || orders.isEmpty()) { %><tr><td colspan="8"><div class="empty-table-message">No food orders match the current filters.</div></td></tr>
            <% } else { for (FoodOrderRecord order : orders) { %>
                <tr>
                    <td><strong class="mono"><%= HtmlUtil.escape(order.getReference()) %></strong></td>
                    <td><strong><%= HtmlUtil.escape(order.getCustomerName()) %></strong><span class="table-subtext"><%= HtmlUtil.escape(order.getPhone()) %></span></td>
                    <td><%= HtmlUtil.escape(order.getOrderTypeDisplay()) %></td>
                    <td><%= HtmlUtil.escape(order.getRequestedForDisplay()) %></td>
                    <td><%= order.getTotalQuantity() %></td>
                    <td><%= order.getTotalAmountDisplay() %></td>
                    <td><span class="status <%= order.getStatusCss() %>"><%= HtmlUtil.escape(order.getStatus()) %></span></td>
                    <td><a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/orders/view?reference=<%= order.getReference() %>">Manage</a></td>
                </tr>
            <% }} %>
            </tbody></table></div>
        </section>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
