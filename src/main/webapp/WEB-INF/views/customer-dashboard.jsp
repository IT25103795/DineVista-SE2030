<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.ReservationRecord" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<% request.setAttribute("pageTitle", "Customer Dashboard"); request.setAttribute("activeNav", ""); %>
<%@ include file="fragments/header.jspf" %>
<%
    List<ReservationRecord> customerReservations = (List<ReservationRecord>) session.getAttribute("reservations");
    List<EventBookingRecord> customerEvents = (List<EventBookingRecord>) session.getAttribute("eventBookings");
    int reservationCount = customerReservations == null ? 0 : customerReservations.size();
    int eventCount = customerEvents == null ? 0 : customerEvents.size();
%>
<section class="dashboard-page">
    <div class="container">
        <div class="dashboard-header">
            <div><span class="eyebrow">Customer portal</span><h1>Hello, <%= session.getAttribute("displayName") == null ? "DineVista Guest" : session.getAttribute("displayName") %>.</h1><p>Review your recent activity and continue planning your next dining experience.</p></div>
            <a class="btn btn-secondary" href="<%= ctx %>/logout">Sign out</a>
        </div>
        <div class="kpi-grid">
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg></span><span class="trend">Session demo</span></div><strong><%= reservationCount %></strong><span>Table reservations</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10"/></svg></span><span class="trend">Session demo</span></div><strong><%= eventCount %></strong><span>Event inquiries</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 4h2l2 10h10l2-7H7M9 20h.01M17 20h.01"/></svg></span><span class="trend">Browser cart</span></div><strong data-cart-count>0</strong><span>Food cart items</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3l2.5 5.1L20 9l-4 4 1 5.6-5-2.6-5 2.6 1-5.6-4-4 5.5-.9z"/></svg></span><span class="trend">Member level</span></div><strong>Silver</strong><span>Loyalty status demo</span></article>
        </div>

        <div class="dashboard-grid">
            <section class="panel">
                <div class="panel-header"><h3>Recent reservations</h3><a class="btn btn-ghost btn-sm" href="<%= ctx %>/reservations">New reservation</a></div>
                <div class="table-wrap">
                    <table class="data-table"><thead><tr><th>Reference</th><th>Date and time</th><th>Guests</th><th>Status</th></tr></thead><tbody>
                    <% if (customerReservations == null || customerReservations.isEmpty()) { %>
                        <tr><td colspan="4">No reservation requests in this session.</td></tr>
                    <% } else { for (ReservationRecord item : customerReservations) { %>
                        <tr><td><strong><%= item.getReference() %></strong></td><td><%= item.getDate() %> at <%= item.getTime() %></td><td><%= item.getPartySize() %></td><td><span class="status pending"><%= item.getStatus() %></span></td></tr>
                    <% }} %>
                    </tbody></table>
                </div>
            </section>
            <aside class="panel">
                <div class="panel-header"><h3>Quick actions</h3></div>
                <div class="quick-actions">
                    <a class="quick-action" href="<%= ctx %>/orders"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 4h2l2 10h10l2-7H7"/></svg><span><strong>Order food</strong><span>Build a fresh cart</span></span></a>
                    <a class="quick-action" href="<%= ctx %>/reservations"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/></svg><span><strong>Book a table</strong><span>Select date and time</span></span></a>
                    <a class="quick-action" href="<%= ctx %>/events"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10"/></svg><span><strong>Explore events</strong><span>Compare packages</span></span></a>
                    <a class="quick-action" href="<%= ctx %>/menu"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 5h16v14H4zM8 9h8M8 13h6"/></svg><span><strong>Browse menu</strong><span>Find new favourites</span></span></a>
                </div>
            </aside>
        </div>

        <section class="panel" style="margin-top:22px">
            <div class="panel-header"><h3>Event inquiries</h3><a class="btn btn-ghost btn-sm" href="<%= ctx %>/event-booking">New inquiry</a></div>
            <div class="table-wrap"><table class="data-table"><thead><tr><th>Reference</th><th>Event</th><th>Package</th><th>Date</th><th>Status</th></tr></thead><tbody>
            <% if (customerEvents == null || customerEvents.isEmpty()) { %><tr><td colspan="5">No event inquiries in this session.</td></tr><% } else { for (EventBookingRecord item : customerEvents) { %>
                <tr><td><strong><%= item.getReference() %></strong></td><td><%= item.getEventType() %></td><td><%= item.getPackageName() %></td><td><%= item.getEventDate() %></td><td><span class="status processing"><%= item.getStatus() %></span></td></tr>
            <% }} %>
            </tbody></table></div>
        </section>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
