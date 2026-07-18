<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.FoodOrderRecord" %>
<%@ page import="com.dinevista.model.OrderItemRecord" %>
<%@ page import="com.dinevista.model.StatusHistoryRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Food Order Details");
    request.setAttribute("activeNav", "orders");
    FoodOrderRecord order = (FoodOrderRecord) request.getAttribute("foodOrder");
%>
<%@ include file="fragments/header.jspf" %>
<section class="page-hero compact-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/orders">Food Orders</a><span>/</span><span><%= HtmlUtil.escape(order.getReference()) %></span></div>
        <span class="eyebrow">Food order record</span>
        <h1><%= HtmlUtil.escape(order.getReference()) %></h1>
        <p>Review ordered items, fulfilment information, totals, kitchen progress, and the full status timeline.</p>
    </div>
</section>
<section class="section-sm">
    <div class="container detail-layout">
        <div class="detail-main">
            <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
            <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><div><strong>Unable to complete the action:</strong><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div></div><% } %>

            <article class="detail-card">
                <div class="detail-card-header">
                    <div><span class="record-reference"><%= HtmlUtil.escape(order.getOrderTypeDisplay()) %></span><h2><%= HtmlUtil.escape(order.getCustomerName()) %></h2></div>
                    <span class="status <%= order.getStatusCss() %>"><%= HtmlUtil.escape(order.getStatus()) %></span>
                </div>
                <div class="detail-fact-grid">
                    <div><span>Requested for</span><strong><%= HtmlUtil.escape(order.getRequestedForDisplay()) %></strong></div>
                    <div><span>Created</span><strong><%= order.getCreatedAtDisplay() %></strong></div>
                    <div><span>Items</span><strong><%= order.getTotalQuantity() %></strong></div>
                    <div><span>Reservation</span><strong><%= order.getReservationReference().isEmpty() ? "Not linked" : HtmlUtil.escape(order.getReservationReference()) %></strong></div>
                </div>
                <div class="order-line-list">
                    <% for (OrderItemRecord item : order.getItems()) { %>
                        <div class="order-line">
                            <div><strong><%= HtmlUtil.escape(item.getItemName()) %></strong><span><%= item.getQuantity() %> × <%= item.getUnitPriceDisplay() %></span></div>
                            <strong><%= item.getLineTotalDisplay() %></strong>
                        </div>
                    <% } %>
                </div>
                <div class="cart-summary order-total-box">
                    <div class="summary-row"><span>Subtotal</span><strong><%= order.getSubtotalDisplay() %></strong></div>
                    <div class="summary-row"><span>Service charge</span><strong><%= order.getServiceChargeDisplay() %></strong></div>
                    <div class="summary-row total"><span>Total</span><strong><%= order.getTotalAmountDisplay() %></strong></div>
                </div>
                <% if (!order.getOrderNotes().isEmpty()) { %><div class="note-box"><span>Customer notes</span><p><%= HtmlUtil.escape(order.getOrderNotes()) %></p></div><% } %>
                <% if (!order.getStaffNote().isEmpty()) { %><div class="note-box staff"><span>Latest staff note</span><p><%= HtmlUtil.escape(order.getStaffNote()) %></p></div><% } %>
                <div class="record-actions">
                    <% if ("PENDING".equals(order.getStatus()) || "CONFIRMED".equals(order.getStatus())) { %>
                        <button class="btn btn-danger" type="button" data-dialog-open="cancel-order-dialog">Cancel order</button>
                    <% } %>
                    <a class="btn btn-secondary" href="<%= ctx %>/orders">Back to food ordering</a>
                </div>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Order status timeline</h3><span class="muted small">Newest activity appears first.</span></div></div>
                <div class="timeline">
                    <% for (StatusHistoryRecord item : order.getHistory()) { %>
                        <div class="timeline-item">
                            <span class="timeline-dot"></span>
                            <div>
                                <div class="timeline-top"><strong><%= HtmlUtil.escape(item.getStatus()) %></strong><span><%= item.getChangedAtDisplay() %></span></div>
                                <p><%= HtmlUtil.escape(item.getNote()) %></p>
                                <small>By <%= HtmlUtil.escape(item.getChangedBy()) %></small>
                            </div>
                        </div>
                    <% } %>
                </div>
            </article>
        </div>
        <aside class="detail-sidebar">
            <article class="panel">
                <span class="section-kicker">Customer contact</span>
                <div class="contact-stack">
                    <div><span>Email</span><strong><%= HtmlUtil.escape(order.getEmail()) %></strong></div>
                    <div><span>Phone</span><strong><%= HtmlUtil.escape(order.getPhone()) %></strong></div>
                    <div><span>Reference</span><strong class="mono"><%= HtmlUtil.escape(order.getReference()) %></strong></div>
                </div>
            </article>
            <article class="panel policy-panel"><h3>Order cancellation</h3><p>Customers may cancel pending or confirmed orders. Cancellation is blocked after preparation begins.</p></article>
        </aside>
    </div>
</section>

<div class="dialog-backdrop" id="cancel-order-dialog" data-dialog>
    <div class="dialog-card" role="dialog" aria-modal="true" aria-labelledby="cancel-order-title">
        <button class="icon-btn dialog-close" type="button" data-dialog-close aria-label="Close">×</button>
        <span class="section-kicker">Cancel food order</span>
        <h3 id="cancel-order-title">Confirm order cancellation.</h3>
        <p class="muted">The reason is stored in the order status history.</p>
        <form method="post" action="<%= ctx %>/orders/cancel">
            <input type="hidden" name="reference" value="<%= HtmlUtil.escape(order.getReference()) %>">
            <div class="form-group"><label for="orderCancelReason">Cancellation reason</label><textarea class="form-control" id="orderCancelReason" name="reason" minlength="5" required placeholder="Enter a short reason"></textarea></div>
            <div class="form-actions"><button class="btn btn-danger" type="submit">Cancel order</button><button class="btn btn-secondary" type="button" data-dialog-close>Keep order</button></div>
        </form>
    </div>
</div>
<%@ include file="fragments/footer.jspf" %>
