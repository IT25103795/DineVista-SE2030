package com.dinevista.repository;

import com.dinevista.model.EventPackageRecord;
import com.dinevista.util.DatabaseConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEventPackageRepository implements EventPackageRepository {
    private final DatabaseConfig config;
    public JdbcEventPackageRepository(DatabaseConfig config) throws SQLException {
        this.config=config;
        try(Connection c=config.openConnection(); PreparedStatement s=c.prepareStatement("SELECT 1 FROM event_package LIMIT 1")) { s.executeQuery(); }
    }
    @Override public EventPackageRecord save(EventPackageRecord r) {
        String sql="INSERT INTO event_package (package_name,event_category,description,base_price_per_guest,minimum_guests,maximum_guests,duration_minutes,inclusions,is_active) VALUES (?,?,?,?,?,?,?,?,?)";
        try(Connection c=config.openConnection(); PreparedStatement s=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            bind(s,r,false); s.executeUpdate();
            try(ResultSet k=s.getGeneratedKeys()){ if(!k.next()) throw new SQLException("No package ID generated."); return withId(r,k.getLong(1)); }
        } catch(SQLException e){ throw failure("Unable to create event package.",e); }
    }
    @Override public EventPackageRecord update(EventPackageRecord r) {
        String sql="UPDATE event_package SET package_name=?,event_category=?,description=?,base_price_per_guest=?,minimum_guests=?,maximum_guests=?,duration_minutes=?,inclusions=?,is_active=? WHERE package_id=?";
        try(Connection c=config.openConnection(); PreparedStatement s=c.prepareStatement(sql)) {
            bind(s,r,true); if(s.executeUpdate()!=1) throw new SQLException("Event package was not found."); return r;
        } catch(SQLException e){ throw failure("Unable to update event package.",e); }
    }
    @Override public boolean deactivate(long id) {
        try(Connection c=config.openConnection(); PreparedStatement s=c.prepareStatement("UPDATE event_package SET is_active=FALSE WHERE package_id=?")){
            s.setLong(1,id); return s.executeUpdate()==1;
        } catch(SQLException e){ throw failure("Unable to deactivate event package.",e); }
    }
    @Override public boolean delete(long id) {
        try(Connection c=config.openConnection(); PreparedStatement s=c.prepareStatement("DELETE FROM event_package WHERE package_id=?")){
            s.setLong(1,id); return s.executeUpdate()==1;
        } catch(SQLException e){ throw failure("Unable to delete event package. Existing bookings may reference it; deactivate it instead.",e); }
    }
    @Override public Optional<EventPackageRecord> findById(long id) {
        try(Connection c=config.openConnection(); PreparedStatement s=c.prepareStatement("SELECT * FROM event_package WHERE package_id=?")){
            s.setLong(1,id); try(ResultSet r=s.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}
        } catch(SQLException e){throw failure("Unable to read event package.",e);}
    }
    @Override public List<EventPackageRecord> findAll(boolean activeOnly,String search) {
        String sql="SELECT * FROM event_package WHERE (?=FALSE OR is_active=TRUE) AND (LOWER(package_name) LIKE ? OR LOWER(event_category) LIKE ? OR LOWER(COALESCE(description,'')) LIKE ?) ORDER BY package_name";
        String q="%"+(search==null?"":search.trim().toLowerCase())+"%";
        List<EventPackageRecord> out=new ArrayList<>();
        try(Connection c=config.openConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setBoolean(1,activeOnly); s.setString(2,q); s.setString(3,q); s.setString(4,q);
            try(ResultSet r=s.executeQuery()){while(r.next())out.add(map(r));} return out;
        } catch(SQLException e){throw failure("Unable to list event packages.",e);}
    }
    @Override public long nextId(){return 0;}
    private static void bind(PreparedStatement s,EventPackageRecord r,boolean update)throws SQLException{
        int i=1;s.setString(i++,r.getName());s.setString(i++,r.getCategory());s.setString(i++,r.getDescription());
        s.setBigDecimal(i++,r.getPricePerGuest());s.setInt(i++,r.getMinimumGuests());s.setInt(i++,r.getMaximumGuests());
        s.setInt(i++,r.getDurationMinutes());s.setString(i++,r.getInclusions());s.setBoolean(i++,r.isActive());
        if(update)s.setLong(i,r.getId());
    }
    private static EventPackageRecord map(ResultSet r)throws SQLException{
        return new EventPackageRecord(r.getLong("package_id"),r.getString("package_name"),r.getString("event_category"),
                r.getString("description"),r.getBigDecimal("base_price_per_guest"),r.getInt("minimum_guests"),
                r.getInt("maximum_guests"),r.getInt("duration_minutes"),r.getString("inclusions"),r.getBoolean("is_active"));
    }
    private static EventPackageRecord withId(EventPackageRecord r,long id){return new EventPackageRecord(id,r.getName(),r.getCategory(),r.getDescription(),r.getPricePerGuest(),r.getMinimumGuests(),r.getMaximumGuests(),r.getDurationMinutes(),r.getInclusions(),r.isActive());}
    private static IllegalStateException failure(String m,SQLException e){return new IllegalStateException(m+" "+e.getMessage(),e);}
}
