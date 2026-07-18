<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Events and Packages"); request.setAttribute("activeNav", "events"); %>
<%@ include file="fragments/header.jspf" %>
<section class="page-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><span>Events</span></div>
        <span class="eyebrow">DineVista events</span>
        <h1>Celebrate beautifully, from intimate moments to grand occasions.</h1>
        <p>Choose a starting package, preferred venue, guest capacity, and service level. Every inquiry can be refined with our event coordination team.</p>
    </div>
</section>

<section class="section-sm">
    <div class="container">
        <div class="section-heading">
            <div><span class="section-kicker">Event packages</span><h2>A clear starting point for every celebration.</h2></div>
            <p>Packages provide a practical foundation for food, venue, service, decor, and staffing. Final quotations can be adjusted to your event.</p>
        </div>
        <div class="package-grid">
            <article class="package-card">
                <div class="card-media"><img src="<%= ctx %>/assets/images/event-birthday.svg" alt="Birthday celebration package"><span class="card-badge">Social events</span></div>
                <div class="card-body">
                    <span class="eyebrow">Celebration package</span>
                    <h3>Joyful Gatherings</h3>
                    <p>Ideal for birthdays, anniversaries, reunions, and family celebrations.</p>
                    <div class="package-price">LKR 4,500 <small>per guest from</small></div>
                    <ul class="check-list"><li>Choice of buffet or set menu</li><li>Basic venue styling</li><li>Welcome beverage</li><li>Event service staff</li></ul>
                    <a class="btn btn-secondary btn-block" data-package-select="Joyful Gatherings" href="<%= ctx %>/event-booking">Choose package</a>
                </div>
            </article>
            <article class="package-card featured">
                <div class="card-media"><img src="<%= ctx %>/assets/images/event-wedding.svg" alt="Wedding event package"><span class="card-badge">Most requested</span></div>
                <div class="card-body">
                    <span class="eyebrow">Wedding package</span>
                    <h3>Everlasting Elegance</h3>
                    <p>A refined wedding reception package with food, styling, service, and coordination.</p>
                    <div class="package-price">LKR 7,900 <small>per guest from</small></div>
                    <ul class="check-list"><li>Premium menu collection</li><li>Venue and table styling</li><li>Dedicated coordinator</li><li>Bridal table and cake service</li></ul>
                    <a class="btn btn-primary btn-block" data-package-select="Everlasting Elegance" href="<%= ctx %>/event-booking">Choose package</a>
                </div>
            </article>
            <article class="package-card">
                <div class="card-media"><img src="<%= ctx %>/assets/images/event-corporate.svg" alt="Corporate event package"><span class="card-badge">Business events</span></div>
                <div class="card-body">
                    <span class="eyebrow">Corporate package</span>
                    <h3>Professional Impact</h3>
                    <p>Designed for meetings, launches, workshops, staff events, and formal dinners.</p>
                    <div class="package-price">LKR 5,800 <small>per guest from</small></div>
                    <ul class="check-list"><li>Meeting or banquet setup</li><li>Tea, coffee, and meal service</li><li>Audio-visual essentials</li><li>Registration desk support</li></ul>
                    <a class="btn btn-secondary btn-block" data-package-select="Professional Impact" href="<%= ctx %>/event-booking">Choose package</a>
                </div>
            </article>
        </div>
    </div>
</section>

<section class="section">
    <div class="container">
        <div class="section-heading">
            <div><span class="section-kicker">Venue choices</span><h2>Spaces with a distinct atmosphere.</h2></div>
            <p>Select a venue preference during the inquiry. Capacity, layout, availability, and equipment will be verified by the event team.</p>
        </div>
        <div class="venue-grid">
            <article class="venue-card">
                <img src="<%= ctx %>/assets/images/venue-garden.svg" alt="Garden pavilion event venue">
                <div class="venue-card-content"><span class="eyebrow">Outdoor venue</span><h3>Garden Pavilion</h3><p class="muted">A landscaped setting for receptions, celebrations, evening dinners, and ceremony-style arrangements.</p><div class="venue-details"><span>Up to 220 guests</span><span>Weather backup plan</span><span>Custom layouts</span></div></div>
            </article>
            <article class="venue-card">
                <img src="<%= ctx %>/assets/images/event-corporate.svg" alt="Indoor grand hall event venue">
                <div class="venue-card-content"><span class="eyebrow">Indoor venue</span><h3>Vista Grand Hall</h3><p class="muted">A climate-controlled hall with presentation facilities, stage options, flexible dining layouts, and service access.</p><div class="venue-details"><span>Up to 350 guests</span><span>Stage and projection</span><span>Private entrance</span></div></div>
            </article>
        </div>
    </div>
</section>

<section class="section-sm">
    <div class="container cta-banner">
        <div><h2>Tell us what you are planning.</h2><p>Submit an event inquiry and receive a clear reference for follow-up, package review, and availability checking.</p></div>
        <div class="cta-actions"><a class="btn btn-secondary" href="<%= ctx %>/event-booking">Request consultation</a></div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
