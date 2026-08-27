<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.IngredientRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Inventory Management");
    request.setAttribute("activeNav", "staffInventory");
    List<IngredientRecord> ingredients = (List<IngredientRecord>) request.getAttribute("ingredients");
    String search = (String) request.getAttribute("inventorySearch");
    boolean lowStockOnly = Boolean.TRUE.equals(request.getAttribute("inventoryLowStockOnly"));
    Long lowStockCount = (Long) request.getAttribute("lowStockCount");
    if (search == null) search = "";
    if (lowStockCount == null) lowStockCount = 0L;
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <span class="eyebrow">Restaurant staff operations · Owner: Hansaka A. K.</span>
            <h1>Inventory control centre.</h1>
            <p>Maintain accurate ingredients, stock quantities, reorder levels, and an auditable transaction history so the kitchen and menu always reflect real availability.</p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/dashboard">Operations dashboard</a>
            <a class="btn btn-primary" href="<%= ctx %>/staff/inventory/new">Add ingredient</a>
        </div>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
        <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div><% } %>

        <form class="operations-filter" method="get" action="<%= ctx %>/staff/inventory">
            <div class="form-group"><label for="search">Search ingredient</label>
                <input class="form-control" id="search" name="search" type="text" placeholder="e.g. Chicken" value="<%= HtmlUtil.escape(search) %>">
            </div>
            <div class="form-group">
                <label for="lowStockToggle">&nbsp;</label>
                <label style="display:flex;align-items:center;gap:8px;font-weight:600">
                    <input id="lowStockToggle" type="checkbox" name="lowStock" value="1" <%= lowStockOnly ? "checked" : "" %>>
                    Low stock only
                </label>
            </div>
            <button class="btn btn-dark" type="submit">Apply filters</button>
            <a class="btn btn-secondary" href="<%= ctx %>/staff/inventory">Clear</a>
        </form>

        <div class="operations-summary">
            <div><strong><%= ingredients == null ? 0 : ingredients.size() %></strong><span>Matching ingredients</span></div>
            <div><strong><%= lowStockCount %></strong><span>Below reorder level</span></div>
            <div><strong><%= ingredients == null ? 0 : ingredients.size() - lowStockCount %></strong><span>Healthy stock</span></div>
        </div>

        <section class="panel operations-table-panel">
            <div class="panel-header"><div><h3>Ingredient records</h3><span class="muted small">Open a record to review history or log a stock movement.</span></div></div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead><tr><th>Ingredient</th><th>On hand</th><th>Reorder level</th><th>Unit cost</th><th>Supplier</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <% if (ingredients == null || ingredients.isEmpty()) { %>
                        <tr><td colspan="7"><div class="empty-table-message">No ingredients match the current filters.</div></td></tr>
                    <% } else { for (IngredientRecord ingredient : ingredients) { %>
                        <tr>
                            <td><strong><%= HtmlUtil.escape(ingredient.getName()) %></strong></td>
                            <td><%= HtmlUtil.escape(ingredient.getQuantityDisplay()) %></td>
                            <td><%= HtmlUtil.escape(ingredient.getReorderLevelDisplay()) %></td>
                            <td><%= HtmlUtil.escape(ingredient.getUnitCostDisplay()) %></td>
                            <td><%= HtmlUtil.escape(ingredient.getSupplierName() == null ? "—" : ingredient.getSupplierName()) %></td>
                            <td><span class="status <%= ingredient.getStockStatusCss() %>"><%= ingredient.getStockStatusLabel() %></span></td>
                            <td><a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/inventory/view?id=<%= ingredient.getId() %>">Manage</a></td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
