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
            <span class="eyebrow">Restaurant staff operations</span>
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
            <div class="panel-header table-panel-highlight-header" style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:16px;">
                <div style="display:flex; align-items:center; gap:14px;">
                    <div style="display:inline-grid; width:44px; height:44px; place-items:center; border-radius:12px; background:linear-gradient(135deg, #a855f7, #7c3aed); color:#ffffff; box-shadow:0 6px 18px rgba(168, 85, 247, 0.38); flex-shrink:0;">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" style="width:22px; height:22px;" aria-hidden="true">
                            <path d="M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8zM6 1v3M10 1v3M14 1v3"/>
                        </svg>
                    </div>
                    <div>
                        <h3 class="table-main-title" style="margin:0; font-size:1.32rem; font-weight:850; letter-spacing:-0.01em; display:flex; align-items:center; gap:10px;">
                            Menu items
                            <span class="badge badge-table-count" style="background:rgba(168, 85, 247, 0.16); color:#7c3aed; border:1px solid rgba(168, 85, 247, 0.35); padding:3px 12px; border-radius:99px; font-size:0.82rem; font-weight:800;">
                                <%= items == null ? 0 : items.size() %> dishes
                            </span>
                        </h3>
                        <span class="muted small" style="margin-top:4px; display:block;">All dishes visible to staff. Changes update the public customer menu instantly.</span>
                    </div>
                </div>
                <div>
                    <a class="btn btn-secondary btn-sm" href="#categories-section" style="font-weight:700;">&darr; Manage categories</a>
                </div>
            </div>
            <div class="table-wrap" style="overflow-x: auto;">
                <table class="data-table operations-table menu-admin-table" style="min-width: 980px; width: 100%;">
                    <thead>
                        <tr>
                            <th style="min-width: 220px; white-space: nowrap;">Dish name</th>
                            <th style="min-width: 120px; white-space: nowrap;">Category</th>
                            <th style="min-width: 95px; white-space: nowrap;">Price</th>
                            <th style="min-width: 100px; white-space: nowrap;">Prep time</th>
                            <th style="min-width: 140px; white-space: nowrap;">Dietary &amp; spice</th>
                            <th style="min-width: 110px; white-space: nowrap;">Status</th>
                            <th style="min-width: 260px; text-align:right; white-space: nowrap;">Actions</th>
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
                                    <strong style="display: block; font-size: 0.94rem;"><%= HtmlUtil.escape(item.getName()) %></strong>
                                    <% if (item.getDescription() != null && !item.getDescription().isEmpty()) { %>
                                        <div class="muted small" style="max-width: 320px; white-space: normal; line-height: 1.35; margin-top: 2px;">
                                            <%= HtmlUtil.escape(item.getDescription()) %>
                                        </div>
                                    <% } %>
                                </div>
                            </td>
                            <td style="white-space: nowrap;">
                                <span class="badge badge-light" style="font-weight: 600; white-space: nowrap; display: inline-block;">
                                    <%= HtmlUtil.escape(item.getCategoryName()) %>
                                </span>
                            </td>
                            <td style="white-space: nowrap;">
                                <strong style="font-size: 0.98rem; color: var(--brand-strong); white-space: nowrap;"><%= item.getPriceDisplay() %></strong>
                            </td>
                            <td style="white-space: nowrap;">
                                <span style="white-space: nowrap;"><%= item.getPreparationMinutes() %> mins</span>
                            </td>
                            <td style="white-space: nowrap;">
                                <div style="display:flex; gap:4px; flex-wrap:nowrap; align-items:center;">
                                    <% if (!"REGULAR".equals(item.getDietaryType())) { %>
                                        <span class="chip" style="font-size:0.75rem; padding:2px 8px; min-height:auto; white-space: nowrap;"><%= item.getDietaryType() %></span>
                                    <% } %>
                                    <% if (!"NONE".equals(item.getSpiceLevel())) { %>
                                        <span class="chip" style="font-size:0.75rem; padding:2px 8px; min-height:auto; color:#b5451b; white-space: nowrap;"><%= item.getSpiceLevel() %></span>
                                    <% } %>
                                </div>
                            </td>
                            <td style="white-space: nowrap;">
                                <span class="status <%= item.getStatusCss() %>" style="white-space: nowrap; display: inline-block;">
                                    <%= item.getStatusLabel() %>
                                </span>
                            </td>
                            <td style="text-align:right; white-space: nowrap;">
                                <div style="display:inline-flex; gap:8px; align-items:center; justify-content:flex-end; white-space:nowrap;">
                                    <!-- 1-Click Availability Toggle -->
                                    <form method="post" action="<%= ctx %>/staff/menu/toggle-status" style="display:inline; margin:0;">
                                        <input type="hidden" name="id" value="<%= item.getId() %>">
                                        <button class="btn btn-secondary btn-sm" style="white-space: nowrap; font-weight: 600;" type="submit" title="Toggle between Available and Sold Out">
                                            <%= item.isAvailable() ? "Mark Sold Out" : "Make Available" %>
                                        </button>
                                    </form>

                                    <!-- Edit Item -->
                                    <a class="btn btn-secondary btn-sm" style="white-space: nowrap; font-weight: 600;" href="<%= ctx %>/staff/menu/edit?id=<%= item.getId() %>">
                                        Edit
                                    </a>

                                    <!-- Delete / Archive Item -->
                                    <form method="post" action="<%= ctx %>/staff/menu/delete" style="display:inline; margin:0;" onsubmit="return confirm('Are you sure you want to remove \'<%= HtmlUtil.escape(item.getName()) %>\'? If it has linked orders, it will be safely archived.');">
                                        <input type="hidden" name="id" value="<%= item.getId() %>">
                                        <button class="btn btn-sm" style="white-space: nowrap; font-weight: 600; color: #b3261e; background: rgba(179,38,30,.08); border: 1px solid rgba(179,38,30,.2);" type="submit" title="Delete or Archive">
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
            <div class="panel-header table-panel-highlight-header" style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:16px;">
                <div style="display:flex; align-items:center; gap:14px;">
                    <div style="display:inline-grid; width:44px; height:44px; place-items:center; border-radius:12px; background:linear-gradient(135deg, #a855f7, #7c3aed); color:#ffffff; box-shadow:0 6px 18px rgba(168, 85, 247, 0.38); flex-shrink:0;">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" style="width:22px; height:22px;" aria-hidden="true">
                            <rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect>
                            <rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect>
                        </svg>
                    </div>
                    <div>
                        <h3 class="table-main-title" style="margin:0; font-size:1.32rem; font-weight:850; letter-spacing:-0.01em; display:flex; align-items:center; gap:10px;">
                            Menu Categories
                            <span class="badge badge-table-count" style="background:rgba(168, 85, 247, 0.16); color:#7c3aed; border:1px solid rgba(168, 85, 247, 0.35); padding:3px 12px; border-radius:99px; font-size:0.82rem; font-weight:800;">
                                <%= categories == null ? 0 : categories.size() %> active
                            </span>
                        </h3>
                        <span class="muted small" style="margin-top:4px; display:block;">Categories group dishes on both the public menu and the staff kitchen screen.</span>
                    </div>
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

            <div class="table-wrap" style="overflow-x: auto;">
                <table class="data-table operations-table" style="min-width: 760px; width: 100%;">
                    <thead>
                        <tr>
                            <th style="min-width: 120px; white-space: nowrap;">Display order</th>
                            <th style="min-width: 180px; white-space: nowrap;">Category name</th>
                            <th style="min-width: 260px;">Description</th>
                            <th style="min-width: 110px; white-space: nowrap;">Status</th>
                            <th style="min-width: 100px; text-align:right; white-space: nowrap;">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                    <% if (categories != null) { for (MenuCategoryRecord cat : categories) { %>
                        <tr>
                            <td style="white-space: nowrap;"><strong>#<%= cat.getDisplayOrder() %></strong></td>
                            <td style="white-space: nowrap;"><strong><%= HtmlUtil.escape(cat.getName()) %></strong></td>
                            <td><%= HtmlUtil.escape(cat.getDescription() == null || cat.getDescription().isEmpty() ? "—" : cat.getDescription()) %></td>
                            <td style="white-space: nowrap;"><span class="status <%= cat.getStatusCss() %>" style="white-space: nowrap;"><%= cat.getStatusLabel() %></span></td>
                            <td style="text-align:right; white-space: nowrap;">
                                <form method="post" action="<%= ctx %>/staff/menu/category/delete" style="display:inline; margin:0;" onsubmit="return confirm('Delete category \'<%= HtmlUtil.escape(cat.getName()) %>\'? This is only allowed if it contains zero dishes.');">
                                    <input type="hidden" name="categoryId" value="<%= cat.getId() %>">
                                    <button class="btn btn-sm" style="white-space: nowrap; font-weight: 600; color:#b3261e; background:rgba(179,38,30,.06); border:1px solid rgba(179,38,30,.2);" type="submit">
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
