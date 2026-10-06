package com.dinevista.controller;

import com.dinevista.model.*;
import com.dinevista.service.*;
import com.dinevista.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.*;
import java.util.*;

@WebServlet(urlPatterns={"/event-booking","/event-booking/*","/staff/event-bookings","/staff/event-bookings/*"})
public class EventBookingServlet extends HttpServlet {
    private EventBookingService service;
    private BillingService billingService;
    @Override public void init(){service=EventBookingContext.service(getServletContext());billingService=BillingContext.service(getServletContext());}
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        String servlet=req.getServletPath(), path=path(req);
        boolean isStaff = servlet != null && servlet.startsWith("/staff/");
        if(!ReservationOrderContext.isSignedIn(req)){res.sendRedirect(req.getContextPath()+(isStaff?"/manager/login":"/login"));return;}
        boolean manager=ReservationOrderContext.isManager(req);
        if(isStaff&&!manager){res.sendError(403);return;}
        if(isStaff){
            if("/view".equals(path)){view(req,res,true);return;}
            if("/edit".equals(path)){edit(req,res,true);return;}
            req.setAttribute("eventBookings",service.allBookings(RequestUtil.clean(req,"search")));req.setAttribute("managerView",true);
            res.setStatus(200);req.getRequestDispatcher("/WEB-INF/views/staff-event-bookings.jsp").forward(req,res);return;
        }
        if("/view".equals(path)){view(req,res,false);return;}
        if("/edit".equals(path)){edit(req,res,false);return;}
        renderCustomer(req,res,null); 
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        String servlet=req.getServletPath(),path=path(req);
        boolean isStaff = servlet != null && servlet.startsWith("/staff/");
        if(!ReservationOrderContext.isSignedIn(req)){res.sendRedirect(req.getContextPath()+(isStaff?"/manager/login":"/login"));return;}
        boolean manager=ReservationOrderContext.isManager(req);
        if(isStaff&&!manager){res.sendError(403);return;}
        if(isStaff){
            if("/update".equals(path)){managerUpdate(req,res);return;}
            if("/delete".equals(path)){
                String ref=RequestUtil.clean(req,"reference");
                if(billingService.existingInvoiceForSource("EVENT_BOOKING",ref).isPresent()){
                    FlashUtil.error(req,"This booking has an active invoice. Resolve the invoice before deleting the booking.");
                    res.sendRedirect(req.getContextPath()+"/staff/event-bookings/view?reference="+ref);return;
                }
                OperationResult<Void> r=service.delete(ref);res.sendRedirect(req.getContextPath()+"/staff/event-bookings?deleted="+r.isSuccess());return;}
        } else {
            if("/create".equals(path)){create(req,res);return;}
            if("/update".equals(path)){customerUpdate(req,res);return;}
            if("/accept-quote".equals(path)){acceptQuote(req,res);return;}
            if("/pay".equals(path)){simulatePayment(req,res);return;}
            if("/cancel".equals(path)){cancel(req,res);return;}
        }
        res.sendError(404);
    }
    private void create(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        String promotionCode=RequestUtil.clean(r,"promotionCode");
        if(!promotionCode.isEmpty()){
            EventPackageRecord selectedPackage=service.packageById(longValue(r,"packageId")).orElse(null);
            EventVenueRecord selectedVenue=service.venues().stream().filter(v->v.getId()==longValue(r,"venueId")).findFirst().orElse(null);
            if(selectedPackage!=null&&selectedVenue!=null){
                java.math.BigDecimal estimate=selectedPackage.getPricePerGuest()
                        .multiply(java.math.BigDecimal.valueOf(RequestUtil.integer(r,"guestCount",0)))
                        .add(selectedVenue.getBaseFee());
                OperationResult<java.math.BigDecimal> discount=billingService.previewDiscount(promotionCode,estimate);
                if(!discount.isSuccess()){r.setAttribute("errors",discount.getErrors());renderCustomer(r,s,null);return;}
            }
        }
        OperationResult<EventBookingRecord> x=service.create(userId(r),RequestUtil.clean(r,"customerName"),RequestUtil.clean(r,"email"),RequestUtil.clean(r,"phone"),RequestUtil.clean(r,"eventType"),
                longValue(r,"packageId"),longValue(r,"venueId"),date(r,"eventDate"),time(r,"eventTime"),RequestUtil.integer(r,"guestCount",0),RequestUtil.clean(r,"notes"),promotionCode);
        if(!x.isSuccess()){r.setAttribute("errors",x.getErrors());renderCustomer(r,s,x.getValue());return;}
        s.sendRedirect(r.getContextPath()+"/event-booking/view?reference="+x.getValue().getReference()+"&created=1");
    }
    private void customerUpdate(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        String ref=RequestUtil.clean(r,"reference");
        OperationResult<EventBookingRecord> x=service.customerUpdate(userId(r),email(r),ref,RequestUtil.clean(r,"customerName"),RequestUtil.clean(r,"email"),RequestUtil.clean(r,"phone"),
                RequestUtil.clean(r,"eventType"),longValue(r,"packageId"),longValue(r,"venueId"),date(r,"eventDate"),time(r,"eventTime"),RequestUtil.integer(r,"guestCount",0),RequestUtil.clean(r,"notes"));
        if(!x.isSuccess()){r.setAttribute("errors",x.getErrors());r.setAttribute("booking",service.booking(ref).orElse(null));r.setAttribute("customerEdit",true);renderCustomer(r,s,null);return;}
        s.sendRedirect(r.getContextPath()+"/event-booking/view?reference="+ref+"&updated=1");
    }
    private void cancel(HttpServletRequest r,HttpServletResponse s)throws IOException{
        String ref=RequestUtil.clean(r,"reference");
        if(billingService.existingInvoiceForSource("EVENT_BOOKING",ref).isPresent()){
            FlashUtil.error(r,"This booking has an invoice. Please contact the restaurant to arrange a cancellation or refund.");
            s.sendRedirect(r.getContextPath()+"/event-booking/view?reference="+ref);return;
        }
        OperationResult<EventBookingRecord> x=service.cancelByCustomer(userId(r),email(r),RequestUtil.clean(r,"reference"),RequestUtil.clean(r,"reason"));
        s.sendRedirect(r.getContextPath()+"/event-booking/view?reference="+RequestUtil.clean(r,"reference")+"&cancelled="+x.isSuccess());
    }
    private void acceptQuote(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        String ref=RequestUtil.clean(r,"reference");
        OperationResult<EventQuoteRecord> result=service.acceptQuote(userId(r),email(r),ref,longValue(r,"quoteId"));
        if(!result.isSuccess()){
            r.setAttribute("errors",result.getErrors());
            view(r,s,false);
            return;
        }
        s.sendRedirect(r.getContextPath()+"/event-booking/view?reference="+ref+"&quoteAccepted=1");
    }
    private void simulatePayment(HttpServletRequest r,HttpServletResponse s)throws IOException{
        String ref=RequestUtil.clean(r,"reference");
        EventBookingRecord booking=service.booking(ref).orElse(null);
        if(booking==null||!service.owns(userId(r),email(r),booking)){s.sendError(404);return;}
        EventQuoteRecord latest=service.quotes(ref).stream().findFirst().orElse(null);
        InvoiceRecord invoice=billingService.existingInvoiceForSource("EVENT_BOOKING",ref).orElse(null);
        if(latest==null||!latest.isAccepted()||invoice==null||invoice.isCancelled()
                ||!invoice.getCustomerKey().equalsIgnoreCase(booking.getEmail())){
            FlashUtil.error(r,"An approved quote and final invoice are needed before the demo payment.");
        }else{
            OperationResult<?> result=billingService.recordPayment(invoice.getId(),"ONLINE",
                    invoice.getBalance().toPlainString(),"","Sandbox demonstration only; no real funds transferred.",
                    "Demo checkout");
            if(result.isSuccess())FlashUtil.success(r,"Demo payment recorded. No real money was transferred.");
            else FlashUtil.errors(r,result.getErrors());
        }
        s.sendRedirect(r.getContextPath()+"/event-booking/view?reference="+ref);
    }
    private void managerUpdate(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{
        String ref=RequestUtil.clean(r,"reference");
        EventBookingRecord prior=service.booking(ref).orElse(null);
        if(prior!=null&&billingService.existingInvoiceForSource("EVENT_BOOKING",ref).isPresent()
                &&(prior.getPackageId()!=longValue(r,"packageId")
                ||prior.getVenueId()!=longValue(r,"venueId")
                ||prior.getGuestCount()!=RequestUtil.integer(r,"guestCount",0)
                ||!prior.getCustomerName().equals(RequestUtil.clean(r,"customerName"))
                ||!prior.getEmail().equals(RequestUtil.clean(r,"email"))
                ||!prior.getPhone().equals(RequestUtil.clean(r,"phone"))
                ||!prior.getEventType().equals(RequestUtil.clean(r,"eventType"))
                ||!prior.getNotes().equals(RequestUtil.clean(r,"notes"))
                ||!prior.getEventDate().equals(String.valueOf(date(r,"eventDate")))
                ||!sameTime(prior.getEventTime(),time(r,"eventTime")))){
            r.setAttribute("errors",List.of("This booking already has an invoice. Resolve it before changing the quoted details or customer information."));
            r.setAttribute("booking",prior);r.setAttribute("managerView",true);renderManagerEdit(r,s);return;
        }
        if("CANCELLED".equals(RequestUtil.clean(r,"status"))
                &&billingService.existingInvoiceForSource("EVENT_BOOKING",ref).isPresent()){
            r.setAttribute("errors",List.of("Resolve the active invoice and any payment before cancelling this booking."));
            r.setAttribute("booking",service.booking(ref).orElse(null));r.setAttribute("managerView",true);renderManagerEdit(r,s);return;
        }
        OperationResult<EventBookingRecord> x=service.managerUpdate(ref,RequestUtil.clean(r,"customerName"),RequestUtil.clean(r,"email"),RequestUtil.clean(r,"phone"),RequestUtil.clean(r,"eventType"),
                longValue(r,"packageId"),longValue(r,"venueId"),date(r,"eventDate"),time(r,"eventTime"),RequestUtil.integer(r,"guestCount",0),RequestUtil.clean(r,"notes"),RequestUtil.clean(r,"status"));
        if(!x.isSuccess()){r.setAttribute("errors",x.getErrors());r.setAttribute("booking",service.booking(ref).orElse(null));r.setAttribute("managerView",true);renderManagerEdit(r,s);return;}
        s.sendRedirect(r.getContextPath()+"/staff/event-bookings/view?reference="+ref+"&updated=1");
    }
    private void view(HttpServletRequest r,HttpServletResponse s,boolean manager)throws ServletException,IOException{
        FlashUtil.expose(r);
        Optional<EventBookingRecord> x=service.booking(RequestUtil.clean(r,"reference"));if(x.isEmpty()){s.sendError(404);return;}
        if(!manager&&!owned(r,x.get())){s.sendError(404);return;}
        r.setAttribute("booking",x.get());r.setAttribute("managerView",manager);
        r.setAttribute("quotes",service.quotes(x.get().getReference()));
        r.setAttribute("eventInvoice", billingService.existingInvoiceForSource("EVENT_BOOKING", x.get().getReference()).orElse(null));
        if(!manager){
            OperationResult<java.math.BigDecimal> discount=billingService.previewDiscount(
                    x.get().getPromotionCode(),x.get().getTotalAmount());
            r.setAttribute("eventEstimatedDiscount",discount.isSuccess()?discount.getValue():java.math.BigDecimal.ZERO);
            if(!discount.isSuccess())r.setAttribute("eventPromotionNote",discount.getErrors().get(0));
        }
        r.getRequestDispatcher(manager?"/WEB-INF/views/staff-event-booking-detail.jsp":"/WEB-INF/views/event-booking-detail.jsp").forward(r,s);
    }
    private void edit(HttpServletRequest r,HttpServletResponse s,boolean manager)throws ServletException,IOException{
        Optional<EventBookingRecord> x=service.booking(RequestUtil.clean(r,"reference"));if(x.isEmpty()){s.sendError(404);return;}
        if(!manager&&!owned(r,x.get())){s.sendError(404);return;}
        r.setAttribute("booking",x.get());r.setAttribute("managerView",manager);r.setAttribute("packages",service.activePackages(""));r.setAttribute("venues",service.venues());
        r.getRequestDispatcher(manager?"/WEB-INF/views/staff-event-booking-form.jsp":"/WEB-INF/views/event-booking-form.jsp").forward(r,s);
    }
    private void renderCustomer(HttpServletRequest r,HttpServletResponse s,EventBookingRecord ignored)throws ServletException,IOException{
        r.setAttribute("packages",service.activePackages(""));r.setAttribute("venues",service.venues());
        r.setAttribute("promotions",billingService.allPromotions());
        java.util.Map<Long,Integer> usageCounts=new java.util.HashMap<>();
        for(PromotionRecord promotion:billingService.allPromotions())usageCounts.put(promotion.getId(),billingService.usageCount(promotion.getId()));
        r.setAttribute("promotionUsageCounts",usageCounts);
        r.setAttribute("customerBookings",service.customerBookings(userId(r),email(r)));
        if(r.getAttribute("booking")==null&&RequestUtil.clean(r,"reference").length()>0)r.setAttribute("booking",service.booking(RequestUtil.clean(r,"reference")).orElse(null));
        r.getRequestDispatcher("/WEB-INF/views/event-booking.jsp").forward(r,s);
    }
    private void renderManagerEdit(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException{r.setAttribute("packages",service.activePackages(""));r.setAttribute("venues",service.venues());r.getRequestDispatcher("/WEB-INF/views/staff-event-booking-form.jsp").forward(r,s);}
    private boolean owned(HttpServletRequest r,EventBookingRecord b){return service.owns(userId(r),email(r),b);}
    private long userId(HttpServletRequest r){Object x=r.getSession(false).getAttribute("userId");try{return Long.parseLong(String.valueOf(x));}catch(Exception e){return 0;}}
    private String email(HttpServletRequest r){return String.valueOf(r.getSession(false).getAttribute("demoEmail"));}
    private long longValue(HttpServletRequest r,String n){try{return Long.parseLong(RequestUtil.clean(r,n));}catch(Exception e){return -1;}}
    private LocalDate date(HttpServletRequest r,String n){try{return LocalDate.parse(RequestUtil.clean(r,n));}catch(Exception e){return null;}}
    private LocalTime time(HttpServletRequest r,String n){try{return LocalTime.parse(RequestUtil.clean(r,n));}catch(Exception e){return null;}}
    private boolean sameTime(String saved,LocalTime submitted){try{return submitted!=null&&LocalTime.parse(saved).equals(submitted);}catch(Exception e){return false;}}
    private String path(HttpServletRequest r){String p=r.getPathInfo();if(p==null||"/".equals(p))return "";if(p.endsWith("/"))p=p.substring(0,p.length()-1);return p;}
}
