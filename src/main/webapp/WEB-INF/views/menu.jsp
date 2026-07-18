<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.dinevista.model.MenuItemRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Restaurant Menu");
    request.setAttribute("activeNav", "menu");
    List<MenuItemRecord> menuItems = (List<MenuItemRecord>) request.getAttribute("menuItems");
    Set<String> categories = new LinkedHashSet<>();
    if (menuItems != null) {
        for (MenuItemRecord item : menuItems) categories.add(item.getCategory());
    }
%>
<%@ include file="fragments/header.jspf" %>
<section class="page-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><span>Menu</span></div>
        <span class="eyebrow">Restaurant menu</span>
        <h1>Fresh ingredients. Confident flavours. Something for every table.</h1>
        <p>Explore the live DineVista menu, filter by category, and add available items directly to your food order.</p>
        <div class="hero-actions">
            <a class="btn btn-primary" href="<%= ctx %>/orders#food-menu">Build an order</a>
            <a class="btn btn-secondary" href="<%= ctx %>/reservations">Reserve a table</a>
        </div>
    </div>
</section>

<section class="section-sm">
    <div class="container">
        <div class="filter-bar">
            <div class="search-control">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg>
                <input type="search" data-menu-search placeholder="Search dishes, ingredients, or categories" aria-label="Search menu">
            </div>
            <div class="chip-row" aria-label="Menu categories">
                <button class="chip active" type="button" data-menu-filter="all">All</button>
                <% for (String category : categories) {
                    String filterValue = category.toLowerCase().replace(' ', '-');
                %>
                    <button class="chip" type="button" data-menu-filter="<%= HtmlUtil.escape(filterValue) %>"><%= HtmlUtil.escape(category) %></button>
                <% } %>
            </div>
        </div>

        <% if (menuItems == null || menuItems.isEmpty()) { %>
            <div class="empty-module-state">
                <strong>No menu items are available.</strong>
                <p>Restaurant staff can publish menu items after inventory and menu setup is complete.</p>
            </div>
        <% } else { %>
            <div class="menu-grid">
                <% for (MenuItemRecord item : menuItems) {
                    String categoryValue = item.getCategory().toLowerCase().replace(' ', '-');
                    String image = item.getImagePath() == null || item.getImagePath().isBlank()
                            ? "dish-signature.svg" : item.getImagePath();
                %>
                    <article class="menu-item <%= item.isAvailable() ? "" : "is-unavailable" %>"
                             data-menu-item data-category="<%= HtmlUtil.escape(categoryValue) %>">
                        <div class="menu-item-media">
                            <img src="<%= ctx %>/assets/images/<%= HtmlUtil.escape(image) %>" alt="<%= HtmlUtil.escape(item.getName()) %>">
                            <% if (!item.isAvailable()) { %><span class="availability-ribbon">Unavailable</span><% } %>
                        </div>
                        <div class="menu-item-content">
                            <div class="menu-meta">
                                <span class="tag orange"><%= HtmlUtil.escape(item.getCategory()) %></span>
                                <span class="tag"><%= HtmlUtil.escape(item.getDietaryType().replace('_', ' ')) %></span>
                                <% if (!"NONE".equals(item.getSpiceLevel())) { %>
                                    <span class="tag"><%= HtmlUtil.escape(item.getSpiceLevel()) %> spice</span>
                                <% } %>
                            </div>
                            <h3><%= HtmlUtil.escape(item.getName()) %></h3>
                            <p><%= HtmlUtil.escape(item.getDescription()) %></p>
                            <div class="menu-item-footer">
                                <span class="price"><%= HtmlUtil.escape(item.getPriceDisplay()) %></span>
                                <% if (item.isAvailable()) { %>
                                    <form method="post" action="<%= ctx %>/orders/cart/add" class="inline-order-form">
                                        <input type="hidden" name="menuItemId" value="<%= item.getId() %>">
                                        <input type="hidden" name="quantity" value="1">
                                        <button class="btn btn-primary btn-sm" type="submit">Add to order</button>
                                    </form>
                                <% } else { %>
                                    <button class="btn btn-secondary btn-sm" type="button" disabled>Unavailable</button>
                                <% } %>
                            </div>
                        </div>
                    </article>
                <% } %>
            </div>
        <% } %>

        <div class="empty-state" data-menu-empty hidden>
            <strong>No dishes match your search.</strong>
            <p>Try another name, ingredient, or category.</p>
        </div>
    </div>
</section>

<section class="section-sm section-soft">
    <div class="container split-panel">
        <div>
            <span class="section-kicker">Flexible ordering</span>
            <h2>Dine in, pre-order for a reservation, or collect takeaway.</h2>
            <p>All cart totals and business rules are validated by the Java service layer before an order is stored.</p>
            <div class="feature-list compact">
                <div><strong>Dine-in</strong><span>Link an order to a confirmed table reservation.</span></div>
                <div><strong>Pre-order</strong><span>Schedule food for the reservation time.</span></div>
                <div><strong>Takeaway</strong><span>Select a collection time at least 30 minutes ahead.</span></div>
            </div>
        </div>
        <aside class="cta-card">
            <span class="section-kicker">Ready to order?</span>
            <h3>Review your cart and complete checkout.</h3>
            <p>Your selected dishes stay in the server-side session cart until checkout or removal.</p>
            <a class="btn btn-primary btn-block" href="<%= ctx %>/orders#order-cart">Open food cart</a>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
