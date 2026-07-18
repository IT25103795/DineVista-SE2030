<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.TableReservationRecord" %>
<%@ page import="com.dinevista.model.StatusHistoryRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Reservation Details");
    request.setAttribute("activeNav", "reservations");
    TableReservationRecord reservation = (TableReservationRecord) request.getAttribute("reservation");
%>
<%@ include file="fragments/header.jspf" %>
<section class="page-hero compact-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/reservations">Reservations</a><span>/</span><span><%= HtmlUtil.escape(reservation.getReference()) %></span></div>
        <span class="eyebrow">Reservation record</span>
        <h1><%= HtmlUtil.escape(reservation.getReference()) %></h1>
        <p>Review reservation details, table allocation, business status, and the full activity timeline.</p>
    </div>
</section>

<section class="section-sm">
    <div class="container detail-layout">
        <div class="detail-main">
            <% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div><% } %>
            <% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger"><div><strong>Unable to complete the action:</strong><ul><% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %></ul></div></div><% } %>

            <article class="detail-card">
                <div class="detail-card-header">
                    <div><span class="record-reference">Table reservation</span><h2><%= HtmlUtil.escape(reservation.getGuestName()) %></h2></div>
                    <span class="status <%= reservation.getStatusCss() %>"><%= HtmlUtil.escape(reservation.getStatus().replace('_', ' ')) %></span>
                </div>
                <div class="detail-fact-grid">
                    <div><span>Date</span><strong><%= reservation.getDateDisplay() %></strong></div>
                    <div><span>Time</span><strong><%= reservation.getTimeDisplay() %></strong></div>
                    <div><span>Party size</span><strong><%= reservation.getPartySize() %> guests</strong></div>
                    <div><span>Table</span><strong><%= reservation.getTableCode() == null ? "Awaiting assignment" : HtmlUtil.escape(reservation.getTableCode()) %></strong></div>
                    <div><span>Seating preference</span><strong><%= HtmlUtil.escape(reservation.getSeatingPreferenceDisplay()) %></strong></div>
                    <div><span>Contact</span><strong><%= HtmlUtil.escape(reservation.getPhone()) %></strong></div>
                </div>
                <% if (!reservation.getOccasionNotes().isEmpty()) { %>
                    <div class="note-box"><span>Occasion or special request</span><p><%= HtmlUtil.escape(reservation.getOccasionNotes()) %></p></div>
                <% } %>
                <% if (!reservation.getStaffNote().isEmpty()) { %>
                    <div class="note-box staff"><span>Latest staff note</span><p><%= HtmlUtil.escape(reservation.getStaffNote()) %></p></div>
                <% } %>
                <div class="record-actions">
                    <% if ("PENDING".equals(reservation.getStatus())) { %>
                        <a class="btn btn-secondary" href="<%= ctx %>/reservations/edit?reference=<%= reservation.getReference() %>">Edit pending reservation</a>
                    <% } %>
                    <% if ("PENDING".equals(reservation.getStatus()) || "CONFIRMED".equals(reservation.getStatus())) { %>
                        <button class="btn btn-danger" type="button" data-dialog-open="cancel-reservation-dialog">Cancel reservation</button>
                    <% } %>
                    <a class="btn btn-ghost" href="<%= ctx %>/orders">Create linked food order</a>
                </div>
            </article>

            <article class="detail-card">
                <div class="panel-header"><div><h3>Reservation timeline</h3><span class="muted small">Newest activity appears first.</span></div></div>
                <div class="timeline">
                    <% for (StatusHistoryRecord item : reservation.getHistory()) { %>
                        <div class="timeline-item">
                            <span class="timeline-dot"></span>
                            <div>
                                <div class="timeline-top"><strong><%= HtmlUtil.escape(item.getStatus().replace('_', ' ')) %></strong><span><%= item.getChangedAtDisplay() %></span></div>
                                <p><%= HtmlUtil.escape(item.getNote()) %></p>
                                <small>By <%= HtmlUtil.escape(item.getChangedBy()) %></small>
                            </div>
                        </div>
                    <% } %>
                </div>
            </article>
        </div>

        <aside class="detail-sidebar">
            <article class="panel">
                <span class="section-kicker">Contact information</span>
                <div class="contact-stack">
                    <div><span>Email</span><strong><%= HtmlUtil.escape(reservation.getEmail()) %></strong></div>
                    <div><span>Phone</span><strong><%= HtmlUtil.escape(reservation.getPhone()) %></strong></div>
                    <div><span>Reference</span><strong class="mono"><%= HtmlUtil.escape(reservation.getReference()) %></strong></div>
                </div>
            </article>
            <article class="panel policy-panel">
                <h3>Cancellation policy</h3>
                <p>Customers may cancel pending or confirmed reservations at least two hours before the reserved time.</p>
            </article>
        </aside>
    </div>
</section>

<div class="dialog-backdrop" id="cancel-reservation-dialog" data-dialog>
    <div class="dialog-card" role="dialog" aria-modal="true" aria-labelledby="cancel-reservation-title">
        <button class="icon-btn dialog-close" type="button" data-dialog-close aria-label="Close">×</button>
        <span class="section-kicker">Cancel reservation</span>
        <h3 id="cancel-reservation-title">Tell us why you are cancelling.</h3>
        <p class="muted">The reason is stored in the reservation history.</p>
        <form method="post" action="<%= ctx %>/reservations/cancel">
            <input type="hidden" name="reference" value="<%= HtmlUtil.escape(reservation.getReference()) %>">
            <div class="form-group"><label for="cancelReason">Cancellation reason</label><textarea class="form-control" id="cancelReason" name="reason" minlength="5" required placeholder="Enter a short reason"></textarea></div>
            <div class="form-actions"><button class="btn btn-danger" type="submit">Confirm cancellation</button><button class="btn btn-secondary" type="button" data-dialog-close>Keep reservation</button></div>
        </form>
    </div>
</div>
<%@ include file="fragments/footer.jspf" %>
