<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.FoodOrderRecord" %>
<%@ page import="com.dinevista.model.OrderItemRecord" %>
<%@ page import="com.dinevista.model.InvoiceRecord" %>
<%@ page import="com.dinevista.model.PromotionRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Generate Invoice");
    request.setAttribute("activeNav", "staffBilling");
    FoodOrderRecord prefillOrder = (FoodOrderRecord) request.getAttribute("prefillOrder");
    InvoiceRecord existingInvoice = (InvoiceRecord) request.getAttribute("existingInvoice");
    List<PromotionRecord> promotions = (List<PromotionRecord>) request.getAttribute("promotions");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/billing">Billing</a><span>/</span><span>Generate invoice</span></div>
            <span class="eyebrow">Billing, Promotions &amp; Discounts</span>
            <h1>Generate an invoice.</h1>
            <p>Totals are calculated from the lines below plus a fixed 2.5% service tax. Enter an eligible promotion code to apply a controlled discount.</p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/billing">Back to billing</a>
    </div>
</section>

<section class="section-sm">
    <div class="container form-layout">
        <div class="form-card">
            <% if (request.getAttribute("errors") != null) { %>
                <div class="alert alert-danger"><div><strong>Unable to generate that invoice:</strong><ul>
                    <% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %>
                </ul></div></div>
            <% } %>

            <% if (existingInvoice != null) { %>
                <div class="alert alert-danger">
                    An active invoice <strong><%= HtmlUtil.escape(existingInvoice.getInvoiceNumber()) %></strong>
                    already exists for this order.
                    <a href="<%= ctx %>/staff/billing/view?id=<%= existingInvoice.getId() %>">Open it instead</a>.
                </div>
            <% } %>

            <form method="post" action="<%= ctx %>/staff/billing/generate" id="invoiceForm">
                <div class="form-grid">
                    <div class="form-group">
                        <label for="sourceType">Source type</label>
                        <select class="form-control" id="sourceType" name="sourceType">
                            <option value="FOOD_ORDER" <%= prefillOrder != null ? "selected" : "" %>>Food order</option>
                            <option value="EVENT_BOOKING">Event booking</option>
                            <option value="OTHER">Other / manual</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="sourceReference">Source reference (optional)</label>
                        <input class="form-control" id="sourceReference" name="sourceReference" type="text" maxlength="30"
                               placeholder="e.g. DV-O-1A2B3C4D"
                               value="<%= prefillOrder != null ? HtmlUtil.escape(prefillOrder.getReference()) : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="customerName">Customer name</label>
                        <input class="form-control" id="customerName" name="customerName" type="text" maxlength="160"
                               value="<%= prefillOrder != null ? HtmlUtil.escape(prefillOrder.getCustomerName()) : "" %>" placeholder="Walk-in customer">
                    </div>
                    <div class="form-group">
                        <label for="customerEmail">Customer email (optional)</label>
                        <input class="form-control" id="customerEmail" name="customerEmail" type="email" maxlength="160"
                               value="<%= prefillOrder != null ? HtmlUtil.escape(prefillOrder.getEmail()) : "" %>">
                    </div>
                    <input type="hidden" name="customerKey" value="<%= prefillOrder != null ? HtmlUtil.escape(prefillOrder.getCustomerKey()) : "" %>">
                    <div class="form-group">
                        <label for="promotionCode">Promotion / discount code (optional)</label>
                        <input class="form-control" id="promotionCode" name="promotionCode" type="text" maxlength="40" placeholder="e.g. WELCOME10">
                    </div>
                </div>

                <h3 style="margin-top:24px">Billable lines</h3>
                <p class="muted small">Add every confirmed charge. Totals (subtotal, 2.5% tax, discount, grand total) are calculated automatically.</p>
                <div id="lineRows">
                <% boolean anyPrefill = false;
                   if (prefillOrder != null) {
                       for (OrderItemRecord item : prefillOrder.getItems()) {
                           anyPrefill = true; %>
                    <div class="form-grid line-row">
                        <div class="form-group full"><label>Description</label>
                            <input class="form-control" name="description" type="text" maxlength="255" value="<%= HtmlUtil.escape(item.getItemName()) %>"></div>
                        <div class="form-group"><label>Quantity</label>
                            <input class="form-control" name="quantity" type="number" step="0.01" min="0.01" value="<%= item.getQuantity() %>"></div>
                        <div class="form-group"><label>Unit price (LKR)</label>
                            <input class="form-control" name="unitPrice" type="number" step="0.01" min="0" value="<%= item.getUnitPrice().toPlainString() %>"></div>
                    </div>
                    <% }
                       if (prefillOrder.getServiceCharge() != null && prefillOrder.getServiceCharge().signum() > 0) { %>
                    <div class="form-grid line-row">
                        <div class="form-group full"><label>Description</label>
                            <input class="form-control" name="description" type="text" maxlength="255" value="Service charge"></div>
                        <div class="form-group"><label>Quantity</label>
                            <input class="form-control" name="quantity" type="number" step="0.01" min="0.01" value="1"></div>
                        <div class="form-group"><label>Unit price (LKR)</label>
                            <input class="form-control" name="unitPrice" type="number" step="0.01" min="0" value="<%= prefillOrder.getServiceCharge().toPlainString() %>"></div>
                    </div>
                    <% }
                   }
                   if (!anyPrefill) { %>
                    <div class="form-grid line-row">
                        <div class="form-group full"><label>Description</label>
                            <input class="form-control" name="description" type="text" maxlength="255" placeholder="e.g. Table service charges"></div>
                        <div class="form-group"><label>Quantity</label>
                            <input class="form-control" name="quantity" type="number" step="0.01" min="0.01" value="1"></div>
                        <div class="form-group"><label>Unit price (LKR)</label>
                            <input class="form-control" name="unitPrice" type="number" step="0.01" min="0" value="0"></div>
                    </div>
                <% } %>
                </div>
                <button type="button" class="btn btn-secondary btn-sm" id="addLineBtn" style="margin-top:8px">Add another line</button>

                <div class="form-actions">
                    <button class="btn btn-primary" type="submit">Generate invoice</button>
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/billing">Cancel</a>
                </div>
            </form>
        </div>

        <aside class="detail-sidebar">
            <article class="panel">
                <span class="section-kicker">Active promotions</span>
                <ul class="check-list operational-checks">
                <% if (promotions == null || promotions.isEmpty()) { %>
                    <li>No promotions configured yet.</li>
                <% } else { for (PromotionRecord promotion : promotions) { if (promotion.isCurrentlyValid()) { %>
                    <li><strong><%= HtmlUtil.escape(promotion.getCode()) %></strong> — <%= promotion.getDiscountValueDisplay() %> off, min. <%= HtmlUtil.escape(promotion.getMinimumSpendDisplay()) %></li>
                <% }}} %>
                </ul>
                <a class="btn btn-secondary btn-sm" style="margin-top:12px;display:inline-flex" href="<%= ctx %>/staff/billing/promotions">Manage promotions</a>
            </article>
            <article class="panel">
                <span class="section-kicker">Business checks</span>
                <ul class="check-list operational-checks">
                    <li>Totals always derive from the lines entered here.</li>
                    <li>An invalid or ineligible code never blocks the invoice — it simply isn't applied.</li>
                    <li>Only one active invoice is allowed per source reference.</li>
                </ul>
            </article>
        </aside>
    </div>
</section>
<script>
document.getElementById('addLineBtn').addEventListener('click', function () {
    var rows = document.getElementById('lineRows');
    var row = rows.querySelector('.line-row').cloneNode(true);
    row.querySelectorAll('input').forEach(function (input) {
        if (input.name === 'description') input.value = '';
        else if (input.name === 'quantity') input.value = '1';
        else input.value = '0';
    });
    rows.appendChild(row);
});
</script>
<%@ include file="fragments/footer.jspf" %>
