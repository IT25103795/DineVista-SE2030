package com.dinevista.service;

import com.dinevista.model.EventBookingRecord;
import com.dinevista.model.EventQuoteRecord;
import com.dinevista.model.InvoiceRecord;
import com.dinevista.repository.InMemoryBillingRepository;
import com.dinevista.repository.InMemoryEventBookingRepository;
import com.dinevista.repository.InMemoryEventPackageRepository;
import com.dinevista.repository.JdbcBillingRepository;
import com.dinevista.util.DatabaseConfig;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Exact-price checks for accepted event quote -> invoice -> simulated payment. */
public final class EventInvoicePaymentSelfTest {
    public static void main(String[] args) {
        if (args.length > 0 && "--mysql".equals(args[0])) {
            try {
                JdbcBillingRepository repository = new JdbcBillingRepository(DatabaseConfig.load());
                long first = repository.nextInvoiceItemId();
                long second = repository.nextInvoiceItemId();
                check(second == first + 1, "two billable lines must receive different MySQL identifiers");
                System.out.println("PASS: MySQL billable-line identifiers are unique before insert.");
            } catch (Exception ex) {
                throw new IllegalStateException("MySQL identifier check could not complete.", ex);
            }
            return;
        }
        EventBookingService bookings = new EventBookingService(
                new InMemoryEventBookingRepository(), new InMemoryEventPackageRepository());
        BillingService billing = new BillingService(new InMemoryBillingRepository());
        LocalDate date = LocalDate.now().plusDays(14);
        LocalTime time = LocalTime.of(14, 0);
        String name = "LAB7 Billing Customer", email = "event-billing@example.test", phone = "0771234567";
        OperationResult<EventBookingRecord> created = bookings.create(42, name, email, phone,
                "Birthday celebration", 1, 1, date, time, 60, "Agreed requirements", "WELCOME10");
        check(created.isSuccess(), "booking created with promotion choice");
        String reference = created.getValue().getReference();
        check("WELCOME10".equals(bookings.booking(reference).orElseThrow().getPromotionCode()),
                "promotion choice kept on booking");
        check(!billing.generateEventInvoice(created.getValue(), null, null, null, null,
                "WELCOME10", "Manager").isSuccess(), "unquoted event rejected");
        check(bookings.managerUpdate(reference, name, email, phone, "Birthday celebration", 1, 1,
                date, time, 60, "Agreed requirements", "QUOTED").isSuccess(), "quote issued");
        EventQuoteRecord quote = bookings.quotes(reference).get(0);
        check(!billing.generateEventInvoice(bookings.booking(reference).orElseThrow(), quote,
                null, null, null, "WELCOME10", "Manager").isSuccess(), "unaccepted quote rejected");
        check(bookings.acceptQuote(42, email, reference, quote.getId()).isSuccess(), "latest quote accepted");
        EventBookingRecord ready = bookings.booking(reference).orElseThrow();
        EventQuoteRecord accepted = bookings.quotes(reference).get(0);
        amount(accepted.getTotal(), "420000.00", "accepted base quote");
        amount(billing.previewDiscount("WELCOME10", accepted.getTotal()).getValue(),
                "42000.00", "booking-page preview discount");
        check(!billing.generateEventInvoice(ready, accepted,
                new String[]{"Extra decoration"}, new String[]{"1"}, new String[]{"10000"},
                "NOT-A-CODE", "Manager").isSuccess(), "invalid event discount rejected");
        OperationResult<InvoiceRecord> generated = billing.generateEventInvoice(ready, accepted,
                new String[]{"Extra decoration", ""}, new String[]{"1", "1"},
                new String[]{"10000", "0"}, "WELCOME10", "Manager");
        check(generated.isSuccess(), "invoice generated from accepted quote");
        InvoiceRecord invoice = generated.getValue();
        check(invoice.getItems().size() == 2, "fixed base plus one extra line");
        amount(invoice.getItems().get(0).getLineTotal(), "420000.00", "base cannot be omitted");
        amount(invoice.getSubtotal(), "430000.00", "subtotal includes extra");
        amount(invoice.getTaxAmount(), "10750.00", "service tax");
        amount(invoice.getDiscountAmount(), "43000.00", "discount follows final subtotal");
        amount(invoice.getTotalAmount(), "397750.00", "exact final amount");
        check(invoice.getCustomerKey().equals(email), "customer identity comes from booking");
        check(!billing.generateEventInvoice(ready, accepted, null, null, null,
                "WELCOME10", "Manager").isSuccess(), "second active invoice rejected");
        check(!billing.recordPayment(invoice.getId(), "ONLINE", "397750.01", "", "Sandbox", "Demo checkout")
                .isSuccess(), "overpayment rejected");
        check(billing.recordPayment(invoice.getId(), "ONLINE", "397750.00", "", "Sandbox", "Demo checkout")
                .isSuccess(), "exact sandbox payment recorded");
        check(billing.invoice(invoice.getId()).orElseThrow().isFullyPaid(), "invoice marked paid");
        check(!billing.recordPayment(invoice.getId(), "ONLINE", "397750.00", "", "Sandbox", "Demo checkout")
                .isSuccess(), "repeat payment rejected");
        System.out.println("PASS: accepted quote, promotion, extras, tax, exact sandbox payment and duplicate guards.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void amount(BigDecimal actual, String expected, String message) {
        check(actual != null && actual.compareTo(new BigDecimal(expected)) == 0, message + ": " + actual);
    }
}
