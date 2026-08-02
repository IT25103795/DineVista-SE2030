package com.dinevista.service;

import com.dinevista.model.FoodOrderRecord;
import com.dinevista.model.NotificationRecord;
import com.dinevista.model.RestaurantTableRecord;
import com.dinevista.model.TableReservationRecord;
import com.dinevista.repository.InMemoryReservationOrderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dependency-free business-rule regression suite. Run with assertions enabled or
 * invoke main directly; failures throw AssertionError and return a non-zero exit.
 */
public final class ReservationOrderServiceSelfTest {
    private static int checks;

    public static void main(String[] args) {
        InMemoryReservationOrderRepository repository = new InMemoryReservationOrderRepository();
        ReservationOrderService service = new ReservationOrderService(repository);
        LocalDate base = LocalDate.now().plusDays(10);

        reservationCrudAndValidation(service, base);
        reservationStaffWorkflow(service, base.plusDays(10));
        cartAndOrderCrud(service, base.plusDays(20));
        linkedOrderRules(service, base.plusDays(30));
        notificationWorkflow(service, base.plusDays(40));
        proposalAlignmentRegressions(service, base.plusDays(50));
        orderTypeEligibilityBoundary();

        System.out.println("DineVista reservation/order self-test passed: " + checks + " checks.");
    }

    private static void reservationCrudAndValidation(ReservationOrderService service, LocalDate base) {
        ok(!service.searchAvailableTables(LocalDate.now().minusDays(1), LocalTime.of(18, 30),
                2, "INDOOR").isSuccess(), "past availability search blocked");
        ok(!service.searchAvailableTables(base, LocalTime.of(5, 0),
                2, "INDOOR").isSuccess(), "out-of-hours availability search blocked");
        ok(service.searchAvailableTables(base, LocalTime.of(18, 30),
                2, "INDOOR").isSuccess(), "valid availability search");

        OperationResult<TableReservationRecord> created = reservation(
                service, "customer-a", base, LocalTime.of(18, 30), "INDOOR");
        ok(created.isSuccess(), "reservation create");
        ok(created.getValue().getStaffNote().isEmpty()
                        && created.getValue().getCancellationReason().isEmpty(),
                "new reservation detail fields are render-safe");
        String reference = created.getValue().getReference();
        ok(service.reservation(reference).isPresent(), "reservation read");

        OperationResult<TableReservationRecord> duplicate = reservation(
                service, "customer-a", base, LocalTime.of(18, 30), "INDOOR");
        ok(!duplicate.isSuccess(), "duplicate reservation blocked");

        OperationResult<TableReservationRecord> second = reservation(
                service, "customer-a", base.plusDays(1), LocalTime.of(19, 30), "INDOOR");
        ok(second.isSuccess(), "second reservation create");
        OperationResult<TableReservationRecord> duplicateUpdate = service.updateReservation(
                "customer-a", second.getValue().getReference(), "Guest User", "guest@example.com",
                "0771234567", base, LocalTime.of(18, 30), 2, "INDOOR", "Updated request");
        ok(!duplicateUpdate.isSuccess(), "duplicate reservation update blocked");

        OperationResult<TableReservationRecord> updated = service.updateReservation(
                "customer-a", reference, "Guest User", "guest@example.com", "0771234567",
                base.plusDays(2), LocalTime.of(20, 30), 2, "INDOOR", "Window seat");
        ok(updated.isSuccess(), "reservation update");
        ok(updated.getValue().getReservationDate().equals(base.plusDays(2)), "updated date persisted");

        OperationResult<TableReservationRecord> unauthorized = service.updateReservation(
                "other-customer", reference, "Intruder", "other@example.com", "0712345678",
                base.plusDays(3), LocalTime.of(20, 30), 2, "INDOOR", "");
        ok(!unauthorized.isSuccess(), "reservation ownership enforced");

        OperationResult<TableReservationRecord> tooLong = service.createReservation(
                "customer-long", "Guest User", "guest@example.com", "0771234567",
                base.plusDays(4), LocalTime.of(18, 30), 2, "INDOOR", "x".repeat(501));
        ok(!tooLong.isSuccess(), "reservation database length protected");
        ok(!service.createReservation("invalid-customer", "A", "invalid-email", "123",
                LocalDate.now().minusDays(1), LocalTime.of(5, 0), 25, "ROOFTOP", "").isSuccess(),
                "invalid reservation fields blocked");

        OperationResult<TableReservationRecord> cancelled = service.cancelReservation(
                "customer-a", reference, "Plans changed");
        ok(cancelled.isSuccess(), "reservation controlled delete/cancel");
        ok("CANCELLED".equals(cancelled.getValue().getStatus()), "reservation cancellation persisted");
    }

    private static void reservationStaffWorkflow(ReservationOrderService service, LocalDate base) {
        OperationResult<TableReservationRecord> pending = reservation(
                service, "customer-staff", base, LocalTime.of(18, 30), "INDOOR");
        ok(pending.isSuccess(), "staff workflow reservation created");
        String reference = pending.getValue().getReference();

        OperationResult<TableReservationRecord> assignedPending = service.staffUpdateReservation(
                reference, 1, "PENDING", "Table held while reviewing", "Manager");
        ok(assignedPending.isSuccess(), "pending table assignment");
        OperationResult<TableReservationRecord> selfEdit = service.updateReservation(
                "customer-staff", reference, "Guest User", "guest@example.com", "0771234567",
                base, LocalTime.of(18, 30), 2, "INDOOR", "Still attending");
        ok(selfEdit.isSuccess(), "assigned reservation excludes itself during availability check");

        OperationResult<TableReservationRecord> confirmed = service.staffUpdateReservation(
                reference, 0, "CONFIRMED", "Confirmed", "Manager");
        ok(confirmed.isSuccess(), "reservation confirm with retained table");
        ok(service.staffUpdateReservation(reference, 0, "SEATED", "Guests arrived", "Host").isSuccess(),
                "reservation seat transition");
        ok(service.staffUpdateReservation(reference, 0, "COMPLETED", "Visit complete", "Host").isSuccess(),
                "reservation complete transition");
        ok(service.findAvailableTables(base, LocalTime.of(18, 30), 2, "INDOOR", null)
                        .stream().anyMatch(table -> table.getId() == 1),
                "completed reservation releases table availability");
        ok(!service.staffUpdateReservation(reference, 0, "CONFIRMED", "Reopen", "Manager").isSuccess(),
                "closed reservation cannot reopen");

        OperationResult<TableReservationRecord> reschedule = reservation(
                service, "customer-reschedule", base.plusDays(4), LocalTime.of(18, 30), "INDOOR");
        ok(reschedule.isSuccess(), "reschedule test reservation created");
        ok(service.staffUpdateReservation(reschedule.getValue().getReference(), 1,
                "PENDING", "Temporary assignment", "Manager").isSuccess(),
                "reschedule test table assigned");
        OperationResult<TableReservationRecord> rescheduled = service.updateReservation(
                "customer-reschedule", reschedule.getValue().getReference(), "Guest User",
                "guest@example.com", "0771234567", base.plusDays(5), LocalTime.of(19, 30),
                4, "INDOOR", "Schedule changed");
        ok(rescheduled.isSuccess(), "assigned pending reservation rescheduled");
        ok(rescheduled.getValue().getTableId() == null,
                "table assignment released after schedule/capacity change");

        OperationResult<TableReservationRecord> mismatched = reservation(
                service, "customer-garden", base.plusDays(1), LocalTime.of(18, 30), "GARDEN");
        ok(mismatched.isSuccess(), "garden reservation created");
        ok(!service.staffUpdateReservation(mismatched.getValue().getReference(), 1,
                        "CONFIRMED", "Wrong area", "Manager").isSuccess(),
                "mismatched seating area blocked");

        RestaurantTableRecord tableTwo = service.tables().stream()
                .filter(table -> table.getId() == 2).findFirst().orElseThrow();
        tableTwo.setStatus("OUT_OF_SERVICE");
        OperationResult<TableReservationRecord> maintenance = reservation(
                service, "customer-maintenance", base.plusDays(2), LocalTime.of(18, 30), "INDOOR");
        ok(maintenance.isSuccess(), "maintenance test reservation created");
        ok(!service.staffUpdateReservation(maintenance.getValue().getReference(), 2,
                        "CONFIRMED", "Assign unavailable table", "Manager").isSuccess(),
                "out-of-service table blocked");
        tableTwo.setStatus("AVAILABLE");

        OperationResult<TableReservationRecord> noTable = reservation(
                service, "customer-no-table", base.plusDays(6), LocalTime.of(18, 30), "INDOOR");
        ok(noTable.isSuccess(), "unassigned confirmation test created");
        ok(!service.staffUpdateReservation(noTable.getValue().getReference(), 0,
                "CONFIRMED", "Confirm", "Manager").isSuccess(),
                "confirmation without a table blocked");

        OperationResult<TableReservationRecord> largeParty = service.createReservation(
                "customer-large", "Large Party", "large@example.com", "0771234567",
                base.plusDays(7), LocalTime.of(18, 30), 6, "INDOOR", "");
        ok(largeParty.isSuccess(), "large party reservation created");
        ok(!service.staffUpdateReservation(largeParty.getValue().getReference(), 1,
                "CONFIRMED", "Too small", "Manager").isSuccess(), "undersized table blocked");

        OperationResult<TableReservationRecord> overlapOne = reservation(
                service, "overlap-one", base.plusDays(8), LocalTime.of(18, 30), "INDOOR");
        OperationResult<TableReservationRecord> overlapTwo = reservation(
                service, "overlap-two", base.plusDays(8), LocalTime.of(18, 30), "INDOOR");
        ok(overlapOne.isSuccess() && overlapTwo.isSuccess(), "overlap test reservations created");
        ok(service.staffUpdateReservation(overlapOne.getValue().getReference(), 1,
                "CONFIRMED", "First table", "Manager").isSuccess(), "first table assignment accepted");
        ok(!service.staffUpdateReservation(overlapTwo.getValue().getReference(), 1,
                "CONFIRMED", "Conflicting table", "Manager").isSuccess(),
                "overlapping table assignment blocked");

        OperationResult<TableReservationRecord> rejected = reservation(
                service, "customer-reject", base.plusDays(9), LocalTime.of(18, 30), "INDOOR");
        ok(rejected.isSuccess(), "rejection test reservation created");
        ok(!service.staffUpdateReservation(rejected.getValue().getReference(), 0,
                "REJECTED", "No", "Manager").isSuccess(), "reservation rejection reason required");
        ok(service.staffUpdateReservation(rejected.getValue().getReference(), 0,
                "REJECTED", "Restaurant unavailable", "Manager").isSuccess(),
                "reservation rejection with reason");
    }

    private static void cartAndOrderCrud(ReservationOrderService service, LocalDate base) {
        Map<Long, Integer> cart = new LinkedHashMap<>();
        ok(!service.addCartItem(cart, 1, 0).isSuccess(), "zero add quantity blocked");
        service.menuItem(3).orElseThrow().setAvailable(false);
        ok(!service.addCartItem(cart, 3, 1).isSuccess(), "unavailable menu item blocked");
        service.menuItem(3).orElseThrow().setAvailable(true);
        ok(service.addCartItem(cart, 1, 2).isSuccess(), "cart create item");
        ok(!service.addCartItem(cart, 1, 9).isSuccess(), "combined cart maximum enforced");
        ok(service.updateCartItem(cart, 1, 4).isSuccess(), "cart quantity update");
        ok(cart.get(1L) == 4, "cart quantity persisted");
        ok(!service.updateCartItem(cart, 1, -1).isSuccess(), "negative cart quantity blocked");
        ok(cart.get(1L) == 4, "invalid quantity does not remove cart line");
        ok(!service.updateCartItem(cart, 999, 0).isSuccess(), "missing cart removal reports failure");

        Map<Long, Integer> removable = new LinkedHashMap<>();
        service.addCartItem(removable, 5, 1);
        ok(service.updateCartItem(removable, 5, 0).isSuccess() && removable.isEmpty(),
                "cart item delete/remove");

        Map<Long, Integer> missingTimeCart = new LinkedHashMap<>();
        service.addCartItem(missingTimeCart, 1, 1);
        ok(!service.createOrder("missing-time", "Order Guest", "order@example.com", "0771234567",
                "TAKEAWAY", "", null, "", missingTimeCart).isSuccess(),
                "takeaway collection time required");
        ok(!service.createOrder("bad-note", "Order Guest", "order@example.com", "0771234567",
                "TAKEAWAY", "", LocalDateTime.of(base, LocalTime.of(18, 30)),
                "x".repeat(501), missingTimeCart).isSuccess(), "order note length protected");

        OperationResult<FoodOrderRecord> tooSoon = service.createOrder(
                "order-customer", "Order Guest", "order@example.com", "0771234567",
                "TAKEAWAY", "", LocalDateTime.now().plusMinutes(10), "", cart);
        ok(!tooSoon.isSuccess(), "takeaway minimum lead time enforced");

        OperationResult<FoodOrderRecord> created = service.createOrder(
                "order-customer", "Order Guest", "order@example.com", "0771234567",
                "TAKEAWAY", "", LocalDateTime.of(base, LocalTime.of(18, 30)), "No cutlery", cart);
        ok(created.isSuccess(), "takeaway order create");
        ok(created.getValue().getStaffNote().isEmpty()
                        && created.getValue().getCancellationReason().isEmpty(),
                "new order detail fields are render-safe");
        ok(cart.isEmpty(), "cart cleared only after successful checkout");
        String reference = created.getValue().getReference();
        ok(service.order(reference).isPresent(), "food order read");
        ok(service.ordersForCustomer("order-customer").stream()
                .anyMatch(order -> order.getReference().equals(reference)), "food order list read");

        ok(service.staffUpdateOrder(reference, "CONFIRMED", "Accepted", "Manager").isSuccess(),
                "order confirm update");
        ok(service.staffUpdateOrder(reference, "PREPARING", "Kitchen started", "Chef").isSuccess(),
                "order preparing update");
        ok(!service.cancelOrder("order-customer", reference, "Changed plans").isSuccess(),
                "customer cancellation blocked after preparation");
        ok(service.staffUpdateOrder(reference, "READY", "Ready", "Chef").isSuccess(),
                "order ready update");
        ok(service.staffUpdateOrder(reference, "COMPLETED", "Collected", "Cashier").isSuccess(),
                "takeaway completes from ready");
        ok(!service.staffUpdateOrder(reference, "CONFIRMED", "Reopen", "Manager").isSuccess(),
                "closed order cannot reopen");

        Map<Long, Integer> cancelCart = new LinkedHashMap<>();
        service.addCartItem(cancelCart, 5, 1);
        OperationResult<FoodOrderRecord> cancellable = service.createOrder(
                "cancel-customer", "Cancel Guest", "cancel@example.com", "0712345678",
                "TAKEAWAY", "", LocalDateTime.of(base.plusDays(1), LocalTime.of(19, 30)), "", cancelCart);
        ok(cancellable.isSuccess(), "cancellable order create");
        ok(service.cancelOrder("cancel-customer", cancellable.getValue().getReference(),
                "Ordered by mistake").isSuccess(), "food order controlled delete/cancel");
        ok(service.allOrders("CANCELLED", "TAKEAWAY").stream()
                .anyMatch(order -> order.getReference().equals(cancellable.getValue().getReference())),
                "staff order status/type filters");
    }

    private static void linkedOrderRules(ReservationOrderService service, LocalDate base) {
        OperationResult<TableReservationRecord> reservation = reservation(
                service, "linked-customer", base, LocalTime.of(19, 30), "GARDEN");
        ok(reservation.isSuccess(), "linked reservation create");
        String reservationReference = reservation.getValue().getReference();
        ok(service.staffUpdateReservation(reservationReference, 4, "CONFIRMED",
                "Table confirmed", "Manager").isSuccess(), "linked reservation confirm");
        ok(service.eligiblePreOrderReservations("linked-customer").stream()
                        .anyMatch(item -> item.getReference().equals(reservationReference)),
                "confirmed table appears for pre-order");
        ok(service.eligibleDineInReservations("linked-customer").stream()
                        .noneMatch(item -> item.getReference().equals(reservationReference)),
                "confirmed but unseated table excluded from dine-in");

        Map<Long, Integer> cart = new LinkedHashMap<>();
        service.addCartItem(cart, 2, 2);
        LocalDateTime manipulated = LocalDateTime.of(base.plusDays(20), LocalTime.NOON);
        ok(!service.createOrder(
                "linked-customer", "Linked Guest", "linked@example.com", "0771234567",
                "DINE_IN", reservationReference, manipulated, "Too early", cart).isSuccess(),
                "dine-in order blocked before seating");
        ok(!cart.isEmpty(), "failed dine-in attempt preserves cart");
        OperationResult<FoodOrderRecord> linked = service.createOrder(
                "linked-customer", "Linked Guest", "linked@example.com", "0771234567",
                "PRE_ORDER", reservationReference, manipulated, "Serve at table", cart);
        ok(linked.isSuccess(), "linked pre-order create");
        ok(linked.getValue().getRequestedFor().equals(LocalDateTime.of(base, LocalTime.of(19, 30))),
                "linked order time forced to reservation slot");
        ok(linked.getValue().getServiceCharge().signum() > 0,
                "pre-order restaurant service charge calculated server-side");
        Map<Long, Integer> foreignCart = new LinkedHashMap<>();
        service.addCartItem(foreignCart, 2, 1);
        ok(!service.createOrder("other-customer", "Other Guest", "other@example.com", "0712345678",
                "PRE_ORDER", reservationReference, null, "", foreignCart).isSuccess(),
                "linked reservation ownership enforced");
        ok(!service.cancelReservation("linked-customer", reservationReference,
                "Visit cancelled").isSuccess(), "reservation closure blocked by active linked order");
        ok(service.cancelOrder("linked-customer", linked.getValue().getReference(),
                "Cancel food first").isSuccess(), "linked pending order cancelled first");
        ok(service.cancelReservation("linked-customer", reservationReference,
                "Visit cancelled").isSuccess(), "reservation cancels after linked order closes");

        OperationResult<TableReservationRecord> diningReservation = reservation(
                service, "dining-customer", base.plusDays(1), LocalTime.of(18, 30), "GARDEN");
        ok(diningReservation.isSuccess(), "dine-in lifecycle reservation created");
        ok(service.staffUpdateReservation(diningReservation.getValue().getReference(), 4,
                "CONFIRMED", "Confirmed", "Manager").isSuccess(),
                "dine-in lifecycle reservation confirmed");
        Map<Long, Integer> diningCart = new LinkedHashMap<>();
        service.addCartItem(diningCart, 1, 1);
        ok(!service.createOrder(
                "dining-customer", "Dining Guest", "dining@example.com", "0771234567",
                "DINE_IN", diningReservation.getValue().getReference(), null, "", diningCart).isSuccess(),
                "confirmed reservation cannot place dine-in order before seating");
        ok(service.staffUpdateReservation(diningReservation.getValue().getReference(), 0,
                "SEATED", "Guests arrived", "Host").isSuccess(),
                "linked dining reservation seated");
        ok(service.eligibleDineInReservations("dining-customer").stream()
                        .anyMatch(item -> item.getReference().equals(
                                diningReservation.getValue().getReference())),
                "seated table appears for dine-in");
        ok(service.eligiblePreOrderReservations("dining-customer").stream()
                        .noneMatch(item -> item.getReference().equals(
                                diningReservation.getValue().getReference())),
                "seated table excluded from pre-order");
        ok(!service.createOrder(
                "dining-customer", "Dining Guest", "dining@example.com", "0771234567",
                "PRE_ORDER", diningReservation.getValue().getReference(), null, "", diningCart).isSuccess(),
                "pre-order blocked after seating");
        OperationResult<FoodOrderRecord> diningOrder = service.createOrder(
                "dining-customer", "Dining Guest", "dining@example.com", "0771234567",
                "DINE_IN", diningReservation.getValue().getReference(), null, "", diningCart);
        ok(diningOrder.isSuccess(), "seated dine-in order created");
        ok(diningOrder.getValue().getServiceCharge().signum() > 0,
                "dine-in service charge calculated server-side");
        String diningReference = diningOrder.getValue().getReference();
        ok(service.staffUpdateReservation(diningReservation.getValue().getReference(), 0,
                "COMPLETED", "Dining visit complete", "Manager").isSuccess(),
                "seated reservation completes while linked order remains active");
        ok("PENDING".equals(service.order(diningReference).orElseThrow().getStatus()),
                "reservation completion preserves independent order workflow");
        ok(!service.staffUpdateOrder(diningReference, "PREPARING", "Skip", "Manager").isSuccess(),
                "invalid order status jump blocked");
        ok(service.staffUpdateOrder(diningReference, "CONFIRMED", "Accepted", "Manager").isSuccess(),
                "dine-in order confirmed");
        ok(service.staffUpdateOrder(diningReference, "PREPARING", "Cooking", "Chef").isSuccess(),
                "dine-in order preparing");
        ok(service.staffUpdateOrder(diningReference, "READY", "Ready", "Chef").isSuccess(),
                "dine-in order ready");
        ok(!service.staffUpdateOrder(diningReference, "COMPLETED", "Skip served", "Manager").isSuccess(),
                "dine-in cannot skip served status");
        ok(service.staffUpdateOrder(diningReference, "SERVED", "Served", "Waiter").isSuccess(),
                "dine-in order served");
        ok(service.staffUpdateOrder(diningReference, "COMPLETED", "Completed", "Waiter").isSuccess(),
                "dine-in order completed");
    }

    private static void notificationWorkflow(ReservationOrderService service, LocalDate base) {
        String customerKey = "notify-customer";
        long managerUnreadBefore = service.unreadNotificationCount(
                ReservationOrderService.MANAGER_NOTIFICATION_KEY);

        OperationResult<TableReservationRecord> reservation = reservation(
                service, customerKey, base, LocalTime.of(19, 30), "INDOOR");
        ok(reservation.isSuccess(), "notification reservation created");
        String reservationReference = reservation.getValue().getReference();
        ok(service.unreadNotificationCount(ReservationOrderService.MANAGER_NOTIFICATION_KEY)
                        == managerUnreadBefore + 1,
                "manager receives new reservation notification");

        NotificationRecord managerReservationNotification = service.notifications(
                        ReservationOrderService.MANAGER_NOTIFICATION_KEY, 50).stream()
                .filter(item -> reservationReference.equals(item.getReferenceCode()))
                .findFirst().orElseThrow();
        ok(managerReservationNotification.getActionPath().startsWith("/staff/reservations/view"),
                "manager reservation notification links to staff record");
        ok(service.openNotification(managerReservationNotification.getId(), customerKey).isEmpty(),
                "notification ownership blocks another user");
        ok(service.openNotification(managerReservationNotification.getId(),
                        ReservationOrderService.MANAGER_NOTIFICATION_KEY).isPresent(),
                "manager notification opens and marks read");

        ok(service.staffUpdateReservation(reservationReference, 1, "CONFIRMED",
                "Confirmed for notification test", "Manager").isSuccess(),
                "notification reservation confirmed");
        NotificationRecord customerReservationNotification = service.notifications(customerKey, 20).stream()
                .filter(item -> reservationReference.equals(item.getReferenceCode()))
                .findFirst().orElseThrow();
        ok(!customerReservationNotification.isRead()
                        && customerReservationNotification.getActionPath().startsWith("/reservations/view"),
                "customer receives linked reservation status notification");

        Map<Long, Integer> cart = new LinkedHashMap<>();
        service.addCartItem(cart, 1, 1);
        long managerUnreadBeforeOrder = service.unreadNotificationCount(
                ReservationOrderService.MANAGER_NOTIFICATION_KEY);
        OperationResult<FoodOrderRecord> order = service.createOrder(
                customerKey, "Notify Guest", "notify@example.com", "0771234567",
                "TAKEAWAY", "", LocalDateTime.of(base.plusDays(1), LocalTime.of(18, 30)),
                "Notification test", cart);
        ok(order.isSuccess(), "notification food order created");
        String orderReference = order.getValue().getReference();
        ok(service.unreadNotificationCount(ReservationOrderService.MANAGER_NOTIFICATION_KEY)
                        == managerUnreadBeforeOrder + 1,
                "manager receives new food order notification");
        NotificationRecord managerOrderNotification = service.notifications(
                        ReservationOrderService.MANAGER_NOTIFICATION_KEY, 50).stream()
                .filter(item -> orderReference.equals(item.getReferenceCode()))
                .findFirst().orElseThrow();
        ok(managerOrderNotification.getActionPath().startsWith("/staff/orders/view"),
                "manager order notification links to staff record");

        long customerUnreadBefore = service.unreadNotificationCount(customerKey);
        ok(service.staffUpdateOrder(orderReference, "CONFIRMED", "Accepted", "Manager").isSuccess(),
                "notification food order confirmed");
        ok(service.unreadNotificationCount(customerKey) == customerUnreadBefore + 1,
                "customer receives food order status notification");
        NotificationRecord customerOrderNotification = service.notifications(customerKey, 20).stream()
                .filter(item -> orderReference.equals(item.getReferenceCode()))
                .findFirst().orElseThrow();
        ok(service.openNotification(customerOrderNotification.getId(), customerKey)
                        .orElse("").startsWith("/orders/view"),
                "customer order notification opens relevant record");

        service.markAllNotificationsRead(customerKey);
        ok(service.unreadNotificationCount(customerKey) == 0,
                "customer can mark every notification as read");

        int managerNotificationCount = service.notifications(
                ReservationOrderService.MANAGER_NOTIFICATION_KEY, 50).size();
        service.clearNotifications(customerKey);
        ok(service.notifications(customerKey, 20).isEmpty(),
                "customer can clear every owned notification");
        ok(service.notifications(ReservationOrderService.MANAGER_NOTIFICATION_KEY, 50).size()
                        == managerNotificationCount,
                "clearing customer notifications preserves manager notifications");
    }

    private static void proposalAlignmentRegressions(ReservationOrderService service,
                                                     LocalDate base) {
        RestaurantTableRecord occupiedTable = service.tables().stream()
                .filter(table -> table.getId() == 1).findFirst().orElseThrow();
        occupiedTable.setStatus("OCCUPIED");
        ok(service.findAvailableTables(base, LocalTime.of(18, 30), 2, "INDOOR", null)
                        .stream().noneMatch(table -> table.getId() == occupiedTable.getId()),
                "occupied table excluded from customer availability");
        occupiedTable.setStatus("AVAILABLE");

        OperationResult<TableReservationRecord> occupiedConfirmation = reservation(
                service, "occupied-confirmation", base, LocalTime.of(20, 30), "INDOOR");
        ok(occupiedConfirmation.isSuccess(), "occupied confirmation reservation created");
        ok(service.staffUpdateReservation(occupiedConfirmation.getValue().getReference(), 1,
                "PENDING", "Temporary table assignment", "Manager").isSuccess(),
                "occupied confirmation table assigned while available");
        occupiedTable.setStatus("OCCUPIED");
        ok(!service.staffUpdateReservation(occupiedConfirmation.getValue().getReference(), 0,
                "CONFIRMED", "Attempt confirmation", "Manager").isSuccess(),
                "occupied retained table cannot confirm a pending reservation");
        occupiedTable.setStatus("AVAILABLE");
        ok(service.staffUpdateReservation(occupiedConfirmation.getValue().getReference(), 0,
                "REJECTED", "Test reservation closed", "Manager").isSuccess(),
                "occupied confirmation test record closed safely");

        OperationResult<TableReservationRecord> closure = reservation(
                service, "staff-closure", base.plusDays(1), LocalTime.of(18, 30), "INDOOR");
        ok(closure.isSuccess(), "staff closure reservation created");
        String closureReference = closure.getValue().getReference();
        ok(service.staffUpdateReservation(closureReference, 1, "CONFIRMED",
                "Table confirmed", "Manager").isSuccess(),
                "staff closure reservation confirmed");
        ok(service.staffUpdateReservation(closureReference, 1, "CANCELLED",
                "Customer could not attend", "Manager").isSuccess(),
                "confirmed reservation closes even when UI submits its current table");

        LocalDate unavailableDate = base.plusDays(2);
        for (long tableId = 1; tableId <= 3; tableId++) {
            OperationResult<TableReservationRecord> tableReservation = reservation(
                    service, "alternative-" + tableId, unavailableDate,
                    LocalTime.of(18, 30), "INDOOR");
            ok(tableReservation.isSuccess(), "alternative-slot blocking reservation " + tableId);
            ok(service.staffUpdateReservation(tableReservation.getValue().getReference(), tableId,
                    "CONFIRMED", "Table allocated", "Manager").isSuccess(),
                    "alternative-slot table allocation " + tableId);
        }
        ok(service.findAvailableTables(unavailableDate, LocalTime.of(18, 30),
                2, "INDOOR", null).isEmpty(), "fully booked requested slot detected");
        ok(!service.alternativeTableSlots(unavailableDate, LocalTime.of(18, 30),
                        2, "INDOOR", 4).isEmpty(),
                "nearest alternative reservation slots returned");
        ok(service.alternativeTableSlots(unavailableDate, LocalTime.of(18, 30),
                        2, "INDOOR", 4).stream()
                        .noneMatch(item -> item.getDate().equals(unavailableDate)
                                && item.getTime().equals(LocalTime.of(18, 30))),
                "requested full slot excluded from alternatives");
        ok(service.alternativeTableSlots(unavailableDate, LocalTime.of(5, 0),
                        2, "INDOOR", 4).isEmpty(),
                "alternative search rejects an invalid requested time");

        Map<Long, Integer> editableCart = new LinkedHashMap<>();
        service.addCartItem(editableCart, 1, 1);
        service.addCartItem(editableCart, 2, 1);
        OperationResult<FoodOrderRecord> editableOrder = service.createOrder(
                "item-editor", "Item Editor", "editor@example.com", "0771234567",
                "TAKEAWAY", "", LocalDateTime.of(base.plusDays(3), LocalTime.of(19, 30)),
                "Pending item CRUD", editableCart);
        ok(editableOrder.isSuccess(), "pending item CRUD order created");
        String editableReference = editableOrder.getValue().getReference();
        ok(!service.updatePendingOrderItem("another-customer", editableReference, 1, 2).isSuccess(),
                "pending item update ownership enforced");
        long managerUnreadBeforeItemChange = service.unreadNotificationCount(
                ReservationOrderService.MANAGER_NOTIFICATION_KEY);
        ok(service.addPendingOrderItem("item-editor", editableReference, 3, 1).isSuccess(),
                "item added to submitted pending order");
        ok(service.unreadNotificationCount(ReservationOrderService.MANAGER_NOTIFICATION_KEY)
                        == managerUnreadBeforeItemChange + 1,
                "manager notified when submitted order items change");
        ok(service.updatePendingOrderItem("item-editor", editableReference, 1, 3).isSuccess(),
                "submitted pending order quantity updated");
        ok(service.order(editableReference).orElseThrow().getItems().stream()
                        .anyMatch(item -> item.getMenuItemId() == 1 && item.getQuantity() == 3),
                "pending quantity persisted in order record");
        ok(service.removePendingOrderItem("item-editor", editableReference, 2).isSuccess(),
                "item removed from submitted pending order");
        ok(service.order(editableReference).orElseThrow().getItems().stream()
                        .noneMatch(item -> item.getMenuItemId() == 2),
                "pending item removal persisted");
        ok(service.order(editableReference).orElseThrow().getSubtotal()
                        .compareTo(service.order(editableReference).orElseThrow().getItems().stream()
                                .map(item -> item.getLineTotal())
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)) == 0,
                "pending order totals remain synchronized with edited items");
        ok(service.removePendingOrderItem("item-editor", editableReference, 3).isSuccess(),
                "second pending item removed");
        ok(!service.removePendingOrderItem("item-editor", editableReference, 1).isSuccess(),
                "submitted order cannot lose its final item");
        ok(service.staffUpdateOrder(editableReference, "CONFIRMED", "Accepted", "Manager").isSuccess(),
                "edited pending order confirmed");
        ok(!service.addPendingOrderItem("item-editor", editableReference, 4, 1).isSuccess(),
                "item edits blocked after staff confirmation");

        OperationResult<TableReservationRecord> preOrderReservation = reservation(
                service, "pre-order-lifecycle", base.plusDays(4), LocalTime.of(19, 30), "GARDEN");
        ok(preOrderReservation.isSuccess(), "pre-order lifecycle reservation created");
        ok(service.staffUpdateReservation(preOrderReservation.getValue().getReference(), 5,
                "CONFIRMED", "Garden table confirmed", "Manager").isSuccess(),
                "pre-order lifecycle reservation confirmed");
        Map<Long, Integer> preOrderCart = new LinkedHashMap<>();
        service.addCartItem(preOrderCart, 4, 1);
        OperationResult<FoodOrderRecord> preOrder = service.createOrder(
                "pre-order-lifecycle", "Pre Order Guest", "preorder@example.com", "0771234567",
                "PRE_ORDER", preOrderReservation.getValue().getReference(), null,
                "Serve after seating", preOrderCart);
        ok(preOrder.isSuccess(), "pre-order created");
        ok(preOrder.getValue().getServiceCharge().signum() > 0,
                "pre-order receives restaurant service charge");
        String preOrderReference = preOrder.getValue().getReference();
        ok(service.staffUpdateOrder(preOrderReference, "CONFIRMED", "Accepted", "Manager").isSuccess(),
                "pre-order confirmed");
        ok(service.staffUpdateOrder(preOrderReference, "PREPARING", "Cooking", "Chef").isSuccess(),
                "pre-order preparing");
        ok(service.staffUpdateOrder(preOrderReference, "READY", "Ready", "Chef").isSuccess(),
                "pre-order ready");
        ok(!service.staffUpdateOrder(preOrderReference, "COMPLETED", "Skip service", "Manager").isSuccess(),
                "pre-order cannot skip served status");
        ok(service.staffUpdateOrder(preOrderReference, "SERVED", "Served", "Waiter").isSuccess(),
                "pre-order served");
        ok(service.staffUpdateOrder(preOrderReference, "COMPLETED", "Completed", "Waiter").isSuccess(),
                "pre-order completed after service");
    }

    private static void orderTypeEligibilityBoundary() {
        InMemoryReservationOrderRepository repository = new InMemoryReservationOrderRepository();
        ReservationOrderService service = new ReservationOrderService(repository);
        LocalDateTime closeAt = LocalDateTime.now().plusMinutes(20).withSecond(0).withNano(0);
        TableReservationRecord closeReservation = new TableReservationRecord(
                repository.nextReservationId(), "DV-R-CUTOFF", "cutoff-customer",
                "Cutoff Guest", "cutoff@example.com", "0771234567",
                closeAt.toLocalDate(), closeAt.toLocalTime(), 2, "INDOOR", "");
        closeReservation.assignTable(1L, "I-01", "Manager");
        closeReservation.changeStatus("CONFIRMED", "Confirmed", "Manager");
        repository.saveReservation(closeReservation);

        ok(service.eligiblePreOrderReservations("cutoff-customer").isEmpty(),
                "reservation inside 30-minute cutoff excluded from pre-order");
        Map<Long, Integer> cart = new LinkedHashMap<>();
        service.addCartItem(cart, 1, 1);
        ok(!service.createOrder(
                "cutoff-customer", "Cutoff Guest", "cutoff@example.com", "0771234567",
                "PRE_ORDER", closeReservation.getReference(), null, "", cart).isSuccess(),
                "pre-order inside 30-minute cutoff blocked by service");
        ok(!cart.isEmpty(), "pre-order cutoff failure preserves cart");

        closeReservation.changeStatus("SEATED", "Guest seated", "Host");
        repository.saveReservation(closeReservation);
        ok(service.eligibleDineInReservations("cutoff-customer").stream()
                        .anyMatch(item -> item.getReference().equals(closeReservation.getReference())),
                "seated reservation becomes eligible for dine-in");
        ok(service.createOrder(
                "cutoff-customer", "Cutoff Guest", "cutoff@example.com", "0771234567",
                "DINE_IN", closeReservation.getReference(), null, "", cart).isSuccess(),
                "seated customer can place dine-in order");
    }

    private static OperationResult<TableReservationRecord> reservation(
            ReservationOrderService service, String customer, LocalDate date,
            LocalTime time, String area) {
        return service.createReservation(customer, "Guest User", "guest@example.com",
                "0771234567", date, time, 2, area, "");
    }

    private static void ok(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError("Failed: " + label);
    }
}
