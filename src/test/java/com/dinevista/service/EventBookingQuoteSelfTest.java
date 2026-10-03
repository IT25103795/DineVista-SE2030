package com.dinevista.service;

import com.dinevista.model.EventBookingRecord;
import com.dinevista.model.EventPackageRecord;
import com.dinevista.model.EventQuoteRecord;
import com.dinevista.repository.InMemoryEventBookingRepository;
import com.dinevista.repository.InMemoryEventPackageRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Run with java -cp target/test-classes;target/classes ...EventBookingQuoteSelfTest. */
public final class EventBookingQuoteSelfTest {
    public static void main(String[] args) {
        InMemoryEventBookingRepository bookings = new InMemoryEventBookingRepository();
        InMemoryEventPackageRepository packages = new InMemoryEventPackageRepository();
        EventBookingService service = new EventBookingService(bookings, packages);
        LocalDate date = LocalDate.now().plusDays(14);
        LocalTime time = LocalTime.of(14, 0);
        String name = "LAB7 Quote Customer", email = "quote@example.com", phone = "0771234567";
        OperationResult<EventBookingRecord> created = service.create(42, name, email, phone,
                "Birthday celebration", 1, 1, date, time, 60, "Initial requirements");
        check(created.isSuccess(), "booking creation");
        String ref = created.getValue().getReference();
        check(!service.managerUpdate(ref, name, email, phone, "Birthday celebration", 1, 1,
                null, time, 60, "Initial requirements", "QUOTED").isSuccess(), "null date rejected");
        check(service.managerUpdate(ref, name, email, phone, "Birthday celebration", 1, 1,
                date, time, 60, "Initial requirements", "QUOTED").isSuccess(), "issue v1");
        EventQuoteRecord v1 = service.quotes(ref).get(0);
        check(service.managerUpdate(ref, name, email, phone, "Birthday celebration", 1, 1,
                date, time, 70, "Revised requirements", "QUOTED").isSuccess(), "issue v2");
        EventQuoteRecord v2 = service.quotes(ref).get(0);
        check(v2.getVersion() == 2 && v2.getTotal().compareTo(new BigDecimal("465000")) == 0,
                "versioned revised total");
        check(!service.acceptQuote(42, email, ref, v1.getId()).isSuccess(), "stale quote blocked");
        check(!service.acceptQuote(99, email, ref, v2.getId()).isSuccess(), "wrong owner blocked");
        check(!service.managerUpdate(ref, name, email, phone, "Birthday celebration", 1, 1,
                date, time, 70, "Revised requirements", "CONFIRMED").isSuccess(),
                "unapproved confirmation blocked");
        check(service.acceptQuote(42, email, ref, v2.getId()).isSuccess(), "latest approval");
        check(!service.managerUpdate(ref, name, email, phone, "Birthday celebration", 1, 1,
                date.plusDays(1), time, 70, "Revised requirements", "CONFIRMED").isSuccess(),
                "schedule change requires new quote");
        check(service.managerUpdate(ref, name, email, phone, "Birthday celebration", 1, 1,
                date, time, 70, "Revised requirements", "CONFIRMED").isSuccess(), "approved confirmation");
        check(!service.managerUpdate(ref, name, email, phone, "Birthday celebration", 1, 1,
                date.plusDays(1), time, 70, "Revised requirements", "CONFIRMED").isSuccess(),
                "confirmed schedule frozen");
        check(!service.managerUpdate(ref, name, email, phone, "Birthday celebration", 1, 1,
                date, time, 70, "Changed after confirmation", "CONFIRMED").isSuccess(),
                "confirmed requirements frozen");
        EventPackageRecord old = packages.findById(1).orElseThrow();
        packages.update(new EventPackageRecord(old.getId(), old.getName(), old.getCategory(),
                old.getDescription(), new BigDecimal("9999"), old.getMinimumGuests(),
                old.getMaximumGuests(), old.getDurationMinutes(), old.getInclusions(), true));
        check(service.managerUpdate(ref, name, email, phone, "Birthday celebration", 1, 1,
                date, time, 70, "Revised requirements", "COMPLETED").isSuccess(),
                "completed after catalogue reprice");
        check(service.booking(ref).orElseThrow().getTotalAmount().compareTo(new BigDecimal("465000")) == 0,
                "accepted total retained after catalogue reprice");
        check(!service.cancelByCustomer(42, email, ref, "LAB6 completed cancellation probe").isSuccess(),
                "completed booking cannot be cancelled");
        check("COMPLETED".equals(service.booking(ref).orElseThrow().getStatus()),
                "completed status retained after rejected cancellation");
        System.out.println("PASS: quote versions, ownership, approval, schedule, confirmed-price and completed-cancellation guards.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
