<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "About DineVista"); request.setAttribute("activeNav", "about"); %>
<%@ include file="fragments/header.jspf" %>

<section class="page-hero about-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><span>About</span></div>
        <span class="eyebrow">The DineVista story</span>
        <h1>A place for everyday meals and unforgettable milestones.</h1>
        <p>DineVista brings warm Sri Lankan hospitality, thoughtfully prepared food, and effortless planning together in one welcoming dining experience.</p>
    </div>
</section>

<section class="stats-strip">
    <div class="container">
        <div class="stats-grid">
            <div class="stat"><strong>Daily</strong><span>Open from 11 AM to 11 PM</span></div>
            <div class="stat"><strong>350</strong><span>Guests in our largest venue</span></div>
            <div class="stat"><strong>3</strong><span>Ways to enjoy DineVista</span></div>
            <div class="stat"><strong>1</strong><span>Connected guest experience</span></div>
        </div>
    </div>
</section>

<section class="section">
    <div class="container split-panel">
        <div class="split-copy">
            <span class="section-kicker">Our promise</span>
            <h2>Good food should feel personal from the first click to the final course.</h2>
            <p>Whether you are stopping by for lunch, ordering a favourite dish, reserving dinner with family, or planning a celebration, DineVista keeps every step clear, comfortable, and cared for.</p>
            <ul class="check-list">
                <li>Fresh flavours inspired by local ingredients and global favourites</li>
                <li>Welcoming service shaped around each guest and occasion</li>
                <li>Flexible dining spaces for intimate and large celebrations</li>
                <li>Simple online reservations, ordering, and event inquiries</li>
            </ul>
            <a class="btn btn-primary" href="<%= ctx %>/menu">Explore our menu</a>
        </div>
        <div class="split-visual"><img src="<%= ctx %>/assets/images/hero-dining.svg" alt="A welcoming table prepared for guests at DineVista"></div>
    </div>
</section>

<section class="section-sm">
    <div class="container">
        <div class="section-heading">
            <div><span class="section-kicker">What guides us</span><h2>Hospitality with purpose.</h2></div>
            <p>Every DineVista experience is designed around flavour, comfort, trust, and the little details that make people want to return.</p>
        </div>
        <div class="feature-grid">
            <article class="feature-card">
                <span class="feature-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M7 3v8M11 3v8M7 7h4M9 11v10M16 3v7c0 2 1 3 3 3v8"/></svg></span>
                <span class="feature-owner">Flavour first</span>
                <h3>Food worth remembering</h3>
                <p>Balanced flavours, thoughtful presentation, and choices for different tastes and dietary preferences.</p>
            </article>
            <article class="feature-card">
                <span class="feature-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 11c0 5.6-7 10-7 10Z"/></svg></span>
                <span class="feature-owner">Genuine care</span>
                <h3>Hospitality that listens</h3>
                <p>Clear communication and attentive service help every guest feel recognised, relaxed, and welcome.</p>
            </article>
            <article class="feature-card">
                <span class="feature-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10M9 20v-6h6v6"/></svg></span>
                <span class="feature-owner">Made for moments</span>
                <h3>Celebrations with character</h3>
                <p>Flexible venues, menus, styling, and service plans turn personal ideas into well-managed occasions.</p>
            </article>
            <article class="feature-card">
                <span class="feature-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 12h16M12 4v16"/><circle cx="12" cy="12" r="9"/></svg></span>
                <span class="feature-owner">Effortless planning</span>
                <h3>Convenience that connects</h3>
                <p>One account brings together table bookings, food orders, event inquiries, updates, and history.</p>
            </article>
        </div>
    </div>
</section>

<section class="section">
    <div class="container split-panel">
        <div class="split-copy">
            <span class="section-kicker">Celebrations by DineVista</span>
            <h2>Your occasion, shaped around your story.</h2>
            <p>From birthdays and anniversaries to wedding receptions and corporate gatherings, our event experience combines a fitting venue, memorable food, and coordinated service.</p>
            <ul class="check-list">
                <li>Garden, grand-hall, private-dining, and off-site options</li>
                <li>Packages for intimate gatherings and events up to 350 guests</li>
                <li>Custom menus, dietary planning, styling, stage, and equipment</li>
                <li>A clear inquiry process from first idea to event day</li>
            </ul>
            <a class="btn btn-primary" href="<%= ctx %>/events">Discover event experiences</a>
        </div>
        <div class="split-visual"><img src="<%= ctx %>/assets/images/event-wedding.svg" alt="An elegant celebration prepared by DineVista"></div>
    </div>
</section>

<section class="section-sm">
    <div class="container cta-banner">
        <div>
            <span class="section-kicker">Visit us in Malabe</span>
            <h2>Come for the food. Stay for the feeling.</h2>
            <p>Join us daily from 11 AM to 11 PM, or plan ahead in just a few clicks.</p>
        </div>
        <div class="cta-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/reservations">Reserve a table</a>
            <a class="btn btn-dark" href="<%= ctx %>/event-booking">Plan an event</a>
        </div>
    </div>
</section>

<%@ include file="fragments/footer.jspf" %>
