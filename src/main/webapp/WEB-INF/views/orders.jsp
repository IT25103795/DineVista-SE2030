<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Order Food"); request.setAttribute("activeNav", "orders"); %>
<%@ include file="fragments/header.jspf" %>
<section class="page-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><span>Order Food</span></div>
        <span class="eyebrow">Online food ordering</span>
        <h1>Build your order and enjoy DineVista wherever you are.</h1>
        <p>Add dishes to the cart, adjust quantities, review totals, and complete a polished demo checkout.</p>
    </div>
</section>

<section class="section-sm">
    <div class="container order-layout">
        <div class="order-menu">
            <div class="filter-bar">
                <div class="search-control">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></svg>
                    <input type="search" data-menu-search placeholder="Search order items" aria-label="Search order items">
                </div>
                <div class="chip-row">
                    <button class="chip active" type="button" data-menu-filter="all">All</button>
                    <button class="chip" type="button" data-menu-filter="main">Mains</button>
                    <button class="chip" type="button" data-menu-filter="local">Local</button>
                    <button class="chip" type="button" data-menu-filter="dessert">Desserts</button>
                </div>
            </div>
            <div class="menu-grid">
                <article class="menu-item" data-menu-item data-category="main">
                    <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-signature.svg" alt="Fire-roasted chicken"></div>
                    <div class="menu-item-content"><div class="menu-meta"><span class="tag orange">Main</span></div><h3>Fire-Roasted Chicken</h3><p>Herb-marinated chicken, vegetables, coconut pepper sauce, and greens.</p><div class="menu-item-footer"><span class="price">LKR 2,450</span><button class="btn btn-primary btn-sm add-to-cart" type="button" data-add-cart data-id="chicken" data-name="Fire-Roasted Chicken" data-price="2450">Add</button></div></div>
                </article>
                <article class="menu-item" data-menu-item data-category="local">
                    <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-curry.svg" alt="Island curry collection"></div>
                    <div class="menu-item-content"><div class="menu-meta"><span class="tag orange">Local</span></div><h3>Island Curry Collection</h3><p>Chicken curry, dhal, vegetables, rice, sambols, and papadam.</p><div class="menu-item-footer"><span class="price">LKR 2,150</span><button class="btn btn-primary btn-sm add-to-cart" type="button" data-add-cart data-id="curry" data-name="Island Curry Collection" data-price="2150">Add</button></div></div>
                </article>
                <article class="menu-item" data-menu-item data-category="main">
                    <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-seafood.svg" alt="Lagoon grilled fish"></div>
                    <div class="menu-item-content"><div class="menu-meta"><span class="tag orange">Seafood</span></div><h3>Lagoon Grilled Fish</h3><p>Daily catch, lime butter, herb rice, salad, and chilli coconut relish.</p><div class="menu-item-footer"><span class="price">LKR 2,850</span><button class="btn btn-primary btn-sm add-to-cart" type="button" data-add-cart data-id="fish" data-name="Lagoon Grilled Fish" data-price="2850">Add</button></div></div>
                </article>
                <article class="menu-item" data-menu-item data-category="local">
                    <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-curry.svg" alt="Kottu royale"></div>
                    <div class="menu-item-content"><div class="menu-meta"><span class="tag orange">Popular</span></div><h3>Kottu Royale</h3><p>Chopped roti, vegetables, egg, chicken, curry gravy, and onion sambol.</p><div class="menu-item-footer"><span class="price">LKR 1,950</span><button class="btn btn-primary btn-sm add-to-cart" type="button" data-add-cart data-id="kottu" data-name="Kottu Royale" data-price="1950">Add</button></div></div>
                </article>
                <article class="menu-item" data-menu-item data-category="main">
                    <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-curry.svg" alt="Garden harvest bowl"></div>
                    <div class="menu-item-content"><div class="menu-meta"><span class="tag">Vegetarian</span></div><h3>Garden Harvest Bowl</h3><p>Roasted pumpkin, chickpeas, avocado, red rice, and lime dressing.</p><div class="menu-item-footer"><span class="price">LKR 1,850</span><button class="btn btn-primary btn-sm add-to-cart" type="button" data-add-cart data-id="garden" data-name="Garden Harvest Bowl" data-price="1850">Add</button></div></div>
                </article>
                <article class="menu-item" data-menu-item data-category="dessert">
                    <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-dessert.svg" alt="Ceylon cocoa slice"></div>
                    <div class="menu-item-content"><div class="menu-meta"><span class="tag orange">Dessert</span></div><h3>Ceylon Cocoa Slice</h3><p>Dark chocolate mousse, vanilla cream, and berry compote.</p><div class="menu-item-footer"><span class="price">LKR 1,150</span><button class="btn btn-primary btn-sm add-to-cart" type="button" data-add-cart data-id="dessert" data-name="Ceylon Cocoa Slice" data-price="1150">Add</button></div></div>
                </article>
            </div>
            <div class="empty-state" data-menu-empty><h3>No matching items</h3><p>Try a different search or category.</p></div>
        </div>

        <aside class="cart-panel" aria-label="Food cart">
            <div class="cart-header"><div><span class="eyebrow">Your order</span><h3>Cart summary</h3></div><span class="cart-count" data-cart-count>0</span></div>
            <div class="cart-items" data-cart-items></div>
            <div class="cart-summary">
                <div class="summary-row"><span>Subtotal</span><strong data-cart-subtotal>LKR 0</strong></div>
                <div class="summary-row"><span>Service fee</span><strong data-cart-service>LKR 0</strong></div>
                <div class="summary-row total"><span>Total</span><strong data-cart-total>LKR 0</strong></div>
                <button class="btn btn-primary btn-block" type="button" data-checkout disabled>Continue to checkout</button>
                <p class="form-note">This frontend demo stores the cart in your browser and creates a sample order reference.</p>
            </div>
        </aside>
    </div>
</section>

<div class="modal-backdrop" data-modal role="dialog" aria-modal="true" aria-label="Checkout">
    <div class="modal">
        <div class="modal-header"><div><span class="eyebrow">Checkout</span><h3>Delivery details</h3></div><button class="icon-btn" type="button" data-modal-close aria-label="Close"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 6 12 12M18 6 6 18"/></svg></button></div>
        <form data-checkout-form>
            <div class="form-grid">
                <div class="form-group full"><label for="checkoutName">Full name</label><input class="form-control" id="checkoutName" name="name" required autocomplete="name"></div>
                <div class="form-group"><label for="checkoutPhone">Mobile number</label><input class="form-control" id="checkoutPhone" name="phone" required pattern="(?:\\+94|0)7[0-9]{8}" placeholder="0771234567"></div>
                <div class="form-group"><label for="checkoutMethod">Order method</label><select class="form-control" id="checkoutMethod" required><option value="">Select</option><option>Delivery</option><option>Pickup</option></select></div>
                <div class="form-group full"><label for="checkoutAddress">Delivery or pickup note</label><textarea class="form-control" id="checkoutAddress" required placeholder="Enter the address or preferred pickup time"></textarea></div>
                <div class="form-group full"><label for="checkoutPayment">Payment method</label><select class="form-control" id="checkoutPayment" required><option value="">Select</option><option>Cash on delivery</option><option>Card on delivery</option><option>Online payment demo</option></select></div>
            </div>
            <div class="form-actions"><button class="btn btn-primary btn-block" type="submit">Place demo order</button></div>
        </form>
    </div>
</div>
<%@ include file="fragments/footer.jspf" %>
