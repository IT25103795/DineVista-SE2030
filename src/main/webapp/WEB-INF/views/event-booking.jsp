<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<% request.setAttribute("pageTitle", "Event Booking Request"); request.setAttribute("activeNav", "events"); %>
<%@ include file="fragments/header.jspf" %>
<section class="page-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><a href="<%= ctx %>/events">Events</a><span>/</span><span>Booking Request</span></div>
        <span class="eyebrow">Event consultation</span>
        <h1>Share the first details of your event.</h1>
        <p>This request helps the team understand your date, event type, package, venue preference, and guest count before preparing a detailed quotation.</p>
    </div>
</section>

<section class="section-sm">
    <div class="container form-layout">
        <div class="form-card">
            <span class="section-kicker">Event request</span>
            <h2>Start planning with DineVista</h2>
            <p>Event requests must be submitted at least seven days before the preferred date.</p>

            <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= request.getAttribute("successMessage") %></strong></div><% } %>
            <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><div><strong>Please correct the following:</strong><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= error %></li><% } %></ul></div></div><% } %>
            <% EventBookingRecord eventRecord = (EventBookingRecord) request.getAttribute("latestEventBooking"); if (eventRecord != null) { %>
                <div class="confirmation-card"><strong>Consultation requested for <%= eventRecord.getEventType() %>.</strong><p class="muted small"><%= eventRecord.getPackageName() %> | <%= eventRecord.getEventDate() %> | <%= eventRecord.getGuestCount() %> guests | <%= eventRecord.getStatus() %></p><span class="confirmation-ref"><%= eventRecord.getReference() %></span></div>
            <% } %>

            <form method="post" action="<%= ctx %>/event-booking" novalidate>
                <div class="form-grid">
                    <div class="form-group full"><label for="customerName">Contact person</label><input class="form-control" id="customerName" name="customerName" required minlength="2" autocomplete="name" value="<%= request.getAttribute("formCustomerName") == null ? "" : request.getAttribute("formCustomerName") %>" placeholder="Full name"></div>
                    <div class="form-group"><label for="eventEmail">Email address</label><input class="form-control" id="eventEmail" name="email" type="email" required autocomplete="email" value="<%= request.getAttribute("formEmail") == null ? "" : request.getAttribute("formEmail") %>" placeholder="name@example.com"></div>
                    <div class="form-group"><label for="eventPhone">Mobile number</label><input class="form-control" id="eventPhone" name="phone" required pattern="(?:\\+94|0)7[0-9]{8}" value="<%= request.getAttribute("formPhone") == null ? "" : request.getAttribute("formPhone") %>" placeholder="0771234567"></div>
                    <div class="form-group"><label for="eventType">Event type</label><select class="form-control" id="eventType" name="eventType" required data-event-field><option value="">Select event</option><option>Wedding reception</option><option>Corporate event</option><option>Birthday celebration</option><option>Anniversary</option><option>Workshop or seminar</option><option>Other private event</option></select></div>
                    <div class="form-group"><label for="packageName">Event package</label><select class="form-control" id="packageName" name="packageName" required data-event-field><option value="">Select package</option><option>Joyful Gatherings</option><option>Everlasting Elegance</option><option>Professional Impact</option><option>Custom consultation</option></select></div>
                    <div class="form-group"><label for="venue">Venue preference</label><select class="form-control" id="venue" name="venue" required><option value="">Select venue</option><option>Garden Pavilion</option><option>Vista Grand Hall</option><option>Private Dining Suite</option><option>Off-site catering</option></select></div>
                    <div class="form-group"><label for="eventDate">Preferred date</label><input class="form-control" id="eventDate" name="eventDate" type="date" required data-min-event data-event-field value="<%= request.getAttribute("formEventDate") == null ? "" : request.getAttribute("formEventDate") %>"></div>
                    <div class="form-group"><label for="guestCount">Expected guests</label><input class="form-control" id="guestCount" name="guestCount" type="number" min="10" max="500" required data-event-field value="<%= request.getAttribute("formGuestCount") == null ? "50" : request.getAttribute("formGuestCount") %>"></div>
                    <div class="form-group full"><label for="eventNotes">Initial requirements</label><textarea class="form-control" id="eventNotes" name="notes" placeholder="Menu preferences, decor style, stage, audio-visual needs, dietary requirements, or other notes"></textarea></div>
                </div>
                <div class="form-actions"><button class="btn btn-primary" type="submit">Submit event request</button><a class="btn btn-secondary" href="<%= ctx %>/events">Review packages</a></div>
            </form>
        </div>

        <aside class="info-card">
            <span class="section-kicker">Planning summary</span>
            <h3>Your initial selection</h3>
            <div class="cart-summary" data-event-summary></div>
            <ul class="info-list">
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 5h16v14H4zM8 3v4M16 3v4M4 10h16"/></svg><span><strong>Availability check</strong><span>The preferred date and venue remain subject to confirmation.</span></span></li>
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3v18M3 12h18"/></svg><span><strong>Customisation</strong><span>Packages can be adjusted after the initial consultation.</span></span></li>
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 18V6h16v12zM7 9h10M7 13h7"/></svg><span><strong>Reference number</strong><span>A unique reference is generated after successful submission.</span></span></li>
            </ul>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
