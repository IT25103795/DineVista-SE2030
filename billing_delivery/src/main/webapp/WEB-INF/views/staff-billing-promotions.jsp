<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.PromotionRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Promotions & Discounts");
    request.setAttribute("activeNav", "staffBilling");
    List<PromotionRecord> promotions = (List<PromotionRecord>) request.getAttribute("promotions");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/billing">Billing</a><span>/</span><span>Promotions</span></div>
            <span class="eyebrow">Billing, Promotions &amp; Discounts</span>
            <h1>Promotion catalogue.</h1>
            <p>Maintain discount codes with a validity window, minimum spend and usage limit. Only eligible codes are ever applied to an invoice.</p>
        </div>
        <a class="btn btn-primary" href="<%= ctx %>/staff/billing/promotions/new">Add promotion</a>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
        <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div><% } %>

        <section class="panel operations-table-panel">
            <div class="panel-header"><div><h3>Promotion codes</h3><span class="muted small">Edit a code to change its rules, or delete it once it has never been used.</span></div></div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead><tr><th>Code</th><th>Name</th><th>Discount</th><th>Min. spend</th><th>Valid</th><th>Usage limit</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <% if (promotions == null || promotions.isEmpty()) { %>
                        <tr><td colspan="8"><div class="empty-table-message">No promotions have been created yet.</div></td></tr>
                    <% } else { for (PromotionRecord promotion : promotions) { %>
                        <tr>
                            <td><strong><%= HtmlUtil.escape(promotion.getCode()) %></strong></td>
                            <td><%= HtmlUtil.escape(promotion.getName()) %></td>
                            <td><%= promotion.getDiscountValueDisplay() %></td>
                            <td><%= HtmlUtil.escape(promotion.getMinimumSpendDisplay()) %></td>
                            <td><%= promotion.getStartDateDisplay() %> – <%= promotion.getEndDateDisplay() %></td>
                            <td><%= HtmlUtil.escape(promotion.getUsageLimitDisplay()) %></td>
                            <td><span class="status <%= promotion.getStatusCss() %>"><%= promotion.getStatusLabel() %></span></td>
                            <td><a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/billing/promotions/edit?id=<%= promotion.getId() %>">Manage</a></td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
