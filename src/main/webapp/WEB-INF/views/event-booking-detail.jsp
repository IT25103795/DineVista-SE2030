<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<% request.setAttribute("pageTitle","Event Booking Details"); request.setAttribute("activeNav","events"); EventBookingRecord b=(EventBookingRecord)request.getAttribute("booking"); %>
<%@ include file="fragments/header.jspf" %>
<section class="section-sm"><div class="container">
<div class="section-heading"><div><span class="section-kicker">Booking reference</span><h1><%=b.getReference()%></h1></div><span class="status-badge"><%=b.getStatus()%></span></div>
<div class="detail-card"><div class="detail-grid">
<div><span>Package</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getPackageName())%></strong></div><div><span>Venue</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getVenue())%></strong></div>
<div><span>Date</span><strong><%=b.getEventDate()%></strong></div><div><span>Start time</span><strong><%=b.getEventTime()%></strong></div>
<div><span>Guests</span><strong><%=b.getGuestCount()%></strong></div><div><span>Estimated total</span><strong>LKR <%=b.getTotalAmount()%></strong></div>
<div><span>Contact</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getCustomerName())%></strong></div><div><span>Phone</span><strong><%=com.dinevista.util.HtmlUtil.escape(b.getPhone())%></strong></div>
</div><div style="margin-top:24px"><span class="section-kicker">Requirements</span><p><%=com.dinevista.util.HtmlUtil.escape(b.getNotes()).replace("\n","<br>")%></p></div></div>
<div class="form-actions" style="margin-top:20px"><%if(!"CONFIRMED".equals(b.getStatus())&&! "COMPLETED".equals(b.getStatus())&&! "CANCELLED".equals(b.getStatus())){%><a class="btn btn-primary" href="<%=ctx%>/event-booking/edit?reference=<%=b.getReference()%>">Edit booking</a><%}%><%if(!"CANCELLED".equals(b.getStatus())&&! "COMPLETED".equals(b.getStatus())){%><form method="post" action="<%=ctx%>/event-booking/cancel" style="display:flex;gap:8px"><input type="hidden" name="reference" value="<%=b.getReference()%>"><input class="form-control" name="reason" required minlength="5" placeholder="Cancellation reason"><button class="btn btn-secondary" type="submit">Cancel booking</button></form><%}%><a class="btn btn-secondary" href="<%=ctx%>/event-booking">Back to bookings</a></div>
</div></section>
<%@ include file="fragments/footer.jspf" %>
