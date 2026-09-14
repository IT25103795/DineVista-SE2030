package com.dinevista.repository;

import com.dinevista.model.EventResourceRecord;
import com.dinevista.model.EventStaffAssignmentRecord;
import com.dinevista.model.EventVenueBookingRecord;
import com.dinevista.model.EventVenueRecord;
import com.dinevista.model.ResourceBookingRecord;
import com.dinevista.model.StaffMemberRecord;
import com.dinevista.model.StaffScheduleRecord;
import com.dinevista.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL-backed {@link EventOperationsRepository} for the Event Resource and Staff
 * Scheduling Management module (Wijesuriya W. A. T. D. / IT25103799). Persists against
 * event_venue, event_resource, event_venue_booking, resource_booking, staff_profile,
 * staff_schedule, and event_staff_assignment in database/schema.sql, following the
 * same connection and error-handling pattern as {@link JdbcInventoryRepository}.
 *
 * The staff roster itself is created through account registration (see
 * {@link JdbcAccountRepository}); this repository only reads staff_profile joined with
 * user_account, and updates availability_status.
 */
public class JdbcEventOperationsRepository implements EventOperationsRepository {
    private final DatabaseConfig config;

    public JdbcEventOperationsRepository(DatabaseConfig config) throws SQLException {
        this.config = config;
        try (Connection ignored = config.openConnection()) {
            // Fail early so the application can safely fall back to memory mode.
        }
    }

    // ---------------------------------------------------------------- Venues

    @Override
    public List<EventVenueRecord> findAllVenues() {
        String sql = "SELECT venue_id, venue_name, venue_type, capacity, base_fee, description, "
                + "availability_status FROM event_venue ORDER BY venue_name";
        List<EventVenueRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapVenue(rows));
        } catch (SQLException ex) {
            throw failure("Unable to load venues.", ex);
        }
        return result;
    }

    @Override
    public Optional<EventVenueRecord> findVenue(long id) {
        String sql = "SELECT venue_id, venue_name, venue_type, capacity, base_fee, description, "
                + "availability_status FROM event_venue WHERE venue_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapVenue(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to load venue.", ex);
        }
    }

    @Override
    public Optional<EventVenueRecord> findVenueByName(String name) {
        String sql = "SELECT venue_id, venue_name, venue_type, capacity, base_fee, description, "
                + "availability_status FROM event_venue WHERE venue_name = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapVenue(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to load venue.", ex);
        }
    }

    @Override
    public EventVenueRecord saveVenue(EventVenueRecord venue) {
        try (Connection connection = config.openConnection()) {
            if (exists(connection, "event_venue", "venue_id", venue.getId())) {
                String sql = "UPDATE event_venue SET venue_name=?, venue_type=?, capacity=?, base_fee=?, "
                        + "description=?, availability_status=? WHERE venue_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, venue.getName());
                    statement.setString(2, venue.getVenueType());
                    statement.setInt(3, venue.getCapacity());
                    statement.setBigDecimal(4, venue.getBaseFee());
                    statement.setString(5, venue.getDescription());
                    statement.setString(6, venue.getAvailabilityStatus());
                    statement.setLong(7, venue.getId());
                    statement.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO event_venue (venue_id, venue_name, venue_type, capacity, base_fee, "
                        + "description, availability_status) VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, venue.getId());
                    statement.setString(2, venue.getName());
                    statement.setString(3, venue.getVenueType());
                    statement.setInt(4, venue.getCapacity());
                    statement.setBigDecimal(5, venue.getBaseFee());
                    statement.setString(6, venue.getDescription());
                    statement.setString(7, venue.getAvailabilityStatus());
                    statement.executeUpdate();
                }
            }
            return venue;
        } catch (SQLException ex) {
            throw failure("Unable to save venue.", ex);
        }
    }

    @Override
    public boolean deleteVenue(long id) {
        String sql = "DELETE FROM event_venue WHERE venue_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw failure("Unable to delete venue.", ex);
        }
    }

    @Override
    public boolean hasVenueBookings(long venueId) {
        return countWhere("event_venue_booking", "venue_id", venueId) > 0;
    }

    @Override
    public long nextVenueId() { return nextId("event_venue", "venue_id"); }

    // -------------------------------------------------------------- Resources

    @Override
    public List<EventResourceRecord> findAllResources() {
        String sql = "SELECT resource_id, resource_name, resource_category, total_quantity, "
                + "available_quantity, unit_cost, resource_status FROM event_resource ORDER BY resource_name";
        List<EventResourceRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapResource(rows));
        } catch (SQLException ex) {
            throw failure("Unable to load resources.", ex);
        }
        return result;
    }

    @Override
    public Optional<EventResourceRecord> findResource(long id) {
        String sql = "SELECT resource_id, resource_name, resource_category, total_quantity, "
                + "available_quantity, unit_cost, resource_status FROM event_resource WHERE resource_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapResource(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to load resource.", ex);
        }
    }

    @Override
    public Optional<EventResourceRecord> findResourceByName(String name) {
        String sql = "SELECT resource_id, resource_name, resource_category, total_quantity, "
                + "available_quantity, unit_cost, resource_status FROM event_resource WHERE resource_name = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapResource(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to load resource.", ex);
        }
    }

    @Override
    public EventResourceRecord saveResource(EventResourceRecord resource) {
        try (Connection connection = config.openConnection()) {
            if (exists(connection, "event_resource", "resource_id", resource.getId())) {
                String sql = "UPDATE event_resource SET resource_name=?, resource_category=?, total_quantity=?, "
                        + "available_quantity=?, unit_cost=?, resource_status=? WHERE resource_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, resource.getName());
                    statement.setString(2, resource.getCategory());
                    statement.setInt(3, resource.getTotalQuantity());
                    statement.setInt(4, resource.getAvailableQuantity());
                    statement.setBigDecimal(5, resource.getUnitCost());
                    statement.setString(6, resource.getStatus());
                    statement.setLong(7, resource.getId());
                    statement.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO event_resource (resource_id, resource_name, resource_category, "
                        + "total_quantity, available_quantity, unit_cost, resource_status) VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, resource.getId());
                    statement.setString(2, resource.getName());
                    statement.setString(3, resource.getCategory());
                    statement.setInt(4, resource.getTotalQuantity());
                    statement.setInt(5, resource.getAvailableQuantity());
                    statement.setBigDecimal(6, resource.getUnitCost());
                    statement.setString(7, resource.getStatus());
                    statement.executeUpdate();
                }
            }
            return resource;
        } catch (SQLException ex) {
            throw failure("Unable to save resource.", ex);
        }
    }

    @Override
    public boolean deleteResource(long id) {
        String sql = "DELETE FROM event_resource WHERE resource_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw failure("Unable to delete resource.", ex);
        }
    }

    @Override
    public boolean hasResourceBookings(long resourceId) {
        return countWhere("resource_booking", "resource_id", resourceId) > 0;
    }

    @Override
    public long nextResourceId() { return nextId("event_resource", "resource_id"); }

    // ---------------------------------------------------------- Venue bookings

    @Override
    public List<EventVenueBookingRecord> findVenueBookings(long venueId) {
        String sql = venueBookingSelect() + " WHERE b.venue_id = ? ORDER BY b.event_date DESC, b.start_time DESC";
        List<EventVenueBookingRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, venueId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapVenueBooking(rows));
            }
        } catch (SQLException ex) {
            throw failure("Unable to load venue bookings.", ex);
        }
        return result;
    }

    @Override
    public List<EventVenueBookingRecord> findAllVenueBookings() {
        String sql = venueBookingSelect() + " ORDER BY b.event_date DESC, b.start_time DESC";
        List<EventVenueBookingRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapVenueBooking(rows));
        } catch (SQLException ex) {
            throw failure("Unable to load venue bookings.", ex);
        }
        return result;
    }

    @Override
    public Optional<EventVenueBookingRecord> findVenueBooking(long id) {
        String sql = venueBookingSelect() + " WHERE b.venue_booking_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapVenueBooking(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to load venue booking.", ex);
        }
    }

    @Override
    public EventVenueBookingRecord saveVenueBooking(EventVenueBookingRecord booking) {
        try (Connection connection = config.openConnection()) {
            if (exists(connection, "event_venue_booking", "venue_booking_id", booking.getId())) {
                String sql = "UPDATE event_venue_booking SET booking_status=? WHERE venue_booking_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, booking.getStatus());
                    statement.setLong(2, booking.getId());
                    statement.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO event_venue_booking (venue_booking_id, venue_id, event_label, "
                        + "event_date, start_time, end_time, guest_count, booking_status, notes, "
                        + "created_by_name) VALUES (?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, booking.getId());
                    statement.setLong(2, booking.getVenueId());
                    statement.setString(3, booking.getEventLabel());
                    statement.setDate(4, Date.valueOf(booking.getEventDate()));
                    statement.setTime(5, Time.valueOf(booking.getStartTime()));
                    statement.setTime(6, Time.valueOf(booking.getEndTime()));
                    if (booking.getGuestCount() == null) statement.setNull(7, java.sql.Types.INTEGER);
                    else statement.setInt(7, booking.getGuestCount());
                    statement.setString(8, booking.getStatus());
                    statement.setString(9, booking.getNotes());
                    statement.setString(10, booking.getCreatedByName());
                    statement.executeUpdate();
                }
            }
            return booking;
        } catch (SQLException ex) {
            throw failure("Unable to save venue booking.", ex);
        }
    }

    @Override
    public long nextVenueBookingId() { return nextId("event_venue_booking", "venue_booking_id"); }

    // -------------------------------------------------------- Resource bookings

    @Override
    public List<ResourceBookingRecord> findResourceBookings(long resourceId) {
        String sql = resourceBookingSelect() + " WHERE b.resource_id = ? ORDER BY b.event_date DESC";
        List<ResourceBookingRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, resourceId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapResourceBooking(rows));
            }
        } catch (SQLException ex) {
            throw failure("Unable to load resource bookings.", ex);
        }
        return result;
    }

    @Override
    public List<ResourceBookingRecord> findAllResourceBookings() {
        String sql = resourceBookingSelect() + " ORDER BY b.event_date DESC";
        List<ResourceBookingRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapResourceBooking(rows));
        } catch (SQLException ex) {
            throw failure("Unable to load resource bookings.", ex);
        }
        return result;
    }

    @Override
    public Optional<ResourceBookingRecord> findResourceBooking(long id) {
        String sql = resourceBookingSelect() + " WHERE b.resource_booking_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapResourceBooking(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to load resource booking.", ex);
        }
    }

    @Override
    public ResourceBookingRecord saveResourceBooking(ResourceBookingRecord booking) {
        try (Connection connection = config.openConnection()) {
            if (exists(connection, "resource_booking", "resource_booking_id", booking.getId())) {
                String sql = "UPDATE resource_booking SET allocation_status=? WHERE resource_booking_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, booking.getStatus());
                    statement.setLong(2, booking.getId());
                    statement.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO resource_booking (resource_booking_id, resource_id, event_label, "
                        + "event_date, quantity_reserved, allocation_status, requested_by_name, notes) "
                        + "VALUES (?,?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, booking.getId());
                    statement.setLong(2, booking.getResourceId());
                    statement.setString(3, booking.getEventLabel());
                    statement.setDate(4, Date.valueOf(booking.getEventDate()));
                    statement.setInt(5, booking.getQuantityReserved());
                    statement.setString(6, booking.getStatus());
                    statement.setString(7, booking.getRequestedByName());
                    statement.setString(8, booking.getNotes());
                    statement.executeUpdate();
                }
            }
            return booking;
        } catch (SQLException ex) {
            throw failure("Unable to save resource booking.", ex);
        }
    }

    @Override
    public long nextResourceBookingId() { return nextId("resource_booking", "resource_booking_id"); }

    // -------------------------------------------------------------- Staff roster

    @Override
    public List<StaffMemberRecord> findAllStaff() {
        String sql = staffSelect() + " ORDER BY u.first_name, u.last_name";
        List<StaffMemberRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapStaff(rows));
        } catch (SQLException ex) {
            throw failure("Unable to load staff.", ex);
        }
        return result;
    }

    @Override
    public Optional<StaffMemberRecord> findStaff(long id) {
        String sql = staffSelect() + " WHERE s.staff_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapStaff(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to load staff member.", ex);
        }
    }

    @Override
    public boolean updateStaffAvailability(long staffId, String availabilityStatus) {
        String sql = "UPDATE staff_profile SET availability_status = ? WHERE staff_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, availabilityStatus);
            statement.setLong(2, staffId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw failure("Unable to update staff availability.", ex);
        }
    }

    // ------------------------------------------------------------- Shift schedule

    @Override
    public List<StaffScheduleRecord> findSchedulesForStaff(long staffId) {
        String sql = scheduleSelect() + " WHERE sc.staff_id = ? ORDER BY sc.shift_date DESC, sc.start_time DESC";
        List<StaffScheduleRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, staffId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapSchedule(rows));
            }
        } catch (SQLException ex) {
            throw failure("Unable to load shifts.", ex);
        }
        return result;
    }

    @Override
    public List<StaffScheduleRecord> findAllSchedules() {
        String sql = scheduleSelect() + " ORDER BY sc.shift_date DESC, sc.start_time DESC";
        List<StaffScheduleRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapSchedule(rows));
        } catch (SQLException ex) {
            throw failure("Unable to load shifts.", ex);
        }
        return result;
    }

    @Override
    public List<StaffScheduleRecord> findActiveSchedulesForStaffOnDate(long staffId, LocalDate date) {
        String sql = scheduleSelect()
                + " WHERE sc.staff_id = ? AND sc.shift_date = ? AND sc.schedule_status <> 'CANCELLED'";
        List<StaffScheduleRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, staffId);
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapSchedule(rows));
            }
        } catch (SQLException ex) {
            throw failure("Unable to check shift conflicts.", ex);
        }
        return result;
    }

    @Override
    public Optional<StaffScheduleRecord> findSchedule(long id) {
        String sql = scheduleSelect() + " WHERE sc.schedule_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapSchedule(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to load shift.", ex);
        }
    }

    @Override
    public StaffScheduleRecord saveSchedule(StaffScheduleRecord schedule) {
        try (Connection connection = config.openConnection()) {
            if (exists(connection, "staff_schedule", "schedule_id", schedule.getId())) {
                String sql = "UPDATE staff_schedule SET schedule_status=? WHERE schedule_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, schedule.getStatus());
                    statement.setLong(2, schedule.getId());
                    statement.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO staff_schedule (schedule_id, staff_id, shift_date, start_time, "
                        + "end_time, shift_type, schedule_status, notes) VALUES (?,?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, schedule.getId());
                    statement.setLong(2, schedule.getStaffId());
                    statement.setDate(3, Date.valueOf(schedule.getShiftDate()));
                    statement.setTime(4, Time.valueOf(schedule.getStartTime()));
                    statement.setTime(5, Time.valueOf(schedule.getEndTime()));
                    statement.setString(6, schedule.getShiftType());
                    statement.setString(7, schedule.getStatus());
                    statement.setString(8, schedule.getNotes());
                    statement.executeUpdate();
                }
            }
            return schedule;
        } catch (SQLException ex) {
            throw failure("Unable to save shift.", ex);
        }
    }

    @Override
    public long nextScheduleId() { return nextId("staff_schedule", "schedule_id"); }

    // ------------------------------------------------------ Event staff assignments

    @Override
    public List<EventStaffAssignmentRecord> findAssignmentsForStaff(long staffId) {
        String sql = assignmentSelect()
                + " WHERE a.staff_id = ? ORDER BY a.assignment_date DESC, a.start_time DESC";
        List<EventStaffAssignmentRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, staffId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapAssignment(rows));
            }
        } catch (SQLException ex) {
            throw failure("Unable to load event assignments.", ex);
        }
        return result;
    }

    @Override
    public List<EventStaffAssignmentRecord> findAllAssignments() {
        String sql = assignmentSelect() + " ORDER BY a.assignment_date DESC, a.start_time DESC";
        List<EventStaffAssignmentRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapAssignment(rows));
        } catch (SQLException ex) {
            throw failure("Unable to load event assignments.", ex);
        }
        return result;
    }

    @Override
    public List<EventStaffAssignmentRecord> findActiveAssignmentsForStaffOnDate(long staffId, LocalDate date) {
        String sql = assignmentSelect()
                + " WHERE a.staff_id = ? AND a.assignment_date = ? AND a.assignment_status <> 'CANCELLED'";
        List<EventStaffAssignmentRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, staffId);
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapAssignment(rows));
            }
        } catch (SQLException ex) {
            throw failure("Unable to check assignment conflicts.", ex);
        }
        return result;
    }

    @Override
    public Optional<EventStaffAssignmentRecord> findAssignment(long id) {
        String sql = assignmentSelect() + " WHERE a.assignment_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapAssignment(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to load event assignment.", ex);
        }
    }

    @Override
    public EventStaffAssignmentRecord saveAssignment(EventStaffAssignmentRecord assignment) {
        try (Connection connection = config.openConnection()) {
            if (exists(connection, "event_staff_assignment", "assignment_id", assignment.getId())) {
                String sql = "UPDATE event_staff_assignment SET assignment_status=? WHERE assignment_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, assignment.getStatus());
                    statement.setLong(2, assignment.getId());
                    statement.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO event_staff_assignment (assignment_id, staff_id, event_label, "
                        + "assignment_role, assignment_date, start_time, end_time, assignment_status, notes) "
                        + "VALUES (?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, assignment.getId());
                    statement.setLong(2, assignment.getStaffId());
                    statement.setString(3, assignment.getEventLabel());
                    statement.setString(4, assignment.getAssignmentRole());
                    statement.setDate(5, Date.valueOf(assignment.getAssignmentDate()));
                    statement.setTime(6, Time.valueOf(assignment.getStartTime()));
                    statement.setTime(7, Time.valueOf(assignment.getEndTime()));
                    statement.setString(8, assignment.getStatus());
                    statement.setString(9, assignment.getNotes());
                    statement.executeUpdate();
                }
            }
            return assignment;
        } catch (SQLException ex) {
            throw failure("Unable to save event assignment.", ex);
        }
    }

    @Override
    public long nextAssignmentId() { return nextId("event_staff_assignment", "assignment_id"); }

    // --------------------------------------------------------------- Helpers

    private String venueBookingSelect() {
        return "SELECT b.venue_booking_id, b.venue_id, v.venue_name, b.event_label, b.event_date, "
                + "b.start_time, b.end_time, b.guest_count, b.booking_status, b.notes, b.created_by_name, "
                + "b.created_at FROM event_venue_booking b JOIN event_venue v ON v.venue_id = b.venue_id";
    }

    private String resourceBookingSelect() {
        return "SELECT b.resource_booking_id, b.resource_id, r.resource_name, b.event_label, b.event_date, "
                + "b.quantity_reserved, b.allocation_status, b.requested_by_name, b.notes, b.created_at "
                + "FROM resource_booking b JOIN event_resource r ON r.resource_id = b.resource_id";
    }

    private String staffSelect() {
        return "SELECT s.staff_id, s.employee_code, s.job_title, s.department, s.availability_status, "
                + "u.first_name, u.last_name FROM staff_profile s JOIN user_account u ON u.user_id = s.user_id";
    }

    private String scheduleSelect() {
        return "SELECT sc.schedule_id, sc.staff_id, u.first_name, u.last_name, sc.shift_date, sc.start_time, "
                + "sc.end_time, sc.shift_type, sc.schedule_status, sc.notes FROM staff_schedule sc "
                + "JOIN staff_profile s ON s.staff_id = sc.staff_id "
                + "JOIN user_account u ON u.user_id = s.user_id";
    }

    private String assignmentSelect() {
        return "SELECT a.assignment_id, a.staff_id, u.first_name, u.last_name, a.event_label, "
                + "a.assignment_role, a.assignment_date, a.start_time, a.end_time, a.assignment_status, "
                + "a.notes, a.created_at FROM event_staff_assignment a "
                + "JOIN staff_profile s ON s.staff_id = a.staff_id "
                + "JOIN user_account u ON u.user_id = s.user_id";
    }

    private EventVenueRecord mapVenue(ResultSet rows) throws SQLException {
        return new EventVenueRecord(rows.getLong("venue_id"), rows.getString("venue_name"),
                rows.getString("venue_type"), rows.getInt("capacity"), rows.getBigDecimal("base_fee"),
                rows.getString("description"), rows.getString("availability_status"));
    }

    private EventResourceRecord mapResource(ResultSet rows) throws SQLException {
        return new EventResourceRecord(rows.getLong("resource_id"), rows.getString("resource_name"),
                rows.getString("resource_category"), rows.getInt("total_quantity"),
                rows.getInt("available_quantity"), rows.getBigDecimal("unit_cost"),
                rows.getString("resource_status"));
    }

    private EventVenueBookingRecord mapVenueBooking(ResultSet rows) throws SQLException {
        int guestCount = rows.getInt("guest_count");
        Timestamp createdAt = rows.getTimestamp("created_at");
        return new EventVenueBookingRecord(rows.getLong("venue_booking_id"), rows.getLong("venue_id"),
                rows.getString("venue_name"), rows.getString("event_label"),
                rows.getDate("event_date").toLocalDate(), rows.getTime("start_time").toLocalTime(),
                rows.getTime("end_time").toLocalTime(), rows.wasNull() ? null : guestCount,
                rows.getString("booking_status"), rows.getString("notes"), rows.getString("created_by_name"),
                createdAt == null ? LocalDateTime.now() : createdAt.toLocalDateTime());
    }

    private ResourceBookingRecord mapResourceBooking(ResultSet rows) throws SQLException {
        Timestamp createdAt = rows.getTimestamp("created_at");
        return new ResourceBookingRecord(rows.getLong("resource_booking_id"), rows.getLong("resource_id"),
                rows.getString("resource_name"), rows.getString("event_label"),
                rows.getDate("event_date").toLocalDate(), rows.getInt("quantity_reserved"),
                rows.getString("allocation_status"), rows.getString("requested_by_name"),
                rows.getString("notes"), createdAt == null ? LocalDateTime.now() : createdAt.toLocalDateTime());
    }

    private StaffMemberRecord mapStaff(ResultSet rows) throws SQLException {
        String fullName = (rows.getString("first_name") + " " + rows.getString("last_name")).trim();
        return new StaffMemberRecord(rows.getLong("staff_id"), fullName, rows.getString("employee_code"),
                rows.getString("job_title"), rows.getString("department"),
                rows.getString("availability_status"));
    }

    private StaffScheduleRecord mapSchedule(ResultSet rows) throws SQLException {
        String fullName = (rows.getString("first_name") + " " + rows.getString("last_name")).trim();
        return new StaffScheduleRecord(rows.getLong("schedule_id"), rows.getLong("staff_id"), fullName,
                rows.getDate("shift_date").toLocalDate(), rows.getTime("start_time").toLocalTime(),
                rows.getTime("end_time").toLocalTime(), rows.getString("shift_type"),
                rows.getString("schedule_status"), rows.getString("notes"));
    }

    private EventStaffAssignmentRecord mapAssignment(ResultSet rows) throws SQLException {
        String fullName = (rows.getString("first_name") + " " + rows.getString("last_name")).trim();
        Timestamp createdAt = rows.getTimestamp("created_at");
        return new EventStaffAssignmentRecord(rows.getLong("assignment_id"), rows.getLong("staff_id"),
                fullName, rows.getString("event_label"), rows.getString("assignment_role"),
                rows.getDate("assignment_date").toLocalDate(), rows.getTime("start_time").toLocalTime(),
                rows.getTime("end_time").toLocalTime(), rows.getString("assignment_status"),
                rows.getString("notes"), createdAt == null ? LocalDateTime.now() : createdAt.toLocalDateTime());
    }

    private boolean exists(Connection connection, String table, String idColumn, long id) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE " + idColumn + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    private int countWhere(String table, String column, long value) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, value);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw failure("Unable to check related records.", ex);
        }
    }

    private long nextId(String table, String column) {
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) + 1 FROM " + table;
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            return rows.next() ? rows.getLong(1) : 1;
        } catch (SQLException ex) {
            throw failure("Unable to allocate a database identifier.", ex);
        }
    }

    private static IllegalStateException failure(String message, SQLException ex) {
        return new IllegalStateException(message + " " + ex.getMessage(), ex);
    }
}
