<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.IngredientRecord" %>
<%@ page import="com.dinevista.model.StockTransactionRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    IngredientRecord ingredient = (IngredientRecord) request.getAttribute("ingredient");
    List<StockTransactionRecord> history = (List<StockTransactionRecord>) request.getAttribute("stockHistory");
    request.setAttribute("pageTitle", ingredient.getName());
    request.setAttribute("activeNav", "staffInventory");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/inventory">Inventory</a><span>/</span><span><%= HtmlUtil.escape(ingredient.getName()) %></span></div>
            <span class="eyebrow">Inventory Management</span>
            <h1><%= HtmlUtil.escape(ingredient.getName()) %></h1>
            <p>Review stock on hand, record purchases or usage, and keep a complete, auditable movement history.</p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/inventory">Back to inventory</a>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container detail-layout">
        <div class="detail-main">
            <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
            <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><div><strong>Unable to complete that action:</strong><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div></div><% } %>

            <article class="detail-card">
                <div class="detail-card-header"><div><span class="record-reference">Ingredient record</span><h2><%= HtmlUtil.escape(ingredient.getName()) %></h2></div><span class="status <%= ingredient.getStockStatusCss() %>"><%= ingredient.getStockStatusLabel() %></span></div>
                <div class="detail-fact-grid">
                    <div><span>On hand</span><strong><%= HtmlUtil.escape(ingredient.getQuantityDisplay()) %></strong></div>
                    <div><span>Reorder level</span><strong><%= HtmlUtil.escape(ingredient.getReorderLevelDisplay()) %></strong></div>
                    <div><span>Unit cost</span><strong><%= HtmlUtil.escape(ingredient.getUnitCostDisplay()) %></strong></div>
                    <div><span>Supplier</span><strong><%= HtmlUtil.escape(ingredient.getSupplierName() == null ? "Not set" : ingredient.getSupplierName()) %></strong></div>
                    <div><span>Last updated</span><strong><%= HtmlUtil.escape(ingredient.getLastUpdatedDisplay()) %></strong></div>
                </div>
            </article>

            <article class="detail-card">
                <span class="section-kicker">Staff action</span>
                <h3>Record a stock transaction</h3>
                <p class="muted">Purchases and returns increase stock; usage and waste decrease it. Every movement is blocked if it would take stock below zero, and an adjustment sets the newly counted total.</p>
                <form method="post" action="<%= ctx %>/staff/inventory/stock">
                    <input type="hidden" name="ingredientId" value="<%= ingredient.getId() %>">
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="transactionType">Transaction type</label>
                            <select class="form-control" id="transactionType" name="transactionType" required>
                                <option value="PURCHASE">Purchase (stock in)</option>
                                <option value="USAGE">Usage (kitchen consumption)</option>
                                <option value="WASTE">Waste / spoilage</option>
                                <option value="RETURN">Return to stock</option>
                                <option value="ADJUSTMENT">Adjustment (set counted quantity)</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="quantity">Quantity (<%= HtmlUtil.escape(ingredient.getUnit()) %>)</label>
                            <input class="form-control" id="quantity" name="quantity" type="number" step="0.001" min="0.001" required>
                        </div>
                        <div class="form-group full"><label for="note">Note (optional)</label><input class="form-control" id="note" name="note" type="text" maxlength="255" placeholder="e.g. Supplier invoice #4021"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary" type="submit">Record transaction</button></div>
                </form>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Stock transaction history</h3><span class="muted small">Every movement is recorded and cannot be edited or removed.</span></div></div>
                <div class="timeline">
                    <% if (history == null || history.isEmpty()) { %>
                        <p class="muted">No stock transactions recorded yet.</p>
                    <% } else { for (StockTransactionRecord transaction : history) { %>
                        <div class="timeline-item"><span class="timeline-dot"></span><div><div class="timeline-top"><strong><%= transaction.getTransactionType().replace('_', ' ') %> · <%= HtmlUtil.escape(transaction.getQuantityDisplay()) %> <%= HtmlUtil.escape(ingredient.getUnit()) %></strong><span><%= HtmlUtil.escape(transaction.getTransactionTimeDisplay()) %></span></div><p><%= HtmlUtil.escape(transaction.getReferenceNote() == null ? "No note provided." : transaction.getReferenceNote()) %></p><small>By <%= HtmlUtil.escape(transaction.getPerformedBy()) %></small></div></div>
                    <% }} %>
                </div>
            </article>
        </div>

        <aside class="detail-sidebar">
            <article class="panel">
                <span class="section-kicker">Manage this record</span>
                <div class="contact-stack"><div><span>Unit</span><strong><%= HtmlUtil.escape(ingredient.getUnit()) %></strong></div></div>
                <a class="btn btn-secondary btn-sm" style="margin-top:12px;display:inline-flex" href="<%= ctx %>/staff/inventory/edit?id=<%= ingredient.getId() %>">Edit details</a>
                <form method="post" action="<%= ctx %>/staff/inventory/delete" style="margin-top:10px"
                      onsubmit="return confirm('Delete this ingredient? Only possible while it has no stock history.');">
                    <input type="hidden" name="id" value="<%= ingredient.getId() %>">
                    <button class="btn btn-danger btn-sm" type="submit">Delete ingredient</button>
                </form>
            </article>
            <article class="panel">
                <span class="section-kicker">Business checks</span>
                <ul class="check-list operational-checks"><li>Stock can never go below zero.</li><li>Every purchase, usage, waste, return, or adjustment is recorded.</li><li>Ingredients with recorded history cannot be deleted.</li><li>Names must stay unique across the catalogue.</li></ul>
            </article>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
