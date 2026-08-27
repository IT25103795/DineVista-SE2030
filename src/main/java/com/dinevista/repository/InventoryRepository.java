package com.dinevista.repository;

import com.dinevista.model.IngredientRecord;
import com.dinevista.model.StockTransactionRecord;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for the Inventory Management module (Hansaka A. K. / IT25103798).
 * An in-memory implementation is provided for the default runtime mode; a JDBC
 * implementation can be added following the same pattern as
 * {@link JdbcReservationOrderRepository} against the {@code ingredient} and
 * {@code stock_transaction} tables once MySQL mode is required.
 */
public interface InventoryRepository {
    List<IngredientRecord> findAllIngredients();
    Optional<IngredientRecord> findIngredient(long id);
    Optional<IngredientRecord> findIngredientByName(String name);
    IngredientRecord saveIngredient(IngredientRecord ingredient);
    boolean deleteIngredient(long id);

    StockTransactionRecord saveStockTransaction(StockTransactionRecord transaction);
    List<StockTransactionRecord> findTransactionsForIngredient(long ingredientId);
    List<StockTransactionRecord> findAllTransactions();
    boolean hasTransactions(long ingredientId);

    long nextIngredientId();
    long nextTransactionId();
}
