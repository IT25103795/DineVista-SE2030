<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.InvoiceRecord" %>
<%@ page import="com.dinevista.service.BillingService" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Billing Management");
    request.setAttribute("activeNav", "staffBilling");
    List<InvoiceRecord> invoices = (List<InvoiceRecord>) request.getAttribute("invoices");
    String status = (String) request.getAttribute("billingStatus");
    String search = (String) request.getAttribute("billingSearch");
    BillingService.FinanceSummary summary = (BillingService.FinanceSummary) request.getAttribute("financeSummary");
    if (status == null) status = "";
    if (search == null) search = "";
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <span class="eyebrow">Restaurant &amp; event finance</span>
            <h1>Billing control centre.</h1>
            <p>Generate invoices from confirmed charges, apply only eligible promotions, and record and verify simulated payments with a complete, auditable history.</p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/staff/billing/promotions">Manage promotions</a>
            <a class="btn btn-primary" href="<%= ctx %>/staff/billing/new">Generate invoice</a>
        </div>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
        <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div><% } %>

        <% if (summary != null) { %>
        <div class="operations-summary">
            <div><strong><%= summary.getTotalInvoicedDisplay() %></strong><span>Total invoiced</span></div>
            <div><strong><%= summary.getTotalCollectedDisplay() %></strong><span>Collected</span></div>
            <div><strong><%= summary.getTotalOutstandingDisplay() %></strong><span>Outstanding balance</span></div>
            <div><strong><%= summary.paidCount %></strong><span>Fully paid</span></div>
            <div><strong><%= summary.openCount %></strong><span>Open / partial</span></div>
        </div>
        <% } %>

        <form class="operations-filter" method="get" action="<%= ctx %>/staff/billing">
            <div class="form-group"><label for="search">Search invoice, customer or reference</label>
                <input class="form-control" id="search" name="search" type="text" placeholder="e.g. DV-INV or DV-O-" value="<%= HtmlUtil.escape(search) %>">
            </div>
            <div class="form-group">
                <label for="status">Status</label>
                <select class="form-control" id="status" name="status">
                    <option value="">All statuses</option>
                    <option value="ISSUED" <%= HtmlUtil.selected("ISSUED", status) %>>Issued</option>
                    <option value="PARTIALLY_PAID" <%= HtmlUtil.selected("PARTIALLY_PAID", status) %>>Partially paid</option>
                    <option value="PAID" <%= HtmlUtil.selected("PAID", status) %>>Paid</option>
                    <option value="CANCELLED" <%= HtmlUtil.selected("CANCELLED", status) %>>Cancelled</option>
                </select>
            </div>
            <button class="btn btn-dark" type="submit">Apply filters</button>
            <a class="btn btn-secondary" href="<%= ctx %>/staff/billing">Clear</a>
        </form>

        <section class="panel operations-table-panel">
            <div class="panel-header"><div><h3>Invoices</h3><span class="muted small">Open an invoice to record a payment, void/refund, or cancel it.</span></div></div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead><tr><th>Invoice #</th><th>Customer</th><th>Source</th><th>Total</th><th>Balance</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <% if (invoices == null || invoices.isEmpty()) { %>
                        <tr><td colspan="7"><div class="empty-table-message">No invoices match the current filters.</div></td></tr>
                    <% } else { for (InvoiceRecord invoice : invoices) { %>
                        <tr>
                            <td><strong><%= HtmlUtil.escape(invoice.getInvoiceNumber()) %></strong><br><small class="muted"><%= HtmlUtil.escape(invoice.getIssueDateDisplay()) %></small></td>
                            <td><%= HtmlUtil.escape(invoice.getCustomerName()) %></td>
                            <td><%= HtmlUtil.escape(invoice.getSourceTypeDisplay()) %><% if (!invoice.getSourceReference().isEmpty()) { %><br><small class="muted"><%= HtmlUtil.escape(invoice.getSourceReference()) %></small><% } %></td>
                            <td><%= invoice.getTotalAmountDisplay() %></td>
                            <td><%= invoice.getBalanceDisplay() %></td>
                            <td><span class="status <%= invoice.getStatusCss() %>"><%= invoice.getStatusLabel() %></span></td>
                            <td><a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/billing/view?id=<%= invoice.getId() %>">Manage</a></td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
