package com.dinevista.service;

import com.dinevista.model.MenuCategoryRecord;
import com.dinevista.model.MenuItemAdminRecord;
import com.dinevista.repository.MenuRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Business logic and server-side validation for the Menu Management module
 * (M. A. I. Samarasinghe / IT25103794).
 *
 * Implements use-case UC-MEN-02 (Maintain Menu Item):
 *  - BR-MEN-01: Prices must be strictly positive (> 0).
 *  - BR-MEN-02: Unavailable or sold-out items cannot be ordered by customers.
 *  - BR-MEN-03: Referenced items are safely archived (set to UNAVAILABLE), not hard-deleted.
 *  - Exception A2: Prevents duplicate active items within the same category.
 */
public class MenuService {
    private static final Set<String> ALLOWED_DIETARY =
            Set.of("REGULAR", "VEGETARIAN", "VEGAN", "GLUTEN_AWARE");
    private static final Set<String> ALLOWED_SPICE =
            Set.of("NONE", "MILD", "MEDIUM", "HOT");
    private static final Set<String> ALLOWED_STATUS =
            Set.of("AVAILABLE", "SOLD_OUT", "UNAVAILABLE");

    private final MenuRepository repository;

    public MenuService(MenuRepository repository) {
        this.repository = repository;
    }

    public MenuRepository getRepository() {
        return repository;
    }

    // ──────────────────────── Categories ────────────────────────

    public List<MenuCategoryRecord> allCategories() {
        return repository.findAllCategories();
    }

    public List<MenuCategoryRecord> activeCategories() {
        return repository.findActiveCategories();
    }

    public Optional<MenuCategoryRecord> category(long id) {
        return repository.findCategory(id);
    }

    public OperationResult<MenuCategoryRecord> saveCategory(long id, String name, String description,
                                                            int displayOrder, boolean active) {
        List<String> errors = new ArrayList<>();
        String cleanName = name == null ? "" : name.trim();
        String cleanDesc = description == null ? "" : description.trim();

        if (cleanName.isEmpty()) {
            errors.add("Category name is required.");
        } else if (cleanName.length() > 100) {
            errors.add("Category name cannot exceed 100 characters.");
        }

        if (cleanDesc.length() > 255) {
            errors.add("Category description cannot exceed 255 characters.");
        }

        Optional<MenuCategoryRecord> existingByName = repository.findCategoryByName(cleanName);
        if (existingByName.isPresent() && existingByName.get().getId() != id) {
            errors.add("A category named '" + cleanName + "' already exists.");
        }

        if (!errors.isEmpty()) {
            return OperationResult.failure(errors);
        }

        long categoryId = id > 0 ? id : repository.nextCategoryId();
        MenuCategoryRecord record = new MenuCategoryRecord(categoryId, cleanName, cleanDesc, displayOrder, active);
        return OperationResult.success(repository.saveCategory(record));
    }

    public OperationResult<Void> deleteCategory(long id) {
        Optional<MenuCategoryRecord> cat = repository.findCategory(id);
        if (cat.isEmpty()) {
            return OperationResult.failure("Category not found.");
        }
        if (repository.categoryHasItems(id)) {
            return OperationResult.failure("Cannot delete category '" + cat.get().getName()
                    + "' because it contains menu items. Please reassign or delete those items first.");
        }
        boolean deleted = repository.deleteCategory(id);
        if (!deleted) {
            return OperationResult.failure("Failed to delete category.");
        }
        return OperationResult.success(null);
    }

    // ──────────────────────── Menu Items ────────────────────────

    public List<MenuItemAdminRecord> allItems(String search, Long categoryId, String statusFilter) {
        String needle = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String status = statusFilter == null ? "" : statusFilter.trim().toUpperCase(Locale.ROOT);

        List<MenuItemAdminRecord> result = new ArrayList<>();
        for (MenuItemAdminRecord item : repository.findAllItems()) {
            if (!needle.isEmpty()) {
                boolean matchesName = item.getName() != null && item.getName().toLowerCase(Locale.ROOT).contains(needle);
                boolean matchesDesc = item.getDescription() != null && item.getDescription().toLowerCase(Locale.ROOT).contains(needle);
                boolean matchesCat = item.getCategoryName() != null && item.getCategoryName().toLowerCase(Locale.ROOT).contains(needle);
                if (!matchesName && !matchesDesc && !matchesCat) continue;
            }
            if (categoryId != null && categoryId > 0 && item.getCategoryId() != categoryId) {
                continue;
            }
            if (!status.isEmpty() && !"ALL".equals(status) && !status.equals(item.getAvailabilityStatus())) {
                continue;
            }
            result.add(item);
        }
        return result;
    }

    public Optional<MenuItemAdminRecord> item(long id) {
        return repository.findItem(id);
    }

    public OperationResult<MenuItemAdminRecord> saveItem(long id, Long categoryId, String name,
                                                         String description, String priceRaw,
                                                         String imagePath, String prepMinutesRaw,
                                                         String dietaryTypeRaw, String spiceLevelRaw,
                                                         String availabilityStatusRaw) {
        List<String> errors = new ArrayList<>();

        String cleanName = name == null ? "" : name.trim();
        String cleanDesc = description == null ? "" : description.trim();
        String cleanImage = imagePath == null || imagePath.trim().isEmpty() ? "dish-signature.svg" : imagePath.trim();

        if (cleanName.isEmpty()) {
            errors.add("Menu item name is required.");
        } else if (cleanName.length() > 140) {
            errors.add("Menu item name cannot exceed 140 characters.");
        }

        if (categoryId == null || categoryId <= 0) {
            errors.add("Category selection is required.");
        } else if (repository.findCategory(categoryId).isEmpty()) {
            errors.add("Selected category does not exist.");
        }

        BigDecimal price = null;
        if (priceRaw == null || priceRaw.trim().isEmpty()) {
            errors.add("Price is required.");
        } else {
            try {
                price = new BigDecimal(priceRaw.trim());
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add("Price must be greater than zero (BR-MEN-01).");
                } else if (price.scale() > 2) {
                    price = price.setScale(2, RoundingMode.HALF_UP);
                }
            } catch (NumberFormatException ex) {
                errors.add("Price must be a valid positive number.");
            }
        }

        int prepMinutes = 20;
        if (prepMinutesRaw != null && !prepMinutesRaw.trim().isEmpty()) {
            try {
                prepMinutes = Integer.parseInt(prepMinutesRaw.trim());
                if (prepMinutes < 0) {
                    errors.add("Preparation time cannot be negative.");
                }
            } catch (NumberFormatException ex) {
                errors.add("Preparation time must be a valid whole number of minutes.");
            }
        }

        String dietary = dietaryTypeRaw == null || dietaryTypeRaw.trim().isEmpty()
                ? "REGULAR" : dietaryTypeRaw.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_DIETARY.contains(dietary)) {
            errors.add("Dietary type must be one of: Regular, Vegetarian, Vegan, Gluten-aware.");
        }

        String spice = spiceLevelRaw == null || spiceLevelRaw.trim().isEmpty()
                ? "NONE" : spiceLevelRaw.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_SPICE.contains(spice)) {
            errors.add("Spice level must be one of: None, Mild, Medium, Hot.");
        }

        String status = availabilityStatusRaw == null || availabilityStatusRaw.trim().isEmpty()
                ? "AVAILABLE" : availabilityStatusRaw.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUS.contains(status)) {
            errors.add("Availability status must be Available, Sold out, or Unavailable.");
        }

        // Duplicate active item check (UC-MEN-02 Exception A2)
        if (categoryId != null && categoryId > 0 && !cleanName.isEmpty()) {
            Optional<MenuItemAdminRecord> duplicate = repository.findItemByNameInCategory(cleanName, categoryId);
            if (duplicate.isPresent() && duplicate.get().getId() != id) {
                errors.add("An item named '" + cleanName + "' already exists in this category (A2 duplicate prevention).");
            }
        }

        if (!errors.isEmpty()) {
            return OperationResult.failure(errors);
        }

        long itemId = id > 0 ? id : repository.nextItemId();
        String catName = repository.findCategory(categoryId).map(MenuCategoryRecord::getName).orElse("General");

        MenuItemAdminRecord record = new MenuItemAdminRecord(
                itemId, categoryId, catName, cleanName, cleanDesc, price, cleanImage,
                prepMinutes, dietary, spice, status,
                LocalDateTime.now(), LocalDateTime.now()
        );

        MenuItemAdminRecord saved = repository.saveItem(record);
        return OperationResult.success(saved);
    }

    /**
     * Deletes or safely archives a menu item per UC-MEN-02 rule BR-MEN-03 and flow A4:
     * If the item is referenced by existing customer order history, it cannot be hard-deleted;
     * instead, its availability is set to 'UNAVAILABLE' (archived) to preserve audit trails.
     */
    public DeleteOutcome deleteOrArchiveItem(long id) {
        Optional<MenuItemAdminRecord> existing = repository.findItem(id);
        if (existing.isEmpty()) {
            return new DeleteOutcome(false, false, "Menu item not found.");
        }

        MenuItemAdminRecord item = existing.get();
        if (repository.itemReferencedByOrders(id)) {
            // BR-MEN-03: Soft archive
            item.setAvailabilityStatus("UNAVAILABLE");
            repository.saveItem(item);
            return new DeleteOutcome(true, true,
                    "'" + item.getName() + "' is linked to past order records. It was safely archived as 'Unavailable' instead of deleted (BR-MEN-03).");
        }

        // Safe to hard delete
        boolean deleted = repository.deleteItem(id);
        if (deleted) {
            return new DeleteOutcome(true, false,
                    "'" + item.getName() + "' was permanently removed from the menu.");
        } else {
            return new DeleteOutcome(false, false, "Failed to remove menu item.");
        }
    }

    /**
     * 1-click status toggle for quick operational updates during kitchen service.
     */
    public OperationResult<MenuItemAdminRecord> toggleAvailability(long id) {
        Optional<MenuItemAdminRecord> existing = repository.findItem(id);
        if (existing.isEmpty()) {
            return OperationResult.failure("Menu item not found.");
        }

        MenuItemAdminRecord item = existing.get();
        String newStatus;
        if ("AVAILABLE".equals(item.getAvailabilityStatus())) {
            newStatus = "SOLD_OUT";
        } else {
            newStatus = "AVAILABLE";
        }
        item.setAvailabilityStatus(newStatus);
        MenuItemAdminRecord updated = repository.saveItem(item);
        return OperationResult.success(updated);
    }

    // ──────────────────────── KPIs & Counts ────────────────────────

    public long totalItemsCount() {
        return repository.findAllItems().size();
    }

    public long availableItemsCount() {
        return repository.findAllItems().stream().filter(MenuItemAdminRecord::isAvailable).count();
    }

    public long soldOutOrUnavailableCount() {
        return repository.findAllItems().stream().filter(i -> !i.isAvailable()).count();
    }

    public long totalCategoriesCount() {
        return repository.findAllCategories().size();
    }

    public static class DeleteOutcome {
        private final boolean success;
        private final boolean archived;
        private final String message;

        public DeleteOutcome(boolean success, boolean archived, String message) {
            this.success = success;
            this.archived = archived;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public boolean isArchived() { return archived; }
        public String getMessage() { return message; }
    }
}
