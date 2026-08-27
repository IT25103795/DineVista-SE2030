<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Restaurant and Events"); request.setAttribute("activeNav", "home"); %>
<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<!-- HERO — CINEMATIC DARK -->
<section class="lp-hero">
    <!-- Decorative noise grain overlay -->
    <div class="lp-hero-grain" aria-hidden="true"></div>
    <!-- Ambient light splashes -->
    <div class="lp-hero-ambient" aria-hidden="true">
        <span class="lp-orb lp-orb-1"></span>
        <span class="lp-orb lp-orb-2"></span>
        <span class="lp-orb lp-orb-3"></span>
    </div>
    <!-- Diagonal accent strip -->
    <div class="lp-hero-strip" aria-hidden="true"></div>

    <div class="container lp-hero-grid">
        <!-- LEFT: copy -->
        <div class="lp-hero-copy">
            <span class="lp-pill-badge">
                <span class="lp-pill-dot"></span>
                Now taking reservations
            </span>

            <div class="lp-hero-ornament" aria-hidden="true">
                <span></span><em>EST. 2024</em><span></span>
            </div>

            <h1>
                Where every meal<br>
                becomes a<br>
                <em class="lp-serif-em">memory.</em>
            </h1>

            <p>Discover chef-crafted dishes, secure your perfect table, order online, and plan extraordinary events — all through one beautifully crafted experience.</p>

            <div class="lp-hero-actions">
                <a class="btn lp-btn-gold" href="<%= ctx %>/reservations">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg>
                    Reserve a Table
                </a>
                <a class="btn lp-btn-ghost-light" href="<%= ctx %>/menu">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M6 3v8M3.5 3v5a2.5 2.5 0 0 0 5 0V3M6 11v10M15 3v18M15 3c3 1 5 4 5 8h-5"/></svg>
                    Explore Menu
                </a>
            </div>

            <div class="lp-hero-metrics">
                <div class="lp-metric">
                    <strong>4.9</strong>
                    <span>Guest rating</span>
                </div>
                <div class="lp-metric-sep"></div>
                <div class="lp-metric">
                    <strong>2,400+</strong>
                    <span>Happy guests</span>
                </div>
                <div class="lp-metric-sep"></div>
                <div class="lp-metric">
                    <strong>120+</strong>
                    <span>Events hosted</span>
                </div>
            </div>
        </div>

        <!-- RIGHT: visual stack -->
        <div class="lp-hero-visual" aria-hidden="true">
            <div class="lp-hero-plate-wrap">
                <div class="lp-hero-plate-ring lp-ring-1"></div>
                <div class="lp-hero-plate-ring lp-ring-2"></div>
                <div class="lp-hero-plate-ring lp-ring-3"></div>
                <div class="lp-hero-plate-center">
                    <img src="<%= ctx %>/assets/images/hero-dining.svg" alt="">
                </div>
            </div>

            <!-- Floating badge: live table -->
            <div class="lp-badge lp-badge-top">
                <span class="lp-badge-dot"></span>
                <div>
                    <strong>Tables available tonight</strong>
                    <span>Garden · Indoor · Terrace</span>
                </div>
            </div>

            <!-- Floating badge: chef special -->
            <div class="lp-badge lp-badge-bottom">
                <span class="lp-badge-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 3v8M11 3v8M7 7h4M9 11v10M16 3v7c0 2 1 3 3 3v8"/></svg>
                </span>
                <div>
                    <strong>Chef's Seasonal Menu</strong>
                    <span>Updated weekly · 48 dishes</span>
                </div>
            </div>

            <!-- Floating star rating badge -->
            <div class="lp-badge lp-badge-stars">
                <span class="lp-star-row">&#9733;&#9733;&#9733;&#9733;&#9733;</span>
                <span class="lp-badge-sub">Rated by guests</span>
            </div>
        </div>
    </div>

    <!-- Scroll hint -->
    <div class="lp-scroll-hint" aria-hidden="true">
        <span class="lp-scroll-line"></span>
        <span class="lp-scroll-text">scroll</span>
    </div>
</section>



<!-- STATS RIBBON -->
<section class="lp-stats-ribbon" aria-label="DineVista at a glance">
    <div class="container">
        <div class="lp-stats-grid">
            <div class="lp-stat"><strong data-counter="48" data-suffix="+">48+</strong><span>Signature dishes</span></div>
            <div class="lp-stat-div" aria-hidden="true"></div>
            <div class="lp-stat"><strong data-counter="120" data-suffix="+">120+</strong><span>Events managed</span></div>
            <div class="lp-stat-div" aria-hidden="true"></div>
            <div class="lp-stat"><strong data-counter="94" data-suffix="%">94%</strong><span>Guest satisfaction</span></div>
            <div class="lp-stat-div" aria-hidden="true"></div>
            <div class="lp-stat"><strong>7 days</strong><span>Open every week</span></div>
        </div>
    </div>
</section>

<!-- FEATURED DISHES -->
<section class="section lp-dishes-section">
    <div class="container">
        <div class="lp-section-header">
            <div>
                <span class="eyebrow">Featured menu</span>
                <h2>Flavours made to<br>be remembered.</h2>
            </div>
            <div class="lp-section-header-right">
                <p>Our menu combines Sri Lankan inspiration with contemporary presentation — comfort, colour, and quality in every plate.</p>
                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/menu">View full menu <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"/></svg></a>
            </div>
        </div>
        <div class="lp-dishes-grid">
            <article class="lp-dish-card">
                <div class="lp-dish-media">
                    <img src="<%= ctx %>/assets/images/dish-signature.svg" alt="Fire-Roasted Chicken">
                    <span class="lp-dish-badge">Chef's choice</span>
                    <div class="lp-dish-overlay"><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order now</a></div>
                </div>
                <div class="lp-dish-body">
                    <div class="lp-dish-meta"><span class="lp-dish-category">Mains</span><span class="lp-dish-price">LKR 2,450</span></div>
                    <h3>Fire-Roasted Chicken</h3>
                    <p>Herb-marinated chicken, charred garden vegetables, coconut pepper sauce, and crisp greens.</p>
                </div>
            </article>
            <article class="lp-dish-card lp-dish-card-featured">
                <div class="lp-dish-media">
                    <img src="<%= ctx %>/assets/images/dish-curry.svg" alt="Island Curry Collection">
                    <span class="lp-dish-badge lp-badge-gold">Local favourite</span>
                    <div class="lp-dish-overlay"><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order now</a></div>
                </div>
                <div class="lp-dish-body">
                    <div class="lp-dish-meta"><span class="lp-dish-category">Sri Lankan</span><span class="lp-dish-price">LKR 2,150</span></div>
                    <h3>Island Curry Collection</h3>
                    <p>Aromatic curries, fragrant rice, sambols, and house-made accompaniments — rotating weekly.</p>
                </div>
            </article>
            <article class="lp-dish-card">
                <div class="lp-dish-media">
                    <img src="<%= ctx %>/assets/images/dish-dessert.svg" alt="Ceylon Cocoa Slice">
                    <span class="lp-dish-badge">Sweet finish</span>
                    <div class="lp-dish-overlay"><a class="btn btn-primary btn-sm" href="<%= ctx %>/orders">Order now</a></div>
                </div>
                <div class="lp-dish-body">
                    <div class="lp-dish-meta"><span class="lp-dish-category">Desserts</span><span class="lp-dish-price">LKR 1,150</span></div>
                    <h3>Ceylon Cocoa Slice</h3>
                    <p>Dark chocolate mousse, vanilla cream, berry compote, and a delicate cocoa biscuit base.</p>
                </div>
            </article>
        </div>
    </div>
</section>

<!-- EXPERIENCE PILLARS -->
<section class="lp-pillars-section">
    <div class="container">
        <div class="lp-pillars-header">
            <span class="eyebrow">Everything in one place</span>
            <h2>Designed for guests &amp; operations teams.</h2>
        </div>
        <div class="lp-pillars-grid">
            <div class="lp-pillar">
                <div class="lp-pillar-icon lp-pillar-icon-orange">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 3v8M3.5 3v5a2.5 2.5 0 0 0 5 0V3M6 11v10M15 3v18M15 3c3 1 5 4 5 8h-5"/></svg>
                </div>
                <div class="lp-pillar-num">01</div>
                <h3>Menu &amp; Ordering</h3>
                <p>Browse categories, discover dishes, build a cart, and submit an order from any device.</p>
                <a class="lp-pillar-link" href="<%= ctx %>/menu">Browse menu <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"/></svg></a>
            </div>
            <div class="lp-pillar">
                <div class="lp-pillar-icon lp-pillar-icon-green">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg>
                </div>
                <div class="lp-pillar-num">02</div>
                <h3>Table Reservations</h3>
                <p>Select a date, time, party size, and seating preference with instant request confirmation.</p>
                <a class="lp-pillar-link" href="<%= ctx %>/reservations">Book a table <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"/></svg></a>
            </div>
            <div class="lp-pillar">
                <div class="lp-pillar-icon lp-pillar-icon-gold">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10M9 20v-6h6v6"/></svg>
                </div>
                <div class="lp-pillar-num">03</div>
                <h3>Event Planning</h3>
                <p>Compare packages, venues, guest capacities, catering choices, and service requirements.</p>
                <a class="lp-pillar-link" href="<%= ctx %>/events">Explore events <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"/></svg></a>
            </div>
            <div class="lp-pillar">
                <div class="lp-pillar-icon lp-pillar-icon-teal">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19V9M10 19V5M16 19v-7M22 19H2"/></svg>
                </div>
                <div class="lp-pillar-num">04</div>
                <h3>Operational Insights</h3>
                <p>Track reservations, orders, event requests, staffing, inventory, and daily revenue.</p>
                <a class="lp-pillar-link" href="<%= ctx %>/dashboard">View dashboard <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"/></svg></a>
            </div>
        </div>
    </div>
</section>

<!-- EVENTS SHOWCASE -->
<section class="section lp-events-section">
    <div class="container">
        <div class="lp-events-card">
            <div class="lp-events-copy">
                <span class="eyebrow lp-eyebrow-light">Events by DineVista</span>
                <h2>From first idea<br>to final toast.</h2>
                <p>Structured around clear packages, flexible venues, thoughtful food, and coordinated service — every celebration feels effortless.</p>
                <ul class="lp-event-types">
                    <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>Wedding receptions and intimate celebrations</li>
                    <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>Corporate meetings, launches, and team events</li>
                    <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M20 12V22H4V12"/><path d="M22 7H2v5h20V7z"/><path d="M12 22V7"/></svg>Birthday parties and family occasions</li>
                    <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M4 20h16M6 20V10l6-6 6 6v10"/></svg>Custom menus, venue styling, and staffing plans</li>
                </ul>
                <div class="lp-events-actions">
                    <a class="btn btn-primary" href="<%= ctx %>/event-booking">Request Consultation</a>
                    <a class="btn lp-btn-outline-white" href="<%= ctx %>/events">View Packages</a>
                </div>
            </div>
            <div class="lp-events-visual">
                <img src="<%= ctx %>/assets/images/event-wedding.svg" alt="Elegant wedding event setup at DineVista">
                <div class="lp-events-visual-tag" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
                    <div><strong>120+ events</strong><span>beautifully executed</span></div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- SOCIAL PROOF -->
<section class="section-sm lp-proof-section">
    <div class="container">
        <div class="lp-proof-grid">
            <article class="lp-quote-card">
                <div class="lp-quote-icon" aria-hidden="true">&ldquo;</div>
                <blockquote>"The booking experience felt simple, the team understood our event immediately, and every detail was managed with care. Truly unforgettable."</blockquote>
                <footer class="lp-quote-person">
                    <span class="lp-quote-avatar">AS</span>
                    <span><strong>Amaya Silva</strong><small>Wedding reception, June 2026</small></span>
                </footer>
            </article>
            <aside class="lp-rating-card">
                <p class="lp-rating-label">Guest rating</p>
                <div class="lp-rating-number">4.9</div>
                <div class="lp-rating-stars" aria-label="Five star rating">&#9733;&#9733;&#9733;&#9733;&#9733;</div>
                <p class="lp-rating-caption">Based on dining, online orders, table reservations, and event experiences.</p>
                <a class="btn btn-primary btn-block" href="<%= ctx %>/reservations">Plan your visit <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"/></svg></a>
            </aside>
            <article class="lp-mini-quote">
                <div class="lp-mini-stars" aria-hidden="true">&#9733;&#9733;&#9733;&#9733;&#9733;</div>
                <p>"Outstanding curry selection — best I've had in Malabe. The staff made us feel genuinely welcome."</p>
                <footer><strong>Roshan P.</strong> <small>· Regular diner</small></footer>
            </article>
        </div>
    </div>
</section>

<!-- CLOSING CTA -->
<section class="section lp-cta-section">
    <div class="container">
        <div class="lp-cta-card">
            <div class="lp-cta-orbs" aria-hidden="true">
                <span class="lp-cta-orb lp-cta-orb-1"></span>
                <span class="lp-cta-orb lp-cta-orb-2"></span>
            </div>
            <div class="lp-cta-copy">
                <h2>Your next table or celebration starts here.</h2>
                <p>A quick restaurant reservation or a complete event — DineVista handles every detail with care.</p>
            </div>
            <div class="lp-cta-actions">
                <a class="btn lp-btn-cta-white" href="<%= ctx %>/reservations">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg>
                    Reserve a Table
                </a>
                <a class="btn lp-btn-cta-outline" href="<%= ctx %>/event-booking">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M4 20h16M6 20V10l6-6 6 6v10M9 20v-6h6v6"/></svg>
                    Request Event Consultation
                </a>
            </div>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

