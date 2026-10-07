package com.dinevista.service;

import com.dinevista.repository.InMemoryBillingRepository;
import com.dinevista.repository.InMemoryEventBookingRepository;
import com.dinevista.repository.InMemoryEventOperationsRepository;
import com.dinevista.repository.InMemoryEventPackageRepository;
import com.dinevista.repository.InMemoryInventoryRepository;
import com.dinevista.repository.InMemoryMenuRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Direct service calls prove the rules work even when HTML validation is bypassed. */
public final class ServerSideInputRulesSelfTest {
    private static int checks;

    public static void main(String[] args) {
        AccountService accounts = new AccountService(null);
        reject(accounts.authenticate("not-an-email", "some-password", "CUSTOMER"), "login email format");
        reject(accounts.authenticate("a@b.com", "x".repeat(129), "CUSTOMER"), "login password length");
        reject(accounts.register("CUSTOMER", "A", "Valid", "bad", "123", "short", "different", ""),
                "registration names, email, phone and password");
        reject(accounts.register("CUSTOMER", "x".repeat(81), "Valid", "customer@example.com",
                "0771234567", "valid-password", "valid-password", ""), "name maximum");
        reject(accounts.register("CUSTOMER", "Valid", "Person", "not-email",
                "0771234567", "valid-password", "valid-password", ""), "registration email format");
        reject(accounts.register("CUSTOMER", "Valid", "Person", "customer@example.com",
                "0123456789", "valid-password", "valid-password", ""), "phone pattern");
        reject(accounts.register("CUSTOMER", "Valid", "Person", "customer@example.com",
                "0771234567", "short", "short", ""), "password minimum");
        reject(accounts.register("CUSTOMER", "Valid", "Person", "customer@example.com",
                "0771234567", "valid-password", "different-password", ""), "password confirmation");

        MenuService menu = new MenuService(new InMemoryMenuRepository());
        long categoryId = menu.allCategories().get(0).getId();
        reject(menu.saveCategory(0, "Test", "", -1, true), "negative category order");
        reject(menu.saveItem(0, categoryId, "Rule Check", "x".repeat(601), "100", "dish-signature.svg",
                "20", "REGULAR", "NONE", "AVAILABLE"), "menu description length");
        reject(menu.saveItem(0, categoryId, "Rule Check", "", "100", "dish-signature.svg",
                "181", "REGULAR", "NONE", "AVAILABLE"), "preparation maximum");
        reject(menu.saveItem(0, categoryId, "Rule Check", "", "1.001", "dish-signature.svg",
                "20", "REGULAR", "NONE", "AVAILABLE"), "money decimal places");

        InventoryService inventory = new InventoryService(new InMemoryInventoryRepository());
        reject(inventory.saveIngredient(0, "x".repeat(141), "kg", "1", "2", "Supplier"),
                "ingredient name length");
        reject(inventory.saveIngredient(0, "Valid", "kg", "0.0001", "2", "Supplier"),
                "stock decimal places");
        long ingredientId = inventory.allIngredients("", false).get(0).getId();
        reject(inventory.recordTransaction(ingredientId, "PURCHASE", "1", "x".repeat(256), "Manager"),
                "stock note length");

        EventPackageService packages = new EventPackageService(new InMemoryEventPackageRepository());
        reject(packages.create("Valid Package", "BIRTHDAY", "", new BigDecimal("1.001"),
                1, 2, 60, ""), "package price decimal places");
        EventBookingService bookings = new EventBookingService(new InMemoryEventBookingRepository(),
                new InMemoryEventPackageRepository());
        reject(bookings.create(1, "Valid Customer", "x".repeat(155) + "@a.com", "0771234567",
                "Party", 1, 1, LocalDate.now().plusDays(14), LocalTime.NOON, 50, ""),
                "event contact email length");
        var validBooking = bookings.create(1, "Valid Customer", "customer@example.com", "0771234567",
                "Party", 1, 1, LocalDate.now().plusDays(15), LocalTime.NOON, 50, "");
        accept(validBooking, "valid event booking");
        reject(bookings.cancelByCustomer(1, "customer@example.com", validBooking.getValue().getReference(),
                "x".repeat(501)), "event cancellation reason length");

        EventOperationsService operations = new EventOperationsService(new InMemoryEventOperationsRepository());
        reject(operations.saveVenue(0, "x".repeat(161), "INDOOR", "10", "0", "", "AVAILABLE"),
                "venue name length");
        reject(operations.saveVenue(0, "Valid Venue", "INDOOR", "10", "1.001", "", "AVAILABLE"),
                "venue fee decimal places");
        reject(operations.saveResource(0, "Valid Resource", "DECOR", "10", "10", "1.001", "AVAILABLE"),
                "resource cost decimal places");
        long venueId = operations.allVenues().get(0).getId();
        reject(operations.bookVenue(venueId, "Event", LocalDate.now().plusDays(14).toString(),
                "12:00", "14:00", "2", "x".repeat(501), "Manager"), "venue booking notes length");
        long resourceId = operations.allResources().get(0).getId();
        reject(operations.bookResource(resourceId, "x".repeat(181), LocalDate.now().plusDays(14).toString(),
                "1", "", "Manager"), "resource event label length");

        BillingService billing = new BillingService(new InMemoryBillingRepository());
        reject(billing.generateInvoice("OTHER", "", "customer", "Customer", "customer@example.com",
                new String[]{"x".repeat(256)}, new String[]{"1"}, new String[]{"10"}, "", "Manager"),
                "invoice line description length");
        reject(billing.generateInvoice("OTHER", "", "customer", "Customer", "customer@example.com",
                new String[]{"Service"}, new String[]{"1.001"}, new String[]{"10"}, "", "Manager"),
                "invoice quantity decimal places");
        reject(billing.savePromotion(0, "BAD CODE", "Test", "PERCENTAGE", "10", "0",
                LocalDate.now().toString(), LocalDate.now().plusDays(1).toString(), "", true),
                "promotion code format");
        reject(billing.savePromotion(0, "TEST10", "Test", "PERCENTAGE", "10.001", "0",
                LocalDate.now().toString(), LocalDate.now().plusDays(1).toString(), "", true),
                "promotion money decimal places");
        var validInvoice = billing.generateInvoice("OTHER", "", "customer", "Customer", "customer@example.com",
                new String[]{"Service"}, new String[]{"1"}, new String[]{"10"}, "", "Manager");
        accept(validInvoice, "valid invoice");
        long invoiceId = validInvoice.getValue().getId();
        reject(billing.recordPayment(invoiceId, "CASH", "1.001", "", "", "Manager"),
                "payment decimal places");
        reject(billing.recordPayment(invoiceId, "CASH", "1", "x".repeat(41), "", "Manager"),
                "payment reference length");
        reject(billing.recordPayment(invoiceId, "CASH", "1", "", "x".repeat(256), "Manager"),
                "payment note length");
        System.out.println("PASS: " + checks + " direct server-side validation checks.");
    }

    private static void reject(OperationResult<?> result, String rule) {
        if (result.isSuccess()) throw new AssertionError("Browser-bypass input accepted: " + rule);
        checks++;
    }

    private static void accept(OperationResult<?> result, String rule) {
        if (!result.isSuccess()) throw new AssertionError("Valid input rejected: " + rule + " " + result.getErrors());
        checks++;
    }
}
