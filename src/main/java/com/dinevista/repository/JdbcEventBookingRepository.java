package com.dinevista.repository;

import com.dinevista.model.EventBookingRecord;
import com.dinevista.model.EventVenueRecord;
import com.dinevista.util.DatabaseConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class JdbcEventBookingRepository implements EventBookingRepository {
    private final DatabaseConfig config;
    public JdbcEventBookingRepository(DatabaseConfig config)throws SQLException{
        this.config=config;
        try(Connection c=config.openConnection();PreparedStatement s=c.prepareStatement("SELECT 1 FROM event_booking LIMIT 1")){s.executeQuery();}
    }
    @Override public EventBookingRecord save(EventBookingRecord b){
        String sql="INSERT INTO event_booking (event_reference,customer_id,package_id,venue_id,contact_name,email,phone,event_type,event_date,event_time,guest_count,requirements_summary,booking_status,estimated_amount) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        return write(b,sql,false);
    }
    @Override public EventBookingRecord update(EventBookingRecord b){
        String sql="UPDATE event_booking SET customer_id=?,package_id=?,venue_id=?,contact_name=?,email=?,phone=?,event_type=?,event_date=?,event_time=?,guest_count=?,requirements_summary=?,booking_status=?,estimated_amount=? WHERE event_reference=?";
        return write(b,sql,true);
    }
    private EventBookingRecord write(EventBookingRecord b,String sql,boolean update){
        try(Connection c=config.openConnection();PreparedStatement s=c.prepareStatement(sql,update?Statement.NO_GENERATED_KEYS:Statement.RETURN_GENERATED_KEYS)){
            int i=1;
            if(update){s.setLong(i++,b.getCustomerId());s.setLong(i++,b.getPackageId());s.setLong(i++,b.getVenueId());}
            else {s.setString(i++,b.getReference()); if(b.getCustomerId()>0)s.setLong(i++,b.getCustomerId());else s.setNull(i++,Types.BIGINT);
                s.setLong(i++,b.getPackageId());s.setLong(i++,b.getVenueId());}
            s.setString(i++,b.getCustomerName());s.setString(i++,b.getEmail());s.setString(i++,b.getPhone());s.setString(i++,b.getEventType());
            s.setDate(i++,java.sql.Date.valueOf(b.getEventDate()));s.setTime(i++,Time.valueOf(b.getEventTime()));s.setInt(i++,b.getGuestCount());
            s.setString(i++,b.getNotes());s.setString(i++,b.getStatus());s.setBigDecimal(i++,b.getTotalAmount());
            if(update)s.setString(i,b.getReference());
            if(s.executeUpdate()!=1)throw new SQLException("Event booking was not found or could not be saved.");
            return b;
        }catch(SQLException e){throw failure(update?"Unable to update event booking.":"Unable to create event booking.",e);}
    }
    @Override public boolean cancel(String ref,String note){
        try(Connection c=config.openConnection()){c.setAutoCommit(false);
            try(PreparedStatement s=c.prepareStatement("UPDATE event_booking SET booking_status='CANCELLED',requirements_summary=CONCAT(COALESCE(requirements_summary,''),' | Cancellation: ',?) WHERE event_reference=?")){
                s.setString(1,note==null?"Customer cancelled":note);s.setString(2,ref);
                if(s.executeUpdate()!=1)throw new SQLException("Booking was not found.");
            }
            addHistory(c,ref,"CANCELLED",note);c.commit();return true;
        }catch(SQLException e){throw failure("Unable to cancel event booking.",e);}
    }
    @Override public boolean delete(String ref){
        try(Connection c=config.openConnection();PreparedStatement s=c.prepareStatement("DELETE FROM event_booking WHERE event_reference=?")){
            s.setString(1,ref);return s.executeUpdate()==1;
        }catch(SQLException e){throw failure("Unable to delete event booking. It may already be linked to another record.",e);}
    }
    @Override public Optional<EventBookingRecord> findByReference(String ref){return query("SELECT eb.*,ep.package_name,ev.venue_name FROM event_booking eb LEFT JOIN event_package ep ON ep.package_id=eb.package_id LEFT JOIN event_venue ev ON ev.venue_id=eb.venue_id WHERE eb.event_reference=? LIMIT 1",ref);}
    @Override public List<EventBookingRecord> findForCustomer(long customerId,String email){
        String sql="SELECT eb.*,ep.package_name,ev.venue_name FROM event_booking eb LEFT JOIN event_package ep ON ep.package_id=eb.package_id LEFT JOIN event_venue ev ON ev.venue_id=eb.venue_id WHERE (eb.customer_id=? OR (eb.customer_id IS NULL AND LOWER(eb.email)=LOWER(?))) ORDER BY eb.event_date DESC,eb.event_time DESC";
        return list(sql,customerId,email);
    }
    @Override public List<EventBookingRecord> findAll(String search){
        String q="%"+(search==null?"":search.trim().toLowerCase())+"%";
        String sql="SELECT eb.*,ep.package_name,ev.venue_name FROM event_booking eb LEFT JOIN event_package ep ON ep.package_id=eb.package_id LEFT JOIN event_venue ev ON ev.venue_id=eb.venue_id WHERE LOWER(eb.event_reference) LIKE ? OR LOWER(eb.contact_name) LIKE ? OR LOWER(COALESCE(ep.package_name,'')) LIKE ? OR LOWER(eb.booking_status) LIKE ? ORDER BY eb.event_date DESC,eb.event_time DESC";
        return list(sql,q,q,q,q);
    }
    @Override public boolean hasConflict(long packageId,long venueId,LocalDate date,LocalTime time,int duration,String excluding){
        String sql="SELECT 1 FROM event_booking eb JOIN event_package ep ON ep.package_id=eb.package_id WHERE eb.package_id=? AND eb.venue_id=? AND eb.event_date=? AND eb.booking_status<>'CANCELLED' AND (?='' OR eb.event_reference<>?) AND TIME_TO_SEC(eb.event_time) < TIME_TO_SEC(?) + ?*60 AND TIME_TO_SEC(eb.event_time) + ep.duration_minutes*60 > TIME_TO_SEC(?) LIMIT 1";
        try(Connection c=config.openConnection();PreparedStatement s=c.prepareStatement(sql)){
            int i=1;s.setLong(i++,packageId);s.setLong(i++,venueId);s.setDate(i++,java.sql.Date.valueOf(date));s.setString(i++,excluding==null?"":excluding);s.setString(i++,excluding==null?"":excluding);
            s.setTime(i++,Time.valueOf(time));s.setInt(i++,duration);s.setTime(i,Time.valueOf(time));
            try(ResultSet r=s.executeQuery()){return r.next();}
        }catch(SQLException e){throw failure("Unable to check event availability.",e);}
    }
    @Override public List<EventVenueRecord> findVenues(){
        List<EventVenueRecord> out=new ArrayList<>();
        try(Connection c=config.openConnection();PreparedStatement s=c.prepareStatement("SELECT * FROM event_venue ORDER BY venue_name");ResultSet r=s.executeQuery()){
            while(r.next())out.add(new EventVenueRecord(r.getLong("venue_id"),r.getString("venue_name"),r.getString("venue_type"),r.getInt("capacity"),r.getBigDecimal("base_fee"),r.getString("description"),r.getString("availability_status")));
            return out;
        }catch(SQLException e){throw failure("Unable to list event venues.",e);}
    }
    @Override public Long customerIdForUser(long userId){
        try(Connection c=config.openConnection();PreparedStatement s=c.prepareStatement("SELECT customer_id FROM customer_profile WHERE user_id=?")){
            s.setLong(1,userId);try(ResultSet r=s.executeQuery()){return r.next()?r.getLong(1):null;}
        }catch(SQLException e){throw failure("Unable to identify the customer profile.",e);}
    }
    @Override public void addStatusHistory(String reference,String status,String note){
        String sql="INSERT INTO event_booking_status_history(event_booking_id,status,note) SELECT event_booking_id,?,? FROM event_booking WHERE event_reference=?";
        try(Connection c=config.openConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setString(1,status);s.setString(2,note==null?"":note);s.setString(3,reference);s.executeUpdate();
        }catch(SQLException e){throw failure("Unable to record booking status history.",e);}
    }
    @Override public long nextId(){return 0;}
    private Optional<EventBookingRecord> query(String sql,String ref){List<EventBookingRecord> l=list(sql,ref);return l.stream().findFirst();}
    private List<EventBookingRecord> list(String sql,Object...args){
        List<EventBookingRecord> out=new ArrayList<>();
        try(Connection c=config.openConnection();PreparedStatement s=c.prepareStatement(sql)){int i=1;for(Object a:args){if(a instanceof Long)s.setLong(i++,(Long)a);else s.setString(i++,String.valueOf(a));}
            try(ResultSet r=s.executeQuery()){while(r.next())out.add(map(r));}return out;
        }catch(SQLException e){throw failure("Unable to read event bookings.",e);}
    }
    private static EventBookingRecord map(ResultSet r)throws SQLException{
        Time t=r.getTime("event_time");String time=t==null?"":t.toLocalTime().toString();
        return new EventBookingRecord(r.getLong("event_booking_id"),r.getString("event_reference"),
            r.getLong("customer_id"),r.getLong("package_id"),r.getLong("venue_id"),r.getString("contact_name"),
            r.getString("email"),r.getString("phone"),r.getString("event_type"),r.getString("package_name"),
            r.getString("venue_name"),r.getDate("event_date").toLocalDate().toString(),time,r.getInt("guest_count"),
            r.getBigDecimal("estimated_amount"),r.getString("booking_status"),r.getString("requirements_summary"));
    }
    private static void addHistory(Connection c,String ref,String status,String note)throws SQLException{
        String sql="INSERT INTO event_booking_status_history(event_booking_id,status,note) SELECT event_booking_id,?,? FROM event_booking WHERE event_reference=?";
        try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,status);s.setString(2,note);s.setString(3,ref);s.executeUpdate();}
    }
    private static IllegalStateException failure(String m,SQLException e){return new IllegalStateException(m+" "+e.getMessage(),e);}
}
