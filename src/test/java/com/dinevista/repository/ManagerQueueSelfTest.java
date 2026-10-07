package com.dinevista.repository;

import com.dinevista.model.EventBookingRecord;
import com.dinevista.model.FoodOrderRecord;
import com.dinevista.model.TableReservationRecord;
import com.dinevista.util.ManagerRequestReadState;

import javax.servlet.ServletContext;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Run with DINEVISTA_STORAGE_MODE=memory; no live request records are changed. */
public final class ManagerQueueSelfTest {
    private static int checks;

    public static void main(String[] args) {
        if (!"memory".equalsIgnoreCase(System.getenv("DINEVISTA_STORAGE_MODE"))) {
            throw new IllegalStateException("Run this self-test in memory mode.");
        }
        reservationOrder();
        foodOrder();
        eventOrderAfterEdit();
        sharedOpenedState();
        System.out.println("Manager queues passed: " + checks + " checks.");
    }

    private static void reservationOrder() {
        InMemoryReservationOrderRepository repository = new InMemoryReservationOrderRepository();
        LocalDateTime older = LocalDateTime.of(2026, 10, 1, 12, 0);
        LocalDateTime newer = older.plusDays(1);
        repository.saveReservation(reservation(9001, "OLDER", LocalDate.of(2026, 12, 1), older));
        repository.saveReservation(reservation(9002, "NEWER", LocalDate.of(2026, 10, 9), newer));
        check(reservationIndex(repository, "NEWER") < reservationIndex(repository, "OLDER"),
                "newer submission must precede a later visit date");
        repository.saveReservation(reservation(9003, "TIED", LocalDate.of(2026, 10, 8), newer));
        check(reservationIndex(repository, "TIED") < reservationIndex(repository, "NEWER"),
                "reservation ID breaks equal-time ties");
    }

    private static int reservationIndex(InMemoryReservationOrderRepository repository, String reference) {
        for (int i = 0; i < repository.findAllReservations().size(); i++) {
            if (reference.equals(repository.findAllReservations().get(i).getReference())) return i;
        }
        throw new AssertionError("Missing reservation " + reference);
    }

    private static TableReservationRecord reservation(long id, String reference, LocalDate visit,
                                                       LocalDateTime created) {
        return new TableReservationRecord(id, reference, "customer", "Guest", "guest@example.com", "0770000000",
                visit, LocalTime.of(18, 0), 2, "INDOOR", "", "PENDING", null, null,
                "", "", created, created, Collections.emptyList());
    }

    private static void foodOrder() {
        InMemoryReservationOrderRepository repository = new InMemoryReservationOrderRepository();
        LocalDateTime created = LocalDateTime.of(2026, 10, 2, 10, 0);
        repository.saveOrder(order(9001, "ORDER-OLDER", created.minusDays(1)));
        repository.saveOrder(order(9002, "ORDER-NEWER", created));
        check(orderIndex(repository, "ORDER-NEWER") < orderIndex(repository, "ORDER-OLDER"),
                "newest food order must be first");
        repository.saveOrder(order(9003, "ORDER-TIED", created));
        check(orderIndex(repository, "ORDER-TIED") < orderIndex(repository, "ORDER-NEWER"),
                "food order ID breaks equal-time ties");
    }

    private static int orderIndex(InMemoryReservationOrderRepository repository, String reference) {
        for (int i = 0; i < repository.findAllOrders().size(); i++) {
            if (reference.equals(repository.findAllOrders().get(i).getReference())) return i;
        }
        throw new AssertionError("Missing order " + reference);
    }

    private static FoodOrderRecord order(long id, String reference, LocalDateTime created) {
        return new FoodOrderRecord(id, reference, "customer", "Guest", "guest@example.com", "0770000000",
                "TAKEAWAY", "", created.plusDays(2), "", "PENDING", Collections.emptyList(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "", "", created, created,
                Collections.emptyList());
    }

    private static void eventOrderAfterEdit() {
        InMemoryEventBookingRepository repository = new InMemoryEventBookingRepository();
        EventBookingRecord older = event("EVENT-OLDER", "2026-12-01", "INQUIRY");
        EventBookingRecord newer = event("EVENT-NEWER", "2026-10-10", "INQUIRY");
        repository.save(older);
        repository.save(newer);
        check("EVENT-NEWER".equals(repository.findAll("").get(0).getReference()),
                "new event request must precede an older request with a later event date");
        repository.update(event("EVENT-OLDER", "2026-12-01", "QUOTED"));
        check("EVENT-NEWER".equals(repository.findAll("").get(0).getReference()),
                "editing an older event must not move it above a new request");
        check("EVENT-NEWER".equals(repository.findAll("EVENT-NEWER").get(0).getReference()),
                "event search keeps the expected request");
    }

    private static EventBookingRecord event(String reference, String eventDate, String status) {
        return new EventBookingRecord(0, reference, 1, 1, 1, "Guest", "guest@example.com", "0770000000",
                "PARTY", "Package", "Venue", eventDate, "18:00:00", 20, BigDecimal.TEN, status, "", "");
    }

    private static void sharedOpenedState() {
        Map<String, Object> attributes = new HashMap<>();
        ServletContext context = (ServletContext) Proxy.newProxyInstance(
                ServletContext.class.getClassLoader(), new Class<?>[]{ServletContext.class},
                (proxy, method, args) -> {
                    if ("getAttribute".equals(method.getName())) return attributes.get(args[0]);
                    if ("setAttribute".equals(method.getName())) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    }
                    return null;
                });
        ManagerRequestReadState state = ManagerRequestReadState.get(context);
        check(!state.openedReferences(ManagerRequestReadState.RESERVATION).contains("NEW-REQUEST"),
                "unopened reservation starts NEW");
        state.markOpened(ManagerRequestReadState.RESERVATION, "NEW-REQUEST");
        check(ManagerRequestReadState.get(context).openedReferences(ManagerRequestReadState.RESERVATION)
                .contains("NEW-REQUEST"), "opened state is shared through the manager context");
        check(!state.openedReferences(ManagerRequestReadState.ORDER).contains("NEW-REQUEST"),
                "opened state does not spill into another request type");
    }

    private static void check(boolean result, String message) {
        checks++;
        if (!result) throw new AssertionError(message);
    }
}
