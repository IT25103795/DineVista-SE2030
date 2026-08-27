package com.dinevista.repository;

import com.dinevista.model.IngredientRecord;
import com.dinevista.model.StockTransactionRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory {@link InventoryRepository} used in the default demo storage mode.
 * Seeded with representative ingredients so the module is demonstrable immediately.
 */
public class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<Long, IngredientRecord> ingredients = new ConcurrentHashMap<>();
    private final Map<Long, StockTransactionRecord> transactions = new ConcurrentHashMap<>();
    private final AtomicLong ingredientSequence = new AtomicLong(0);
    private final AtomicLong transactionSequence = new AtomicLong(0);

    public InMemoryInventoryRepository() {
        seed();
    }

    private void seed() {
        seedIngredient("Basmati Rice", "kg", "12", "5", "420", "Colombo Grain Traders");
        seedIngredient("Chicken Breast", "kg", "8", "6", "1450", "Fresh Farms Lanka");
        seedIngredient("Prawns", "kg", "3.5", "4", "3200", "Negombo Seafood Co.");
        seedIngredient("Coconut Milk", "l", "20", "8", "380", "Island Coconut Products");
        seedIngredient("Fresh Cream", "l", "6", "5", "950", "Highland Dairy");
        seedIngredient("Mixed Vegetables", "kg", "15", "6", "260", "Green Valley Suppliers");
        seedIngredient("Cheese", "kg", "4", "3", "2100", "Highland Dairy");
        seedIngredient("Cocoa Powder", "kg", "2", "2", "1800", "Island Coconut Products");
    }

    private void seedIngredient(String name, String unit, String qty, String reorder,
                                 String unitCost, String supplier) {
        long id = nextIngredientId();
        ingredients.put(id, new IngredientRecord(id, name, unit, new BigDecimal(qty),
                new BigDecimal(reorder), new BigDecimal(unitCost), supplier, LocalDateTime.now()));
    }

    @Override
    public List<IngredientRecord> findAllIngredients() {
        List<IngredientRecord> list = new ArrayList<>(ingredients.values());
        list.sort(Comparator.comparing(IngredientRecord::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    @Override
    public Optional<IngredientRecord> findIngredient(long id) {
        return Optional.ofNullable(ingredients.get(id));
    }

    @Override
    public Optional<IngredientRecord> findIngredientByName(String name) {
        return ingredients.values().stream()
                .filter(i -> i.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public IngredientRecord saveIngredient(IngredientRecord ingredient) {
        ingredients.put(ingredient.getId(), ingredient);
        return ingredient;
    }

    @Override
    public boolean deleteIngredient(long id) {
        return ingredients.remove(id) != null;
    }

    @Override
    public StockTransactionRecord saveStockTransaction(StockTransactionRecord transaction) {
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public List<StockTransactionRecord> findTransactionsForIngredient(long ingredientId) {
        List<StockTransactionRecord> list = new ArrayList<>();
        for (StockTransactionRecord transaction : transactions.values()) {
            if (transaction.getIngredientId() == ingredientId) list.add(transaction);
        }
        list.sort(Comparator.comparing(StockTransactionRecord::getTransactionTime).reversed());
        return list;
    }

    @Override
    public List<StockTransactionRecord> findAllTransactions() {
        List<StockTransactionRecord> list = new ArrayList<>(transactions.values());
        list.sort(Comparator.comparing(StockTransactionRecord::getTransactionTime).reversed());
        return list;
    }

    @Override
    public boolean hasTransactions(long ingredientId) {
        return transactions.values().stream().anyMatch(t -> t.getIngredientId() == ingredientId);
    }

    @Override
    public long nextIngredientId() {
        return ingredientSequence.incrementAndGet();
    }

    @Override
    public long nextTransactionId() {
        return transactionSequence.incrementAndGet();
    }
}
