package com.dinevista.repository;

import com.dinevista.model.EventResourceRecord;
import com.dinevista.model.EventStaffAssignmentRecord;
import com.dinevista.model.EventVenueBookingRecord;
import com.dinevista.model.EventVenueRecord;
import com.dinevista.model.ResourceBookingRecord;
import com.dinevista.model.StaffMemberRecord;
import com.dinevista.model.StaffScheduleRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory {@link EventOperationsRepository} used in the default demo
 * storage mode. Seeded with representative venues, resources, and a small operational
 * staff roster so the module is demonstrable immediately, without depending on a
 * registered manager account.
 */
public class InMemoryEventOperationsRepository implements EventOperationsRepository {
    private final Map<Long, EventVenueRecord> venues = new ConcurrentHashMap<>();
    private final Map<Long, EventResourceRecord> resources = new ConcurrentHashMap<>();
    private final Map<Long, EventVenueBookingRecord> venueBookings = new ConcurrentHashMap<>();
    private final Map<Long, ResourceBookingRecord> resourceBookings = new ConcurrentHashMap<>();
    private final Map<Long, StaffMemberRecord> staff = new ConcurrentHashMap<>();
    private final Map<Long, StaffScheduleRecord> schedules = new ConcurrentHashMap<>();
    private final Map<Long, EventStaffAssignmentRecord> assignments = new ConcurrentHashMap<>();

    private final AtomicLong venueSequence = new AtomicLong(0);
    private final AtomicLong resourceSequence = new AtomicLong(0);
    private final AtomicLong venueBookingSequence = new AtomicLong(0);
    private final AtomicLong resourceBookingSequence = new AtomicLong(0);
    private final AtomicLong staffSequence = new AtomicLong(0);
    private final AtomicLong scheduleSequence = new AtomicLong(0);
    private final AtomicLong assignmentSequence = new AtomicLong(0);

    public InMemoryEventOperationsRepository() {
        seed();
    }

    private void seed() {
        seedVenue("Garden Pavilion", "OUTDOOR", 220, "150000.00",
                "Landscaped outdoor event venue with weather backup options.");
        seedVenue("Vista Grand Hall", "INDOOR", 350, "250000.00",
                "Climate-controlled hall with stage and projection facilities.");
        seedVenue("Private Dining Suite", "PRIVATE_ROOM", 40, "60000.00",
                "Private room for intimate celebrations and executive dinners.");

        seedResource("Round Banquet Table", "FURNITURE", 40, "3500.00");
        seedResource("Chiavari Chair", "FURNITURE", 300, "650.00");
        seedResource("Wireless PA System", "AUDIO_VISUAL", 6, "12000.00");
        seedResource("LED Uplighting Set", "LIGHTING", 20, "4500.00");
        seedResource("Backdrop & Floral Decor Kit", "DECOR", 10, "18000.00");
        seedResource("Chafing Dish Set", "KITCHEN", 25, "2200.00");

        seedStaff("Nadeesha Perera", "EMP-2001", "Event Coordinator", "Events");
        seedStaff("Kasun Fernando", "EMP-2002", "Audio-Visual Technician", "Events");
        seedStaff("Dilani Wickramasinghe", "EMP-2003", "Banquet Supervisor", "Events");
        seedStaff("Ruwan Jayasuriya", "EMP-2004", "Setup Crew Lead", "Events");
    }

    private void seedVenue(String name, String type, int capacity, String baseFee, String description) {
        long id = nextVenueId();
        venues.put(id, new EventVenueRecord(id, name, type, capacity, new BigDecimal(baseFee),
                description, "AVAILABLE"));
    }

    private void seedResource(String name, String category, int totalQuantity, String unitCost) {
        long id = nextResourceId();
        resources.put(id, new EventResourceRecord(id, name, category, totalQuantity, totalQuantity,
                new BigDecimal(unitCost), "AVAILABLE"));
    }

    private void seedStaff(String name, String employeeCode, String jobTitle, String department) {
        long id = staffSequence.incrementAndGet();
        staff.put(id, new StaffMemberRecord(id, name, employeeCode, jobTitle, department, "AVAILABLE"));
    }

    // Venues
    @Override
    public List<EventVenueRecord> findAllVenues() {
        List<EventVenueRecord> list = new ArrayList<>(venues.values());
        list.sort(Comparator.comparing(EventVenueRecord::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    @Override
    public Optional<EventVenueRecord> findVenue(long id) { return Optional.ofNullable(venues.get(id)); }

    @Override
    public Optional<EventVenueRecord> findVenueByName(String name) {
        return venues.values().stream().filter(v -> v.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public EventVenueRecord saveVenue(EventVenueRecord venue) {
        venues.put(venue.getId(), venue);
        return venue;
    }

    @Override
    public boolean deleteVenue(long id) { return venues.remove(id) != null; }

    @Override
    public boolean hasVenueBookings(long venueId) {
        return venueBookings.values().stream().anyMatch(b -> b.getVenueId() == venueId);
    }

    @Override
    public long nextVenueId() { return venueSequence.incrementAndGet(); }

    // Resources
    @Override
    public List<EventResourceRecord> findAllResources() {
        List<EventResourceRecord> list = new ArrayList<>(resources.values());
        list.sort(Comparator.comparing(EventResourceRecord::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    @Override
    public Optional<EventResourceRecord> findResource(long id) { return Optional.ofNullable(resources.get(id)); }

    @Override
    public Optional<EventResourceRecord> findResourceByName(String name) {
        return resources.values().stream().filter(r -> r.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public EventResourceRecord saveResource(EventResourceRecord resource) {
        resources.put(resource.getId(), resource);
        return resource;
    }

    @Override
    public boolean deleteResource(long id) { return resources.remove(id) != null; }

    @Override
    public boolean hasResourceBookings(long resourceId) {
        return resourceBookings.values().stream().anyMatch(b -> b.getResourceId() == resourceId);
    }

    @Override
    public long nextResourceId() { return resourceSequence.incrementAndGet(); }

    // Venue bookings
    @Override
    public List<EventVenueBookingRecord> findVenueBookings(long venueId) {
        List<EventVenueBookingRecord> list = new ArrayList<>();
        for (EventVenueBookingRecord booking : venueBookings.values()) {
            if (booking.getVenueId() == venueId) list.add(booking);
        }
        list.sort(Comparator.comparing(EventVenueBookingRecord::getEventDate).reversed());
        return list;
    }

    @Override
    public List<EventVenueBookingRecord> findAllVenueBookings() {
        List<EventVenueBookingRecord> list = new ArrayList<>(venueBookings.values());
        list.sort(Comparator.comparing(EventVenueBookingRecord::getEventDate).reversed());
        return list;
    }

    @Override
    public Optional<EventVenueBookingRecord> findVenueBooking(long id) {
        return Optional.ofNullable(venueBookings.get(id));
    }

    @Override
    public EventVenueBookingRecord saveVenueBooking(EventVenueBookingRecord booking) {
        venueBookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public long nextVenueBookingId() { return venueBookingSequence.incrementAndGet(); }

    // Resource bookings
    @Override
    public List<ResourceBookingRecord> findResourceBookings(long resourceId) {
        List<ResourceBookingRecord> list = new ArrayList<>();
        for (ResourceBookingRecord booking : resourceBookings.values()) {
            if (booking.getResourceId() == resourceId) list.add(booking);
        }
        list.sort(Comparator.comparing(ResourceBookingRecord::getEventDate).reversed());
        return list;
    }

    @Override
    public List<ResourceBookingRecord> findAllResourceBookings() {
        List<ResourceBookingRecord> list = new ArrayList<>(resourceBookings.values());
        list.sort(Comparator.comparing(ResourceBookingRecord::getEventDate).reversed());
        return list;
    }

    @Override
    public Optional<ResourceBookingRecord> findResourceBooking(long id) {
        return Optional.ofNullable(resourceBookings.get(id));
    }

    @Override
    public ResourceBookingRecord saveResourceBooking(ResourceBookingRecord booking) {
        resourceBookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public long nextResourceBookingId() { return resourceBookingSequence.incrementAndGet(); }

    // Staff roster
    @Override
    public List<StaffMemberRecord> findAllStaff() {
        List<StaffMemberRecord> list = new ArrayList<>(staff.values());
        list.sort(Comparator.comparing(StaffMemberRecord::getFullName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    @Override
    public Optional<StaffMemberRecord> findStaff(long id) { return Optional.ofNullable(staff.get(id)); }

    @Override
    public boolean updateStaffAvailability(long staffId, String availabilityStatus) {
        StaffMemberRecord member = staff.get(staffId);
        if (member == null) return false;
        member.setAvailabilityStatus(availabilityStatus);
        return true;
    }

    // Shift schedule
    @Override
    public List<StaffScheduleRecord> findSchedulesForStaff(long staffId) {
        List<StaffScheduleRecord> list = new ArrayList<>();
        for (StaffScheduleRecord schedule : schedules.values()) {
            if (schedule.getStaffId() == staffId) list.add(schedule);
        }
        list.sort(Comparator.comparing(StaffScheduleRecord::getShiftDate).reversed());
        return list;
    }

    @Override
    public List<StaffScheduleRecord> findAllSchedules() {
        List<StaffScheduleRecord> list = new ArrayList<>(schedules.values());
        list.sort(Comparator.comparing(StaffScheduleRecord::getShiftDate).reversed());
        return list;
    }

    @Override
    public List<StaffScheduleRecord> findActiveSchedulesForStaffOnDate(long staffId, LocalDate date) {
        List<StaffScheduleRecord> list = new ArrayList<>();
        for (StaffScheduleRecord schedule : schedules.values()) {
            if (schedule.getStaffId() == staffId && date.equals(schedule.getShiftDate()) && schedule.isActive()) {
                list.add(schedule);
            }
        }
        return list;
    }

    @Override
    public Optional<StaffScheduleRecord> findSchedule(long id) { return Optional.ofNullable(schedules.get(id)); }

    @Override
    public StaffScheduleRecord saveSchedule(StaffScheduleRecord schedule) {
        schedules.put(schedule.getId(), schedule);
        return schedule;
    }

    @Override
    public long nextScheduleId() { return scheduleSequence.incrementAndGet(); }

    // Event staff assignments
    @Override
    public List<EventStaffAssignmentRecord> findAssignmentsForStaff(long staffId) {
        List<EventStaffAssignmentRecord> list = new ArrayList<>();
        for (EventStaffAssignmentRecord assignment : assignments.values()) {
            if (assignment.getStaffId() == staffId) list.add(assignment);
        }
        list.sort(Comparator.comparing(EventStaffAssignmentRecord::getAssignmentDate).reversed());
        return list;
    }

    @Override
    public List<EventStaffAssignmentRecord> findAllAssignments() {
        List<EventStaffAssignmentRecord> list = new ArrayList<>(assignments.values());
        list.sort(Comparator.comparing(EventStaffAssignmentRecord::getAssignmentDate).reversed());
        return list;
    }

    @Override
    public List<EventStaffAssignmentRecord> findActiveAssignmentsForStaffOnDate(long staffId, LocalDate date) {
        List<EventStaffAssignmentRecord> list = new ArrayList<>();
        for (EventStaffAssignmentRecord assignment : assignments.values()) {
            if (assignment.getStaffId() == staffId && date.equals(assignment.getAssignmentDate())
                    && assignment.isActive()) {
                list.add(assignment);
            }
        }
        return list;
    }

    @Override
    public Optional<EventStaffAssignmentRecord> findAssignment(long id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public EventStaffAssignmentRecord saveAssignment(EventStaffAssignmentRecord assignment) {
        assignments.put(assignment.getId(), assignment);
        return assignment;
    }

    @Override
    public long nextAssignmentId() { return assignmentSequence.incrementAndGet(); }
}
