<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.MenuCategoryRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    MenuCategoryRecord category = (MenuCategoryRecord) request.getAttribute("category");
    request.setAttribute("pageTitle", "Edit Menu Category");
    request.setAttribute("activeNav", "staffMenu");
    String name = request.getAttribute("categoryNameDraft") == null
            ? category.getName() : String.valueOf(request.getAttribute("categoryNameDraft"));
    String description = request.getAttribute("categoryDescriptionDraft") == null
            ? category.getDescription() : String.valueOf(request.getAttribute("categoryDescriptionDraft"));
    String displayOrder = request.getAttribute("displayOrderDraft") == null
            ? String.valueOf(category.getDisplayOrder()) : String.valueOf(request.getAttribute("displayOrderDraft"));
    boolean active = request.getAttribute("activeDraft") == null
            ? category.isActive() : Boolean.TRUE.equals(request.getAttribute("activeDraft"));
%>
<%@ include file="fragments/header.jspf" %>

<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark">
                <a href="<%= ctx %>/staff/menu#categories-section">Menu categories</a><span>/</span><span>Edit</span>
            </div>
            <span class="eyebrow">Menu management</span>
            <h1>Edit menu category.</h1>
            <p>Update the category name, guest description, display order, and visibility.</p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/menu#categories-section">Back to categories</a>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container" style="max-width:900px;">
        <div class="form-card">
            <% if (request.getAttribute("errors") != null) { %>
                <div class="alert alert-danger" role="alert">
                    <ul>
                        <% for (String error : (List<String>) request.getAttribute("errors")) { %>
                            <li><%= HtmlUtil.escape(error) %></li>
                        <% } %>
                    </ul>
                </div>
            <% } %>
            <form method="post" action="<%= ctx %>/staff/menu/category/save" novalidate>
                <input type="hidden" name="categoryId" value="<%= category.getId() %>">
                <div class="form-grid">
                    <div class="form-group full">
                        <label for="categoryName">Category name</label>
                        <input class="form-control" id="categoryName" name="categoryName" type="text"
                               value="<%= HtmlUtil.escape(name) %>" maxlength="100" required>
                    </div>
                    <div class="form-group full">
                        <label for="categoryDescription">Description for guests (optional)</label>
                        <input class="form-control" id="categoryDescription" name="categoryDescription" type="text"
                               value="<%= HtmlUtil.escape(description) %>" maxlength="255">
                    </div>
                    <div class="form-group">
                        <label for="displayOrder">Display order</label>
                        <input class="form-control" id="displayOrder" name="displayOrder" type="number"
                               value="<%= HtmlUtil.escape(displayOrder) %>" min="0" step="1" required>
                    </div>
                    <div class="form-group">
                        <label for="active">Visibility</label>
                        <select class="form-control" id="active" name="active">
                            <option value="1" <%= active ? "selected" : "" %>>Active — shown to guests</option>
                            <option value="0" <%= active ? "" : "selected" %>>Hidden — not shown to guests</option>
                        </select>
                    </div>
                </div>
                <div style="display:flex; justify-content:flex-end; flex-wrap:wrap; gap:12px; margin-top:26px;">
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/menu#categories-section">Cancel</a>
                    <button class="btn btn-primary" type="submit">Update category</button>
                </div>
            </form>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
