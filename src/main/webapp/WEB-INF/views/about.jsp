<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "About DineVista"); request.setAttribute("activeNav", "about"); %>
<%@ include file="fragments/header.jspf" %>
<section class="page-hero about-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><span>About</span></div>
        <span class="eyebrow">About DineVista</span>
        <h1>A connected platform for restaurant service and event operations.</h1>
        <p>DineVista is the SE2030 group project developed to demonstrate software engineering practices through a realistic Java web application.</p>
    </div>
</section>

<section class="section-sm">
    <div class="container split-panel">
        <div class="split-copy"><span class="section-kicker">Project vision</span><h2>Convenience for guests. Clarity for teams.</h2><p>The system is designed to centralise menu management, inventory, reservations, food orders, event packages, bookings, billing, promotions, resources, and staff scheduling.</p><ul class="check-list"><li>Java Servlets, JSP, JDBC, MySQL, Maven, and Tomcat</li><li>MVC and DAO-oriented project structure</li><li>Responsive, accessible, UTF-8-safe user interface</li><li>Six major functions distributed across six students</li></ul></div>
        <div class="split-visual"><img src="<%= ctx %>/assets/images/hero-dining.svg" alt="DineVista platform illustration"></div>
    </div>
</section>

<section class="section">
    <div class="container">
        <div class="section-heading"><div><span class="section-kicker">Team functions</span><h2>Six connected modules.</h2></div><p>Every member owns a major function and must understand the requirements, design, implementation, testing, and documentation for that module.</p></div>
        <div class="feature-grid">
            <article class="feature-card"><span class="feature-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-5 3-8 8-8s8 3 8 8"/></svg></span><h3>User, customer, and staff</h3><p>Profiles, roles, access, account status, and staff information.</p></article>
            <article class="feature-card"><span class="feature-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 5h16v14H4zM8 9h8M8 13h6"/></svg></span><h3>Menu and inventory</h3><p>Categories, dishes, ingredients, stock, and availability.</p></article>
            <article class="feature-card"><span class="feature-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg></span><h3>Reservations and orders</h3><p>Table requests, order carts, order records, and statuses.</p></article>
            <article class="feature-card"><span class="feature-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10"/></svg></span><h3>Event packages and bookings</h3><p>Packages, venues, requirements, and booking workflows.</p></article>
            <article class="feature-card"><span class="feature-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 10h18M7 15h4"/></svg></span><h3>Billing and promotions</h3><p>Invoices, payments, discounts, receipts, and usage tracking.</p></article>
            <article class="feature-card"><span class="feature-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19V8M10 19V4M16 19v-8M22 19H2"/></svg></span><h3>Resources and scheduling</h3><p>Venues, equipment, staff shifts, and event assignments.</p></article>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
