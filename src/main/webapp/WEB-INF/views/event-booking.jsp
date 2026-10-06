<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.model.*,java.util.*" %>
<% request.setAttribute("pageTitle","Event Bookings"); request.setAttribute("activeNav","events"); List<EventPackageRecord> packages=(List<EventPackageRecord>)request.getAttribute("packages"); List<EventVenueRecord> venues=(List<EventVenueRecord>)request.getAttribute("venues"); List<EventBookingRecord> bookings=(List<EventBookingRecord>)request.getAttribute("customerBookings"); List<PromotionRecord> promotions=(List<PromotionRecord>)request.getAttribute("promotions"); Map<Long,Integer> promotionUsageCounts=(Map<Long,Integer>)request.getAttribute("promotionUsageCounts"); %>
<%@ include file="fragments/header.jspf" %>
<section class="section-sm"><div class="container">
<div class="section-heading"><div><span class="section-kicker">Event booking</span><h1>Plan your event with DineVista.</h1></div><p>See an estimated total now. After you accept the final quote, the restaurant prepares an invoice and you can make a simulated payment. No real money is charged.</p></div>
<% List<String> errors=(List<String>)request.getAttribute("errors"); if(errors!=null&&!errors.isEmpty()){%><div class="alert alert-error"><ul><%for(String e:errors){%><li><%=com.dinevista.util.HtmlUtil.escape(e)%></li><%}%></ul></div><%}%>
<div class="content-grid">
<div class="form-card"><form method="post" action="<%=ctx%>/event-booking/create">
<div class="form-grid">
<div class="form-group"><label>Contact name</label><input class="form-control" name="customerName" required value="<%=request.getAttribute("formCustomerName")==null?com.dinevista.util.ReservationOrderContext.displayName(request):com.dinevista.util.HtmlUtil.escape(String.valueOf(request.getAttribute("formCustomerName")))%>"></div>
<div class="form-group"><label>Email</label><input class="form-control" type="email" name="email" required value="<%=request.getAttribute("formEmail")==null?com.dinevista.util.HtmlUtil.escape(String.valueOf(session.getAttribute("demoEmail"))):com.dinevista.util.HtmlUtil.escape(String.valueOf(request.getAttribute("formEmail")))%>"></div>
<div class="form-group"><label>Phone</label><input class="form-control" name="phone" required pattern="(?:\+94|0)7[0-9]{8}" placeholder="0771234567"></div>
<div class="form-group"><label>Event type</label><select class="form-control" name="eventType" required><option value="">Select event</option><option>Wedding reception</option><option>Corporate event</option><option>Birthday celebration</option><option>Anniversary</option><option>Workshop or seminar</option><option>Private event</option></select></div>
<div class="form-group"><label>Event package</label><select class="form-control" id="bookingPackage" name="packageId" required><option value="">Select package</option><%if(packages!=null)for(EventPackageRecord p:packages){%><option value="<%=p.getId()%>" data-price="<%=p.getPricePerGuest()%>" <%=String.valueOf(request.getParameter("packageId")).equals(String.valueOf(p.getId()))?"selected":""%>><%=com.dinevista.util.HtmlUtil.escape(p.getName())%> - LKR <%=p.getPricePerGuest()%>/guest</option><%}%></select></div>
<div class="form-group"><label>Venue</label><select class="form-control" id="bookingVenue" name="venueId" required><option value="">Select venue</option><%if(venues!=null)for(EventVenueRecord v:venues){%><option value="<%=v.getId()%>" data-fee="<%=v.getBaseFee()%>" <%=String.valueOf(request.getParameter("venueId")).equals(String.valueOf(v.getId()))?"selected":""%>><%=com.dinevista.util.HtmlUtil.escape(v.getName())%> (up to <%=v.getCapacity()%>)</option><%}%></select></div>
<div class="form-group"><label>Event date</label><input class="form-control" type="date" name="eventDate" required min="<%=java.time.LocalDate.now().plusDays(7)%>"></div>
<div class="form-group"><label>Start time</label><input class="form-control" type="time" name="eventTime" required></div>
<div class="form-group"><label>Expected guests</label><input class="form-control" id="bookingGuests" type="number" name="guestCount" min="1" required value="<%=request.getParameter("guestCount")==null?"50":com.dinevista.util.HtmlUtil.escape(request.getParameter("guestCount"))%>"></div>
<div class="form-group"><label>Promotion code</label><select class="form-control" id="bookingPromotion" name="promotionCode"><option value="">No promotion</option><%if(promotions!=null)for(PromotionRecord promo:promotions){if(promo.isCurrentlyValid()&&(promo.getUsageLimit()==null||promotionUsageCounts.getOrDefault(promo.getId(),0)<promo.getUsageLimit())){%><option value="<%=com.dinevista.util.HtmlUtil.escape(promo.getCode())%>" data-type="<%=com.dinevista.util.HtmlUtil.escape(promo.getDiscountType())%>" data-value="<%=promo.getDiscountValue()%>" data-minimum="<%=promo.getMinimumSpend()%>" <%=promo.getCode().equalsIgnoreCase(request.getParameter("promotionCode"))?"selected":""%>><%=com.dinevista.util.HtmlUtil.escape(promo.getCode())%> - <%=promo.getDiscountValueDisplay()%> off</option><%}}%></select></div>
<div class="form-group full"><label>Requirements / notes</label><textarea class="form-control" name="notes" maxlength="5000" rows="5" placeholder="Decor, menu preferences, AV, dietary needs, seating or other requirements"></textarea></div>
</div>
<article class="detail-card" style="margin-top:20px" aria-live="polite"><h3>Your estimated event total</h3><div class="detail-fact-grid" style="margin-top:12px"><div><span>Package and venue</span><strong id="bookingBase">LKR 0.00</strong></div><div><span>Service tax (2.5%)</span><strong id="bookingTax">LKR 0.00</strong></div><div><span>Promotion discount</span><strong id="bookingDiscount">- LKR 0.00</strong></div><div><span>Estimated total</span><strong id="bookingTotal">LKR 0.00</strong></div></div><p class="muted small" id="bookingPromotionNote" style="margin-top:10px">Choose a package and venue to see the estimate.</p><p class="muted small">This is not a charge. The manager may revise the quote or add agreed extras. You will see the final invoice before choosing the demo payment.</p></article>
<div class="form-actions"><button class="btn btn-primary" type="submit">Submit booking request</button><a class="btn btn-secondary" href="<%=ctx%>/events">Browse packages</a></div></form></div>
</div>
<%if(bookings!=null){%><div class="section-heading" style="margin-top:40px"><div><span class="section-kicker">My bookings</span><h2>Event booking history</h2></div></div><div class="table-wrap"><table class="data-table"><thead><tr><th>Reference</th><th>Package</th><th>Date</th><th>Guests</th><th>Base estimate</th><th>Status</th><th></th></tr></thead><tbody><%for(EventBookingRecord b:bookings){%><tr><td><strong><%=b.getReference()%></strong></td><td><%=com.dinevista.util.HtmlUtil.escape(b.getPackageName())%></td><td><%=b.getEventDate()%> <%=b.getEventTime()%></td><td><%=b.getGuestCount()%></td><td>LKR <%=String.format("%,.2f",b.getTotalAmount())%></td><td><span class="status-badge"><%=b.getStatus()%></span></td><td><a class="btn btn-secondary btn-sm" href="<%=ctx%>/event-booking/view?reference=<%=b.getReference()%>">View</a></td></tr><%}%></tbody></table></div><%}%>
</div></section>
<script>
(function(){
    var pkg=document.getElementById('bookingPackage'),venue=document.getElementById('bookingVenue'),guests=document.getElementById('bookingGuests'),promotion=document.getElementById('bookingPromotion');
    var money=function(n){return 'LKR '+n.toLocaleString('en-LK',{minimumFractionDigits:2,maximumFractionDigits:2});};
    var round=function(n){return Math.round((n+Number.EPSILON)*100)/100;};
    function update(){
        var packageOption=pkg.options[pkg.selectedIndex],venueOption=venue.options[venue.selectedIndex];
        var base=Number(packageOption.dataset.price||0)*Number(guests.value||0)+Number(venueOption.dataset.fee||0);
        if(!packageOption.value||!venueOption.value||Number(guests.value)<=0)base=0;
        base=round(base);
        var tax=round(base*.025),discount=0,chosen=promotion.options[promotion.selectedIndex];
        var note=document.getElementById('bookingPromotionNote');
        if(chosen.value&&base>0){
            if(base<Number(chosen.dataset.minimum))note.textContent='This promotion needs a higher package-and-venue amount.';
            else {discount=chosen.dataset.type==='PERCENTAGE'?round(base*Number(chosen.dataset.value)/100):round(Number(chosen.dataset.value));discount=Math.min(base,discount);note.textContent=chosen.value+' discount is included in this estimate.';}
        }else note.textContent=base>0?'The manager will confirm the final quote before any demo payment.':'Choose a package and venue to see the estimate.';
        document.getElementById('bookingBase').textContent=money(base);
        document.getElementById('bookingTax').textContent=money(tax);
        document.getElementById('bookingDiscount').textContent='- '+money(discount);
        document.getElementById('bookingTotal').textContent=money(Math.max(0,round(base+tax-discount)));
    }
    [pkg,venue,promotion].forEach(function(el){el.addEventListener('change',update);});
    guests.addEventListener('input',update);update();
})();
</script>
<%@ include file="fragments/footer.jspf" %>
