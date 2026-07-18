<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="com.dinevista.model.MenuItemRecord" %>
<%@ page import="com.dinevista.model.CartLineRecord" %>
<%@ page import="com.dinevista.model.FoodOrderRecord" %>
<%@ page import="com.dinevista.model.TableReservationRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Food Orders");
    request.setAttribute("activeNav", "orders");
%>
<%@ include file="fragments/header.jspf" %>
<%
    List<MenuItemRecord> menuItems = (List<MenuItemRecord>) request.getAttribute("menuItems");
    List<CartLineRecord> cartLines = (List<CartLineRecord>) request.getAttribute("cartLines");
    List<FoodOrderRecord> customerOrders = (List<FoodOrderRecord>) request.getAttribute("customerOrders");
    List<TableReservationRecord> eligibleReservations = (List<TableReservationRecord>) request.getAttribute("eligibleReservations");
    BigDecimal cartSubtotal = (BigDecimal) request.getAttribute("cartSubtotal");
    BigDecimal cartService = (BigDecimal) request.getAttribute("cartServiceCharge");

    String checkoutName = request.getAttribute("checkoutName") == null
            ? (session.getAttribute("displayName") == null ? "" : session.getAttribute("displayName").toString())
            : request.getAttribute("checkoutName").toString();
    String checkoutEmail = request.getAttribute("checkoutEmail") == null
            ? (session.getAttribute("demoEmail") == null ? "" : session.getAttribute("demoEmail").toString())
            : request.getAttribute("checkoutEmail").toString();
    String checkoutPhone = request.getAttribute("checkoutPhone") == null ? "" : request.getAttribute("checkoutPhone").toString();
    String checkoutType = request.getAttribute("checkoutOrderType") == null ? "TAKEAWAY" : request.getAttribute("checkoutOrderType").toString();
    String checkoutReservation = request.getAttribute("checkoutReservation") == null ? "" : request.getAttribute("checkoutReservation").toString();
    String checkoutRequestedFor = request.getAttribute("checkoutRequestedFor") == null ? "" : request.getAttribute("checkoutRequestedFor").toString();
    String checkoutNotes = request.getAttribute("checkoutNotes") == null ? "" : request.getAttribute("checkoutNotes").toString();
%>
<section class="page-hero order-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><span>Food Orders</span></div>
        <span class="eyebrow">Table-linked and takeaway ordering</span>
        <h1>Build, submit, and track every DineVista food order.</h1>
        <p>Create dine-in, takeaway, or pre-order food orders. Restaurant staff can confirm them, send them to the kitchen queue, and update preparation status.</p>
        <div class="hero-actions">
            <a class="btn btn-primary" href="#food-menu">Browse dishes</a>
            <a class="btn btn-secondary" href="#my-orders">Track my orders</a>
        </div>
    </div>
</section>

<section class="section-sm" id="food-menu">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div>
        <% } %>
        <% if (request.getAttribute("errors") != null) { %>
            <div class="alert alert-danger"><div><strong>Please correct the following:</strong><ul>
                <% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %>
            </ul></div></div>
        <% } %>

        <div class="module-heading">
            <div>
                <span class="section-kicker">Available menu</span>
                <h2>Choose your dishes.</h2>
                <p>Only available items can be added. Each item quantity is validated from 1 to 10.</p>
            </div>
            <span class="module-badge">Server-side cart</span>
        </div>

        <div class="filter-bar">
            <div class="search-control">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg>
                <input type="search" data-menu-search placeholder="Search dishes, categories, or dietary types" aria-label="Search order menu">
            </div>
            <div class="chip-row">
                <button class="chip active" type="button" data-menu-filter="all">All</button>
                <button class="chip" type="button" data-menu-filter="signature">Signature</button>
                <button class="chip" type="button" data-menu-filter="sri-lankan">Sri Lankan</button>
                <button class="chip" type="button" data-menu-filter="seafood">Seafood</button>
                <button class="chip" type="button" data-menu-filter="vegetarian">Vegetarian</button>
                <button class="chip" type="button" data-menu-filter="desserts">Desserts</button>
            </div>
        </div>

        <div class="menu-grid order-menu-grid">
            <% for (MenuItemRecord item : menuItems) {
                String categoryKey = item.getCategory().toLowerCase().replace(" ", "-");
            %>
                <article class="menu-item <%= item.isAvailable() ? "" : "sold-out" %>"
                         data-menu-item data-category="<%= HtmlUtil.escape(categoryKey) %>">
                    <div class="menu-item-media">
                        <img src="<%= ctx %>/assets/images/<%= HtmlUtil.escape(item.getImagePath()) %>" alt="<%= HtmlUtil.escape(item.getName()) %>">
                        <% if (!item.isAvailable()) { %><span class="sold-out-badge">Sold out</span><% } %>
                    </div>
                    <div class="menu-item-content">
                        <div class="menu-meta">
                            <span class="tag orange"><%= HtmlUtil.escape(item.getCategory()) %></span>
                            <span class="tag"><%= HtmlUtil.escape(item.getDietaryType().replace('_', ' ')) %></span>
                        </div>
                        <h3><%= HtmlUtil.escape(item.getName()) %></h3>
                        <p><%= HtmlUtil.escape(item.getDescription()) %></p>
                        <div class="menu-item-footer">
                            <span class="price"><%= item.getPriceDisplay() %></span>
                            <% if (item.isAvailable()) { %>
                                <form class="inline-add-form" method="post" action="<%= ctx %>/orders/cart/add">
                                    <input type="hidden" name="menuItemId" value="<%= item.getId() %>">
                                    <input class="mini-qty" type="number" name="quantity" min="1" max="10" value="1" aria-label="Quantity">
                                    <button class="btn btn-primary btn-sm" type="submit">Add</button>
                                </form>
                            <% } else { %>
                                <button class="btn btn-secondary btn-sm" type="button" disabled>Unavailable</button>
                            <% } %>
                        </div>
                    </div>
                </article>
            <% } %>
        </div>
        <div class="empty-state" data-menu-empty>No menu items match your search.</div>
    </div>
</section>

<section class="section-sm section-soft" id="order-cart">
    <div class="container order-workspace">
        <section class="cart-panel cart-panel-static">
            <div class="cart-header">
                <div><span class="section-kicker">Current cart</span><h2>Your food order.</h2></div>
                <span class="cart-total-items"><%= cartLines == null ? 0 : cartLines.stream().mapToInt(CartLineRecord::getQuantity).sum() %> item(s)</span>
            </div>

            <% if (cartLines == null || cartLines.isEmpty()) { %>
                <div class="empty-module-state compact">
                    <strong>Your cart is waiting for something delicious.</strong>
                    <p>Add a dish from the menu to begin.</p>
                </div>
            <% } else { %>
                <div class="server-cart-list">
                    <% for (CartLineRecord line : cartLines) { %>
                        <article class="server-cart-item">
                            <div class="cart-item-image"><img src="<%= ctx %>/assets/images/<%= HtmlUtil.escape(line.getMenuItem().getImagePath()) %>" alt=""></div>
                            <div class="cart-item-main">
                                <strong><%= HtmlUtil.escape(line.getMenuItem().getName()) %></strong>
                                <span><%= line.getMenuItem().getPriceDisplay() %> each</span>
                            </div>
                            <form class="cart-quantity-form" method="post" action="<%= ctx %>/orders/cart/update">
                                <input type="hidden" name="menuItemId" value="<%= line.getMenuItem().getId() %>">
                                <input class="form-control mini-qty" name="quantity" type="number" min="0" max="10" value="<%= line.getQuantity() %>" aria-label="Quantity">
                                <button class="btn btn-secondary btn-sm" type="submit">Update</button>
                            </form>
                            <strong class="cart-line-total"><%= line.getLineTotalDisplay() %></strong>
                            <form method="post" action="<%= ctx %>/orders/cart/remove">
                                <input type="hidden" name="menuItemId" value="<%= line.getMenuItem().getId() %>">
                                <button class="icon-btn danger-icon" type="submit" aria-label="Remove item">×</button>
                            </form>
                        </article>
                    <% } %>
                </div>
                <div class="cart-summary server-cart-summary">
                    <div class="summary-row"><span>Subtotal</span><strong>LKR <%= String.format("%,.0f", cartSubtotal) %></strong></div>
                    <div class="summary-row"><span>Dine-in service charge (5%)</span><strong>LKR <%= String.format("%,.0f", cartService) %></strong></div>
                    <div class="summary-row total"><span>Takeaway total</span><strong>LKR <%= String.format("%,.0f", cartSubtotal) %></strong></div>
                </div>
            <% } %>
        </section>

        <section class="form-card checkout-card">
            <span class="section-kicker">Confirm food order</span>
            <h2>Order details and fulfilment.</h2>
            <p>Dine-in and pre-order food must be linked to a confirmed reservation. Takeaway collection must be at least 30 minutes ahead.</p>

            <form method="post" action="<%= ctx %>/orders/checkout" novalidate data-order-checkout>
                <div class="form-grid">
                    <div class="form-group full">
                        <label>Order type</label>
                        <div class="choice-grid three">
                            <label class="choice-card"><input type="radio" name="orderType" value="DINE_IN" <%= "DINE_IN".equals(checkoutType) ? "checked" : "" %>><span><strong>Dine-in</strong><small>Linked to a confirmed table</small></span></label>
                            <label class="choice-card"><input type="radio" name="orderType" value="TAKEAWAY" <%= "TAKEAWAY".equals(checkoutType) ? "checked" : "" %>><span><strong>Takeaway</strong><small>Collect from the restaurant</small></span></label>
                            <label class="choice-card"><input type="radio" name="orderType" value="PRE_ORDER" <%= "PRE_ORDER".equals(checkoutType) ? "checked" : "" %>><span><strong>Pre-order</strong><small>Prepare for your reservation</small></span></label>
                        </div>
                    </div>
                    <div class="form-group full" data-reservation-order-field>
                        <label for="reservationReference">Confirmed reservation</label>
                        <select class="form-control" id="reservationReference" name="reservationReference">
                            <option value="">Select reservation</option>
                            <% if (eligibleReservations != null) { for (TableReservationRecord reservation : eligibleReservations) { %>
                                <option value="<%= HtmlUtil.escape(reservation.getReference()) %>" <%= HtmlUtil.selected(reservation.getReference(), checkoutReservation) %>>
                                    <%= HtmlUtil.escape(reservation.getReference()) %> — <%= reservation.getDateDisplay() %> at <%= reservation.getTimeDisplay() %> — Table <%= HtmlUtil.escape(reservation.getTableCode()) %>
                                </option>
                            <% }} %>
                        </select>
                        <% if (eligibleReservations == null || eligibleReservations.isEmpty()) { %>
                            <span class="form-note">No confirmed reservation is currently available. Create a table reservation first or choose takeaway.</span>
                        <% } %>
                    </div>
                    <div class="form-group full" data-pickup-order-field>
                        <label for="requestedFor">Takeaway collection time</label>
                        <input class="form-control" id="requestedFor" name="requestedFor" type="datetime-local" value="<%= HtmlUtil.escape(checkoutRequestedFor) %>">
                    </div>
                    <div class="form-group full"><label for="customerName">Customer name</label><input class="form-control" id="customerName" name="customerName" required minlength="2" value="<%= HtmlUtil.escape(checkoutName) %>"></div>
                    <div class="form-group"><label for="orderEmail">Email address</label><input class="form-control" id="orderEmail" name="email" type="email" required value="<%= HtmlUtil.escape(checkoutEmail) %>"></div>
                    <div class="form-group"><label for="orderPhone">Mobile number</label><input class="form-control" id="orderPhone" name="phone" required pattern="(?:\+94|0)7[0-9]{8}" value="<%= HtmlUtil.escape(checkoutPhone) %>" placeholder="0771234567"></div>
                    <div class="form-group full"><label for="orderNotes">Order notes</label><textarea class="form-control" id="orderNotes" name="orderNotes" maxlength="500" placeholder="Allergies, spice preference, serving notes, or collection instructions"><%= HtmlUtil.escape(checkoutNotes) %></textarea></div>
                </div>
                <div class="form-actions">
                    <button class="btn btn-primary btn-block" type="submit" <%= cartLines == null || cartLines.isEmpty() ? "disabled" : "" %>>Submit food order</button>
                </div>
            </form>
        </section>
    </div>
</section>

<section class="section-sm" id="my-orders">
    <div class="container">
        <div class="module-heading">
            <div><span class="section-kicker">Order history</span><h2>Track current and previous food orders.</h2><p>Follow confirmation, kitchen preparation, ready, served, completed, rejected, or cancellation status.</p></div>
        </div>
        <% if (customerOrders == null || customerOrders.isEmpty()) { %>
            <div class="empty-module-state"><strong>No food orders yet.</strong><p>Your submitted orders will appear here.</p><a class="btn btn-primary btn-sm" href="#food-menu">Start an order</a></div>
        <% } else { %>
            <div class="record-grid">
                <% for (FoodOrderRecord order : customerOrders) { %>
                    <article class="record-card">
                        <div class="record-card-top">
                            <div><span class="record-reference"><%= HtmlUtil.escape(order.getReference()) %></span><h3><%= HtmlUtil.escape(order.getOrderTypeDisplay()) %></h3></div>
                            <span class="status <%= order.getStatusCss() %>"><%= HtmlUtil.escape(order.getStatus()) %></span>
                        </div>
                        <div class="record-facts">
                            <span><strong><%= order.getTotalQuantity() %></strong>Items</span>
                            <span><strong><%= order.getTotalAmountDisplay() %></strong>Total</span>
                            <span><strong><%= HtmlUtil.escape(order.getRequestedForDisplay()) %></strong>Requested for</span>
                        </div>
                        <p class="muted small"><%= order.getReservationReference().isEmpty() ? "No linked reservation" : "Reservation " + HtmlUtil.escape(order.getReservationReference()) %></p>
                        <div class="record-actions"><a class="btn btn-secondary btn-sm" href="<%= ctx %>/orders/view?reference=<%= order.getReference() %>">View order</a></div>
                    </article>
                <% } %>
            </div>
        <% } %>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
