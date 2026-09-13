<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.InvoiceRecord" %>
<%@ page import="com.dinevista.model.InvoiceItemRecord" %>
<%@ page import="com.dinevista.model.PaymentRecord" %>
<%@ page import="com.dinevista.model.StatusHistoryRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    InvoiceRecord invoice = (InvoiceRecord) request.getAttribute("invoice");
    List<PaymentRecord> payments = (List<PaymentRecord>) request.getAttribute("payments");
    request.setAttribute("pageTitle", invoice.getInvoiceNumber());
    request.setAttribute("activeNav", "staffBilling");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/billing">Billing</a><span>/</span><span><%= HtmlUtil.escape(invoice.getInvoiceNumber()) %></span></div>
            <span class="eyebrow">Billing, Promotions &amp; Discounts</span>
            <h1><%= HtmlUtil.escape(invoice.getInvoiceNumber()) %></h1>
            <p>Review charges, record and verify simulated payments, and keep a complete audit trail of every change.</p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/billing">Back to billing</a>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container detail-layout">
        <div class="detail-main">
            <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
            <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><div><strong>Unable to complete that action:</strong><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div></div><% } %>

            <article class="detail-card">
                <div class="detail-card-header">
                    <div><span class="record-reference">Invoice</span><h2><%= HtmlUtil.escape(invoice.getInvoiceNumber()) %></h2></div>
                    <span class="status <%= invoice.getStatusCss() %>"><%= invoice.getStatusLabel() %></span>
                </div>
                <div class="detail-fact-grid">
                    <div><span>Customer</span><strong><%= HtmlUtil.escape(invoice.getCustomerName()) %></strong></div>
                    <div><span>Source</span><strong><%= HtmlUtil.escape(invoice.getSourceTypeDisplay()) %><% if (!invoice.getSourceReference().isEmpty()) { %> · <%= HtmlUtil.escape(invoice.getSourceReference()) %><% } %></strong></div>
                    <div><span>Issue date</span><strong><%= invoice.getIssueDateDisplay() %></strong></div>
                    <div><span>Due date</span><strong><%= invoice.getDueDateDisplay() %></strong></div>
                    <div><span>Created by</span><strong><%= HtmlUtil.escape(invoice.getCreatedBy()) %></strong></div>
                    <% if (invoice.getPromotionCode() != null) { %><div><span>Promotion applied</span><strong><%= HtmlUtil.escape(invoice.getPromotionCode()) %></strong></div><% } %>
                </div>
                <% if (!invoice.getNotes().isEmpty()) { %><p class="muted small" style="margin-top:12px"><%= HtmlUtil.escape(invoice.getNotes()) %></p><% } %>

                <div class="table-wrap" style="margin-top:16px">
                    <table class="data-table operations-table">
                        <thead><tr><th>Description</th><th>Qty</th><th>Unit price</th><th>Line total</th></tr></thead>
                        <tbody>
                        <% for (InvoiceItemRecord item : invoice.getItems()) { %>
                            <tr><td><%= HtmlUtil.escape(item.getDescription()) %></td><td><%= item.getQuantityDisplay() %></td><td><%= item.getUnitPriceDisplay() %></td><td><%= item.getLineTotalDisplay() %></td></tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <div class="detail-fact-grid" style="margin-top:16px">
                    <div><span>Subtotal</span><strong><%= invoice.getSubtotalDisplay() %></strong></div>
                    <div><span>Tax (2.5%)</span><strong><%= invoice.getTaxAmountDisplay() %></strong></div>
                    <div><span>Discount</span><strong>- <%= invoice.getDiscountAmountDisplay() %></strong></div>
                    <div><span>Total due</span><strong><%= invoice.getTotalAmountDisplay() %></strong></div>
                    <div><span>Amount paid</span><strong><%= invoice.getAmountPaidDisplay() %></strong></div>
                    <div><span>Balance</span><strong><%= invoice.getBalanceDisplay() %></strong></div>
                </div>
            </article>

            <% if (!invoice.isCancelled() && !invoice.isFullyPaid()) { %>
            <article class="detail-card">
                <span class="section-kicker">Cashier / Finance action</span>
                <h3>Record and verify a payment</h3>
                <p class="muted">Payments are recorded and verified locally — there is no live payment gateway in this prototype. A duplicate reference is always blocked.</p>
                <form method="post" action="<%= ctx %>/staff/billing/pay">
                    <input type="hidden" name="invoiceId" value="<%= invoice.getId() %>">
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="method">Payment method</label>
                            <select class="form-control" id="method" name="method">
                                <option value="CASH">Cash</option>
                                <option value="CARD">Card</option>
                                <option value="BANK_TRANSFER">Bank transfer</option>
                                <option value="ONLINE">Online</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="amount">Amount (LKR)</label>
                            <input class="form-control" id="amount" name="amount" type="number" step="0.01" min="0.01"
                                   max="<%= invoice.getBalance().toPlainString() %>" value="<%= invoice.getBalance().toPlainString() %>" required>
                        </div>
                        <div class="form-group">
                            <label for="reference">Payment reference (optional)</label>
                            <input class="form-control" id="reference" name="reference" type="text" maxlength="40" placeholder="Leave blank to auto-generate">
                        </div>
                        <div class="form-group full">
                            <label for="note">Note (optional)</label>
                            <input class="form-control" id="note" name="note" type="text" maxlength="255">
                        </div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary" type="submit">Record payment</button></div>
                </form>
            </article>
            <% } %>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Payment history</h3><span class="muted small">Every payment is retained; voids and refunds are recorded, never deleted.</span></div></div>
                <div class="table-wrap">
                    <table class="data-table operations-table">
                        <thead><tr><th>Reference</th><th>Method</th><th>Amount</th><th>Status</th><th>Verified by</th><th></th></tr></thead>
                        <tbody>
                        <% if (payments == null || payments.isEmpty()) { %>
                            <tr><td colspan="6"><div class="empty-table-message">No payments recorded yet.</div></td></tr>
                        <% } else { for (PaymentRecord payment : payments) { %>
                            <tr>
                                <td><%= HtmlUtil.escape(payment.getPaymentReference()) %><br><small class="muted"><%= payment.getCreatedAtDisplay() %></small></td>
                                <td><%= HtmlUtil.escape(payment.getMethodDisplay()) %></td>
                                <td><%= payment.getAmountDisplay() %></td>
                                <td><span class="status <%= payment.getStatusCss() %>"><%= HtmlUtil.escape(payment.getStatus()) %></span></td>
                                <td><%= HtmlUtil.escape(payment.getVerifiedBy()) %></td>
                                <td>
                                <% if ("SUCCESS".equals(payment.getStatus())) { %>
                                    <form method="post" action="<%= ctx %>/staff/billing/void" onsubmit="return promptVoidReason(this);">
                                        <input type="hidden" name="paymentId" value="<%= payment.getId() %>">
                                        <input type="hidden" name="invoiceId" value="<%= invoice.getId() %>">
                                        <input type="hidden" name="reason" class="void-reason">
                                        <button class="btn btn-danger btn-sm" type="submit">Void / refund</button>
                                    </form>
                                <% } %>
                                </td>
                            </tr>
                        <% }} %>
                        </tbody>
                    </table>
                </div>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Status history</h3></div></div>
                <div class="timeline">
                    <% if (invoice.getHistory().isEmpty()) { %>
                        <p class="muted">No status changes recorded in this session yet.</p>
                    <% } else { for (StatusHistoryRecord entry : invoice.getHistory()) { %>
                        <div class="timeline-item"><span class="timeline-dot"></span><div><div class="timeline-top"><strong><%= entry.getStatus().replace('_',' ') %></strong><span><%= entry.getChangedAtDisplay() %></span></div><p><%= HtmlUtil.escape(entry.getNote()) %></p><small>By <%= HtmlUtil.escape(entry.getChangedBy()) %></small></div></div>
                    <% }} %>
                </div>
            </article>
        </div>

        <aside class="detail-sidebar">
            <% if (!invoice.isCancelled() && invoice.getAmountPaid().signum() == 0) { %>
            <article class="panel">
                <span class="section-kicker">Manage this invoice</span>
                <form method="post" action="<%= ctx %>/staff/billing/cancel" onsubmit="return promptCancelReason(this);">
                    <input type="hidden" name="invoiceId" value="<%= invoice.getId() %>">
                    <input type="hidden" name="reason" class="cancel-reason">
                    <button class="btn btn-danger btn-sm" type="submit">Cancel invoice</button>
                </form>
            </article>
            <% } %>
            <article class="panel">
                <span class="section-kicker">Business checks</span>
                <ul class="check-list operational-checks">
                    <li>A payment can never exceed the outstanding balance.</li>
                    <li>Duplicate payment references are blocked automatically.</li>
                    <li>Voids and refunds require a reason and stay visible in history.</li>
                    <li>An invoice with recorded payments cannot be cancelled.</li>
                </ul>
            </article>
        </aside>
    </div>
</section>
<script>
function promptVoidReason(form) {
    var reason = window.prompt('Reason for voiding / refunding this payment:');
    if (!reason) return false;
    form.querySelector('.void-reason').value = reason;
    return true;
}
function promptCancelReason(form) {
    if (!window.confirm('Cancel this invoice?')) return false;
    var reason = window.prompt('Reason for cancelling this invoice:') || 'Cancelled by staff.';
    form.querySelector('.cancel-reason').value = reason;
    return true;
}
</script>
<%@ include file="fragments/footer.jspf" %>
