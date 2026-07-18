<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.ReservationRecord" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<% request.setAttribute("pageTitle", "Operations Dashboard"); request.setAttribute("activeNav", ""); %>
<%@ include file="fragments/header.jspf" %>
<%
    List<ReservationRecord> managerReservations = (List<ReservationRecord>) session.getAttribute("reservations");
    List<EventBookingRecord> managerEvents = (List<EventBookingRecord>) session.getAttribute("eventBookings");
    int managerReservationCount = managerReservations == null ? 8 : managerReservations.size() + 8;
    int managerEventCount = managerEvents == null ? 3 : managerEvents.size() + 3;
%>
<section class="dashboard-page">
    <div class="container">
        <div class="dashboard-header">
            <div><span class="eyebrow">Operations centre</span><h1>DineVista overview</h1><p>Monitor restaurant activity, event demand, sales performance, and operational priorities.</p></div>
            <div class="hero-actions" style="margin:0"><a class="btn btn-primary" href="<%= ctx %>/reservations">Create reservation</a><a class="btn btn-secondary" href="<%= ctx %>/logout">Sign out</a></div>
        </div>
        <div class="kpi-grid">
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M8 3v4M16 3v4M3 10h18"/></svg></span><span class="trend">+12% today</span></div><strong><%= managerReservationCount %></strong><span>Table reservations</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 4h2l2 10h10l2-7H7"/></svg></span><span class="trend">+8% this week</span></div><strong>42</strong><span>Food orders</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10"/></svg></span><span class="trend">5 pending</span></div><strong><%= managerEventCount %></strong><span>Event inquiries</span></article>
            <article class="kpi-card"><div class="kpi-top"><span class="kpi-icon green"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19V9M10 19V5M16 19v-7M22 19H2"/></svg></span><span class="trend">+16.4%</span></div><strong>184K</strong><span>Revenue today (LKR)</span></article>
        </div>

        <div class="dashboard-grid">
            <section class="panel">
                <div class="panel-header"><div><h3>Revenue performance</h3><span class="muted small">Last seven operating days</span></div><span class="tag orange">Live demo</span></div>
                <div class="chart-wrap"><canvas data-revenue-chart aria-label="Revenue trend chart"></canvas></div>
            </section>
            <aside class="panel">
                <div class="panel-header"><h3>Recent activity</h3></div>
                <div class="activity-list">
                    <div class="activity-item"><span class="activity-dot"></span><span><strong>New garden table request</strong><span>Four guests for 7:30 PM</span></span></div>
                    <div class="activity-item"><span class="activity-dot"></span><span><strong>Order DV-O-A82D confirmed</strong><span>Kitchen preparation started</span></span></div>
                    <div class="activity-item"><span class="activity-dot"></span><span><strong>Wedding consultation received</strong><span>Everlasting Elegance package</span></span></div>
                    <div class="activity-item"><span class="activity-dot"></span><span><strong>Low stock alert</strong><span>Fresh prawns below reorder level</span></span></div>
                </div>
            </aside>
        </div>

        <div class="dashboard-grid" style="margin-top:22px">
            <section class="panel">
                <div class="panel-header"><h3>Today's reservation schedule</h3><a class="btn btn-ghost btn-sm" href="<%= ctx %>/reservations">View form</a></div>
                <div class="table-wrap"><table class="data-table"><thead><tr><th>Time</th><th>Guest</th><th>Party</th><th>Area</th><th>Status</th></tr></thead><tbody>
                    <tr><td>6:30 PM</td><td>N. Perera</td><td>4 guests</td><td>Garden terrace</td><td><span class="status confirmed">Confirmed</span></td></tr>
                    <tr><td>7:00 PM</td><td>S. Fernando</td><td>2 guests</td><td>Chef's counter</td><td><span class="status confirmed">Confirmed</span></td></tr>
                    <tr><td>7:30 PM</td><td>A. Silva</td><td>8 guests</td><td>Private dining</td><td><span class="status pending">Pending</span></td></tr>
                    <tr><td>8:30 PM</td><td>R. Jayasinghe</td><td>5 guests</td><td>Indoor hall</td><td><span class="status processing">Reviewing</span></td></tr>
                </tbody></table></div>
            </section>
            <aside class="panel">
                <div class="panel-header"><h3>Management shortcuts</h3></div>
                <div class="quick-actions">
                    <a class="quick-action" href="#"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 5h16v14H4z"/></svg><span><strong>Menu items</strong><span>Manage dishes</span></span></a>
                    <a class="quick-action" href="#"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19V5h16v14zM8 9h8M8 13h5"/></svg><span><strong>Inventory</strong><span>Review stock</span></span></a>
                    <a class="quick-action" href="#"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h16M6 20V10l6-6 6 6v10"/></svg><span><strong>Event bookings</strong><span>Plan resources</span></span></a>
                    <a class="quick-action" href="#"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-5 3-8 8-8s8 3 8 8"/></svg><span><strong>Staff schedules</strong><span>Assign shifts</span></span></a>
                </div>
            </aside>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
