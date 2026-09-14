package com.dinevista.repository;

import com.dinevista.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryEventBookingRepository implements EventBookingRepository {
    private final List<EventBookingRecord> bookings=new CopyOnWriteArrayList<>();
    private final AtomicLong ids=new AtomicLong(1000);
    @Override public EventBookingRecord save(EventBookingRecord b){bookings.add(0,b);return b;}
    @Override public EventBookingRecord update(EventBookingRecord b){bookings.removeIf(x->x.getReference().equals(b.getReference()));bookings.add(0,b);return b;}
    @Override public boolean cancel(String ref,String note){
        Optional<EventBookingRecord> found=findByReference(ref); if(found.isEmpty()) return false;
        EventBookingRecord b=found.get();
        update(new EventBookingRecord(b.getId(),b.getReference(),b.getCustomerId(),b.getPackageId(),b.getVenueId(),b.getCustomerName(),
                b.getEmail(),b.getPhone(),b.getEventType(),b.getPackageName(),b.getVenue(),b.getEventDate(),b.getEventTime(),
                b.getGuestCount(),b.getTotalAmount(),"CANCELLED",(b.getNotes()+" | Cancellation: "+(note==null?"Customer cancelled":note)).trim()));
        return true;
    }
    @Override public boolean delete(String ref){return bookings.removeIf(b->b.getReference().equals(ref));}
    @Override public Optional<EventBookingRecord> findByReference(String ref){return bookings.stream().filter(b->b.getReference().equalsIgnoreCase(ref)).findFirst();}
    @Override public List<EventBookingRecord> findForCustomer(long customerId,String email){
        String e=email==null?"":email.toLowerCase();
        return bookings.stream().filter(b->(customerId>0&&b.getCustomerId()==customerId)||b.getEmail().equalsIgnoreCase(e))
                .sorted(Comparator.comparing(EventBookingRecord::getReference).reversed()).collect(Collectors.toList());
    }
    @Override public List<EventBookingRecord> findAll(String search){
        String q=search==null?"":search.trim().toLowerCase();
        return bookings.stream().filter(b->q.isEmpty()||b.getReference().toLowerCase().contains(q)||b.getCustomerName().toLowerCase().contains(q)
                ||b.getPackageName().toLowerCase().contains(q)||b.getStatus().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }
    @Override public boolean hasConflict(long packageId,long venueId,LocalDate date,LocalTime time,int duration,String excluding){
        if(time==null)return false;
        int start=time.toSecondOfDay()/60, end=start+duration;
        return bookings.stream().filter(b->!b.getReference().equalsIgnoreCase(excluding))
                .filter(b->b.getPackageId()==packageId&&b.getVenueId()==venueId&&b.getEventDate().equals(date.toString()))
                .filter(b->!"CANCELLED".equals(b.getStatus())).anyMatch(b->{
                    int s; try{s=LocalTime.parse(b.getEventTime()).toSecondOfDay()/60;}catch(Exception e){s=0;}
                    int d=120; return s<end && start<s+d;
                });
    }
    @Override public List<EventVenueRecord> findVenues(){return Arrays.asList(
        new EventVenueRecord(1,"Garden Pavilion","OUTDOOR",220,new BigDecimal("150000"),"Landscaped outdoor event venue.","AVAILABLE"),
        new EventVenueRecord(2,"Vista Grand Hall","INDOOR",350,new BigDecimal("250000"),"Climate-controlled hall with stage.","AVAILABLE"),
        new EventVenueRecord(3,"Private Dining Suite","PRIVATE_ROOM",40,new BigDecimal("60000"),"Private room for intimate events.","AVAILABLE")); }
    @Override public Long customerIdForUser(long userId){return userId>0?userId:null;}
    @Override public void addStatusHistory(String reference,String status,String note) { }
    @Override public long nextId(){return ids.incrementAndGet();}
}
