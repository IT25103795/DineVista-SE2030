package com.dinevista.repository;

import com.dinevista.model.FoodOrderRecord;
import com.dinevista.model.MenuItemRecord;
import com.dinevista.model.NotificationRecord;
import com.dinevista.model.OrderItemRecord;
import com.dinevista.model.RestaurantTableRecord;
import com.dinevista.model.TableReservationRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryReservationOrderRepository implements ReservationOrderRepository {
    private final Map<Long, MenuItemRecord> menuItems = new LinkedHashMap<>();
    private final Map<Long, RestaurantTableRecord> tables = new LinkedHashMap<>();
    private final Map<String, TableReservationRecord> reservations = new LinkedHashMap<>();
    private final Map<String, FoodOrderRecord> orders = new LinkedHashMap<>();
    private final Map<Long, NotificationRecord> notifications = new LinkedHashMap<>();
    private final AtomicLong reservationIds = new AtomicLong(1000);
    private final AtomicLong orderIds = new AtomicLong(2000);
    private final AtomicLong notificationIds = new AtomicLong(3000);

    public InMemoryReservationOrderRepository() {
        seedMenu();
        seedTables();
        seedOperationalDemo();
    }

    private void seedMenu() {
        addMenu(new MenuItemRecord(1, "Signature", "Fire-Roasted Chicken",
                "Herb-marinated chicken, seasonal vegetables, coconut pepper sauce, and greens.",
                new BigDecimal("2450"), "dish-signature.svg", "REGULAR", "MEDIUM", true));
        addMenu(new MenuItemRecord(2, "Sri Lankan", "Island Curry Collection",
                "Chicken curry, dhal, vegetables, red rice, sambols, and papadam.",
                new BigDecimal("2150"), "dish-curry.svg", "REGULAR", "MEDIUM", true));
        addMenu(new MenuItemRecord(3, "Seafood", "Lagoon Grilled Fish",
                "Daily catch with lime butter, herb rice, and garden vegetables.",
                new BigDecimal("2850"), "dish-seafood.svg", "GLUTEN_AWARE", "MILD", true));
        addMenu(new MenuItemRecord(4, "Vegetarian", "Garden Harvest Bowl",
                "Roasted vegetables, chickpeas, avocado, red rice, and citrus dressing.",
                new BigDecimal("1850"), "dish-curry.svg", "VEGAN", "MILD", true));
        addMenu(new MenuItemRecord(5, "Desserts", "Ceylon Cocoa Slice",
                "Dark chocolate mousse, vanilla cream, berries, and cocoa crumble.",
                new BigDecimal("1150"), "dish-dessert.svg", "VEGETARIAN", "NONE", true));
        addMenu(new MenuItemRecord(6, "Signature", "DineVista Sharing Platter",
                "Fire-grilled chicken, lagoon prawns, vegetable skewers, sauces, and flatbread.",
                new BigDecimal("4650"), "dish-signature.svg", "REGULAR", "MEDIUM", true));
        addMenu(new MenuItemRecord(7, "Sri Lankan", "Kottu Vista",
                "Chopped roti, vegetables, egg, chicken, aromatic spices, and curry sauce.",
                new BigDecimal("1750"), "dish-curry.svg", "REGULAR", "HOT", true));
        addMenu(new MenuItemRecord(8, "Desserts", "Tropical Pavlova",
                "Crisp meringue, vanilla cream, mango, passion fruit, and toasted coconut.",
                new BigDecimal("1250"), "dish-dessert.svg", "VEGETARIAN", "NONE", true));
    }

    private void addMenu(MenuItemRecord item) {
        menuItems.put(item.getId(), item);
    }

    private void seedTables() {
        addTable(new RestaurantTableRecord(1, "I-01", "INDOOR", 2, "AVAILABLE"));
        addTable(new RestaurantTableRecord(2, "I-02", "INDOOR", 4, "AVAILABLE"));
        addTable(new RestaurantTableRecord(3, "I-03", "INDOOR", 6, "AVAILABLE"));
        addTable(new RestaurantTableRecord(4, "G-01", "GARDEN", 4, "AVAILABLE"));
        addTable(new RestaurantTableRecord(5, "G-02", "GARDEN", 6, "AVAILABLE"));
        addTable(new RestaurantTableRecord(6, "P-01", "PRIVATE_DINING", 12, "AVAILABLE"));
        addTable(new RestaurantTableRecord(7, "P-02", "PRIVATE_DINING", 20, "AVAILABLE"));
        addTable(new RestaurantTableRecord(8, "C-01", "CHEF_COUNTER", 2, "AVAILABLE"));
    }

    private void addTable(RestaurantTableRecord table) {
        tables.put(table.getId(), table);
    }

    private void seedOperationalDemo() {
        TableReservationRecord confirmed = new TableReservationRecord(
                nextReservationId(), "DV-R-DEMO01", "demo-customer-1",
                "N. Perera", "n.perera@example.com", "0771234567",
                LocalDate.now().plusDays(1), LocalTime.of(18, 30), 4,
                "GARDEN", "Anniversary dinner");
        confirmed.assignTable(4L, "G-01", "Operations Manager");
        confirmed.changeStatus("CONFIRMED", "Reservation confirmed and table assigned.", "Operations Manager");
        saveReservation(confirmed);

        TableReservationRecord pending = new TableReservationRecord(
                nextReservationId(), "DV-R-DEMO02", "demo-customer-2",
                "A. Silva", "a.silva@example.com", "0712345678",
                LocalDate.now().plusDays(2), LocalTime.of(19, 30), 8,
                "PRIVATE_DINING", "Birthday cake service requested");
        saveReservation(pending);

        List<OrderItemRecord> demoItems = new ArrayList<>();
        demoItems.add(new OrderItemRecord(1, "Fire-Roasted Chicken", 2, new BigDecimal("2450"), ""));
        demoItems.add(new OrderItemRecord(5, "Ceylon Cocoa Slice", 2, new BigDecimal("1150"), ""));
        FoodOrderRecord order = new FoodOrderRecord(
                nextOrderId(), "DV-O-DEMO01", "demo-customer-1",
                "N. Perera", "n.perera@example.com", "0771234567",
                "DINE_IN", "DV-R-DEMO01", LocalDateTime.now().plusDays(1).withHour(18).withMinute(30),
                "Serve dessert after the main course.", demoItems);
        order.changeStatus("CONFIRMED", "Order accepted by restaurant staff.", "Operations Manager");
        saveOrder(order);
    }

    @Override
    public synchronized List<MenuItemRecord> findAllMenuItems() {
        return new ArrayList<>(menuItems.values());
    }

    @Override
    public synchronized Optional<MenuItemRecord> findMenuItem(long id) {
        return Optional.ofNullable(menuItems.get(id));
    }

    @Override
    public synchronized List<RestaurantTableRecord> findAllTables() {
        return new ArrayList<>(tables.values());
    }

    @Override
    public synchronized Optional<RestaurantTableRecord> findTable(long id) {
        return Optional.ofNullable(tables.get(id));
    }

    @Override
    public synchronized TableReservationRecord saveReservation(TableReservationRecord reservation) {
        reservations.put(reservation.getReference(), reservation);
        return reservation;
    }

    @Override
    public synchronized Optional<TableReservationRecord> findReservationByReference(String reference) {
        return Optional.ofNullable(reservations.get(reference));
    }

    @Override
    public synchronized List<TableReservationRecord> findReservationsByCustomer(String customerKey) {
        return reservations.values().stream()
                .filter(item -> item.getCustomerKey().equals(customerKey))
                .sorted(Comparator.comparing(TableReservationRecord::getReservationDate)
                        .thenComparing(TableReservationRecord::getReservationTime)
                        .reversed())
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<TableReservationRecord> findAllReservations() {
        return reservations.values().stream()
                .sorted(Comparator.comparing(TableReservationRecord::getReservationDate)
                        .thenComparing(TableReservationRecord::getReservationTime))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized boolean isTableAvailable(long tableId, LocalDate date, LocalTime time,
                                                  int slotMinutes, String excludingReference) {
        RestaurantTableRecord table = tables.get(tableId);
        if (table == null || "OUT_OF_SERVICE".equals(table.getStatus())) return false;

        LocalDateTime requested = LocalDateTime.of(date, time);
        for (TableReservationRecord reservation : reservations.values()) {
            if (excludingReference != null && excludingReference.equals(reservation.getReference())) continue;
            if (reservation.getTableId() == null || reservation.getTableId() != tableId) continue;
            if (!reservation.getReservationDate().equals(date)) continue;
            if ("CANCELLED".equals(reservation.getStatus())
                    || "REJECTED".equals(reservation.getStatus())
                    || "COMPLETED".equals(reservation.getStatus())
                    || "NO_SHOW".equals(reservation.getStatus())) continue;

            LocalDateTime existing = LocalDateTime.of(
                    reservation.getReservationDate(), reservation.getReservationTime());
            long minutes = Math.abs(java.time.Duration.between(existing, requested).toMinutes());
            if (minutes < slotMinutes) return false;
        }
        return true;
    }

    @Override
    public synchronized FoodOrderRecord saveOrder(FoodOrderRecord order) {
        orders.put(order.getReference(), order);
        return order;
    }

    @Override
    public synchronized Optional<FoodOrderRecord> findOrderByReference(String reference) {
        return Optional.ofNullable(orders.get(reference));
    }

    @Override
    public synchronized List<FoodOrderRecord> findOrdersByCustomer(String customerKey) {
        return orders.values().stream()
                .filter(item -> item.getCustomerKey().equals(customerKey))
                .sorted(Comparator.comparing(FoodOrderRecord::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<FoodOrderRecord> findAllOrders() {
        return orders.values().stream()
                .sorted(Comparator.comparing(FoodOrderRecord::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public synchronized NotificationRecord saveNotification(NotificationRecord notification) {
        NotificationRecord stored = notification.getId() > 0
                ? notification : notification.withId(notificationIds.incrementAndGet());
        notifications.put(stored.getId(), stored);
        return stored;
    }

    @Override
    public synchronized Optional<NotificationRecord> findNotification(long notificationId) {
        return Optional.ofNullable(notifications.get(notificationId));
    }

    @Override
    public synchronized List<NotificationRecord> findNotifications(String recipientKey, int limit) {
        return notifications.values().stream()
                .filter(item -> item.getRecipientKey().equals(recipientKey))
                .sorted(Comparator.comparing(NotificationRecord::getCreatedAt).reversed()
                        .thenComparing(Comparator.comparingLong(NotificationRecord::getId).reversed()))
                .limit(Math.max(1, limit))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized long countUnreadNotifications(String recipientKey) {
        return notifications.values().stream()
                .filter(item -> item.getRecipientKey().equals(recipientKey) && !item.isRead())
                .count();
    }

    @Override
    public synchronized void markNotificationRead(long notificationId, String recipientKey) {
        NotificationRecord notification = notifications.get(notificationId);
        if (notification != null && notification.getRecipientKey().equals(recipientKey)) {
            notifications.put(notificationId, notification.asRead());
        }
    }

    @Override
    public synchronized void markAllNotificationsRead(String recipientKey) {
        notifications.replaceAll((id, notification) ->
                notification.getRecipientKey().equals(recipientKey)
                        ? notification.asRead() : notification);
    }

    @Override
    public synchronized void deleteNotifications(String recipientKey) {
        notifications.entrySet().removeIf(entry ->
                entry.getValue().getRecipientKey().equals(recipientKey));
    }

    @Override
    public long nextReservationId() {
        return reservationIds.incrementAndGet();
    }

    @Override
    public long nextOrderId() {
        return orderIds.incrementAndGet();
    }
}
