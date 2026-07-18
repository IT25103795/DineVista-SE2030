package com.dinevista.service;

import com.dinevista.model.CartLineRecord;
import com.dinevista.model.FoodOrderRecord;
import com.dinevista.model.MenuItemRecord;
import com.dinevista.model.OrderItemRecord;
import com.dinevista.model.RestaurantTableRecord;
import com.dinevista.model.TableReservationRecord;
import com.dinevista.repository.ReservationOrderRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReservationOrderService {
    public static final int RESERVATION_SLOT_MINUTES = 90;
    public static final int MAX_CART_QUANTITY = 10;

    private static final Set<String> SEATING_AREAS = new HashSet<>(
            Arrays.asList("ANY", "INDOOR", "GARDEN", "PRIVATE_DINING", "CHEF_COUNTER"));
    private static final Set<String> ORDER_TYPES = new HashSet<>(
            Arrays.asList("DINE_IN", "TAKEAWAY", "PRE_ORDER"));
    private static final Set<String> RESERVATION_STATUSES = new HashSet<>(
            Arrays.asList("PENDING", "CONFIRMED", "SEATED", "COMPLETED",
                    "CANCELLED", "REJECTED", "NO_SHOW"));
    private static final Set<String> ORDER_STATUSES = new HashSet<>(
            Arrays.asList("PENDING", "CONFIRMED", "PREPARING", "READY",
                    "SERVED", "COMPLETED", "CANCELLED", "REJECTED"));

    private final ReservationOrderRepository repository;

    public ReservationOrderService(ReservationOrderRepository repository) {
        this.repository = repository;
    }

    public List<MenuItemRecord> menuItems() {
        return repository.findAllMenuItems();
    }

    public Optional<MenuItemRecord> menuItem(long id) {
        return repository.findMenuItem(id);
    }

    public List<RestaurantTableRecord> tables() {
        return repository.findAllTables();
    }

    public List<TableReservationRecord> reservationsForCustomer(String customerKey) {
        return repository.findReservationsByCustomer(customerKey);
    }

    public List<TableReservationRecord> allReservations(String status, String date) {
        return repository.findAllReservations().stream()
                .filter(item -> status == null || status.isBlank()
                        || "ALL".equalsIgnoreCase(status)
                        || item.getStatus().equalsIgnoreCase(status))
                .filter(item -> date == null || date.isBlank()
                        || item.getReservationDate().toString().equals(date))
                .collect(Collectors.toList());
    }

    public List<FoodOrderRecord> ordersForCustomer(String customerKey) {
        return repository.findOrdersByCustomer(customerKey);
    }

    public List<FoodOrderRecord> allOrders(String status, String type) {
        return repository.findAllOrders().stream()
                .filter(item -> status == null || status.isBlank()
                        || "ALL".equalsIgnoreCase(status)
                        || item.getStatus().equalsIgnoreCase(status))
                .filter(item -> type == null || type.isBlank()
                        || "ALL".equalsIgnoreCase(type)
                        || item.getOrderType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public Optional<TableReservationRecord> reservation(String reference) {
        return repository.findReservationByReference(reference);
    }

    public Optional<FoodOrderRecord> order(String reference) {
        return repository.findOrderByReference(reference);
    }

    public List<RestaurantTableRecord> findAvailableTables(LocalDate date, LocalTime time,
                                                            int partySize, String seatingArea,
                                                            String excludingReference) {
        if (date == null || time == null || partySize < 1) return Collections.emptyList();
        String area = normalize(seatingArea);
        return repository.findAllTables().stream()
                .filter(table -> table.getCapacity() >= partySize)
                .filter(table -> !"OUT_OF_SERVICE".equals(table.getStatus()))
                .filter(table -> "ANY".equals(area) || area.isEmpty()
                        || table.getSeatingArea().equals(area))
                .filter(table -> repository.isTableAvailable(
                        table.getId(), date, time, RESERVATION_SLOT_MINUTES, excludingReference))
                .sorted(Comparator.comparingInt(RestaurantTableRecord::getCapacity)
                        .thenComparing(RestaurantTableRecord::getCode))
                .collect(Collectors.toList());
    }

    public OperationResult<List<RestaurantTableRecord>> searchAvailableTables(
            LocalDate date, LocalTime time, int partySize, String seatingArea) {
        List<String> errors = new ArrayList<>();
        String area = normalize(seatingArea);
        if (date == null) errors.add("Select a valid reservation date.");
        if (time == null) errors.add("Select a valid reservation time.");
        if (partySize < 1 || partySize > 20) {
            errors.add("Party size must be between 1 and 20 guests.");
        }
        if (!SEATING_AREAS.contains(area)) errors.add("Select a valid seating preference.");
        if (date != null && time != null) {
            LocalDateTime selected = LocalDateTime.of(date, time);
            if (selected.isBefore(LocalDateTime.now().plusMinutes(30))) {
                errors.add("Availability can only be checked at least 30 minutes in advance.");
            }
            if (time.isBefore(LocalTime.of(11, 0)) || time.isAfter(LocalTime.of(22, 0))) {
                errors.add("Select a reservation time between 11:00 AM and 10:00 PM.");
            }
        }
        if (!errors.isEmpty()) return OperationResult.failure(errors);
        return OperationResult.success(findAvailableTables(date, time, partySize, area, null));
    }

    public synchronized OperationResult<TableReservationRecord> createReservation(
            String customerKey, String guestName, String email, String phone,
            LocalDate date, LocalTime time, int partySize,
            String seatingPreference, String occasionNotes) {

        List<String> errors = validateReservation(
                guestName, email, phone, date, time, partySize, seatingPreference,
                occasionNotes, null);

        boolean duplicate = repository.findReservationsByCustomer(customerKey).stream()
                .anyMatch(item -> item.getReservationDate().equals(date)
                        && item.getReservationTime().equals(time)
                        && !isReservationClosed(item.getStatus()));
        if (duplicate) {
            errors.add("You already have an active reservation request for the selected date and time.");
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        String reference = reference("DV-R-");
        TableReservationRecord record = new TableReservationRecord(
                repository.nextReservationId(), reference, customerKey,
                clean(guestName), clean(email), clean(phone), date, time, partySize,
                normalize(seatingPreference), clean(occasionNotes));
        repository.saveReservation(record);
        return OperationResult.success(record);
    }

    public synchronized OperationResult<TableReservationRecord> updateReservation(
            String customerKey, String reference, String guestName, String email, String phone,
            LocalDate date, LocalTime time, int partySize,
            String seatingPreference, String occasionNotes) {

        Optional<TableReservationRecord> optional = repository.findReservationByReference(reference);
        if (optional.isEmpty()) return OperationResult.failure("Reservation was not found.");

        TableReservationRecord record = optional.get();
        if (!record.getCustomerKey().equals(customerKey)) {
            return OperationResult.failure("You are not allowed to update this reservation.");
        }
        if (!"PENDING".equals(record.getStatus())) {
            return OperationResult.failure("Only pending reservations can be edited.");
        }

        List<String> errors = validateReservation(
                guestName, email, phone, date, time, partySize, seatingPreference,
                occasionNotes, reference);
        boolean duplicate = repository.findReservationsByCustomer(customerKey).stream()
                .filter(item -> !item.getReference().equals(reference))
                .anyMatch(item -> item.getReservationDate().equals(date)
                        && item.getReservationTime().equals(time)
                        && !isReservationClosed(item.getStatus()));
        if (duplicate) {
            errors.add("You already have another active reservation for the selected date and time.");
        }
        if (!errors.isEmpty()) return OperationResult.failure(errors);

        boolean allocationChanged = record.getTableId() != null
                && (!record.getReservationDate().equals(date)
                || !record.getReservationTime().equals(time)
                || record.getPartySize() != partySize
                || !record.getSeatingPreference().equals(normalize(seatingPreference)));
        record.updateCustomerDetails(clean(guestName), clean(email), clean(phone),
                date, time, partySize, normalize(seatingPreference), clean(occasionNotes));
        if (allocationChanged) record.clearTableAssignment(clean(guestName));
        repository.saveReservation(record);
        return OperationResult.success(record);
    }

    public OperationResult<TableReservationRecord> cancelReservation(
            String customerKey, String reference, String reason) {

        Optional<TableReservationRecord> optional = repository.findReservationByReference(reference);
        if (optional.isEmpty()) return OperationResult.failure("Reservation was not found.");

        TableReservationRecord record = optional.get();
        if (!record.getCustomerKey().equals(customerKey)) {
            return OperationResult.failure("You are not allowed to cancel this reservation.");
        }
        if (!Arrays.asList("PENDING", "CONFIRMED").contains(record.getStatus())) {
            return OperationResult.failure("This reservation can no longer be cancelled.");
        }

        LocalDateTime reservationAt = LocalDateTime.of(
                record.getReservationDate(), record.getReservationTime());
        if (Duration.between(LocalDateTime.now(), reservationAt).toMinutes() < 120) {
            return OperationResult.failure(
                    "Reservations must be cancelled at least two hours before the reserved time.");
        }

        String finalReason = clean(reason);
        if (finalReason.length() < 5) {
            return OperationResult.failure("Enter a short cancellation reason.");
        }
        if (finalReason.length() > 500) {
            return OperationResult.failure("Cancellation reason cannot exceed 500 characters.");
        }
        if (hasActiveLinkedOrders(reference)) {
            return OperationResult.failure(
                    "Cancel or complete linked food orders before cancelling this reservation.");
        }

        record.changeStatus("CANCELLED", finalReason, record.getGuestName());
        repository.saveReservation(record);
        return OperationResult.success(record);
    }

    public OperationResult<TableReservationRecord> staffUpdateReservation(
            String reference, long tableId, String newStatus, String note, String staffName) {

        Optional<TableReservationRecord> optional = repository.findReservationByReference(reference);
        if (optional.isEmpty()) return OperationResult.failure("Reservation was not found.");

        TableReservationRecord record = optional.get();
        String targetStatus = normalize(newStatus);
        String cleanNote = clean(note);
        List<String> errors = new ArrayList<>();

        if (!RESERVATION_STATUSES.contains(targetStatus)) {
            errors.add("Select a valid reservation status.");
        }
        if (isReservationClosed(record.getStatus())) {
            errors.add("Closed reservations cannot be updated.");
        }

        RestaurantTableRecord selectedTable = null;
        if (tableId > 0) {
            selectedTable = repository.findTable(tableId).orElse(null);
            if (selectedTable == null) {
                errors.add("The selected table does not exist.");
            } else if ("OUT_OF_SERVICE".equals(selectedTable.getStatus())) {
                errors.add("The selected table is out of service.");
            } else if (selectedTable.getCapacity() < record.getPartySize()) {
                errors.add("The selected table is too small for this party.");
            } else if (!"ANY".equals(record.getSeatingPreference())
                    && !record.getSeatingPreference().equals(selectedTable.getSeatingArea())) {
                errors.add("The selected table does not match the requested seating area.");
            } else if (!repository.isTableAvailable(
                    tableId, record.getReservationDate(), record.getReservationTime(),
                    RESERVATION_SLOT_MINUTES, record.getReference())) {
                errors.add("The selected table overlaps with another active reservation.");
            }
        }

        RestaurantTableRecord effectiveTable = selectedTable;
        if (effectiveTable == null && record.getTableId() != null) {
            effectiveTable = repository.findTable(record.getTableId()).orElse(null);
        }
        if ("CONFIRMED".equals(targetStatus)) {
            if (effectiveTable == null) {
                errors.add("Assign an available table before confirming the reservation.");
            } else if ("OUT_OF_SERVICE".equals(effectiveTable.getStatus())
                    || effectiveTable.getCapacity() < record.getPartySize()
                    || (!"ANY".equals(record.getSeatingPreference())
                    && !record.getSeatingPreference().equals(effectiveTable.getSeatingArea()))
                    || !repository.isTableAvailable(
                    effectiveTable.getId(), record.getReservationDate(), record.getReservationTime(),
                    RESERVATION_SLOT_MINUTES, record.getReference())) {
                errors.add("The assigned table is no longer suitable or available.");
            }
        }
        if (("REJECTED".equals(targetStatus) || "CANCELLED".equals(targetStatus))
                && cleanNote.length() < 5) {
            errors.add("Enter a reason before rejecting or cancelling.");
        }
        if (cleanNote.length() > 500) errors.add("Staff note cannot exceed 500 characters.");
        if (("REJECTED".equals(targetStatus) || "CANCELLED".equals(targetStatus)) && tableId > 0) {
            errors.add("Do not assign a table when closing a reservation.");
        }

        if (!isValidReservationTransition(record.getStatus(), targetStatus)) {
            errors.add("The requested reservation status change is not allowed.");
        }
        if (isReservationClosed(targetStatus) && hasActiveLinkedOrders(reference)) {
            errors.add("Cancel or complete linked food orders before closing this reservation.");
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        if (selectedTable != null) {
            record.assignTable(selectedTable.getId(), selectedTable.getCode(), staffName);
        }
        record.changeStatus(targetStatus, cleanNote, clean(staffName));
        repository.saveReservation(record);
        return OperationResult.success(record);
    }

    public List<CartLineRecord> cartLines(Map<Long, Integer> cart) {
        List<CartLineRecord> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : new LinkedHashMap<>(cart).entrySet()) {
            Optional<MenuItemRecord> item = repository.findMenuItem(entry.getKey());
            if (item.isEmpty() || !item.get().isAvailable()) {
                cart.remove(entry.getKey());
                continue;
            }
            int quantity = Math.max(1, Math.min(MAX_CART_QUANTITY, entry.getValue()));
            if (quantity != entry.getValue()) cart.put(entry.getKey(), quantity);
            lines.add(new CartLineRecord(item.get(), quantity));
        }
        return lines;
    }

    public BigDecimal cartSubtotal(Map<Long, Integer> cart) {
        return cartLines(cart).stream()
                .map(CartLineRecord::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public OperationResult<Integer> addCartItem(Map<Long, Integer> cart, long menuItemId, int quantity) {
        Optional<MenuItemRecord> optional = repository.findMenuItem(menuItemId);
        if (optional.isEmpty() || !optional.get().isAvailable()) {
            return OperationResult.failure("This menu item is currently unavailable.");
        }
        if (quantity < 1 || quantity > MAX_CART_QUANTITY) {
            return OperationResult.failure("Item quantity must be between 1 and " + MAX_CART_QUANTITY + ".");
        }
        int updated = cart.getOrDefault(menuItemId, 0) + quantity;
        if (updated > MAX_CART_QUANTITY) {
            return OperationResult.failure("The combined item quantity cannot exceed "
                    + MAX_CART_QUANTITY + ".");
        }
        cart.put(menuItemId, updated);
        return OperationResult.success(updated);
    }

    public OperationResult<Integer> updateCartItem(Map<Long, Integer> cart, long menuItemId, int quantity) {
        if (!cart.containsKey(menuItemId)) return OperationResult.failure("Cart item was not found.");
        if (quantity < 0) {
            return OperationResult.failure("Item quantity must be between 0 and "
                    + MAX_CART_QUANTITY + ".");
        }
        if (quantity == 0) {
            cart.remove(menuItemId);
            return OperationResult.success(0);
        }
        if (quantity > MAX_CART_QUANTITY) {
            return OperationResult.failure("Item quantity cannot exceed " + MAX_CART_QUANTITY + ".");
        }
        Optional<MenuItemRecord> item = repository.findMenuItem(menuItemId);
        if (item.isEmpty() || !item.get().isAvailable()) {
            cart.remove(menuItemId);
            return OperationResult.failure("This menu item is no longer available and was removed.");
        }
        cart.put(menuItemId, quantity);
        return OperationResult.success(quantity);
    }

    public synchronized OperationResult<FoodOrderRecord> createOrder(
            String customerKey, String customerName, String email, String phone,
            String orderType, String reservationReference, LocalDateTime requestedFor,
            String orderNotes, Map<Long, Integer> cart) {

        List<String> errors = new ArrayList<>();
        String type = normalize(orderType);

        if (clean(customerName).length() < 2) errors.add("Enter the customer name.");
        if (clean(customerName).length() > 160) errors.add("Customer name cannot exceed 160 characters.");
        if (!validEmail(email)) errors.add("Enter a valid email address.");
        if (clean(email).length() > 160) errors.add("Email address cannot exceed 160 characters.");
        if (!validPhone(phone)) errors.add("Enter a valid Sri Lankan mobile number.");
        if (!ORDER_TYPES.contains(type)) errors.add("Select a valid order type.");
        if (clean(orderNotes).length() > 500) errors.add("Order notes cannot exceed 500 characters.");

        List<CartLineRecord> cartLines = cartLines(cart);
        if (cartLines.isEmpty()) errors.add("Add at least one available menu item to the cart.");

        String linkedReservation = clean(reservationReference);
        if ("DINE_IN".equals(type) || "PRE_ORDER".equals(type)) {
            if (linkedReservation.isEmpty()) {
                errors.add("Select a confirmed reservation for dine-in or pre-order food.");
            } else {
                Optional<TableReservationRecord> reservation = repository
                        .findReservationByReference(linkedReservation);
                if (reservation.isEmpty()
                        || !reservation.get().getCustomerKey().equals(customerKey)
                        || !Arrays.asList("CONFIRMED", "SEATED").contains(reservation.get().getStatus())
                        || reservation.get().getTableId() == null) {
                    errors.add("The selected reservation is not eligible for this order.");
                } else {
                    LocalDateTime reservationAt = LocalDateTime.of(
                            reservation.get().getReservationDate(), reservation.get().getReservationTime());
                    if (reservationAt.isBefore(LocalDateTime.now().minusMinutes(30))) {
                        errors.add("The selected reservation time has already passed.");
                    }
                    requestedFor = reservationAt;
                }
            }
        }

        if ("TAKEAWAY".equals(type)) {
            if (requestedFor == null) errors.add("Select a takeaway collection time.");
            else if (requestedFor.isBefore(LocalDateTime.now().plusMinutes(30))) {
                errors.add("Takeaway collection time must be at least 30 minutes from now.");
            }
            linkedReservation = "";
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        List<OrderItemRecord> items = cartLines.stream()
                .map(line -> new OrderItemRecord(
                        line.getMenuItem().getId(), line.getMenuItem().getName(),
                        line.getQuantity(), line.getMenuItem().getPrice(), ""))
                .collect(Collectors.toList());

        FoodOrderRecord order = new FoodOrderRecord(
                repository.nextOrderId(), reference("DV-O-"), customerKey,
                clean(customerName), clean(email), clean(phone), type,
                linkedReservation, requestedFor, clean(orderNotes), items);
        repository.saveOrder(order);
        cart.clear();
        return OperationResult.success(order);
    }

    public OperationResult<FoodOrderRecord> cancelOrder(
            String customerKey, String reference, String reason) {

        Optional<FoodOrderRecord> optional = repository.findOrderByReference(reference);
        if (optional.isEmpty()) return OperationResult.failure("Food order was not found.");

        FoodOrderRecord order = optional.get();
        if (!order.getCustomerKey().equals(customerKey)) {
            return OperationResult.failure("You are not allowed to cancel this order.");
        }
        if (!Arrays.asList("PENDING", "CONFIRMED").contains(order.getStatus())) {
            return OperationResult.failure("The order cannot be cancelled after preparation begins.");
        }
        if (clean(reason).length() < 5) {
            return OperationResult.failure("Enter a short cancellation reason.");
        }
        if (clean(reason).length() > 255) {
            return OperationResult.failure("Cancellation reason cannot exceed 255 characters.");
        }

        order.changeStatus("CANCELLED", clean(reason), order.getCustomerName());
        repository.saveOrder(order);
        return OperationResult.success(order);
    }

    public OperationResult<FoodOrderRecord> staffUpdateOrder(
            String reference, String newStatus, String note, String staffName) {

        Optional<FoodOrderRecord> optional = repository.findOrderByReference(reference);
        if (optional.isEmpty()) return OperationResult.failure("Food order was not found.");

        FoodOrderRecord order = optional.get();
        String targetStatus = normalize(newStatus);
        String cleanNote = clean(note);
        List<String> errors = new ArrayList<>();

        if (!ORDER_STATUSES.contains(targetStatus)) errors.add("Select a valid order status.");
        if (Arrays.asList("COMPLETED", "CANCELLED", "REJECTED").contains(order.getStatus())) {
            errors.add("Closed food orders cannot be updated.");
        }
        if (("REJECTED".equals(targetStatus) || "CANCELLED".equals(targetStatus))
                && cleanNote.length() < 5) {
            errors.add("Enter a reason before rejecting or cancelling.");
        }
        if (cleanNote.length() > 255) errors.add("Staff note cannot exceed 255 characters.");
        if (!isValidOrderTransition(order.getStatus(), targetStatus, order.getOrderType())) {
            errors.add("The requested order status change is not allowed.");
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        order.changeStatus(targetStatus, cleanNote, clean(staffName));
        repository.saveOrder(order);
        return OperationResult.success(order);
    }

    public List<TableReservationRecord> eligibleReservations(String customerKey) {
        return repository.findReservationsByCustomer(customerKey).stream()
                .filter(item -> Arrays.asList("CONFIRMED", "SEATED").contains(item.getStatus()))
                .filter(item -> LocalDateTime.of(item.getReservationDate(), item.getReservationTime())
                        .isAfter(LocalDateTime.now().minusMinutes(30)))
                .collect(Collectors.toList());
    }

    public long countActiveReservations() {
        return repository.findAllReservations().stream()
                .filter(item -> !isReservationClosed(item.getStatus()))
                .count();
    }

    public long countActiveOrders() {
        return repository.findAllOrders().stream()
                .filter(item -> !Arrays.asList("COMPLETED", "CANCELLED", "REJECTED")
                        .contains(item.getStatus()))
                .count();
    }

    private List<String> validateReservation(
            String guestName, String email, String phone, LocalDate date,
            LocalTime time, int partySize, String seatingPreference,
            String occasionNotes, String excludingReference) {

        List<String> errors = new ArrayList<>();
        if (clean(guestName).length() < 2) errors.add("Enter the guest name.");
        if (clean(guestName).length() > 160) errors.add("Guest name cannot exceed 160 characters.");
        if (!validEmail(email)) errors.add("Enter a valid email address.");
        if (clean(email).length() > 160) errors.add("Email address cannot exceed 160 characters.");
        if (!validPhone(phone)) errors.add("Enter a valid Sri Lankan mobile number.");
        if (clean(occasionNotes).length() > 500) {
            errors.add("Special request cannot exceed 500 characters.");
        }
        if (date == null) errors.add("Select a valid reservation date.");
        if (time == null) errors.add("Select a valid reservation time.");
        if (partySize < 1 || partySize > 20) {
            errors.add("Party size must be between 1 and 20 guests.");
        }
        String area = normalize(seatingPreference);
        if (!SEATING_AREAS.contains(area)) errors.add("Select a valid seating preference.");

        if (date != null && time != null) {
            LocalDateTime selected = LocalDateTime.of(date, time);
            if (selected.isBefore(LocalDateTime.now().plusMinutes(30))) {
                errors.add("Reservations must be made at least 30 minutes in advance.");
            }
            if (time.isBefore(LocalTime.of(11, 0)) || time.isAfter(LocalTime.of(22, 0))) {
                errors.add("Select a reservation time between 11:00 AM and 10:00 PM.");
            }
        }

        boolean capacityAvailable = date != null && time != null && partySize > 0
                && !findAvailableTables(date, time, partySize, area, excludingReference).isEmpty();
        if (date != null && time != null && partySize > 0 && !capacityAvailable) {
            errors.add("No suitable table is available for the selected date, time, party size, and area.");
        }
        return errors;
    }

    private boolean isValidReservationTransition(String current, String target) {
        if (current.equals(target)) return true;
        switch (current) {
            case "PENDING":
                return Arrays.asList("CONFIRMED", "REJECTED", "CANCELLED").contains(target);
            case "CONFIRMED":
                return Arrays.asList("SEATED", "CANCELLED", "NO_SHOW").contains(target);
            case "SEATED":
                return "COMPLETED".equals(target);
            default:
                return false;
        }
    }

    private boolean isValidOrderTransition(String current, String target, String orderType) {
        if (current.equals(target)) return true;
        switch (current) {
            case "PENDING":
                return Arrays.asList("CONFIRMED", "REJECTED", "CANCELLED").contains(target);
            case "CONFIRMED":
                return Arrays.asList("PREPARING", "CANCELLED").contains(target);
            case "PREPARING":
                return "READY".equals(target);
            case "READY":
                if ("DINE_IN".equals(orderType)) return "SERVED".equals(target);
                return "COMPLETED".equals(target);
            case "SERVED":
                return "COMPLETED".equals(target);
            default:
                return false;
        }
    }

    private boolean isReservationClosed(String status) {
        return Arrays.asList("COMPLETED", "CANCELLED", "REJECTED", "NO_SHOW").contains(status);
    }

    private boolean hasActiveLinkedOrders(String reservationReference) {
        return repository.findAllOrders().stream()
                .filter(order -> reservationReference.equals(order.getReservationReference()))
                .anyMatch(order -> !Arrays.asList("COMPLETED", "CANCELLED", "REJECTED")
                        .contains(order.getStatus()));
    }

    private boolean validEmail(String value) {
        return clean(value).matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private boolean validPhone(String value) {
        return clean(value).matches("^(?:\\+94|0)7\\d{8}$");
    }

    private String reference(String prefix) {
        return prefix + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        return clean(value).toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
