<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.ReservationRecord" %>
<% request.setAttribute("pageTitle", "Table Reservations"); request.setAttribute("activeNav", "reservations"); %>
<%@ include file="fragments/header.jspf" %>
<section class="page-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><span>Reserve a Table</span></div>
        <span class="eyebrow">Table reservations</span>
        <h1>Your table, your time, your DineVista experience.</h1>
        <p>Send a reservation request with your preferred dining area and occasion. The restaurant team can confirm availability during the next backend stage.</p>
    </div>
</section>

<section class="section-sm">
    <div class="container form-layout">
        <div class="form-card">
            <span class="section-kicker">Reservation request</span>
            <h2>Plan your visit</h2>
            <p>Complete the details below. Required fields are validated on both the page and the Java servlet.</p>

            <% if (request.getAttribute("successMessage") != null) { %>
                <div class="alert alert-success"><strong><%= request.getAttribute("successMessage") %></strong></div>
            <% } %>
            <% if (request.getAttribute("errors") != null) { %>
                <div class="alert alert-danger"><div><strong>Please correct the following:</strong><ul>
                    <% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= error %></li><% } %>
                </ul></div></div>
            <% } %>
            <% ReservationRecord latest = (ReservationRecord) request.getAttribute("latestReservation"); if (latest != null) { %>
                <div class="confirmation-card">
                    <strong>Reservation request received for <%= latest.getGuestName() %>.</strong>
                    <p class="muted small">Date: <%= latest.getDate() %> at <%= latest.getTime() %> | Guests: <%= latest.getPartySize() %> | <%= latest.getStatus() %></p>
                    <span class="confirmation-ref"><%= latest.getReference() %></span>
                </div>
            <% } %>

            <form method="post" action="<%= ctx %>/reservations" novalidate>
                <div class="form-grid">
                    <div class="form-group full"><label for="guestName">Guest name</label><input class="form-control" id="guestName" name="guestName" required minlength="2" autocomplete="name" value="<%= request.getAttribute("formGuestName") == null ? "" : request.getAttribute("formGuestName") %>" placeholder="Enter the booking name"></div>
                    <div class="form-group"><label for="email">Email address</label><input class="form-control" id="email" name="email" type="email" required autocomplete="email" value="<%= request.getAttribute("formEmail") == null ? "" : request.getAttribute("formEmail") %>" placeholder="name@example.com"></div>
                    <div class="form-group"><label for="phone">Mobile number</label><input class="form-control" id="phone" name="phone" required pattern="(?:\\+94|0)7[0-9]{8}" value="<%= request.getAttribute("formPhone") == null ? "" : request.getAttribute("formPhone") %>" placeholder="0771234567"><span class="form-note">Use 07XXXXXXXX or +947XXXXXXXX.</span></div>
                    <div class="form-group"><label for="date">Reservation date</label><input class="form-control" id="date" name="date" type="date" required data-min-today data-reservation-field value="<%= request.getAttribute("formDate") == null ? "" : request.getAttribute("formDate") %>"></div>
                    <div class="form-group"><label for="time">Preferred time</label><select class="form-control" id="time" name="time" required data-reservation-field><option value="">Select time</option><option>11:30 AM</option><option>12:30 PM</option><option>1:30 PM</option><option>6:30 PM</option><option>7:30 PM</option><option>8:30 PM</option><option>9:30 PM</option></select></div>
                    <div class="form-group"><label for="partySize">Party size</label><input class="form-control" id="partySize" name="partySize" type="number" min="1" max="20" required data-reservation-field value="<%= request.getAttribute("formPartySize") == null ? "2" : request.getAttribute("formPartySize") %>"></div>
                    <div class="form-group"><label for="seatingArea">Seating area</label><select class="form-control" id="seatingArea" name="seatingArea" required data-reservation-field><option value="">Select area</option><option>Indoor dining hall</option><option>Garden terrace</option><option>Private dining room</option><option>Chef's counter</option></select></div>
                    <div class="form-group full"><label for="occasion">Occasion or special request</label><textarea class="form-control" id="occasion" name="occasion" placeholder="Birthday, anniversary, dietary note, accessibility request, or other details"><%= request.getAttribute("formOccasion") == null ? "" : request.getAttribute("formOccasion") %></textarea></div>
                </div>
                <div class="form-actions"><button class="btn btn-primary" type="submit">Submit reservation request</button><a class="btn btn-secondary" href="<%= ctx %>/menu">View menu first</a></div>
            </form>
        </div>

        <aside class="info-card">
            <span class="section-kicker">Your selection</span>
            <h3>Reservation summary</h3>
            <div class="cart-summary" data-reservation-summary></div>
            <ul class="info-list">
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg><span><strong>Opening hours</strong><span>Daily from 11:00 AM to 11:00 PM</span></span></li>
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21s7-5.2 7-12a7 7 0 1 0-14 0c0 6.8 7 12 7 12z"/><circle cx="12" cy="9" r="2"/></svg><span><strong>Location</strong><span>Malabe, Sri Lanka</span></span></li>
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16v16H4zM8 9h8M8 13h5"/></svg><span><strong>Confirmation</strong><span>Requests remain pending until accepted by restaurant staff.</span></span></li>
            </ul>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
