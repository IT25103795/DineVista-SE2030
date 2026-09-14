<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.MenuItemAdminRecord" %>
<%@ page import="com.dinevista.model.MenuCategoryRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    MenuItemAdminRecord item = (MenuItemAdminRecord) request.getAttribute("item");
    List<MenuCategoryRecord> categories = (List<MenuCategoryRecord>) request.getAttribute("categories");
    boolean editing = item != null;

    request.setAttribute("pageTitle", editing ? "Edit Menu Item" : "Add Menu Item");
    request.setAttribute("activeNav", "staffMenu");

    String selectedDietary = editing ? item.getDietaryType() : "REGULAR";
    String selectedSpice = editing ? item.getSpiceLevel() : "NONE";
    String selectedStatus = editing ? item.getAvailabilityStatus() : "AVAILABLE";
    long selectedCategoryId = editing ? item.getCategoryId() : 0L;
%>
<%@ include file="fragments/header.jspf" %>

<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark">
                <a href="<%= ctx %>/staff/menu">Menu Management</a><span>/</span><span><%= editing ? "Edit" : "New" %></span>
            </div>
            <span class="eyebrow">Menu Management</span>
            <h1><%= editing ? "Edit menu dish." : "Add a new dish to the menu." %></h1>
            <p><%= editing ? "Update dish specifications, price, preparation time, and availability for '" + HtmlUtil.escape(item.getName()) + "'." : "Enter dish details, pricing, and dietary labels. Published dishes are immediately visible on the guest menu and ordering system." %></p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/menu">Back to menu</a>
    </div>
</section>

<section class="section-sm">
    <div class="container form-layout">
        <div class="form-card">
            <% if (request.getAttribute("errors") != null) { %>
                <div class="alert alert-danger">
                    <div>
                        <strong>Please resolve the following issues:</strong>
                        <ul>
                            <% for (String error : (List<String>) request.getAttribute("errors")) { %>
                                <li><%= HtmlUtil.escape(error) %></li>
                            <% } %>
                        </ul>
                    </div>
                </div>
            <% } %>

            <form method="post" action="<%= ctx %>/staff/menu/save" novalidate>
                <input type="hidden" name="id" value="<%= editing ? item.getId() : 0 %>">

                <div class="form-grid">
                    <!-- Dish name -->
                    <div class="form-group full">
                        <label for="name">Dish name <span style="color:#b3261e;">*</span></label>
                        <input class="form-control" id="name" name="name" type="text" required maxlength="140"
                               placeholder="e.g. Ceylon Cinnamon Spiced Lamb"
                               value="<%= editing ? HtmlUtil.escape(item.getName()) : "" %>">
                    </div>

                    <!-- Category -->
                    <div class="form-group">
                        <label for="categoryId">Menu category <span style="color:#b3261e;">*</span></label>
                        <select class="form-control" id="categoryId" name="categoryId" required>
                            <option value="">-- Select category --</option>
                            <% if (categories != null) { for (MenuCategoryRecord cat : categories) { %>
                                <option value="<%= cat.getId() %>" <%= selectedCategoryId == cat.getId() ? "selected" : "" %>>
                                    <%= HtmlUtil.escape(cat.getName()) %>
                                </option>
                            <% }} %>
                        </select>
                    </div>

                    <!-- Price -->
                    <div class="form-group">
                        <label for="price">Price in LKR <span style="color:#b3261e;">* (BR-MEN-01: &gt; 0)</span></label>
                        <input class="form-control" id="price" name="price" type="number" step="0.01" min="0.01" required
                               placeholder="e.g. 2450.00"
                               value="<%= editing ? item.getPrice().toPlainString() : "" %>">
                    </div>

                    <!-- Preparation minutes -->
                    <div class="form-group">
                        <label for="preparationMinutes">Preparation time (minutes)</label>
                        <input class="form-control" id="preparationMinutes" name="preparationMinutes" type="number" min="1" max="180"
                               value="<%= editing ? item.getPreparationMinutes() : 20 %>">
                    </div>

                    <!-- Dietary type -->
                    <div class="form-group">
                        <label for="dietaryType">Dietary classification</label>
                        <select class="form-control" id="dietaryType" name="dietaryType">
                            <option value="REGULAR" <%= "REGULAR".equals(selectedDietary) ? "selected" : "" %>>Regular</option>
                            <option value="VEGETARIAN" <%= "VEGETARIAN".equals(selectedDietary) ? "selected" : "" %>>Vegetarian</option>
                            <option value="VEGAN" <%= "VEGAN".equals(selectedDietary) ? "selected" : "" %>>Vegan</option>
                            <option value="GLUTEN_AWARE" <%= "GLUTEN_AWARE".equals(selectedDietary) ? "selected" : "" %>>Gluten-Aware</option>
                        </select>
                    </div>

                    <!-- Spice level -->
                    <div class="form-group">
                        <label for="spiceLevel">Spice level</label>
                        <select class="form-control" id="spiceLevel" name="spiceLevel">
                            <option value="NONE" <%= "NONE".equals(selectedSpice) ? "selected" : "" %>>None (Mild/Sweet)</option>
                            <option value="MILD" <%= "MILD".equals(selectedSpice) ? "selected" : "" %>>Mild</option>
                            <option value="MEDIUM" <%= "MEDIUM".equals(selectedSpice) ? "selected" : "" %>>Medium</option>
                            <option value="HOT" <%= "HOT".equals(selectedSpice) ? "selected" : "" %>>Hot</option>
                        </select>
                    </div>

                    <!-- Availability status -->
                    <div class="form-group">
                        <label for="availabilityStatus">Availability status</label>
                        <select class="form-control" id="availabilityStatus" name="availabilityStatus">
                            <option value="AVAILABLE" <%= "AVAILABLE".equals(selectedStatus) ? "selected" : "" %>>Available for ordering</option>
                            <option value="SOLD_OUT" <%= "SOLD_OUT".equals(selectedStatus) ? "selected" : "" %>>Sold out today</option>
                            <option value="UNAVAILABLE" <%= "UNAVAILABLE".equals(selectedStatus) ? "selected" : "" %>>Unavailable / Archived</option>
                        </select>
                    </div>

                    <!-- Image / Icon -->
                    <div class="form-group">
                        <label for="imagePath">Artwork / icon filename</label>
                        <select class="form-control" id="imagePath" name="imagePath">
                            <%
                                String currentImg = editing && item.getImagePath() != null ? item.getImagePath() : "dish-signature.svg";
                            %>
                            <option value="dish-signature.svg" <%= "dish-signature.svg".equals(currentImg) ? "selected" : "" %>>dish-signature.svg (Signature Chef Dish)</option>
                            <option value="dish-curry.svg" <%= "dish-curry.svg".equals(currentImg) ? "selected" : "" %>>dish-curry.svg (Curry / Sri Lankan)</option>
                            <option value="dish-seafood.svg" <%= "dish-seafood.svg".equals(currentImg) ? "selected" : "" %>>dish-seafood.svg (Seafood / Fish)</option>
                            <option value="dish-dessert.svg" <%= "dish-dessert.svg".equals(currentImg) ? "selected" : "" %>>dish-dessert.svg (Dessert / Sweet)</option>
                            <option value="hero-dish.jpg" <%= "hero-dish.jpg".equals(currentImg) ? "selected" : "" %>>hero-dish.jpg (Gourmet Fine Dining Photo)</option>
                        </select>
                    </div>

                    <!-- Description -->
                    <div class="form-group full">
                        <label for="description">Dish description</label>
                        <textarea class="form-control" id="description" name="description" rows="3" maxlength="600"
                                  placeholder="Describe the dish, ingredients, flavours, or accompaniments..."><%= editing && item.getDescription() != null ? HtmlUtil.escape(item.getDescription()) : "" %></textarea>
                        <span class="muted small">Max 600 characters. Displayed on public menu and order checkout.</span>
                    </div>
                </div>

                <div class="form-actions" style="margin-top: 24px; display: flex; gap: 12px; justify-content: flex-end;">
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/menu">Cancel</a>
                    <button class="btn btn-primary" type="submit">
                        <%= editing ? "Update menu item" : "Publish to menu" %>
                    </button>
                </div>
            </form>
        </div>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
