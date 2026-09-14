package com.dinevista.service;

import com.dinevista.model.MenuCategoryRecord;
import com.dinevista.model.MenuItemAdminRecord;
import com.dinevista.repository.InMemoryMenuRepository;

import java.util.List;
import java.util.Optional;

/**
 * Dependency-free business-rule regression suite for Menu Management (IT25103794).
 * Verifies all UC-MEN-02 requirements and business rules:
 *  - BR-MEN-01: Positive price enforcement
 *  - BR-MEN-02: Availability status tracking
 *  - BR-MEN-03: Safe archival of referenced items
 *  - Flow A2: Duplicate active item prevention
 *  - Category lifecycle & empty constraint on deletion
 */
public final class MenuServiceSelfTest {
    private static int checks = 0;

    public static void main(String[] args) {
        InMemoryMenuRepository repository = new InMemoryMenuRepository();
        MenuService service = new MenuService(repository);

        testCategoryCrudAndRules(service);
        testItemValidationAndBrMen01(service);
        testDuplicateItemPreventionA2(service);
        testAvailabilityToggleAndBrMen02(service);
        testItemSearchAndFilters(service);
        testSafeArchiveBrMen03(service);

        System.out.println("DineVista Menu Management self-test passed: " + checks + " checks.");
    }

    private static void testCategoryCrudAndRules(MenuService service) {
        // Validation: Empty name
        OperationResult<MenuCategoryRecord> emptyCat = service.saveCategory(0, "", "Desc", 1, true);
        ok(!emptyCat.isSuccess(), "Category with empty name must be rejected");

        // Create category
        OperationResult<MenuCategoryRecord> created = service.saveCategory(0, "Beverages", "Refreshing drinks", 10, true);
        ok(created.isSuccess(), "Valid category creation succeeds");
        long catId = created.getValue().getId();

        // Duplicate category name
        OperationResult<MenuCategoryRecord> duplicate = service.saveCategory(0, "Beverages", "Duplicate", 11, true);
        ok(!duplicate.isSuccess(), "Duplicate category name must be rejected");

        // Delete empty category succeeds
        OperationResult<Void> deleted = service.deleteCategory(catId);
        ok(deleted.isSuccess(), "Deleting empty category succeeds");

        // Category with items cannot be deleted
        long signatureId = service.allCategories().stream()
                .filter(c -> "Signature".equalsIgnoreCase(c.getName()))
                .findFirst().orElseThrow().getId();
        OperationResult<Void> blockedDelete = service.deleteCategory(signatureId);
        ok(!blockedDelete.isSuccess(), "Deleting category with active items must be blocked");
    }

    private static void testItemValidationAndBrMen01(MenuService service) {
        long catId = service.allCategories().get(0).getId();

        // BR-MEN-01: Zero price rejected
        OperationResult<MenuItemAdminRecord> zeroPrice = service.saveItem(
                0, catId, "Free Water", "Test", "0", "dish-signature.svg", "5", "REGULAR", "NONE", "AVAILABLE"
        );
        ok(!zeroPrice.isSuccess(), "BR-MEN-01: Zero price must be rejected");

        // BR-MEN-01: Negative price rejected
        OperationResult<MenuItemAdminRecord> negPrice = service.saveItem(
                0, catId, "Negative Lamb", "Test", "-500", "dish-signature.svg", "15", "REGULAR", "NONE", "AVAILABLE"
        );
        ok(!negPrice.isSuccess(), "BR-MEN-01: Negative price must be rejected");

        // Empty dish name rejected
        OperationResult<MenuItemAdminRecord> emptyName = service.saveItem(
                0, catId, "", "Test", "1200", "dish-signature.svg", "15", "REGULAR", "NONE", "AVAILABLE"
        );
        ok(!emptyName.isSuccess(), "Empty dish name must be rejected");

        // Valid dish creation succeeds
        OperationResult<MenuItemAdminRecord> validItem = service.saveItem(
                0, catId, "Spiced Tea", "Fresh Ceylon tea", "450.00", "dish-signature.svg", "5", "VEGETARIAN", "NONE", "AVAILABLE"
        );
        ok(validItem.isSuccess(), "Valid dish creation succeeds");
        ok(validItem.getValue().getPrice().intValue() == 450, "Price correctly stored");
    }

    private static void testDuplicateItemPreventionA2(MenuService service) {
        long catId = service.allCategories().get(0).getId();

        // Exception A2: Duplicate active item in the same category
        OperationResult<MenuItemAdminRecord> duplicate = service.saveItem(
                0, catId, "Spiced Tea", "Duplicate item", "500.00", "dish-signature.svg", "5", "REGULAR", "NONE", "AVAILABLE"
        );
        ok(!duplicate.isSuccess(), "Exception A2: Duplicate item name in same category must be rejected");
    }

    private static void testAvailabilityToggleAndBrMen02(MenuService service) {
        List<MenuItemAdminRecord> items = service.allItems("Spiced Tea", null, null);
        ok(!items.isEmpty(), "Created item found");
        long id = items.get(0).getId();

        // Toggle from AVAILABLE to SOLD_OUT
        OperationResult<MenuItemAdminRecord> toggled1 = service.toggleAvailability(id);
        ok(toggled1.isSuccess(), "Status toggle succeeds");
        ok("SOLD_OUT".equals(toggled1.getValue().getAvailabilityStatus()), "Item is now SOLD_OUT");

        // Toggle from SOLD_OUT to AVAILABLE
        OperationResult<MenuItemAdminRecord> toggled2 = service.toggleAvailability(id);
        ok(toggled2.isSuccess(), "Second status toggle succeeds");
        ok("AVAILABLE".equals(toggled2.getValue().getAvailabilityStatus()), "Item is back to AVAILABLE");
    }

    private static void testItemSearchAndFilters(MenuService service) {
        List<MenuItemAdminRecord> searched = service.allItems("Chicken", null, null);
        ok(!searched.isEmpty(), "Search query filters items correctly");

        List<MenuItemAdminRecord> availableOnly = service.allItems(null, null, "AVAILABLE");
        ok(availableOnly.stream().allMatch(MenuItemAdminRecord::isAvailable), "Available status filter works");
    }

    private static void testSafeArchiveBrMen03(MenuService service) {
        List<MenuItemAdminRecord> items = service.allItems("Spiced Tea", null, null);
        long id = items.get(0).getId();

        // In-memory unreferenced item should be safely deleted
        MenuService.DeleteOutcome outcome = service.deleteOrArchiveItem(id);
        ok(outcome.isSuccess(), "Item deletion outcome is success");
        ok(!outcome.isArchived(), "Unreferenced item is deleted directly");
        ok(service.item(id).isEmpty(), "Item is removed from repository");
    }

    private static void ok(boolean condition, String label) {
        checks++;
        if (!condition) {
            throw new AssertionError("MenuService check FAILED: " + label);
        }
    }
}
