package com.dinevista.service;

import com.dinevista.model.FoodOrderRecord;
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

        Map<Long, Integer> cart = new LinkedHashMap<>();
        service.addCartItem(cart, 2, 2);
        LocalDateTime manipulated = LocalDateTime.of(base.plusDays(20), LocalTime.NOON);
        OperationResult<FoodOrderRecord> linked = service.createOrder(
                "linked-customer", "Linked Guest", "linked@example.com", "0771234567",
                "DINE_IN", reservationReference, manipulated, "Serve at table", cart);
        ok(linked.isSuccess(), "linked dine-in order create");
        ok(linked.getValue().getRequestedFor().equals(LocalDateTime.of(base, LocalTime.of(19, 30))),
                "linked order time forced to reservation slot");
        ok(linked.getValue().getServiceCharge().signum() > 0, "dine-in service charge calculated server-side");
        ok(!service.cancelReservation("linked-customer", reservationReference,
                "Visit cancelled").isSuccess(), "reservation closure blocked by active linked order");
        ok(service.cancelOrder("linked-customer", linked.getValue().getReference(),
                "Cancel food first").isSuccess(), "linked pending order cancelled first");
        ok(service.cancelReservation("linked-customer", reservationReference,
                "Visit cancelled").isSuccess(), "reservation cancels after linked order closes");

        Map<Long, Integer> foreignCart = new LinkedHashMap<>();
        service.addCartItem(foreignCart, 2, 1);
        ok(!service.createOrder("other-customer", "Other Guest", "other@example.com", "0712345678",
                "PRE_ORDER", reservationReference, null, "", foreignCart).isSuccess(),
                "linked reservation ownership enforced");

        OperationResult<TableReservationRecord> diningReservation = reservation(
                service, "dining-customer", base.plusDays(1), LocalTime.of(18, 30), "GARDEN");
        ok(diningReservation.isSuccess(), "dine-in lifecycle reservation created");
        ok(service.staffUpdateReservation(diningReservation.getValue().getReference(), 4,
                "CONFIRMED", "Confirmed", "Manager").isSuccess(),
                "dine-in lifecycle reservation confirmed");
        Map<Long, Integer> diningCart = new LinkedHashMap<>();
        service.addCartItem(diningCart, 1, 1);
        OperationResult<FoodOrderRecord> diningOrder = service.createOrder(
                "dining-customer", "Dining Guest", "dining@example.com", "0771234567",
                "DINE_IN", diningReservation.getValue().getReference(), null, "", diningCart);
        ok(diningOrder.isSuccess(), "dine-in lifecycle order created");
        String diningReference = diningOrder.getValue().getReference();
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
