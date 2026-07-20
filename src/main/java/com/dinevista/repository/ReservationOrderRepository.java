package com.dinevista.repository;

import com.dinevista.model.FoodOrderRecord;
import com.dinevista.model.MenuItemRecord;
import com.dinevista.model.NotificationRecord;
import com.dinevista.model.RestaurantTableRecord;
import com.dinevista.model.TableReservationRecord;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationOrderRepository {
    List<MenuItemRecord> findAllMenuItems();
    Optional<MenuItemRecord> findMenuItem(long id);
    List<RestaurantTableRecord> findAllTables();
    Optional<RestaurantTableRecord> findTable(long id);

    TableReservationRecord saveReservation(TableReservationRecord reservation);
    Optional<TableReservationRecord> findReservationByReference(String reference);
    List<TableReservationRecord> findReservationsByCustomer(String customerKey);
    List<TableReservationRecord> findAllReservations();
    boolean isTableAvailable(long tableId, LocalDate date, LocalTime time,
                             int slotMinutes, String excludingReference);

    FoodOrderRecord saveOrder(FoodOrderRecord order);
    Optional<FoodOrderRecord> findOrderByReference(String reference);
    List<FoodOrderRecord> findOrdersByCustomer(String customerKey);
    List<FoodOrderRecord> findAllOrders();

    NotificationRecord saveNotification(NotificationRecord notification);
    Optional<NotificationRecord> findNotification(long notificationId);
    List<NotificationRecord> findNotifications(String recipientKey, int limit);
    long countUnreadNotifications(String recipientKey);
    void markNotificationRead(long notificationId, String recipientKey);
    void markAllNotificationsRead(String recipientKey);
    void deleteNotifications(String recipientKey);

    long nextReservationId();
    long nextOrderId();
}
