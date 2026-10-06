<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<%@ page import="com.dinevista.model.EventQuoteRecord" %>
<%@ page import="com.dinevista.model.InvoiceRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("pageTitle", "Event Booking Review");
    request.setAttribute("activeNav", "staffEventBookings");
    EventBookingRecord b = (EventBookingRecord) request.getAttribute("booking");
    List<EventQuoteRecord> quotes = (List<EventQuoteRecord>) request.getAttribute("quotes");
    InvoiceRecord eventInvoice = (InvoiceRecord) request.getAttribute("eventInvoice");
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero">
    <div class="container">
        <div>
            <div class="breadcrumbs" style="margin-bottom:8px;">
                <a href="<%= ctx %>/staff/event-bookings">Event bookings</a>
                <span>/</span>
                <span><%= b != null ? HtmlUtil.escape(b.getReference()) : "Details" %></span>
            </div>
            <span class="eyebrow">Booking review &amp; confirmation</span>
            <h1><%= b != null ? HtmlUtil.escape(b.getReference()) : "Booking Not Found" %></h1>
            <p><%= b != null ? HtmlUtil.escape(b.getEventType()) + " for " + HtmlUtil.escape(b.getCustomerName()) : "" %></p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-secondary" href="<%= ctx %>/staff/event-bookings">&larr; Back to bookings</a>
            <% if (b != null) { %>
                <a class="btn btn-primary" href="<%= ctx %>/staff/event-bookings/edit?reference=<%= b.getReference() %>">Edit / update status</a>
            <% } %>
        </div>
    </div>
</section>

<div class="container operations-container">
    <% if (b == null) { %>
        <div class="alert alert-error">Event booking record not found.</div>
    <% } else { %>
        <div class="dashboard-grid">
            <section class="panel">
                <div class="panel-header">
                    <h3>Booking information</h3>
                    <%
                        String statusClass = "status-pending";
                        if ("CONFIRMED".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus())) statusClass = "status-confirmed";
                        else if ("CANCELLED".equalsIgnoreCase(b.getStatus())) statusClass = "status-cancelled";
                    %>
                    <span class="status <%= statusClass %>"><%= HtmlUtil.escape(b.getStatus()) %></span>
                </div>

                <div class="detail-grid" style="display:grid;grid-template-columns:repeat(auto-fit, minmax(200px, 1fr));gap:20px;margin-bottom:24px;">
                    <div>
                        <span class="muted small" style="display:block;">Customer</span>
                        <strong><%= HtmlUtil.escape(b.getCustomerName()) %></strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Email</span>
                        <strong><%= HtmlUtil.escape(b.getEmail()) %></strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Phone</span>
                        <strong><%= HtmlUtil.escape(b.getPhone()) %></strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Event Type</span>
                        <strong><%= HtmlUtil.escape(b.getEventType()) %></strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Package</span>
                        <strong><%= HtmlUtil.escape(b.getPackageName()) %></strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Venue</span>
                        <strong><%= HtmlUtil.escape(b.getVenue()) %></strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Event Schedule</span>
                        <strong><%= b.getEventDate() %> at <%= b.getEventTime() %></strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Guest Count</span>
                        <strong><%= b.getGuestCount() %> guests</strong>
                    </div>
                    <div>
                        <span class="muted small" style="display:block;">Estimated Total</span>
                        <strong style="font-size:1.15rem;color:var(--brand-dark, #2b5138);">LKR <%= String.format("%,.2f", b.getTotalAmount()) %></strong>
                    </div>
                </div>

                <hr style="border:0;border-top:1px solid var(--line, #e6decb);margin:20px 0;">

                <div>
                    <h4>Requirements &amp; coordinator notes</h4>
                    <p style="white-space:pre-wrap;margin-top:8px;line-height:1.6;"><%= HtmlUtil.escape(b.getNotes()) %></p>
                </div>
            </section>

            <aside class="panel">
                <div class="panel-header">
                    <h3>Management actions</h3>
                </div>
                <div style="display:flex;flex-direction:column;gap:12px;">
                    <a class="btn btn-primary" href="<%= ctx %>/staff/event-bookings/edit?reference=<%= b.getReference() %>">Change status or details</a>
                    <% if (eventInvoice != null) { %>
                        <a class="btn btn-secondary" href="<%= ctx %>/staff/billing/view?id=<%= eventInvoice.getId() %>">View invoice - <%= HtmlUtil.escape(eventInvoice.getStatusLabel()) %></a>
                        <p class="muted small">Demo payment: <%= eventInvoice.getAmountPaidDisplay() %> of <%= eventInvoice.getTotalAmountDisplay() %> recorded.</p>
                    <% } else if (quotes != null && !quotes.isEmpty() && quotes.get(0).isAccepted()) { %>
                        <a class="btn btn-secondary" href="<%= ctx %>/staff/billing/new?sourceType=EVENT_BOOKING&amp;sourceReference=<%= b.getReference() %>">Prepare final invoice</a>
                    <% } else { %><p class="muted small">The customer must accept the latest quote before the final invoice and demo payment.</p><% } %>
                    <form method="post" action="<%= ctx %>/staff/event-bookings/delete" onsubmit="return confirm('Permanently delete this event booking record?');">
                        <input type="hidden" name="reference" value="<%= b.getReference() %>">
                        <button class="btn btn-ghost" style="width:100%;color:#c53030;" type="submit">Delete record</button>
                    </form>
                </div>
            </aside>
        </div>
        <section class="panel" style="margin-top:18px">
            <div class="panel-header"><h3>Versioned quotations &amp; customer approval</h3></div>
            <% if (quotes == null || quotes.isEmpty()) { %>
            <p class="muted">No quotation issued yet. Set status to QUOTED to capture the first priced requirements version.</p>
            <% } else { EventQuoteRecord latest=quotes.get(0); %>
            <p class="muted">Current quote v<%= latest.getVersion() %> is <%= latest.isAccepted()?"accepted by the customer":"awaiting customer approval" %>. Confirm only after the latest version is accepted.</p>
            <div class="table-wrap"><table class="data-table operations-table"><thead><tr><th>Version</th><th>Guests</th><th>Per guest</th><th>Venue fee</th><th>Total</th><th>Approval</th></tr></thead><tbody>
            <% for (EventQuoteRecord quote : quotes) { %><tr>
                <td>v<%= quote.getVersion() %></td><td><%= quote.getGuestCount() %></td>
                <td>LKR <%= quote.getPricePerGuest() %></td><td>LKR <%= quote.getVenueFee() %></td>
                <td>LKR <%= quote.getTotal() %></td><td><%= quote.isAccepted()?"Accepted":"Awaiting approval" %></td>
            </tr><% } %></tbody></table></div>
            <p><strong>Latest requirements:</strong> <%= HtmlUtil.escape(latest.getRequirements()) %></p>
            <% } %>
        </section>
    <% } %>
</div>

<%@ include file="fragments/footer.jspf" %>
