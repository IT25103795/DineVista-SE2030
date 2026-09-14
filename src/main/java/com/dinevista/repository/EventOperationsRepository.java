package com.dinevista.repository;

import com.dinevista.model.EventResourceRecord;
import com.dinevista.model.EventStaffAssignmentRecord;
import com.dinevista.model.EventVenueBookingRecord;
import com.dinevista.model.EventVenueRecord;
import com.dinevista.model.ResourceBookingRecord;
import com.dinevista.model.StaffMemberRecord;
import com.dinevista.model.StaffScheduleRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for the Event Resource and Staff Scheduling Management module
 * (Wijesuriya W. A. T. D. / IT25103799). Covers venues, shared equipment, venue and
 * resource bookings, the staff roster, shift scheduling, and per-event staff
 * assignments. An in-memory implementation is provided for the default demo storage
 * mode; a JDBC implementation persists against event_venue, event_resource,
 * event_venue_booking, resource_booking, staff_profile, staff_schedule, and
 * event_staff_assignment in database/schema.sql.
 */
public interface EventOperationsRepository {

    // Venues
    List<EventVenueRecord> findAllVenues();
    Optional<EventVenueRecord> findVenue(long id);
    Optional<EventVenueRecord> findVenueByName(String name);
    EventVenueRecord saveVenue(EventVenueRecord venue);
    boolean deleteVenue(long id);
    boolean hasVenueBookings(long venueId);
    long nextVenueId();

    // Resources
    List<EventResourceRecord> findAllResources();
    Optional<EventResourceRecord> findResource(long id);
    Optional<EventResourceRecord> findResourceByName(String name);
    EventResourceRecord saveResource(EventResourceRecord resource);
    boolean deleteResource(long id);
    boolean hasResourceBookings(long resourceId);
    long nextResourceId();

    // Venue bookings
    List<EventVenueBookingRecord> findVenueBookings(long venueId);
    List<EventVenueBookingRecord> findAllVenueBookings();
    Optional<EventVenueBookingRecord> findVenueBooking(long id);
    EventVenueBookingRecord saveVenueBooking(EventVenueBookingRecord booking);
    long nextVenueBookingId();

    // Resource bookings
    List<ResourceBookingRecord> findResourceBookings(long resourceId);
    List<ResourceBookingRecord> findAllResourceBookings();
    Optional<ResourceBookingRecord> findResourceBooking(long id);
    ResourceBookingRecord saveResourceBooking(ResourceBookingRecord booking);
    long nextResourceBookingId();

    // Staff roster (created via account registration; this module reads and updates status)
    List<StaffMemberRecord> findAllStaff();
    Optional<StaffMemberRecord> findStaff(long id);
    boolean updateStaffAvailability(long staffId, String availabilityStatus);

    // Shift schedule
    List<StaffScheduleRecord> findSchedulesForStaff(long staffId);
    List<StaffScheduleRecord> findAllSchedules();
    List<StaffScheduleRecord> findActiveSchedulesForStaffOnDate(long staffId, LocalDate date);
    Optional<StaffScheduleRecord> findSchedule(long id);
    StaffScheduleRecord saveSchedule(StaffScheduleRecord schedule);
    long nextScheduleId();

    // Event staff assignments
    List<EventStaffAssignmentRecord> findAssignmentsForStaff(long staffId);
    List<EventStaffAssignmentRecord> findAllAssignments();
    List<EventStaffAssignmentRecord> findActiveAssignmentsForStaffOnDate(long staffId, LocalDate date);
    Optional<EventStaffAssignmentRecord> findAssignment(long id);
    EventStaffAssignmentRecord saveAssignment(EventStaffAssignmentRecord assignment);
    long nextAssignmentId();
}
