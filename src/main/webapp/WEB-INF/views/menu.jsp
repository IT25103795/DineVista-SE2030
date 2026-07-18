<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Restaurant Menu"); request.setAttribute("activeNav", "menu"); %>
<%@ include file="fragments/header.jspf" %>
<section class="page-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><span>Menu</span></div>
        <span class="eyebrow">Restaurant menu</span>
        <h1>Fresh ingredients. Confident flavours. Something for every table.</h1>
        <p>Explore signature plates, Sri Lankan favourites, seafood, vegetarian dishes, desserts, and drinks prepared by the DineVista kitchen.</p>
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
                <button class="chip" type="button" data-menu-filter="signature">Signature</button>
                <button class="chip" type="button" data-menu-filter="local">Sri Lankan</button>
                <button class="chip" type="button" data-menu-filter="seafood">Seafood</button>
                <button class="chip" type="button" data-menu-filter="vegetarian">Vegetarian</button>
                <button class="chip" type="button" data-menu-filter="dessert">Desserts</button>
            </div>
        </div>

        <div class="menu-grid">
            <article class="menu-item" data-menu-item data-category="signature">
                <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-signature.svg" alt="Fire-roasted chicken"></div>
                <div class="menu-item-content">
                    <div class="menu-meta"><span class="tag orange">Signature</span><span class="tag">High protein</span></div>
                    <h3>Fire-Roasted Chicken</h3>
                    <p>Herb-marinated chicken, charred garden vegetables, coconut pepper sauce, and crisp greens.</p>
                    <div class="menu-item-footer"><span class="price">LKR 2,450</span><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order</a></div>
                </div>
            </article>
            <article class="menu-item" data-menu-item data-category="local">
                <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-curry.svg" alt="Island curry collection"></div>
                <div class="menu-item-content">
                    <div class="menu-meta"><span class="tag orange">Sri Lankan</span><span class="tag">Spiced</span></div>
                    <h3>Island Curry Collection</h3>
                    <p>Aromatic chicken curry, dhal, seasonal vegetables, fragrant rice, sambols, and papadam.</p>
                    <div class="menu-item-footer"><span class="price">LKR 2,150</span><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order</a></div>
                </div>
            </article>
            <article class="menu-item" data-menu-item data-category="seafood">
                <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-seafood.svg" alt="Lagoon grilled fish"></div>
                <div class="menu-item-content">
                    <div class="menu-meta"><span class="tag orange">Seafood</span><span class="tag">Gluten aware</span></div>
                    <h3>Lagoon Grilled Fish</h3>
                    <p>Daily catch, roasted lime butter, herb rice, fresh salad, and a light chilli coconut relish.</p>
                    <div class="menu-item-footer"><span class="price">LKR 2,850</span><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order</a></div>
                </div>
            </article>
            <article class="menu-item" data-menu-item data-category="vegetarian">
                <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-curry.svg" alt="Garden harvest bowl"></div>
                <div class="menu-item-content">
                    <div class="menu-meta"><span class="tag">Vegetarian</span><span class="tag">Fresh</span></div>
                    <h3>Garden Harvest Bowl</h3>
                    <p>Roasted pumpkin, green beans, avocado, curried chickpeas, red rice, and sesame lime dressing.</p>
                    <div class="menu-item-footer"><span class="price">LKR 1,850</span><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order</a></div>
                </div>
            </article>
            <article class="menu-item" data-menu-item data-category="signature">
                <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-signature.svg" alt="Pepper beef medallions"></div>
                <div class="menu-item-content">
                    <div class="menu-meta"><span class="tag orange">Signature</span><span class="tag">Chef special</span></div>
                    <h3>Pepper Beef Medallions</h3>
                    <p>Seared beef, potato cream, glazed vegetables, and a rich Ceylon black pepper reduction.</p>
                    <div class="menu-item-footer"><span class="price">LKR 3,450</span><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order</a></div>
                </div>
            </article>
            <article class="menu-item" data-menu-item data-category="local">
                <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-curry.svg" alt="Kottu royale"></div>
                <div class="menu-item-content">
                    <div class="menu-meta"><span class="tag orange">Sri Lankan</span><span class="tag">Popular</span></div>
                    <h3>Kottu Royale</h3>
                    <p>Chopped roti, vegetables, egg, roasted chicken, house curry gravy, and crisp onion sambol.</p>
                    <div class="menu-item-footer"><span class="price">LKR 1,950</span><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order</a></div>
                </div>
            </article>
            <article class="menu-item" data-menu-item data-category="seafood">
                <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-seafood.svg" alt="Prawn coconut linguine"></div>
                <div class="menu-item-content">
                    <div class="menu-meta"><span class="tag orange">Seafood</span><span class="tag">Creamy</span></div>
                    <h3>Prawn Coconut Linguine</h3>
                    <p>Seared lagoon prawns, coconut cream, garlic, curry leaf, chilli, and fresh lime.</p>
                    <div class="menu-item-footer"><span class="price">LKR 2,750</span><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order</a></div>
                </div>
            </article>
            <article class="menu-item" data-menu-item data-category="vegetarian">
                <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-curry.svg" alt="Mushroom coconut risotto"></div>
                <div class="menu-item-content">
                    <div class="menu-meta"><span class="tag">Vegetarian</span><span class="tag">Creamy</span></div>
                    <h3>Mushroom Coconut Risotto</h3>
                    <p>Wild mushrooms, coconut cream, toasted cashew, herbs, and shaved local hard cheese.</p>
                    <div class="menu-item-footer"><span class="price">LKR 2,100</span><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order</a></div>
                </div>
            </article>
            <article class="menu-item" data-menu-item data-category="dessert">
                <div class="menu-item-media"><img src="<%= ctx %>/assets/images/dish-dessert.svg" alt="Ceylon cocoa slice"></div>
                <div class="menu-item-content">
                    <div class="menu-meta"><span class="tag orange">Dessert</span><span class="tag">Chocolate</span></div>
                    <h3>Ceylon Cocoa Slice</h3>
                    <p>Dark chocolate mousse, vanilla cream, berry compote, and a delicate cocoa biscuit base.</p>
                    <div class="menu-item-footer"><span class="price">LKR 1,150</span><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order</a></div>
                </div>
            </article>
        </div>
        <div class="empty-state" data-menu-empty>
            <h3>No dishes found</h3>
            <p>Try another search term or choose a different menu category.</p>
        </div>
    </div>
</section>

<section class="section-sm">
    <div class="container cta-banner">
        <div><h2>Ready to enjoy DineVista at home?</h2><p>Build your order, review the cart, and complete the demo checkout in a few steps.</p></div>
        <div class="cta-actions"><a class="btn btn-secondary" href="<%= ctx %>/orders">Start an order</a></div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
