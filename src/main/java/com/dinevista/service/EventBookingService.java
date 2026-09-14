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

    public boolean owns(long userId,String email,EventBookingRecord b){
        Long cid=bookingRepository.customerIdForUser(userId);
        return (cid!=null&&cid>0&&b.getCustomerId()==cid)||(email!=null&&b.getEmail().equalsIgnoreCase(email));
    }
    public OperationResult<EventBookingRecord> create(long userId,String customerName,String email,String phone,String eventType,
                                                       long packageId,long venueId,LocalDate date,LocalTime time,int guests,String notes){
        return save(userId,"customer",null,customerName,email,phone,eventType,packageId,venueId,date,time,guests,notes,"INQUIRY",true);
    }
    public OperationResult<EventBookingRecord> customerUpdate(long userId,String email,String ref,String customerName,String customerEmail,
                                                               String phone,String eventType,long packageId,long venueId,LocalDate date,LocalTime time,int guests,String notes){
        Optional<EventBookingRecord> existing=booking(ref);
        if(existing.isEmpty())return OperationResult.failure("Event booking was not found.");
        Long cid=bookingRepository.customerIdForUser(userId);
        EventBookingRecord old=existing.get();
        boolean owner=(cid!=null&&cid>0&&old.getCustomerId()==cid)||(old.getEmail().equalsIgnoreCase(email));
        if(!owner)return OperationResult.failure("You are not allowed to update this booking.");
        if("CONFIRMED".equals(old.getStatus())||"COMPLETED".equals(old.getStatus())||"CANCELLED".equals(old.getStatus()))
            return OperationResult.failure("This booking can no longer be edited.");
        return save(userId,"customer",ref,customerName,customerEmail,phone,eventType,packageId,venueId,date,time,guests,notes,old.getStatus(),false);
    }
    public OperationResult<EventBookingRecord> managerUpdate(String ref,String customerName,String email,String phone,String eventType,
                                                              long packageId,long venueId,LocalDate date,LocalTime time,int guests,String notes,String status){
        Optional<EventBookingRecord> old=booking(ref);
        if(old.isEmpty())return OperationResult.failure("Event booking was not found.");
        if(!STATUSES.contains(status))return OperationResult.failure("Select a valid booking status.");
        if("CANCELLED".equals(status)&&notes.trim().isEmpty())return OperationResult.failure("Add a note when cancelling a booking.");
        return save(0,"manager",ref,customerName,email,phone,eventType,packageId,venueId,date,time,guests,notes,status,false);
    }
    public OperationResult<EventBookingRecord> cancelByCustomer(long userId,String email,String ref,String reason){
        Optional<EventBookingRecord> o=booking(ref); if(o.isEmpty())return OperationResult.failure("Event booking was not found.");
        EventBookingRecord b=o.get(); Long cid=bookingRepository.customerIdForUser(userId);
        boolean owner=(cid!=null&&cid>0&&b.getCustomerId()==cid)||(b.getEmail().equalsIgnoreCase(email));
        if(!owner)return OperationResult.failure("You are not allowed to cancel this booking.");
        if("CANCELLED".equals(b.getStatus())||"COMPLETED".equals(b.getStatus()))return OperationResult.failure("This booking can no longer be cancelled.");
        if(reason==null||reason.trim().length()<5)return OperationResult.failure("Enter a short cancellation reason.");
        if(!bookingRepository.cancel(ref,reason.trim()))return OperationResult.failure("The booking could not be cancelled.");
        return booking(ref).map(OperationResult::success).orElse(OperationResult.failure("Booking was cancelled but could not be reloaded."));
    }
    public OperationResult<Void> delete(String ref){
        if(!bookingRepository.delete(ref))return OperationResult.failure("Event booking was not found.");
        return OperationResult.success(null);
    }
    private OperationResult<EventBookingRecord> save(long userId,String actor,String ref,String customerName,String email,String phone,
                                                     String eventType,long packageId,long venueId,LocalDate date,LocalTime time,int guests,String notes,
                                                     String status,boolean creating){
        List<String> e=new ArrayList<>();
        customerName=clean(customerName);email=clean(email);phone=clean(phone);eventType=clean(eventType);notes=clean(notes);
        if(customerName.length()<2||customerName.length()>160)e.add("Enter a valid contact name.");
        if(!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))e.add("Enter a valid email address.");
        if(!phone.matches("^(?:\\+94|0)7\\d{8}$"))e.add("Enter a valid Sri Lankan mobile number.");
        if(eventType.isEmpty()||eventType.length()>100)e.add("Enter a valid event type.");
        if(date==null)e.add("Select a valid event date.");
        else if(date.isBefore(LocalDate.now().plusDays(7)))e.add("Event bookings must be requested at least 7 days in advance.");
        if(time==null)e.add("Select a valid event time.");
        if(guests<1)e.add("Guest count must be greater than zero.");
        if(notes.length()>5000)e.add("Requirements cannot exceed 5000 characters.");
        EventPackageRecord p=packageRepository.findById(packageId).orElse(null);
        EventVenueRecord v=venues().stream().filter(x->x.getId()==venueId).findFirst().orElse(null);
        if(p==null||!p.isActive())e.add("Select an available event package.");
        if(v==null||!"AVAILABLE".equals(v.getStatus()))e.add("Select an available event venue.");
        if(p!=null&&(guests<p.getMinimumGuests()||guests>p.getMaximumGuests()))e.add("Guest count must be between "+p.getMinimumGuests()+" and "+p.getMaximumGuests()+" for this package.");
        if(v!=null&&guests>v.getCapacity())e.add("Guest count exceeds the selected venue capacity.");
        if(!e.isEmpty())return OperationResult.failure(e);
        if(bookingRepository.hasConflict(packageId,venueId,date,time,p.getDurationMinutes(),ref==null?"":ref))
            e.add("The selected package and venue are already booked for that time.");
        if(!e.isEmpty())return OperationResult.failure(e);
        BigDecimal total=p.getPricePerGuest().multiply(BigDecimal.valueOf(guests)).add(v.getBaseFee());
        Long cid=bookingRepository.customerIdForUser(userId);
        long customerId=cid==null?0:cid;
        if(ref==null)ref="DV-E-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT);
        long id=creating?bookingRepository.nextId():booking(ref).get().getId();
        EventBookingRecord b=new EventBookingRecord(id,ref,customerId,packageId,venueId,customerName,email,phone,eventType,p.getName(),v.getName(),
                date.toString(),time.toString(),guests,total,status,notes);
        EventBookingRecord saved=creating?bookingRepository.save(b):bookingRepository.update(b);
        if(creating)bookingRepository.addStatusHistory(ref,status,"Booking created by customer.");
        return OperationResult.success(saved);
    }
    private boolean validTransition(String from,String to){
        if(from.equals(to))return true;
        if("INQUIRY".equals(from))return Arrays.asList("CONSULTATION","QUOTED","CANCELLED").contains(to);
        if("CONSULTATION".equals(from))return Arrays.asList("QUOTED","CONFIRMED","CANCELLED").contains(to);
        if("QUOTED".equals(from))return Arrays.asList("CONFIRMED","CANCELLED").contains(to);
        if("CONFIRMED".equals(from))return Arrays.asList("COMPLETED","CANCELLED").contains(to);
        return false;
    }
    public List<String> statuses(){return STATUSES;}
    private static String clean(String s){return s==null?"":s.trim();}
}
