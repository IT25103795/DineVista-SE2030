package com.dinevista.service;

import com.dinevista.model.*;
import com.dinevista.repository.EventBookingRepository;
import com.dinevista.repository.EventPackageRepository;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public class EventBookingService {
    private static final List<String> STATUSES=Collections.unmodifiableList(Arrays.asList("INQUIRY","CONSULTATION","QUOTED","CONFIRMED","COMPLETED","CANCELLED"));
    private final EventBookingRepository bookingRepository;
    private final EventPackageRepository packageRepository;
    public EventBookingService(EventBookingRepository b,EventPackageRepository p){bookingRepository=b;packageRepository=p;}

    public List<EventVenueRecord> venues(){return bookingRepository.findVenues();}
    public List<EventPackageRecord> activePackages(String search){return packageRepository.findAll(true,search);}
    public Optional<EventPackageRecord> packageById(long id){return packageRepository.findById(id);}
    public List<EventBookingRecord> customerBookings(long userId,String email){
        Long cid=bookingRepository.customerIdForUser(userId); return bookingRepository.findForCustomer(cid==null?0:cid,email);
    }
    public List<EventBookingRecord> allBookings(String search){return bookingRepository.findAll(search);}
    public Optional<EventBookingRecord> booking(String ref){return bookingRepository.findByReference(ref);}
    public List<EventQuoteRecord> quotes(String ref){return bookingRepository.findQuotes(ref);}

    public boolean owns(long userId,String email,EventBookingRecord b){
        Long cid=bookingRepository.customerIdForUser(userId);
        if(b.getCustomerId()>0)return cid!=null&&b.getCustomerId()==cid;
        return email!=null&&b.getEmail().equalsIgnoreCase(email);
    }
    public OperationResult<EventBookingRecord> create(long userId,String customerName,String email,String phone,String eventType,
                                                       long packageId,long venueId,LocalDate date,LocalTime time,int guests,String notes){
        return create(userId,customerName,email,phone,eventType,packageId,venueId,date,time,guests,notes,"");
    }
    public OperationResult<EventBookingRecord> create(long userId,String customerName,String email,String phone,String eventType,
                                                       long packageId,long venueId,LocalDate date,LocalTime time,int guests,String notes,String promotionCode){
        return save(userId,"customer",null,customerName,email,phone,eventType,packageId,venueId,date,time,guests,notes,"INQUIRY",true,promotionCode);
    }
    public OperationResult<EventBookingRecord> customerUpdate(long userId,String email,String ref,String customerName,String customerEmail,
                                                               String phone,String eventType,long packageId,long venueId,LocalDate date,LocalTime time,int guests,String notes){
        Optional<EventBookingRecord> existing=booking(ref);
        if(existing.isEmpty())return OperationResult.failure("Event booking was not found.");
        EventBookingRecord old=existing.get();
        if(!owns(userId,email,old))return OperationResult.failure("You are not allowed to update this booking.");
        if("QUOTED".equals(old.getStatus()))
            return OperationResult.failure("A quote has been issued. Ask the coordinator to revise requirements and issue a new version.");
        if("CONFIRMED".equals(old.getStatus())||"COMPLETED".equals(old.getStatus())||"CANCELLED".equals(old.getStatus()))
            return OperationResult.failure("This booking can no longer be edited.");
        return save(userId,"customer",ref,customerName,customerEmail,phone,eventType,packageId,venueId,date,time,guests,notes,old.getStatus(),false,old.getPromotionCode());
    }
    public synchronized OperationResult<EventBookingRecord> managerUpdate(String ref,String customerName,String email,String phone,String eventType,
                                                              long packageId,long venueId,LocalDate date,LocalTime time,int guests,String notes,String status){
        Optional<EventBookingRecord> old=booking(ref);
        if(old.isEmpty())return OperationResult.failure("Event booking was not found.");
        if(date==null||time==null)return OperationResult.failure("Select a valid event date and time.");
        if(!STATUSES.contains(status))return OperationResult.failure("Select a valid booking status.");
        if(!validTransition(old.get().getStatus(),status))return OperationResult.failure("This booking cannot be moved to the selected status.");
        if("CONFIRMED".equals(old.get().getStatus())
                && (old.get().getPackageId()!=packageId||old.get().getVenueId()!=venueId
                ||old.get().getGuestCount()!=guests
                ||!old.get().getEventDate().equals(date.toString())
                ||!old.get().getEventTime().equals(time.toString())))
            return OperationResult.failure("Confirmed event details cannot be changed. Cancel and issue a new quotation if plans change.");
        if("CONFIRMED".equals(old.get().getStatus())&&"CONFIRMED".equals(status)
                && !old.get().getNotes().equals(clean(notes)))
            return OperationResult.failure("Confirmed quotation requirements cannot be changed.");
        if("CANCELLED".equals(status)&&clean(notes).isEmpty())return OperationResult.failure("Add a note when cancelling a booking.");
        if("CONFIRMED".equals(status)&&!"CONFIRMED".equals(old.get().getStatus())){
            if(!old.get().getEventDate().equals(date.toString())
                    ||!old.get().getEventTime().equals(time.toString()))
                return OperationResult.failure("Event schedule changed. Issue and obtain approval for a revised quote before confirmation.");
            List<EventQuoteRecord> versions=quotes(ref);
            if(versions.isEmpty()||!versions.get(0).isAccepted())
                return OperationResult.failure("The customer must accept the current quotation before confirmation.");
            EventQuoteRecord accepted=versions.get(0);
            EventPackageRecord selectedPackage=packageRepository.findById(packageId).orElse(null);
            EventVenueRecord selectedVenue=venues().stream().filter(v->v.getId()==venueId).findFirst().orElse(null);
            if(selectedPackage==null||selectedVenue==null||accepted.getPackageId()!=packageId
                    ||accepted.getVenueId()!=venueId||accepted.getGuestCount()!=guests
                    ||!accepted.getRequirements().equals(clean(notes))
                    ||accepted.getPricePerGuest().compareTo(selectedPackage.getPricePerGuest())!=0
                    ||accepted.getVenueFee().compareTo(selectedVenue.getBaseFee())!=0
                    ||accepted.getTotal().compareTo(selectedPackage.getPricePerGuest()
                    .multiply(BigDecimal.valueOf(guests)).add(selectedVenue.getBaseFee()))!=0)
                return OperationResult.failure("Booking details differ from the accepted quote. Issue and obtain approval for a revised quote.");
        }
        OperationResult<EventBookingRecord> result=save(0,"manager",ref,customerName,email,phone,eventType,
                packageId,venueId,date,time,guests,notes,status,false,old.get().getPromotionCode());
        if(!result.isSuccess()||!"QUOTED".equals(status))return result;
        EventPackageRecord selectedPackage=packageRepository.findById(packageId).orElseThrow();
        EventVenueRecord selectedVenue=venues().stream().filter(v->v.getId()==venueId).findFirst().orElseThrow();
        List<EventQuoteRecord> versions=quotes(ref);
        boolean changed=versions.isEmpty()||versions.get(0).getPackageId()!=packageId
                ||!old.get().getEventDate().equals(date.toString())
                ||!old.get().getEventTime().equals(time.toString())
                ||versions.get(0).getVenueId()!=venueId||versions.get(0).getGuestCount()!=guests
                ||!versions.get(0).getRequirements().equals(clean(notes))
                ||versions.get(0).getPricePerGuest().compareTo(selectedPackage.getPricePerGuest())!=0
                ||versions.get(0).getVenueFee().compareTo(selectedVenue.getBaseFee())!=0
                ||versions.get(0).getTotal().compareTo(result.getValue().getTotalAmount())!=0;
        if(changed){
            EventQuoteRecord issued=bookingRepository.issueQuote(ref,packageId,venueId,guests,clean(notes),
                    selectedPackage.getPricePerGuest(),selectedVenue.getBaseFee(),result.getValue().getTotalAmount());
            bookingRepository.addStatusHistory(ref,"QUOTED","Quotation version "+issued.getVersion()+" issued.");
        }
        return result;
    }
    public synchronized OperationResult<EventQuoteRecord> acceptQuote(long userId,String email,String ref,long quoteId){
        Optional<EventBookingRecord> booking=booking(ref);
        if(booking.isEmpty()||!owns(userId,email,booking.get()))
            return OperationResult.failure("Quotation was not found for this customer.");
        if(!"QUOTED".equals(booking.get().getStatus()))
            return OperationResult.failure("This booking has no quotation awaiting approval.");
        List<EventQuoteRecord> versions=quotes(ref);
        if(versions.isEmpty()||versions.get(0).getId()!=quoteId)
            return OperationResult.failure("Only the latest quotation can be accepted.");
        EventQuoteRecord latest=versions.get(0);
        if(latest.isAccepted())return OperationResult.failure("This quotation is already accepted.");
        if(latest.getGuestCount()!=booking.get().getGuestCount()
                ||latest.getPackageId()!=booking.get().getPackageId()
                ||latest.getVenueId()!=booking.get().getVenueId()
                ||latest.getTotal().compareTo(booking.get().getTotalAmount())!=0
                ||!latest.getRequirements().equals(booking.get().getNotes()))
            return OperationResult.failure("Booking requirements have changed. Ask for a revised quotation.");
        if(!bookingRepository.acceptQuote(ref,quoteId,userId))
            return OperationResult.failure("Quotation approval could not be saved.");
        bookingRepository.addStatusHistory(ref,"QUOTED","Customer accepted quotation version "+latest.getVersion()+".");
        return OperationResult.success(quotes(ref).get(0));
    }
    public OperationResult<EventBookingRecord> cancelByCustomer(long userId,String email,String ref,String reason){
        Optional<EventBookingRecord> o=booking(ref); if(o.isEmpty())return OperationResult.failure("Event booking was not found.");
        EventBookingRecord b=o.get();
        if(!owns(userId,email,b))return OperationResult.failure("You are not allowed to cancel this booking.");
        if("CANCELLED".equals(b.getStatus())||"COMPLETED".equals(b.getStatus()))return OperationResult.failure("This booking can no longer be cancelled.");
        if(reason==null||reason.trim().length()<5)return OperationResult.failure("Enter a short cancellation reason.");
        if(reason.trim().length()>500)return OperationResult.failure("Cancellation reason cannot exceed 500 characters.");
        if(!bookingRepository.cancel(ref,reason.trim()))return OperationResult.failure("The booking could not be cancelled.");
        return booking(ref).map(OperationResult::success).orElse(OperationResult.failure("Booking was cancelled but could not be reloaded."));
    }
    public OperationResult<Void> delete(String ref){
        if(!bookingRepository.delete(ref))return OperationResult.failure("Event booking was not found.");
        return OperationResult.success(null);
    }
    private OperationResult<EventBookingRecord> save(long userId,String actor,String ref,String customerName,String email,String phone,
                                                     String eventType,long packageId,long venueId,LocalDate date,LocalTime time,int guests,String notes,
                                                     String status,boolean creating,String promotionCode){
        List<String> e=new ArrayList<>();
        customerName=clean(customerName);email=clean(email);phone=clean(phone);eventType=clean(eventType);notes=clean(notes);
        promotionCode=clean(promotionCode).toUpperCase(Locale.ROOT);
        EventBookingRecord previous=creating?null:booking(ref).orElse(null);
        // These rules protect direct requests as well as normal browser submissions.
        if(customerName.length()<2||customerName.length()>160)e.add("Enter a valid contact name.");
        if(email.length()>160||!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))e.add("Enter a valid email address.");
        if(!phone.matches("^(?:\\+94|0)7\\d{8}$"))e.add("Enter a valid Sri Lankan mobile number.");
        if(eventType.isEmpty()||eventType.length()>100)e.add("Enter a valid event type.");
        if(date==null)e.add("Select a valid event date.");
        else if(date.isBefore(LocalDate.now().plusDays(7))
                &&(creating||previous==null||!previous.getEventDate().equals(date.toString())))
            e.add("Event bookings must be requested at least 7 days in advance.");
        if(time==null)e.add("Select a valid event time.");
        if(guests<1)e.add("Guest count must be greater than zero.");
        if(notes.length()>5000)e.add("Requirements cannot exceed 5000 characters.");
        if(!promotionCode.isEmpty()&&!promotionCode.matches("[A-Z0-9_-]{1,40}"))e.add("Select a valid promotion code.");
        EventPackageRecord p=packageRepository.findById(packageId).orElse(null);
        EventVenueRecord v=venues().stream().filter(x->x.getId()==venueId).findFirst().orElse(null);
        if(p==null||(!p.isActive()&&(creating||previous==null||previous.getPackageId()!=packageId)))
            e.add("Select an available event package.");
        if(v==null||(!"AVAILABLE".equals(v.getStatus())
                &&(creating||previous==null||previous.getVenueId()!=venueId)))
            e.add("Select an available event venue.");
        if(p!=null&&(guests<p.getMinimumGuests()||guests>p.getMaximumGuests()))e.add("Guest count must be between "+p.getMinimumGuests()+" and "+p.getMaximumGuests()+" for this package.");
        if(v!=null&&guests>v.getCapacity())e.add("Guest count exceeds the selected venue capacity.");
        if(!e.isEmpty())return OperationResult.failure(e);
        if(bookingRepository.hasConflict(packageId,venueId,date,time,p.getDurationMinutes(),ref==null?"":ref))
            e.add("The selected package and venue are already booked for that time.");
        if(!e.isEmpty())return OperationResult.failure(e);
        BigDecimal total=previous!=null&&"CONFIRMED".equals(previous.getStatus())
                ?previous.getTotalAmount()
                :p.getPricePerGuest().multiply(BigDecimal.valueOf(guests)).add(v.getBaseFee());
        Long cid=bookingRepository.customerIdForUser(userId);
        long customerId=creating?(cid==null?0:cid):previous.getCustomerId();
        if(ref==null)ref="DV-E-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT);
        long id=creating?bookingRepository.nextId():previous.getId();
        EventBookingRecord b=new EventBookingRecord(id,ref,customerId,packageId,venueId,customerName,email,phone,eventType,p.getName(),v.getName(),
                date.toString(),time.toString(),guests,total,status,notes,promotionCode);
        String previousStatus=creating?null:previous.getStatus();
        EventBookingRecord saved=creating?bookingRepository.save(b):bookingRepository.update(b);
        if(creating)bookingRepository.addStatusHistory(ref,status,"Booking created by customer.");
        else if(previousStatus!=null&&!previousStatus.equals(status))
            bookingRepository.addStatusHistory(ref,status,"Status updated by "+actor+".");
        return OperationResult.success(saved);
    }
    private boolean validTransition(String from,String to){
        if(from.equals(to))return true;
        if("INQUIRY".equals(from))return Arrays.asList("CONSULTATION","QUOTED","CANCELLED").contains(to);
        if("CONSULTATION".equals(from))return Arrays.asList("QUOTED","CANCELLED").contains(to);
        if("QUOTED".equals(from))return Arrays.asList("CONFIRMED","CANCELLED").contains(to);
        if("CONFIRMED".equals(from))return Arrays.asList("COMPLETED","CANCELLED").contains(to);
        return false;
    }
    public List<String> statuses(){return STATUSES;}
    private static String clean(String s){return s==null?"":s.trim();}
}
