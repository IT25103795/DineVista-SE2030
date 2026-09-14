<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.MenuItemAdminRecord" %>
<%@ page import="com.dinevista.model.MenuCategoryRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Menu Management");
    request.setAttribute("activeNav", "staffMenu");

    List<MenuItemAdminRecord> items = (List<MenuItemAdminRecord>) request.getAttribute("items");
    List<MenuCategoryRecord> categories = (List<MenuCategoryRecord>) request.getAttribute("categories");
    String search = (String) request.getAttribute("menuSearch");
    Long selectedCategory = (Long) request.getAttribute("selectedCategory");
    String selectedStatus = (String) request.getAttribute("selectedStatus");

    Long totalItems = (Long) request.getAttribute("totalItemsCount");
    Long availableCount = (Long) request.getAttribute("availableCount");
    Long unavailableCount = (Long) request.getAttribute("unavailableCount");
    Long categoriesCount = (Long) request.getAttribute("categoriesCount");

    if (search == null) search = "";
    if (selectedCategory == null) selectedCategory = 0L;
    if (selectedStatus == null) selectedStatus = "ALL";
    if (totalItems == null) totalItems = 0L;
    if (availableCount == null) availableCount = 0L;
    if (unavailableCount == null) unavailableCount = 0L;
    if (categoriesCount == null) categoriesCount = 0L;
%>
<%@ include file="fragments/header.jspf" %>

<section class="operations-hero">
    <div class="container">
        <div>
            <span class="eyebrow">Restaurant staff operations &middot; IT25103794</span>
            <h1>Menu management & publishing.</h1>
            <p>Maintain accurate dishes, categories, pricing, and live availability so customers and waiters see real-time options for dine-in, takeaway, and pre-orders.</p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/dashboard">Operations dashboard</a>
            <a class="btn btn-primary" href="<%= ctx %>/staff/menu/new">Add menu item</a>
        </div>
    </div>
</section>

<section class="section-sm operations-section">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div>
        <% } %>
        <% if (request.getAttribute("errors") != null) { %>
            <div class="alert alert-danger">
                <ul>
                    <% for (String error : (List<String>) request.getAttribute("errors")) { %>
                        <li><%= HtmlUtil.escape(error) %></li>
                    <% } %>
                </ul>
            </div>
        <% } %>

        <!-- Filter bar -->
        <form class="operations-filter" method="get" action="<%= ctx %>/staff/menu">
            <div class="form-group" style="flex: 2 1 240px;">
                <label for="search">Search dish or ingredient</label>
                <input class="form-control" id="search" name="search" type="text" placeholder="e.g. Chicken, Curry, Seafood" value="<%= HtmlUtil.escape(search) %>">
            </div>
            <div class="form-group" style="flex: 1 1 180px;">
                <label for="category">Category</label>
                <select class="form-control" id="category" name="category">
                    <option value="0">All categories</option>
                    <% if (categories != null) { for (MenuCategoryRecord cat : categories) { %>
                        <option value="<%= cat.getId() %>" <%= selectedCategory == cat.getId() ? "selected" : "" %>>
                            <%= HtmlUtil.escape(cat.getName()) %>
                        </option>
                    <% }} %>
                </select>
            </div>
            <div class="form-group" style="flex: 1 1 160px;">
                <label for="status">Availability</label>
                <select class="form-control" id="status" name="status">
                    <option value="ALL" <%= "ALL".equals(selectedStatus) ? "selected" : "" %>>All statuses</option>
                    <option value="AVAILABLE" <%= "AVAILABLE".equals(selectedStatus) ? "selected" : "" %>>Available only</option>
                    <option value="SOLD_OUT" <%= "SOLD_OUT".equals(selectedStatus) ? "selected" : "" %>>Sold out today</option>
                    <option value="UNAVAILABLE" <%= "UNAVAILABLE".equals(selectedStatus) ? "selected" : "" %>>Unavailable / Archived</option>
                </select>
            </div>
            <button class="btn btn-dark" type="submit">Apply filters</button>
            <a class="btn btn-secondary" href="<%= ctx %>/staff/menu">Clear</a>
        </form>

        <!-- KPI summary counters -->
        <div class="operations-summary">
            <div><strong><%= totalItems %></strong><span>Total dishes</span></div>
            <div><strong style="color: #2b7f68;"><%= availableCount %></strong><span>Live & available</span></div>
            <div><strong style="color: #e96f3d;"><%= unavailableCount %></strong><span>Sold out / inactive</span></div>
            <div><strong><%= categoriesCount %></strong><span>Active categories</span></div>
        </div>

        <!-- Menu items table -->
        <section class="panel operations-table-panel">
            <div class="panel-header" style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
                <div>
                    <h3>Menu items (<%= items == null ? 0 : items.size() %>)</h3>
                    <span class="muted small">All dishes visible to staff. Changes update the public customer menu instantly.</span>
                </div>
                <div>
                    <a class="btn btn-secondary btn-sm" href="#categories-section">&darr; Manage categories</a>
                </div>
            </div>
            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead>
                        <tr>
                            <th>Dish name</th>
                            <th>Category</th>
                            <th>Price</th>
                            <th>Prep time</th>
                            <th>Dietary &amp; spice</th>
                            <th>Status</th>
                            <th style="text-align:right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (items == null || items.isEmpty()) { %>
                        <tr>
                            <td colspan="7">
                                <div class="empty-table-message" style="padding: 40px 20px; text-align: center;">
                                    <p style="margin-bottom: 12px; font-weight: 600;">No menu items match the specified filters.</p>
                                    <a class="btn btn-primary btn-sm" href="<%= ctx %>/staff/menu/new">Add a new dish</a>
                                </div>
                            </td>
                        </tr>
                    <% } else { for (MenuItemAdminRecord item : items) { %>
                        <tr>
                            <td>
                                <div>
                                    <strong><%= HtmlUtil.escape(item.getName()) %></strong>
                                    <% if (item.getDescription() != null && !item.getDescription().isEmpty()) { %>
                                        <div class="muted small" style="max-width: 320px; white-space: normal; line-height: 1.35; margin-top: 2px;">
                                            <%= HtmlUtil.escape(item.getDescription()) %>
                                        </div>
                                    <% } %>
                                </div>
                            </td>
                            <td>
                                <span class="badge badge-light" style="font-weight: 600;">
                                    <%= HtmlUtil.escape(item.getCategoryName()) %>
                                </span>
                            </td>
                            <td>
                                <strong style="font-size: 0.98rem; color: var(--brand-strong);"><%= item.getPriceDisplay() %></strong>
                            </td>
                            <td>
                                <span><%= item.getPreparationMinutes() %> mins</span>
                            </td>
                            <td>
                                <div style="display:flex; gap:4px; flex-wrap:wrap;">
                                    <% if (!"REGULAR".equals(item.getDietaryType())) { %>
                                        <span class="chip" style="font-size:0.75rem; padding:2px 8px; min-height:auto;"><%= item.getDietaryType() %></span>
                                    <% } %>
                                    <% if (!"NONE".equals(item.getSpiceLevel())) { %>
                                        <span class="chip" style="font-size:0.75rem; padding:2px 8px; min-height:auto; color:#b5451b;"><%= item.getSpiceLevel() %></span>
                                    <% } %>
                                </div>
                            </td>
                            <td>
                                <span class="status <%= item.getStatusCss() %>">
                                    <%= item.getStatusLabel() %>
                                </span>
                            </td>
                            <td style="text-align:right;">
                                <div style="display:inline-flex; gap:6px; align-items:center;">
                                    <!-- 1-Click Availability Toggle -->
                                    <form method="post" action="<%= ctx %>/staff/menu/toggle-status" style="display:inline;">
                                        <input type="hidden" name="id" value="<%= item.getId() %>">
                                        <button class="btn btn-secondary btn-sm" type="submit" title="Toggle between Available and Sold Out">
                                            <%= item.isAvailable() ? "Mark Sold Out" : "Make Available" %>
                                        </button>
                                    </form>

                                    <!-- Edit Item -->
                                    <a class="btn btn-secondary btn-sm" href="<%= ctx %>/staff/menu/edit?id=<%= item.getId() %>">
                                        Edit
                                    </a>

                                    <!-- Delete / Archive Item -->
                                    <form method="post" action="<%= ctx %>/staff/menu/delete" style="display:inline;" onsubmit="return confirm('Are you sure you want to remove \'<%= HtmlUtil.escape(item.getName()) %>\'? If it has linked orders, it will be safely archived.');">
                                        <input type="hidden" name="id" value="<%= item.getId() %>">
                                        <button class="btn btn-sm" style="color: #b3261e; background: rgba(179,38,30,.08); border: 1px solid rgba(179,38,30,.2);" type="submit" title="Delete or Archive">
                                            Delete
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>

        <!-- Categories Section -->
        <section id="categories-section" class="panel operations-table-panel" style="margin-top: 40px;">
            <div class="panel-header" style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
                <div>
                    <h3>Menu Categories (<%= categories == null ? 0 : categories.size() %>)</h3>
                    <span class="muted small">Categories group dishes on both the public menu and the staff kitchen screen.</span>
                </div>
            </div>

            <!-- Quick Add Category Form -->
            <div style="padding: 16px 20px; background: var(--surface-alt, rgba(0,0,0,.02)); border-bottom: 1px solid var(--line);">
                <form method="post" action="<%= ctx %>/staff/menu/category/save" style="display:flex; gap:12px; align-items:flex-end; flex-wrap:wrap;">
                    <input type="hidden" name="categoryId" value="0">
                    <input type="hidden" name="active" value="1">
                    <div style="flex: 2 1 200px;">
                        <label class="small" style="font-weight:700; margin-bottom:4px; display:block;">New Category Name</label>
                        <input class="form-control" name="categoryName" type="text" placeholder="e.g. Starters, Beverages" required maxlength="100">
                    </div>
                    <div style="flex: 3 1 280px;">
                        <label class="small" style="font-weight:700; margin-bottom:4px; display:block;">Description (optional)</label>
                        <input class="form-control" name="categoryDescription" type="text" placeholder="Short description for guests" maxlength="255">
                    </div>
                    <div style="flex: 1 1 100px;">
                        <label class="small" style="font-weight:700; margin-bottom:4px; display:block;">Display Order</label>
                        <input class="form-control" name="displayOrder" type="number" value="<%= (categories != null ? categories.size() + 1 : 1) %>" min="0">
                    </div>
                    <button class="btn btn-primary" type="submit">+ Add Category</button>
                </form>
            </div>

            <div class="table-wrap">
                <table class="data-table operations-table">
                    <thead>
                        <tr>
                            <th>Display order</th>
                            <th>Category name</th>
                            <th>Description</th>
                            <th>Status</th>
                            <th style="text-align:right;">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (categories != null) { for (MenuCategoryRecord cat : categories) { %>
                        <tr>
                            <td><strong>#<%= cat.getDisplayOrder() %></strong></td>
                            <td><strong><%= HtmlUtil.escape(cat.getName()) %></strong></td>
                            <td><%= HtmlUtil.escape(cat.getDescription() == null || cat.getDescription().isEmpty() ? "—" : cat.getDescription()) %></td>
                            <td><span class="status <%= cat.getStatusCss() %>"><%= cat.getStatusLabel() %></span></td>
                            <td style="text-align:right;">
                                <form method="post" action="<%= ctx %>/staff/menu/category/delete" style="display:inline;" onsubmit="return confirm('Delete category \'<%= HtmlUtil.escape(cat.getName()) %>\'? This is only allowed if it contains zero dishes.');">
                                    <input type="hidden" name="categoryId" value="<%= cat.getId() %>">
                                    <button class="btn btn-sm" style="color:#b3261e; background:rgba(179,38,30,.06); border:1px solid rgba(179,38,30,.2);" type="submit">
                                        Delete
                                    </button>
                                </form>
                            </td>
                        </tr>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
