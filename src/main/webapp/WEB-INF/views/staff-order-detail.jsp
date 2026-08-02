<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.FoodOrderRecord" %>
<%@ page import="com.dinevista.model.OrderItemRecord" %>
<%@ page import="com.dinevista.model.StatusHistoryRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Manage Food Order");
    request.setAttribute("activeNav", "staffOrders");
    FoodOrderRecord order = (FoodOrderRecord) request.getAttribute("foodOrder");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div><div class="breadcrumbs dark"><a href="<%= ctx %>/staff/orders">Kitchen orders</a><span>/</span><span><%= HtmlUtil.escape(order.getReference()) %></span></div><span class="eyebrow">Staff food-order review</span><h1><%= HtmlUtil.escape(order.getReference()) %></h1><p>Review menu items, accept or reject the order, send it to the kitchen, and update the fulfilment timeline.</p></div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/orders">Back to orders</a>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container detail-layout">
        <div class="detail-main">
            <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
            <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><div><strong>Unable to update the order:</strong><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div></div><% } %>

            <article class="detail-card">
                <div class="detail-card-header"><div><span class="record-reference"><%= HtmlUtil.escape(order.getOrderTypeDisplay()) %></span><h2><%= HtmlUtil.escape(order.getCustomerName()) %></h2></div><span class="status <%= order.getStatusCss() %>"><%= HtmlUtil.escape(order.getStatus()) %></span></div>
                <div class="detail-fact-grid">
                    <div><span>Requested for</span><strong><%= HtmlUtil.escape(order.getRequestedForDisplay()) %></strong></div>
                    <div><span>Reservation</span><strong><%= order.getReservationReference().isEmpty() ? "Not linked" : HtmlUtil.escape(order.getReservationReference()) %></strong></div>
                    <div><span>Items</span><strong><%= order.getTotalQuantity() %></strong></div>
                    <div><span>Total</span><strong><%= order.getTotalAmountDisplay() %></strong></div>
                </div>
                <div class="kitchen-ticket">
                    <div class="kitchen-ticket-head"><span>Kitchen ticket</span><strong><%= HtmlUtil.escape(order.getReference()) %></strong></div>
                    <% for (OrderItemRecord item : order.getItems()) { %>
                        <div class="kitchen-line"><strong><%= item.getQuantity() %> × <%= HtmlUtil.escape(item.getItemName()) %></strong><span><%= item.getLineTotalDisplay() %></span></div>
                    <% } %>
                </div>
                <% if (!order.getOrderNotes().isEmpty()) { %><div class="note-box"><span>Customer notes</span><p><%= HtmlUtil.escape(order.getOrderNotes()) %></p></div><% } %>
            </article>

            <article class="detail-card">
                <span class="section-kicker">Kitchen and service action</span>
                <h3>Update order status</h3>
                <p class="muted">Only valid next statuses are displayed. Rejection and cancellation require a reason.</p>
                <form method="post" action="<%= ctx %>/staff/orders/update">
                    <input type="hidden" name="reference" value="<%= HtmlUtil.escape(order.getReference()) %>">
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="status">New status</label>
                            <select class="form-control" id="status" name="status" required>
                                <option value="<%= HtmlUtil.escape(order.getStatus()) %>">Keep <%= HtmlUtil.escape(order.getStatus()) %></option>
                                <% if ("PENDING".equals(order.getStatus())) { %><option value="CONFIRMED">Accept order</option><option value="REJECTED">Reject order</option><option value="CANCELLED">Cancel order</option>
                                <% } else if ("CONFIRMED".equals(order.getStatus())) { %><option value="PREPARING">Send to kitchen / start preparation</option><option value="CANCELLED">Cancel order</option>
                                <% } else if ("PREPARING".equals(order.getStatus())) { %><option value="READY">Mark ready</option>
                                <% } else if ("READY".equals(order.getStatus())) { if ("TAKEAWAY".equals(order.getOrderType())) { %><option value="COMPLETED">Complete collection</option><% } else { %><option value="SERVED">Mark served</option><% }
                                } else if ("SERVED".equals(order.getStatus())) { %><option value="COMPLETED">Complete order</option><% } %>
                            </select>
                        </div>
                        <div class="form-group full"><label for="note">Kitchen or staff note</label><textarea class="form-control" id="note" name="note" maxlength="255" placeholder="Acceptance note, kitchen update, rejection reason, or cancellation reason"></textarea></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary" type="submit">Save order status</button></div>
                </form>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Order history</h3><span class="muted small">Complete kitchen and service audit trail.</span></div></div>
                <div class="timeline">
                    <% for (StatusHistoryRecord item : order.getHistory()) { %><div class="timeline-item"><span class="timeline-dot"></span><div><div class="timeline-top"><strong><%= HtmlUtil.escape(item.getStatus()) %></strong><span><%= item.getChangedAtDisplay() %></span></div><p><%= HtmlUtil.escape(item.getNote()) %></p><small>By <%= HtmlUtil.escape(item.getChangedBy()) %></small></div></div><% } %>
                </div>
            </article>
        </div>
        <aside class="detail-sidebar">
            <article class="panel"><span class="section-kicker">Customer details</span><div class="contact-stack"><div><span>Name</span><strong><%= HtmlUtil.escape(order.getCustomerName()) %></strong></div><div><span>Email</span><strong><%= HtmlUtil.escape(order.getEmail()) %></strong></div><div><span>Phone</span><strong><%= HtmlUtil.escape(order.getPhone()) %></strong></div></div></article>
            <article class="panel"><span class="section-kicker">Order totals</span><div class="cart-summary"><div class="summary-row"><span>Subtotal</span><strong><%= order.getSubtotalDisplay() %></strong></div><div class="summary-row"><span>Service charge</span><strong><%= order.getServiceChargeDisplay() %></strong></div><div class="summary-row total"><span>Total</span><strong><%= order.getTotalAmountDisplay() %></strong></div></div></article>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
