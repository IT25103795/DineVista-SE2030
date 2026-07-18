<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Restaurant and Events"); request.setAttribute("activeNav", "home"); %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<section class="hero">
    <div class="container hero-grid">
        <div class="hero-copy">
            <span class="eyebrow">Restaurant and event experiences</span>
            <h1>Great food. Beautiful events. One seamless <span class="highlight">DineVista</span>.</h1>
            <p>Discover thoughtfully prepared dishes, reserve your perfect table, order online, and plan unforgettable events through one polished digital experience.</p>
            <div class="hero-actions">
                <a class="btn btn-primary" href="<%= ctx %>/reservations">
                    Reserve a table
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
                </a>
                <a class="btn btn-secondary" href="<%= ctx %>/events">Explore events</a>
            </div>
            <div class="hero-trust">
                <span class="trust-item"><span class="trust-dot"></span>Fresh daily menus</span>
                <span class="trust-item"><span class="trust-dot"></span>Instant reservation requests</span>
                <span class="trust-item"><span class="trust-dot"></span>Complete event planning</span>
            </div>
        </div>
        <div class="hero-visual">
            <img src="<%= ctx %>/assets/images/hero-dining.svg" alt="Illustration of a premium dining experience">
            <div class="float-card top">
                <span class="float-icon orange">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M7 3v8M11 3v8M7 7h4M9 11v10M16 3v7c0 2 1 3 3 3v8"/></svg>
                </span>
                <span><strong>Chef-crafted menu</strong><span>Seasonal local ingredients</span></span>
            </div>
            <div class="float-card bottom">
                <span class="float-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg>
                </span>
                <span><strong>Easy bookings</strong><span>Restaurant tables and events</span></span>
            </div>
        </div>
    </div>
</section>

<section class="stats-strip" aria-label="DineVista highlights">
    <div class="container stats-grid">
        <div class="stat"><strong data-counter="48" data-suffix="+">48+</strong><span>Signature dishes</span></div>
        <div class="stat"><strong data-counter="120" data-suffix="+">120+</strong><span>Events managed</span></div>
        <div class="stat"><strong data-counter="94" data-suffix="%">94%</strong><span>Guest satisfaction</span></div>
        <div class="stat"><strong data-counter="7" data-suffix=" days">7 days</strong><span>Open every week</span></div>
    </div>
</section>

<section class="section">
    <div class="container">
        <div class="section-heading">
            <div>
                <span class="section-kicker">Featured menu</span>
                <h2>Flavours made to be remembered.</h2>
            </div>
            <div>
                <p>Our menu combines Sri Lankan inspiration with contemporary presentation, offering comfort, colour, and quality in every plate.</p>
                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/menu">View full menu</a>
            </div>
        </div>
        <div class="card-grid">
            <article class="card">
                <div class="card-media">
                    <img src="<%= ctx %>/assets/images/dish-signature.svg" alt="DineVista signature chicken dish">
                    <span class="card-badge">Chef's choice</span>
                </div>
                <div class="card-body">
                    <h3>Fire-Roasted Chicken</h3>
                    <p>Herb-marinated chicken, charred garden vegetables, coconut pepper sauce, and crisp greens.</p>
                    <div class="card-footer"><span class="price">LKR 2,450</span><a class="btn btn-secondary btn-sm" href="<%= ctx %>/orders">Order now</a></div>
                </div>
            </article>
            <article class="card">
                <div class="card-media">
                    <img src="<%= ctx %>/assets/images/dish-curry.svg" alt="Modern Sri Lankan curry dish">
                    <span class="card-badge">Local favourite</span>
                </div>
                <div class="card-body">
                    <h3>Island Curry Collection</h3>
                    <p>A rotating collection of aromatic curries, fragrant rice, sambols, and house-made accompaniments.</p>
                    <div class="card-footer"><span class="price">LKR 2,150</span><a class="btn btn-secondary btn-sm" href="<%= ctx %>/orders">Order now</a></div>
                </div>
            </article>
            <article class="card">
                <div class="card-media">
                    <img src="<%= ctx %>/assets/images/dish-dessert.svg" alt="Chocolate dessert with berry garnish">
                    <span class="card-badge">Sweet finish</span>
                </div>
                <div class="card-body">
                    <h3>Ceylon Cocoa Slice</h3>
                    <p>Dark chocolate mousse, vanilla cream, berry compote, and a delicate cocoa biscuit base.</p>
                    <div class="card-footer"><span class="price">LKR 1,150</span><a class="btn btn-secondary btn-sm" href="<%= ctx %>/orders">Order now</a></div>
                </div>
            </article>
        </div>
    </div>
</section>

<section class="section-sm">
    <div class="container">
        <div class="section-heading">
            <div>
                <span class="section-kicker">Everything in one place</span>
                <h2>Designed for guests and operations teams.</h2>
            </div>
            <p>DineVista connects customer convenience with the management tools needed to run restaurant and event services smoothly.</p>
        </div>
        <div class="feature-grid">
            <article class="feature-card">
                <span class="feature-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 3v8M11 3v8M7 7h4M9 11v10M16 3v7c0 2 1 3 3 3v8"/></svg>
                </span>
                <h3>Menu and ordering</h3>
                <p>Browse categories, discover dishes, build a cart, and submit an order from any device.</p>
            </article>
            <article class="feature-card">
                <span class="feature-icon green">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg>
                </span>
                <h3>Table reservations</h3>
                <p>Select a date, time, party size, and seating preference with a clear confirmation flow.</p>
            </article>
            <article class="feature-card">
                <span class="feature-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10M9 20v-6h6v6"/></svg>
                </span>
                <h3>Event planning</h3>
                <p>Compare packages, venues, guest capacities, catering choices, and service requirements.</p>
            </article>
            <article class="feature-card">
                <span class="feature-icon green">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19V9M10 19V5M16 19v-7M22 19H2"/></svg>
                </span>
                <h3>Operational insights</h3>
                <p>Track reservations, orders, event requests, staffing, inventory, revenue, and daily activity.</p>
            </article>
        </div>
    </div>
</section>

<section class="section">
    <div class="container split-panel">
        <div class="split-copy">
            <span class="section-kicker">Events by DineVista</span>
            <h2>From first idea to final toast.</h2>
            <p>Our event experience is structured around clear packages, flexible venues, thoughtful food, and coordinated service.</p>
            <ul class="check-list">
                <li>Wedding receptions and intimate celebrations</li>
                <li>Corporate meetings, launches, and team events</li>
                <li>Birthday parties and family occasions</li>
                <li>Custom menus, venue styling, and staffing plans</li>
            </ul>
            <a class="btn btn-primary" href="<%= ctx %>/events">Explore event packages</a>
        </div>
        <div class="split-visual">
            <img src="<%= ctx %>/assets/images/event-wedding.svg" alt="Elegant wedding event setup">
        </div>
    </div>
</section>

<section class="section-sm">
    <div class="container testimonial-grid">
        <article class="quote-card">
            <div class="quote-mark">&ldquo;</div>
            <blockquote>The booking experience felt simple, the team understood our event immediately, and every detail was managed with care.</blockquote>
            <div class="person"><span class="avatar">AS</span><span><strong>Amaya Silva</strong><span class="muted small">Wedding reception guest</span></span></div>
        </article>
        <aside class="rating-card">
            <span class="section-kicker">Guest rating</span>
            <div class="rating-number">4.9</div>
            <div class="stars" aria-label="Five star rating">&#9733;&#9733;&#9733;&#9733;&#9733;</div>
            <p class="muted">Based on restaurant dining, online orders, table reservations, and event experiences.</p>
            <a class="btn btn-secondary btn-block" href="<%= ctx %>/reservations">Plan your visit</a>
        </aside>
    </div>
</section>

<section class="section">
    <div class="container cta-banner">
        <div>
            <h2>Your next table or celebration starts here.</h2>
            <p>Choose a quick restaurant reservation or start planning a complete event with the DineVista team.</p>
        </div>
        <div class="cta-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/reservations">Reserve a table</a>
            <a class="btn btn-dark" href="<%= ctx %>/event-booking">Request event consultation</a>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
