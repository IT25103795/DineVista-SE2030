<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.PromotionRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    PromotionRecord promotion = (PromotionRecord) request.getAttribute("promotion");
    boolean editing = promotion != null;
    Integer usageCount = (Integer) request.getAttribute("usageCount");
    request.setAttribute("pageTitle", editing ? "Edit Promotion" : "Add Promotion");
    request.setAttribute("activeNav", "staffBilling");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/billing/promotions">Promotions</a><span>/</span><span><%= editing ? "Edit" : "Add" %></span></div>
            <span class="eyebrow">Billing, Promotions &amp; Discounts</span>
            <h1><%= editing ? "Edit promotion." : "Add a new promotion." %></h1>
            <p>Percentage discounts are capped at 100%, and every discount is capped so it can never exceed the invoice subtotal.</p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/billing/promotions">Back to promotions</a>
    </div>
</section>

<section class="section-sm">
    <div class="container form-layout">
        <div class="form-card">
            <% if (request.getAttribute("errors") != null) { %>
                <div class="alert alert-danger"><div><strong>Please correct the following:</strong><ul>
                    <% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %>
                </ul></div></div>
            <% } %>
            <form method="post" action="<%= ctx %>/staff/billing/promotions/save" novalidate>
                <input type="hidden" name="id" value="<%= editing ? promotion.getId() : 0 %>">
                <div class="form-grid">
                    <div class="form-group">
                        <label for="code">Promotion code</label>
                        <input class="form-control" id="code" name="code" type="text" required maxlength="40"
                               value="<%= editing ? HtmlUtil.escape(promotion.getCode()) : "" %>" style="text-transform:uppercase">
                    </div>
                    <div class="form-group full">
                        <label for="name">Promotion name</label>
                        <input class="form-control" id="name" name="name" type="text" required maxlength="160"
                               value="<%= editing ? HtmlUtil.escape(promotion.getName()) : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="discountType">Discount type</label>
                        <select class="form-control" id="discountType" name="discountType">
                            <option value="PERCENTAGE" <%= editing ? HtmlUtil.selected("PERCENTAGE", promotion.getDiscountType()) : "" %>>Percentage</option>
                            <option value="FIXED_AMOUNT" <%= editing ? HtmlUtil.selected("FIXED_AMOUNT", promotion.getDiscountType()) : "" %>>Fixed amount (LKR)</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="discountValue">Discount value</label>
                        <input class="form-control" id="discountValue" name="discountValue" type="number" step="0.01" min="0.01" required
                               value="<%= editing ? promotion.getDiscountValue().stripTrailingZeros().toPlainString() : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="minimumSpend">Minimum spend (LKR, optional)</label>
                        <input class="form-control" id="minimumSpend" name="minimumSpend" type="number" step="0.01" min="0"
                               value="<%= editing ? promotion.getMinimumSpend().stripTrailingZeros().toPlainString() : "0" %>">
                    </div>
                    <div class="form-group">
                        <label for="usageLimit">Usage limit (optional)</label>
                        <input class="form-control" id="usageLimit" name="usageLimit" type="number" step="1" min="1"
                               value="<%= editing && promotion.getUsageLimit() != null ? String.valueOf(promotion.getUsageLimit()) : "" %>" placeholder="Unlimited">
                    </div>
                    <div class="form-group">
                        <label for="startDate">Start date</label>
                        <input class="form-control" id="startDate" name="startDate" type="date" required
                               value="<%= editing && promotion.getStartDate() != null ? promotion.getStartDate().toString() : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="endDate">End date</label>
                        <input class="form-control" id="endDate" name="endDate" type="date" required
                               value="<%= editing && promotion.getEndDate() != null ? promotion.getEndDate().toString() : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="activeToggle">&nbsp;</label>
                        <label style="display:flex;align-items:center;gap:8px;font-weight:600">
                            <input id="activeToggle" type="checkbox" name="active" value="1" <%= !editing || promotion.isActive() ? "checked" : "" %>>
                            Active
                        </label>
                    </div>
                </div>
                <div class="form-actions">
                    <button class="btn btn-primary" type="submit"><%= editing ? "Save changes" : "Add promotion" %></button>
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/billing/promotions">Cancel</a>
                </div>
            </form>

            <% if (editing) { %>
            <div style="margin-top:24px;padding-top:20px;border-top:1px solid #e5e5e5">
                <p class="muted small">Used <%= usageCount == null ? 0 : usageCount %> time(s).</p>
                <form method="post" action="<%= ctx %>/staff/billing/promotions/delete"
                      onsubmit="return confirm('Delete this promotion? Only possible if it has never been used.');">
                    <input type="hidden" name="id" value="<%= promotion.getId() %>">
                    <button class="btn btn-danger btn-sm" type="submit">Delete promotion</button>
                </form>
            </div>
            <% } %>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
