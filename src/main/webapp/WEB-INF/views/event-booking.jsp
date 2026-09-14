<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.*,java.util.*" %>
<% request.setAttribute("pageTitle","Event Bookings"); request.setAttribute("activeNav","events"); String ctx=request.getContextPath(); List<EventPackageRecord> packages=(List<EventPackageRecord>)request.getAttribute("packages"); List<EventVenueRecord> venues=(List<EventVenueRecord>)request.getAttribute("venues"); List<EventBookingRecord> bookings=(List<EventBookingRecord>)request.getAttribute("customerBookings"); %>
<%@ include file="fragments/header.jspf" %>
<section class="section-sm"><div class="container">
<div class="section-heading"><div><span class="section-kicker">Event booking</span><h1>Plan your event with DineVista.</h1></div><p>Requests are reviewed by the event team before confirmation.</p></div>
<% List<String> errors=(List<String>)request.getAttribute("errors"); if(errors!=null&&!errors.isEmpty()){%><div class="alert alert-error"><ul><%for(String e:errors){%><li><%=com.dinevista.util.HtmlUtil.escape(e)%></li><%}%></ul></div><%}%>
<div class="content-grid">
<div class="form-card"><form method="post" action="<%=ctx%>/event-booking/create">
<div class="form-grid">
<div class="form-group"><label>Contact name</label><input class="form-control" name="customerName" required value="<%=request.getAttribute("formCustomerName")==null?com.dinevista.util.ReservationOrderContext.displayName(request):com.dinevista.util.HtmlUtil.escape(String.valueOf(request.getAttribute("formCustomerName")))%>"></div>
<div class="form-group"><label>Email</label><input class="form-control" type="email" name="email" required value="<%=request.getAttribute("formEmail")==null?com.dinevista.util.HtmlUtil.escape(String.valueOf(session.getAttribute("demoEmail"))):com.dinevista.util.HtmlUtil.escape(String.valueOf(request.getAttribute("formEmail")))%>"></div>
<div class="form-group"><label>Phone</label><input class="form-control" name="phone" required pattern="(?:\+94|0)7[0-9]{8}" placeholder="0771234567"></div>
<div class="form-group"><label>Event type</label><select class="form-control" name="eventType" required><option value="">Select event</option><option>Wedding reception</option><option>Corporate event</option><option>Birthday celebration</option><option>Anniversary</option><option>Workshop or seminar</option><option>Private event</option></select></div>
<div class="form-group"><label>Event package</label><select class="form-control" name="packageId" required><option value="">Select package</option><%if(packages!=null)for(EventPackageRecord p:packages){%><option value="<%=p.getId()%>" <%=String.valueOf(request.getParameter("packageId")).equals(String.valueOf(p.getId()))?"selected":""%>><%=com.dinevista.util.HtmlUtil.escape(p.getName())%> - LKR <%=p.getPricePerGuest()%>/guest</option><%}%></select></div>
<div class="form-group"><label>Venue</label><select class="form-control" name="venueId" required><option value="">Select venue</option><%if(venues!=null)for(EventVenueRecord v:venues){%><option value="<%=v.getId()%>"><%=com.dinevista.util.HtmlUtil.escape(v.getName())%> (up to <%=v.getCapacity()%>)</option><%}%></select></div>
<div class="form-group"><label>Event date</label><input class="form-control" type="date" name="eventDate" required min="<%=java.time.LocalDate.now().plusDays(7)%>"></div>
<div class="form-group"><label>Start time</label><input class="form-control" type="time" name="eventTime" required></div>
<div class="form-group"><label>Expected guests</label><input class="form-control" type="number" name="guestCount" min="1" required value="50"></div>
<div class="form-group full"><label>Requirements / notes</label><textarea class="form-control" name="notes" maxlength="5000" rows="5" placeholder="Decor, menu preferences, AV, dietary needs, seating or other requirements"></textarea></div>
</div><div class="form-actions"><button class="btn btn-primary" type="submit">Submit booking request</button><a class="btn btn-secondary" href="<%=ctx%>/events">Browse packages</a></div></form></div>
</div>
<%if(bookings!=null){%><div class="section-heading" style="margin-top:40px"><div><span class="section-kicker">My bookings</span><h2>Event booking history</h2></div></div><div class="table-wrap"><table class="data-table"><thead><tr><th>Reference</th><th>Package</th><th>Date</th><th>Guests</th><th>Total</th><th>Status</th><th></th></tr></thead><tbody><%for(EventBookingRecord b:bookings){%><tr><td><strong><%=b.getReference()%></strong></td><td><%=com.dinevista.util.HtmlUtil.escape(b.getPackageName())%></td><td><%=b.getEventDate()%> <%=b.getEventTime()%></td><td><%=b.getGuestCount()%></td><td>LKR <%=b.getTotalAmount()%></td><td><span class="status-badge"><%=b.getStatus()%></span></td><td><a class="btn btn-secondary btn-sm" href="<%=ctx%>/event-booking/view?reference=<%=b.getReference()%>">View</a></td></tr><%}%></tbody></table></div><%}%>
</div></section>
<%@ include file="fragments/footer.jspf" %>
