<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<%@ page import="com.dinevista.model.EventQuoteRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<% request.setAttribute("pageTitle","Event Booking Details"); request.setAttribute("activeNav","events"); EventBookingRecord b=(EventBookingRecord)request.getAttribute("booking"); List<EventQuoteRecord> quotes=(List<EventQuoteRecord>)request.getAttribute("quotes"); %>
<%@ include file="fragments/header.jspf" %>
<section class="section-sm"><div class="container">
<div class="section-heading"><div><span class="section-kicker">Booking reference</span><h1><%=b.getReference()%></h1></div><span class="status-badge"><%=b.getStatus()%></span></div>
<div class="detail-card"><div class="detail-grid">
<div><span>Package</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getPackageName())%></strong></div><div><span>Venue</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getVenue())%></strong></div>
<div><span>Date</span><strong><%=b.getEventDate()%></strong></div><div><span>Start time</span><strong><%=b.getEventTime()%></strong></div>
<div><span>Guests</span><strong><%=b.getGuestCount()%></strong></div><div><span>Estimated total</span><strong>LKR <%=b.getTotalAmount()%></strong></div>
<div><span>Contact</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getCustomerName())%></strong></div><div><span>Phone</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getPhone())%></strong></div>
</div><div style="margin-top:24px"><span class="section-kicker">Requirements</span><p><%=com.dinevista.util.HtmlUtil.escape(b.getNotes()).replace("\n","<br>")%></p></div></div>
<% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger" style="margin-top:16px"><% for (String error : (List<String>)request.getAttribute("errors")) { %><p><%= HtmlUtil.escape(error) %></p><% } %></div><% } %>
<% if (quotes != null && !quotes.isEmpty()) { EventQuoteRecord latest=quotes.get(0); %>
<article class="detail-card" style="margin-top:18px">
    <h2>Quotation versions</h2>
    <p class="muted">Only the latest version can be approved. Earlier versions stay visible as an audit trail.</p>
    <div class="table-wrap"><table class="data-table operations-table"><thead><tr><th>Version</th><th>Guests</th><th>Per guest</th><th>Venue</th><th>Total</th><th>Approval</th></tr></thead><tbody>
    <% for (EventQuoteRecord quote : quotes) { %><tr>
        <td>v<%= quote.getVersion() %><%= quote.getId()==latest.getId()?" · Current":"" %></td>
        <td><%= quote.getGuestCount() %></td><td>LKR <%= quote.getPricePerGuest() %></td>
        <td>LKR <%= quote.getVenueFee() %></td><td>LKR <%= quote.getTotal() %></td>
        <td><%= quote.isAccepted()?"Accepted":"Awaiting approval" %></td>
    </tr><% } %></tbody></table></div>
    <p><strong>Current requirements:</strong> <%= HtmlUtil.escape(latest.getRequirements()) %></p>
    <% if ("QUOTED".equals(b.getStatus()) && !latest.isAccepted()) { %>
    <form method="post" action="<%= ctx %>/event-booking/accept-quote">
        <input type="hidden" name="reference" value="<%= HtmlUtil.escape(b.getReference()) %>">
        <input type="hidden" name="quoteId" value="<%= latest.getId() %>">
        <button class="btn btn-primary" type="submit">Accept quotation v<%= latest.getVersion() %></button>
    </form>
    <% } else if (latest.isAccepted()) { %><p class="status-badge">Latest quotation accepted</p><% } %>
</article>
<% } %>
<div class="form-actions" style="margin-top:20px"><%if(!"QUOTED".equals(b.getStatus())&&!"CONFIRMED".equals(b.getStatus())&&! "COMPLETED".equals(b.getStatus())&&! "CANCELLED".equals(b.getStatus())){%><a class="btn btn-primary" href="<%=ctx%>/event-booking/edit?reference=<%=b.getReference()%>">Edit booking</a><%}%><%if(!"CANCELLED".equals(b.getStatus())&&! "COMPLETED".equals(b.getStatus())){%><form method="post" action="<%=ctx%>/event-booking/cancel" style="display:flex;gap:8px"><input type="hidden" name="reference" value="<%=b.getReference()%>"><input class="form-control" name="reason" required minlength="5" placeholder="Cancellation reason"><button class="btn btn-secondary" type="submit">Cancel booking</button></form><%}%><a class="btn btn-secondary" href="<%=ctx%>/event-booking">Back to bookings</a></div>
</div></section>
<%@ include file="fragments/footer.jspf" %>
