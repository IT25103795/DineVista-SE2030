<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<%@ page import="com.dinevista.model.EventQuoteRecord" %>
<%@ page import="com.dinevista.model.InvoiceRecord" %>
<%@ page import="com.dinevista.model.InvoiceItemRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.math.RoundingMode" %>
<% request.setAttribute("pageTitle","Event Booking Details"); request.setAttribute("activeNav","events"); EventBookingRecord b=(EventBookingRecord)request.getAttribute("booking"); List<EventQuoteRecord> quotes=(List<EventQuoteRecord>)request.getAttribute("quotes"); InvoiceRecord eventInvoice=(InvoiceRecord)request.getAttribute("eventInvoice"); BigDecimal estimatedTax=b.getTotalAmount().multiply(new BigDecimal("0.025")).setScale(2,RoundingMode.HALF_UP); BigDecimal estimatedDiscount=(BigDecimal)request.getAttribute("eventEstimatedDiscount"); if(estimatedDiscount==null)estimatedDiscount=BigDecimal.ZERO; BigDecimal estimatedFinal=b.getTotalAmount().add(estimatedTax).subtract(estimatedDiscount); %>
<%@ include file="fragments/header.jspf" %>
<section class="section-sm"><div class="container">
<div class="section-heading"><div><span class="section-kicker">Booking reference</span><h1><%=b.getReference()%></h1></div><span class="status-badge"><%=b.getStatus()%></span></div>
<div class="detail-card"><div class="detail-grid">
<div><span>Package</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getPackageName())%></strong></div><div><span>Venue</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getVenue())%></strong></div>
<div><span>Date</span><strong><%=b.getEventDate()%></strong></div><div><span>Start time</span><strong><%=b.getEventTime()%></strong></div>
<div><span>Guests</span><strong><%=b.getGuestCount()%></strong></div><div><span>Package and venue amount</span><strong>LKR <%= String.format("%,.2f",b.getTotalAmount()) %></strong></div>
<div><span>Contact</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getCustomerName())%></strong></div><div><span>Phone</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getPhone())%></strong></div>
</div><div style="margin-top:24px"><span class="section-kicker">Requirements</span><p><%=com.dinevista.util.HtmlUtil.escape(b.getNotes()).replace("\n","<br>")%></p></div></div>
<article class="detail-card" style="margin-top:18px">
    <span class="section-kicker">Final price and demo payment</span>
    <% if (eventInvoice == null) { %>
        <h2>Review the estimate; no payment yet</h2>
        <p class="muted">After you accept the manager's final quote, the restaurant prepares an invoice. Review any extra charges, service tax and discount before you choose to simulate payment. No real money is charged.</p>
        <div class="detail-grid" style="margin-top:16px">
            <div><span>Package and venue</span><strong>LKR <%= String.format("%,.2f",b.getTotalAmount()) %></strong></div>
            <div><span>Estimated service tax (2.5%)</span><strong>LKR <%= String.format("%,.2f",estimatedTax) %></strong></div>
            <div><span>Promotion<%= b.getPromotionCode().isEmpty()?"":" ("+HtmlUtil.escape(b.getPromotionCode())+")" %></span><strong>- LKR <%= String.format("%,.2f",estimatedDiscount) %></strong></div>
            <div><span>Estimated total</span><strong>LKR <%= String.format("%,.2f",estimatedFinal) %></strong></div>
        </div>
        <% if (request.getAttribute("eventPromotionNote") != null) { %><p class="muted small"><%= HtmlUtil.escape(request.getAttribute("eventPromotionNote")) %></p><% } %>
        <p class="muted small">The manager may revise the quote. This estimate is not the amount due until the final invoice is issued.</p>
    <% } else { %>
        <h2>Invoice <%= HtmlUtil.escape(eventInvoice.getInvoiceNumber()) %></h2>
        <p class="muted">This is the final amount from the accepted quote, any extra charges, service tax and eligible discount. The payment button below is a sandbox demonstration only; it does not connect to a bank or charge real money.</p>
        <div class="table-wrap"><table class="data-table"><thead><tr><th>Charge</th><th>Quantity</th><th>Unit price</th><th>Amount</th></tr></thead><tbody>
            <% for (InvoiceItemRecord item : eventInvoice.getItems()) { %><tr><td><%= HtmlUtil.escape(item.getDescription()) %></td><td><%= item.getQuantityDisplay() %></td><td><%= item.getUnitPriceDisplay() %></td><td><%= item.getLineTotalDisplay() %></td></tr><% } %>
        </tbody></table></div>
        <div class="detail-grid" style="margin-top:16px">
            <div><span>Subtotal</span><strong><%= eventInvoice.getSubtotalDisplay() %></strong></div>
            <div><span>Service tax (2.5%)</span><strong><%= eventInvoice.getTaxAmountDisplay() %></strong></div>
            <div><span>Discount<%= eventInvoice.getPromotionCode() == null ? "" : " (" + HtmlUtil.escape(eventInvoice.getPromotionCode()) + ")" %></span><strong>- <%= eventInvoice.getDiscountAmountDisplay() %></strong></div>
            <div><span>Final invoice total</span><strong><%= eventInvoice.getTotalAmountDisplay() %></strong></div>
            <div><span>Demo payment recorded</span><strong><%= eventInvoice.getAmountPaidDisplay() %></strong></div>
            <div><span>Balance to pay</span><strong><%= eventInvoice.getBalanceDisplay() %></strong></div>
        </div>
        <% if (!eventInvoice.isCancelled() && !eventInvoice.isFullyPaid()) { %>
        <form method="post" action="<%= ctx %>/event-booking/pay" class="form-actions" style="margin-top:16px">
            <input type="hidden" name="reference" value="<%= HtmlUtil.escape(b.getReference()) %>">
            <button class="btn btn-primary" type="submit">Simulate full payment - no real charge</button>
        </form>
        <% } else if (eventInvoice.isFullyPaid()) { %><p class="status-badge" style="margin-top:16px">Demo payment complete</p><% } %>
    <% } %>
</article>
<% if (request.getAttribute("successMessage") != null) { %><div class="alert alert-success" style="margin-top:16px"><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></div><% } %>
<% if (request.getAttribute("errors") != null) { %><div class="alert alert-danger" style="margin-top:16px"><% for (String error : (List<String>)request.getAttribute("errors")) { %><p><%= HtmlUtil.escape(error) %></p><% } %></div><% } %>
<% if (quotes != null && !quotes.isEmpty()) { EventQuoteRecord latest=quotes.get(0); %>
<article class="detail-card" style="margin-top:18px">
    <h2>Quotation versions</h2>
    <p class="muted">Only the latest version can be approved. Earlier versions remain in your booking history.</p>
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
<div class="form-actions" style="margin-top:20px"><%if(!"QUOTED".equals(b.getStatus())&&!"CONFIRMED".equals(b.getStatus())&&! "COMPLETED".equals(b.getStatus())&&! "CANCELLED".equals(b.getStatus())){%><a class="btn btn-primary" href="<%=ctx%>/event-booking/edit?reference=<%=b.getReference()%>">Edit booking</a><%}%><%if(eventInvoice==null&&!"CANCELLED".equals(b.getStatus())&&! "COMPLETED".equals(b.getStatus())){%><form method="post" action="<%=ctx%>/event-booking/cancel" style="display:flex;gap:8px"><input type="hidden" name="reference" value="<%=b.getReference()%>"><input class="form-control" name="reason" required minlength="5" placeholder="Cancellation reason"><button class="btn btn-secondary" type="submit">Cancel booking</button></form><%}%><a class="btn btn-secondary" href="<%=ctx%>/event-booking">Back to bookings</a></div>
</div></section>
<%@ include file="fragments/footer.jspf" %>
