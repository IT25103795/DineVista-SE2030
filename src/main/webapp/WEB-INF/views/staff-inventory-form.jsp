<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.IngredientRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    IngredientRecord ingredient = (IngredientRecord) request.getAttribute("ingredient");
    boolean editing = ingredient != null;
    request.setAttribute("pageTitle", editing ? "Edit Ingredient" : "Add Ingredient");
    request.setAttribute("activeNav", "staffInventory");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/inventory">Inventory</a><span>/</span><span><%= editing ? "Edit" : "Add" %></span></div>
            <span class="eyebrow">Inventory Management</span>
            <h1><%= editing ? "Edit ingredient." : "Add a new ingredient." %></h1>
            <p><%= editing ? "Update details for " + HtmlUtil.escape(ingredient.getName()) + ". Stock quantity itself changes only through recorded transactions." : "Register a new stock-controlled ingredient. Opening quantity starts at zero -- record a purchase transaction next to bring in stock." %></p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/inventory">Back to inventory</a>
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
            <form method="post" action="<%= ctx %>/staff/inventory/save" novalidate>
                <input type="hidden" name="id" value="<%= editing ? ingredient.getId() : 0 %>">
                <div class="form-grid">
                    <div class="form-group full">
                        <label for="name">Ingredient name</label>
                        <input class="form-control" id="name" name="name" type="text" required maxlength="140"
                               value="<%= editing ? HtmlUtil.escape(ingredient.getName()) : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="unit">Unit of measurement</label>
                        <input class="form-control" id="unit" name="unit" type="text" required maxlength="30"
                               placeholder="kg, l, pcs" value="<%= editing ? HtmlUtil.escape(ingredient.getUnit()) : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="reorderLevel">Reorder level</label>
                        <input class="form-control" id="reorderLevel" name="reorderLevel" type="number" step="0.001" min="0" required
                               value="<%= editing ? ingredient.getReorderLevel().stripTrailingZeros().toPlainString() : "0" %>">
                    </div>
                    <div class="form-group">
                        <label for="unitCost">Unit cost (LKR, optional)</label>
                        <input class="form-control" id="unitCost" name="unitCost" type="number" step="0.01" min="0"
                               value="<%= editing && ingredient.getUnitCost() != null ? ingredient.getUnitCost().toPlainString() : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="supplierName">Supplier (optional)</label>
                        <input class="form-control" id="supplierName" name="supplierName" type="text" maxlength="160"
                               value="<%= editing && ingredient.getSupplierName() != null ? HtmlUtil.escape(ingredient.getSupplierName()) : "" %>">
                    </div>
                </div>
                <div class="form-actions">
                    <button class="btn btn-primary" type="submit"><%= editing ? "Save changes" : "Add ingredient" %></button>
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/inventory">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
