package com.dinevista.service;

import com.dinevista.model.IngredientRecord;
import com.dinevista.model.StockTransactionRecord;
import com.dinevista.repository.InventoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Business logic for the Inventory Management module (Hansaka A. K. / IT25103798).
 * Enforces FR09/FR10/FR18/FR20/FR21/FR24 from the proposal: server-side validation,
 * non-negative stock, and an auditable, non-destructive transaction history.
 */
public class InventoryService {
    private static final Set<String> TRANSACTION_TYPES =
            Set.of("PURCHASE", "USAGE", "ADJUSTMENT", "WASTE", "RETURN");

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    public List<IngredientRecord> allIngredients(String search, boolean lowStockOnly) {
        String needle = search == null ? "" : search.trim().toLowerCase();
        List<IngredientRecord> result = new ArrayList<>();
        for (IngredientRecord ingredient : repository.findAllIngredients()) {
            if (!needle.isEmpty() && !ingredient.getName().toLowerCase().contains(needle)) continue;
            if (lowStockOnly && !ingredient.isLowStock()) continue;
            result.add(ingredient);
        }
        return result;
    }

    public Optional<IngredientRecord> ingredient(long id) {
        return repository.findIngredient(id);
    }

    public List<StockTransactionRecord> historyFor(long ingredientId) {
        return repository.findTransactionsForIngredient(ingredientId);
    }

    public long lowStockCount() {
        return repository.findAllIngredients().stream().filter(IngredientRecord::isLowStock).count();
    }

    /** Creates a new ingredient (id == 0) or updates an existing one. */
    public OperationResult<IngredientRecord> saveIngredient(long id, String name, String unit,
                                                              String reorderLevelRaw,
                                                              String unitCostRaw, String supplierName) {
        List<String> errors = new ArrayList<>();
        String cleanName = name == null ? "" : name.trim();
        String cleanUnit = unit == null ? "" : unit.trim();

        if (cleanName.isEmpty()) errors.add("Ingredient name is required.");
        if (cleanUnit.isEmpty()) errors.add("Unit of measurement is required.");

        BigDecimal reorderLevel = parseNonNegative(reorderLevelRaw, "Reorder level", errors);
        BigDecimal unitCost = null;
        if (unitCostRaw != null && !unitCostRaw.trim().isEmpty()) {
            unitCost = parseNonNegative(unitCostRaw, "Unit cost", errors);
        }

        Optional<IngredientRecord> existingByName = repository.findIngredientByName(cleanName);
        if (existingByName.isPresent() && existingByName.get().getId() != id) {
            errors.add("Another ingredient is already named \"" + cleanName + "\".");
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        IngredientRecord ingredient;
        if (id <= 0) {
            long newId = repository.nextIngredientId();
            ingredient = new IngredientRecord(newId, cleanName, cleanUnit, BigDecimal.ZERO,
                    reorderLevel, unitCost, supplierName, LocalDateTime.now());
        } else {
            Optional<IngredientRecord> existing = repository.findIngredient(id);
            if (existing.isEmpty()) return OperationResult.failure("Ingredient could not be found.");
            ingredient = existing.get();
            ingredient.setName(cleanName);
            ingredient.setUnit(cleanUnit);
            ingredient.setReorderLevel(reorderLevel);
            ingredient.setUnitCost(unitCost);
            ingredient.setSupplierName(supplierName);
            ingredient.setLastUpdated(LocalDateTime.now());
        }
        return OperationResult.success(repository.saveIngredient(ingredient));
    }

    /**
     * Records a stock movement and applies it to the ingredient's running quantity.
     * Rejects any transaction that would drive stock on hand below zero (FR18).
     */
    public OperationResult<StockTransactionRecord> recordTransaction(long ingredientId, String type,
                                                                       String quantityRaw, String note,
                                                                       String performedBy) {
        List<String> errors = new ArrayList<>();
        Optional<IngredientRecord> ingredientOpt = repository.findIngredient(ingredientId);
        if (ingredientOpt.isEmpty()) return OperationResult.failure("Ingredient could not be found.");
        IngredientRecord ingredient = ingredientOpt.get();

        String cleanType = type == null ? "" : type.trim().toUpperCase();
        if (!TRANSACTION_TYPES.contains(cleanType)) {
            errors.add("Select a valid stock transaction type.");
        }

        BigDecimal quantity = parsePositive(quantityRaw, "Quantity", errors);
        if (!errors.isEmpty()) return OperationResult.failure(errors);

        BigDecimal signedQuantity;
        BigDecimal newLevel;
        switch (cleanType) {
            case "PURCHASE":
            case "RETURN":
                signedQuantity = quantity;
                newLevel = ingredient.getCurrentQuantity().add(quantity);
                break;
            case "USAGE":
            case "WASTE":
                signedQuantity = quantity.negate();
                newLevel = ingredient.getCurrentQuantity().subtract(quantity);
                break;
            case "ADJUSTMENT":
                // Adjustment quantity may raise or lower stock to a counted value; treat the
                // submitted amount as the new absolute quantity for a simple, auditable rule.
                signedQuantity = quantity.subtract(ingredient.getCurrentQuantity());
                newLevel = quantity;
                break;
            default:
                return OperationResult.failure("Select a valid stock transaction type.");
        }

        if (newLevel.compareTo(BigDecimal.ZERO) < 0) {
            return OperationResult.failure(
                    "This transaction would take " + ingredient.getName() + " below zero stock.");
        }

        long transactionId = repository.nextTransactionId();
        StockTransactionRecord transaction = new StockTransactionRecord(transactionId, ingredientId,
                ingredient.getName(), cleanType, signedQuantity,
                note == null || note.trim().isEmpty() ? null : note.trim(),
                performedBy, LocalDateTime.now());

        ingredient.setCurrentQuantity(newLevel);
        ingredient.setLastUpdated(LocalDateTime.now());
        repository.saveIngredient(ingredient);
        repository.saveStockTransaction(transaction);

        return OperationResult.success(transaction);
    }

    /**
     * Deletes an ingredient only when it has no recorded stock movements, preserving
     * auditable history for anything that has already been transacted (FR20).
     */
    public OperationResult<Void> deleteIngredient(long id) {
        if (repository.hasTransactions(id)) {
            return OperationResult.failure(
                    "This ingredient has recorded stock history and cannot be deleted.");
        }
        if (!repository.deleteIngredient(id)) {
            return OperationResult.failure("Ingredient could not be found.");
        }
        return OperationResult.success(null);
    }

    private BigDecimal parseNonNegative(String raw, String label, List<String> errors) {
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(label + " cannot be negative.");
                return BigDecimal.ZERO;
            }
            return value;
        } catch (Exception ex) {
            errors.add(label + " must be a valid number.");
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal parsePositive(String raw, String label, List<String> errors) {
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(label + " must be greater than zero.");
                return BigDecimal.ZERO;
            }
            return value;
        } catch (Exception ex) {
            errors.add(label + " must be a valid number.");
            return BigDecimal.ZERO;
        }
    }
}
