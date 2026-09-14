package com.dinevista.service;

import com.dinevista.model.EventResourceRecord;
import com.dinevista.model.EventStaffAssignmentRecord;
import com.dinevista.model.EventVenueBookingRecord;
import com.dinevista.model.EventVenueRecord;
import com.dinevista.model.ResourceBookingRecord;
import com.dinevista.model.StaffMemberRecord;
import com.dinevista.model.StaffScheduleRecord;
import com.dinevista.repository.EventOperationsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Business logic for the Event Resource and Staff Scheduling Management module
 * (Wijesuriya W. A. T. D. / IT25103799). Enforces capacity and time-overlap checks so
 * venues, equipment, and staff can never be double-booked, and keeps a full,
 * non-destructive history of every booking, shift, and assignment.
 */
public class EventOperationsService {
    private static final Set<String> VENUE_TYPES =
            Set.of("INDOOR", "OUTDOOR", "PRIVATE_ROOM", "OFF_SITE");
    private static final Set<String> VENUE_STATUSES = Set.of("AVAILABLE", "UNAVAILABLE", "MAINTENANCE");
    private static final Set<String> RESOURCE_CATEGORIES =
            Set.of("FURNITURE", "AUDIO_VISUAL", "DECOR", "LIGHTING", "KITCHEN", "TRANSPORT", "OTHER");
    private static final Set<String> RESOURCE_STATUSES = Set.of("AVAILABLE", "MAINTENANCE", "RETIRED");
    private static final Set<String> VENUE_BOOKING_STATUSES = Set.of("REQUESTED", "CONFIRMED", "CANCELLED");
    private static final Set<String> RESOURCE_BOOKING_STATUSES =
            Set.of("REQUESTED", "ALLOCATED", "RETURNED", "CANCELLED");
    private static final Set<String> STAFF_AVAILABILITY = Set.of("AVAILABLE", "UNAVAILABLE", "ON_LEAVE");
    private static final Set<String> SHIFT_TYPES =
            Set.of("RESTAURANT", "KITCHEN", "EVENT", "DELIVERY", "ADMIN");
    private static final Set<String> SCHEDULE_STATUSES =
            Set.of("SCHEDULED", "CONFIRMED", "COMPLETED", "ABSENT", "CANCELLED");
    private static final Set<String> ASSIGNMENT_STATUSES =
            Set.of("ASSIGNED", "CONFIRMED", "COMPLETED", "CANCELLED");

    private final EventOperationsRepository repository;

    public EventOperationsService(EventOperationsRepository repository) {
        this.repository = repository;
    }

    // ------------------------------------------------------------------ Venues

    public List<EventVenueRecord> allVenues() { return repository.findAllVenues(); }

    public Optional<EventVenueRecord> venue(long id) { return repository.findVenue(id); }

    public List<EventVenueBookingRecord> venueBookings(long venueId) { return repository.findVenueBookings(venueId); }

    public List<EventVenueBookingRecord> allVenueBookings() { return repository.findAllVenueBookings(); }

    public OperationResult<EventVenueRecord> saveVenue(long id, String name, String venueType, String capacityRaw,
                                                         String baseFeeRaw, String description, String status) {
        List<String> errors = new ArrayList<>();
        String cleanName = clean(name);
        if (cleanName.isEmpty()) errors.add("Venue name is required.");

        String cleanType = clean(venueType).toUpperCase();
        if (!VENUE_TYPES.contains(cleanType)) errors.add("Select a valid venue type.");

        String cleanStatus = clean(status).toUpperCase();
        if (cleanStatus.isEmpty()) cleanStatus = "AVAILABLE";
        if (!VENUE_STATUSES.contains(cleanStatus)) errors.add("Select a valid venue status.");

        int capacity = parsePositiveInt(capacityRaw, "Capacity", errors);
        BigDecimal baseFee = parseNonNegativeDecimal(baseFeeRaw, "Base fee", errors);

        Optional<EventVenueRecord> existingByName = repository.findVenueByName(cleanName);
        if (existingByName.isPresent() && existingByName.get().getId() != id) {
            errors.add("Another venue is already named \"" + cleanName + "\".");
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        EventVenueRecord venue;
        if (id <= 0) {
            long newId = repository.nextVenueId();
            venue = new EventVenueRecord(newId, cleanName, cleanType, capacity, baseFee, description, cleanStatus);
        } else {
            Optional<EventVenueRecord> existing = repository.findVenue(id);
            if (existing.isEmpty()) return OperationResult.failure("Venue could not be found.");
            venue = existing.get();
            venue.setName(cleanName);
            venue.setVenueType(cleanType);
            venue.setCapacity(capacity);
            venue.setBaseFee(baseFee);
            venue.setDescription(description);
            venue.setAvailabilityStatus(cleanStatus);
        }
        return OperationResult.success(repository.saveVenue(venue));
    }

    /** Deletes a venue only when it has no recorded bookings, preserving auditable history. */
    public OperationResult<Void> deleteVenue(long id) {
        if (repository.hasVenueBookings(id)) {
            return OperationResult.failure("This venue has recorded bookings and cannot be deleted.");
        }
        if (!repository.deleteVenue(id)) return OperationResult.failure("Venue could not be found.");
        return OperationResult.success(null);
    }

    /** Books a venue for an event, rejecting the request if it would overlap an existing active booking. */
    public OperationResult<EventVenueBookingRecord> bookVenue(long venueId, String eventLabel, String eventDateRaw,
                                                                String startTimeRaw, String endTimeRaw,
                                                                String guestCountRaw, String notes,
                                                                String createdByName) {
        List<String> errors = new ArrayList<>();
        Optional<EventVenueRecord> venueOpt = repository.findVenue(venueId);
        if (venueOpt.isEmpty()) return OperationResult.failure("Venue could not be found.");
        EventVenueRecord venue = venueOpt.get();

        String cleanLabel = clean(eventLabel);
        if (cleanLabel.isEmpty()) errors.add("Enter an event name or reference for this booking.");
        if (!venue.isBookable()) errors.add("This venue is not currently available for booking.");

        LocalDate eventDate = parseDate(eventDateRaw, errors);
        LocalTime startTime = parseTime(startTimeRaw, "Start time", errors);
        LocalTime endTime = parseTime(endTimeRaw, "End time", errors);
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            errors.add("End time must be after start time.");
        }

        Integer guestCount = null;
        if (guestCountRaw != null && !guestCountRaw.trim().isEmpty()) {
            guestCount = parsePositiveInt(guestCountRaw, "Guest count", errors);
            if (guestCount != null && guestCount > venue.getCapacity()) {
                errors.add("Guest count exceeds this venue's capacity of " + venue.getCapacity() + ".");
            }
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        for (EventVenueBookingRecord existing : repository.findVenueBookings(venueId)) {
            if (existing.isActive() && eventDate.equals(existing.getEventDate())
                    && existing.overlaps(startTime, endTime)) {
                errors.add("This venue is already booked for \"" + existing.getEventLabel()
                        + "\" from " + existing.getStartTimeDisplay() + " to " + existing.getEndTimeDisplay()
                        + " on that date.");
                return OperationResult.failure(errors);
            }
        }

        long id = repository.nextVenueBookingId();
        EventVenueBookingRecord booking = new EventVenueBookingRecord(id, venueId, venue.getName(), cleanLabel,
                eventDate, startTime, endTime, guestCount, "REQUESTED",
                notes == null || notes.trim().isEmpty() ? null : notes.trim(), createdByName, LocalDateTime.now());
        return OperationResult.success(repository.saveVenueBooking(booking));
    }

    /** Transitions a venue booking's status; re-checks for conflicts when confirming. */
    public OperationResult<EventVenueBookingRecord> updateVenueBookingStatus(long bookingId, String newStatus) {
        String cleanStatus = clean(newStatus).toUpperCase();
        if (!VENUE_BOOKING_STATUSES.contains(cleanStatus)) return OperationResult.failure("Select a valid status.");

        Optional<EventVenueBookingRecord> bookingOpt = repository.findVenueBooking(bookingId);
        if (bookingOpt.isEmpty()) return OperationResult.failure("Booking could not be found.");
        EventVenueBookingRecord booking = bookingOpt.get();

        if ("CONFIRMED".equals(cleanStatus)) {
            for (EventVenueBookingRecord other : repository.findVenueBookings(booking.getVenueId())) {
                if (other.getId() != booking.getId() && other.isActive()
                        && booking.getEventDate().equals(other.getEventDate())
                        && other.overlaps(booking.getStartTime(), booking.getEndTime())) {
                    return OperationResult.failure("Cannot confirm: this would overlap \""
                            + other.getEventLabel() + "\" on the same date.");
                }
            }
        }
        booking.setStatus(cleanStatus);
        return OperationResult.success(repository.saveVenueBooking(booking));
    }

    // ---------------------------------------------------------------- Resources

    public List<EventResourceRecord> allResources() { return repository.findAllResources(); }

    public Optional<EventResourceRecord> resource(long id) { return repository.findResource(id); }

    public List<ResourceBookingRecord> resourceBookings(long resourceId) { return repository.findResourceBookings(resourceId); }

    public List<ResourceBookingRecord> allResourceBookings() { return repository.findAllResourceBookings(); }

    public OperationResult<EventResourceRecord> saveResource(long id, String name, String category,
                                                                String totalQuantityRaw, String availableQuantityRaw,
                                                                String unitCostRaw, String status) {
        List<String> errors = new ArrayList<>();
        String cleanName = clean(name);
        if (cleanName.isEmpty()) errors.add("Resource name is required.");

        String cleanCategory = clean(category).toUpperCase();
        if (!RESOURCE_CATEGORIES.contains(cleanCategory)) errors.add("Select a valid resource category.");

        String cleanStatus = clean(status).toUpperCase();
        if (cleanStatus.isEmpty()) cleanStatus = "AVAILABLE";
        if (!RESOURCE_STATUSES.contains(cleanStatus)) errors.add("Select a valid resource status.");

        int totalQuantity = parsePositiveInt(totalQuantityRaw, "Total quantity", errors);
        int availableQuantity = parseNonNegativeInt(availableQuantityRaw, "Available quantity", errors);
        if (errors.isEmpty() && availableQuantity > totalQuantity) {
            errors.add("Available quantity cannot exceed total quantity.");
        }
        BigDecimal unitCost = null;
        if (unitCostRaw != null && !unitCostRaw.trim().isEmpty()) {
            unitCost = parseNonNegativeDecimal(unitCostRaw, "Unit cost", errors);
        }

        Optional<EventResourceRecord> existingByName = repository.findResourceByName(cleanName);
        if (existingByName.isPresent() && existingByName.get().getId() != id) {
            errors.add("Another resource is already named \"" + cleanName + "\".");
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        EventResourceRecord resource;
        if (id <= 0) {
            long newId = repository.nextResourceId();
            resource = new EventResourceRecord(newId, cleanName, cleanCategory, totalQuantity, availableQuantity,
                    unitCost, cleanStatus);
        } else {
            Optional<EventResourceRecord> existing = repository.findResource(id);
            if (existing.isEmpty()) return OperationResult.failure("Resource could not be found.");
            resource = existing.get();
            resource.setName(cleanName);
            resource.setCategory(cleanCategory);
            resource.setTotalQuantity(totalQuantity);
            resource.setAvailableQuantity(availableQuantity);
            resource.setUnitCost(unitCost);
            resource.setStatus(cleanStatus);
        }
        return OperationResult.success(repository.saveResource(resource));
    }

    /** Deletes a resource only when it has no recorded bookings, preserving auditable history. */
    public OperationResult<Void> deleteResource(long id) {
        if (repository.hasResourceBookings(id)) {
            return OperationResult.failure("This resource has recorded bookings and cannot be deleted.");
        }
        if (!repository.deleteResource(id)) return OperationResult.failure("Resource could not be found.");
        return OperationResult.success(null);
    }

    /** Reserves a quantity of a resource for an event date, guarded by a capacity check. */
    public OperationResult<ResourceBookingRecord> bookResource(long resourceId, String eventLabel,
                                                                 String eventDateRaw, String quantityRaw,
                                                                 String notes, String requestedByName) {
        List<String> errors = new ArrayList<>();
        Optional<EventResourceRecord> resourceOpt = repository.findResource(resourceId);
        if (resourceOpt.isEmpty()) return OperationResult.failure("Resource could not be found.");
        EventResourceRecord resource = resourceOpt.get();

        String cleanLabel = clean(eventLabel);
        if (cleanLabel.isEmpty()) errors.add("Enter an event name or reference for this booking.");
        if (!"AVAILABLE".equals(resource.getStatus())) {
            errors.add("This resource is not currently available for booking.");
        }

        LocalDate eventDate = parseDate(eventDateRaw, errors);
        int quantity = parsePositiveInt(quantityRaw, "Quantity", errors);
        if (!errors.isEmpty()) return OperationResult.failure(errors);

        int reservedForDate = 0;
        for (ResourceBookingRecord existing : repository.findResourceBookings(resourceId)) {
            if (existing.holdsCapacity() && eventDate.equals(existing.getEventDate())) {
                reservedForDate += existing.getQuantityReserved();
            }
        }
        int remaining = resource.getAvailableQuantity() - reservedForDate;
        if (quantity > remaining) {
            return OperationResult.failure("Only " + Math.max(remaining, 0) + " of " + resource.getName()
                    + " remain available on that date (" + reservedForDate + " already reserved).");
        }

        long id = repository.nextResourceBookingId();
        ResourceBookingRecord booking = new ResourceBookingRecord(id, resourceId, resource.getName(), cleanLabel,
                eventDate, quantity, "REQUESTED", requestedByName,
                notes == null || notes.trim().isEmpty() ? null : notes.trim(), LocalDateTime.now());
        return OperationResult.success(repository.saveResourceBooking(booking));
    }

    /** Transitions a resource booking's status; re-checks capacity when (re-)allocating. */
    public OperationResult<ResourceBookingRecord> updateResourceBookingStatus(long bookingId, String newStatus) {
        String cleanStatus = clean(newStatus).toUpperCase();
        if (!RESOURCE_BOOKING_STATUSES.contains(cleanStatus)) return OperationResult.failure("Select a valid status.");

        Optional<ResourceBookingRecord> bookingOpt = repository.findResourceBooking(bookingId);
        if (bookingOpt.isEmpty()) return OperationResult.failure("Booking could not be found.");
        ResourceBookingRecord booking = bookingOpt.get();

        if ("ALLOCATED".equals(cleanStatus) && !"ALLOCATED".equals(booking.getStatus())) {
            Optional<EventResourceRecord> resourceOpt = repository.findResource(booking.getResourceId());
            if (resourceOpt.isPresent()) {
                EventResourceRecord resource = resourceOpt.get();
                int reservedForDate = 0;
                for (ResourceBookingRecord other : repository.findResourceBookings(booking.getResourceId())) {
                    if (other.getId() != booking.getId() && other.holdsCapacity()
                            && booking.getEventDate().equals(other.getEventDate())) {
                        reservedForDate += other.getQuantityReserved();
                    }
                }
                if (reservedForDate + booking.getQuantityReserved() > resource.getAvailableQuantity()) {
                    return OperationResult.failure("Cannot allocate: not enough " + resource.getName()
                            + " remain available on that date.");
                }
            }
        }
        booking.setStatus(cleanStatus);
        return OperationResult.success(repository.saveResourceBooking(booking));
    }

    // ------------------------------------------------------------------- Staff

    public List<StaffMemberRecord> allStaff() { return repository.findAllStaff(); }

    public Optional<StaffMemberRecord> staff(long id) { return repository.findStaff(id); }

    public OperationResult<Void> updateStaffAvailability(long staffId, String availabilityStatus) {
        String cleanStatus = clean(availabilityStatus).toUpperCase();
        if (!STAFF_AVAILABILITY.contains(cleanStatus)) return OperationResult.failure("Select a valid availability status.");
        if (!repository.updateStaffAvailability(staffId, cleanStatus)) {
            return OperationResult.failure("Staff member could not be found.");
        }
        return OperationResult.success(null);
    }

    // ------------------------------------------------------------ Shift schedule

    public List<StaffScheduleRecord> schedulesForStaff(long staffId) { return repository.findSchedulesForStaff(staffId); }

    public List<StaffScheduleRecord> allSchedules() { return repository.findAllSchedules(); }

    /** Schedules a shift, rejecting the request if it would overlap another active shift for that staff member. */
    public OperationResult<StaffScheduleRecord> scheduleShift(long staffId, String shiftDateRaw,
                                                                 String startTimeRaw, String endTimeRaw,
                                                                 String shiftType, String notes) {
        List<String> errors = new ArrayList<>();
        Optional<StaffMemberRecord> staffOpt = repository.findStaff(staffId);
        if (staffOpt.isEmpty()) return OperationResult.failure("Staff member could not be found.");
        StaffMemberRecord staff = staffOpt.get();

        String cleanType = clean(shiftType).toUpperCase();
        if (!SHIFT_TYPES.contains(cleanType)) errors.add("Select a valid shift type.");
        if ("UNAVAILABLE".equals(staff.getAvailabilityStatus())) {
            errors.add(staff.getFullName() + " is currently marked unavailable for scheduling.");
        }

        LocalDate shiftDate = parseDate(shiftDateRaw, errors);
        LocalTime startTime = parseTime(startTimeRaw, "Start time", errors);
        LocalTime endTime = parseTime(endTimeRaw, "End time", errors);
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            errors.add("End time must be after start time.");
        }
        if (!errors.isEmpty()) return OperationResult.failure(errors);

        for (StaffScheduleRecord existing : repository.findActiveSchedulesForStaffOnDate(staffId, shiftDate)) {
            if (existing.overlaps(startTime, endTime)) {
                return OperationResult.failure(staff.getFullName() + " already has a shift from "
                        + existing.getStartTimeDisplay() + " to " + existing.getEndTimeDisplay()
                        + " on that date.");
            }
        }
        for (EventStaffAssignmentRecord existing : repository.findActiveAssignmentsForStaffOnDate(staffId, shiftDate)) {
            if (existing.overlaps(startTime, endTime)) {
                return OperationResult.failure(staff.getFullName() + " is already assigned to \""
                        + existing.getEventLabel() + "\" from " + existing.getStartTimeDisplay()
                        + " to " + existing.getEndTimeDisplay() + " on that date.");
            }
        }

        long id = repository.nextScheduleId();
        StaffScheduleRecord schedule = new StaffScheduleRecord(id, staffId, staff.getFullName(), shiftDate,
                startTime, endTime, cleanType, "SCHEDULED",
                notes == null || notes.trim().isEmpty() ? null : notes.trim());
        return OperationResult.success(repository.saveSchedule(schedule));
    }

    public OperationResult<StaffScheduleRecord> updateScheduleStatus(long scheduleId, String newStatus) {
        String cleanStatus = clean(newStatus).toUpperCase();
        if (!SCHEDULE_STATUSES.contains(cleanStatus)) return OperationResult.failure("Select a valid status.");
        Optional<StaffScheduleRecord> scheduleOpt = repository.findSchedule(scheduleId);
        if (scheduleOpt.isEmpty()) return OperationResult.failure("Shift could not be found.");
        StaffScheduleRecord schedule = scheduleOpt.get();
        schedule.setStatus(cleanStatus);
        return OperationResult.success(repository.saveSchedule(schedule));
    }

    // ------------------------------------------------------ Event staff assignments

    public List<EventStaffAssignmentRecord> assignmentsForStaff(long staffId) { return repository.findAssignmentsForStaff(staffId); }

    public List<EventStaffAssignmentRecord> allAssignments() { return repository.findAllAssignments(); }

    /** Assigns a staff member to an event, rejecting the request on any shift or assignment overlap. */
    public OperationResult<EventStaffAssignmentRecord> assignStaffToEvent(long staffId, String eventLabel,
                                                                             String role, String assignmentDateRaw,
                                                                             String startTimeRaw, String endTimeRaw,
                                                                             String notes) {
        List<String> errors = new ArrayList<>();
        Optional<StaffMemberRecord> staffOpt = repository.findStaff(staffId);
        if (staffOpt.isEmpty()) return OperationResult.failure("Staff member could not be found.");
        StaffMemberRecord staff = staffOpt.get();

        String cleanLabel = clean(eventLabel);
        if (cleanLabel.isEmpty()) errors.add("Enter an event name or reference for this assignment.");
        String cleanRole = clean(role);
        if (cleanRole.isEmpty()) errors.add("Enter the role this staff member will perform.");
        if ("UNAVAILABLE".equals(staff.getAvailabilityStatus())) {
            errors.add(staff.getFullName() + " is currently marked unavailable for scheduling.");
        }

        LocalDate assignmentDate = parseDate(assignmentDateRaw, errors);
        LocalTime startTime = parseTime(startTimeRaw, "Start time", errors);
        LocalTime endTime = parseTime(endTimeRaw, "End time", errors);
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            errors.add("End time must be after start time.");
        }
        if (!errors.isEmpty()) return OperationResult.failure(errors);

        for (EventStaffAssignmentRecord existing : repository.findActiveAssignmentsForStaffOnDate(staffId, assignmentDate)) {
            if (existing.overlaps(startTime, endTime)) {
                return OperationResult.failure(staff.getFullName() + " is already assigned to \""
                        + existing.getEventLabel() + "\" from " + existing.getStartTimeDisplay()
                        + " to " + existing.getEndTimeDisplay() + " on that date.");
            }
        }
        for (StaffScheduleRecord existing : repository.findActiveSchedulesForStaffOnDate(staffId, assignmentDate)) {
            if (existing.overlaps(startTime, endTime)) {
                return OperationResult.failure(staff.getFullName() + " already has a shift from "
                        + existing.getStartTimeDisplay() + " to " + existing.getEndTimeDisplay()
                        + " on that date.");
            }
        }

        long id = repository.nextAssignmentId();
        EventStaffAssignmentRecord assignment = new EventStaffAssignmentRecord(id, staffId, staff.getFullName(),
                cleanLabel, cleanRole, assignmentDate, startTime, endTime, "ASSIGNED",
                notes == null || notes.trim().isEmpty() ? null : notes.trim(), LocalDateTime.now());
        return OperationResult.success(repository.saveAssignment(assignment));
    }

    public OperationResult<EventStaffAssignmentRecord> updateAssignmentStatus(long assignmentId, String newStatus) {
        String cleanStatus = clean(newStatus).toUpperCase();
        if (!ASSIGNMENT_STATUSES.contains(cleanStatus)) return OperationResult.failure("Select a valid status.");
        Optional<EventStaffAssignmentRecord> assignmentOpt = repository.findAssignment(assignmentId);
        if (assignmentOpt.isEmpty()) return OperationResult.failure("Assignment could not be found.");
        EventStaffAssignmentRecord assignment = assignmentOpt.get();
        assignment.setStatus(cleanStatus);
        return OperationResult.success(repository.saveAssignment(assignment));
    }

    // ----------------------------------------------------------------- Helpers

    private static String clean(String value) { return value == null ? "" : value.trim(); }

    private static LocalDate parseDate(String raw, List<String> errors) {
        try {
            return LocalDate.parse(clean(raw));
        } catch (Exception ex) {
            errors.add("Select a valid date.");
            return null;
        }
    }

    private static LocalTime parseTime(String raw, String label, List<String> errors) {
        try {
            return LocalTime.parse(clean(raw));
        } catch (Exception ex) {
            errors.add(label + " must be a valid time.");
            return null;
        }
    }

    private static int parsePositiveInt(String raw, String label, List<String> errors) {
        try {
            int value = Integer.parseInt(clean(raw));
            if (value <= 0) { errors.add(label + " must be greater than zero."); return 0; }
            return value;
        } catch (NumberFormatException ex) {
            errors.add(label + " must be a whole number.");
            return 0;
        }
    }

    private static int parseNonNegativeInt(String raw, String label, List<String> errors) {
        try {
            int value = Integer.parseInt(clean(raw));
            if (value < 0) { errors.add(label + " cannot be negative."); return 0; }
            return value;
        } catch (NumberFormatException ex) {
            errors.add(label + " must be a whole number.");
            return 0;
        }
    }

    private static BigDecimal parseNonNegativeDecimal(String raw, String label, List<String> errors) {
        try {
            BigDecimal value = new BigDecimal(clean(raw));
            if (value.compareTo(BigDecimal.ZERO) < 0) { errors.add(label + " cannot be negative."); return BigDecimal.ZERO; }
            return value;
        } catch (Exception ex) {
            errors.add(label + " must be a valid number.");
            return BigDecimal.ZERO;
        }
    }
}
