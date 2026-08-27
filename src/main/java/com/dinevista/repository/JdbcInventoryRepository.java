package com.dinevista.repository;

import com.dinevista.model.IngredientRecord;
import com.dinevista.model.StockTransactionRecord;
import com.dinevista.util.DatabaseConfig;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL-backed {@link InventoryRepository} for the Inventory Management module
 * (Hansaka A. K. / IT25103798). Persists against the {@code ingredient} and
 * {@code stock_transaction} tables in database/schema.sql, following the same
 * connection and error-handling pattern as {@link JdbcReservationOrderRepository}.
 *
 * The demo login system has no numeric user id backing a session, so — exactly
 * like {@code reservation_status_history.changed_by_name} and
 * {@code order_status_history.changed_by_name} — the actor is stored as a plain
 * name in a {@code performed_by_name} column rather than the FK column
 * {@code performed_by}. That column is added automatically on startup if the
 * database was created from an older copy of schema.sql.
 */
public class JdbcInventoryRepository implements InventoryRepository {
    private final DatabaseConfig config;

    public JdbcInventoryRepository(DatabaseConfig config) throws SQLException {
        this.config = config;
        try (Connection ignored = config.openConnection()) {
            // Fail early so the application can safely fall back to memory mode.
        }
        ensurePerformedByNameColumn();
    }

    private void ensurePerformedByNameColumn() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stock_transaction' "
                + "AND COLUMN_NAME = 'performed_by_name'";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(checkSql);
             ResultSet rows = statement.executeQuery()) {
            if (rows.next() && rows.getInt(1) > 0) return;
        }
        String alterSql = "ALTER TABLE stock_transaction ADD COLUMN performed_by_name VARCHAR(160)";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(alterSql)) {
            statement.executeUpdate();
        }
    }

    @Override
    public List<IngredientRecord> findAllIngredients() {
        String sql = "SELECT ingredient_id, ingredient_name, unit, current_quantity, reorder_level, "
                + "unit_cost, supplier_name, last_updated FROM ingredient ORDER BY ingredient_name";
        List<IngredientRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapIngredient(rows));
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load ingredients.", ex);
        }
        return result;
    }

    @Override
    public Optional<IngredientRecord> findIngredient(long id) {
        String sql = "SELECT ingredient_id, ingredient_name, unit, current_quantity, reorder_level, "
                + "unit_cost, supplier_name, last_updated FROM ingredient WHERE ingredient_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapIngredient(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load ingredient.", ex);
        }
    }

    @Override
    public Optional<IngredientRecord> findIngredientByName(String name) {
        String sql = "SELECT ingredient_id, ingredient_name, unit, current_quantity, reorder_level, "
                + "unit_cost, supplier_name, last_updated FROM ingredient WHERE ingredient_name = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapIngredient(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load ingredient.", ex);
        }
    }

    @Override
    public IngredientRecord saveIngredient(IngredientRecord ingredient) {
        String existsSql = "SELECT ingredient_id FROM ingredient WHERE ingredient_id = ?";
        try (Connection connection = config.openConnection()) {
            boolean exists;
            try (PreparedStatement statement = connection.prepareStatement(existsSql)) {
                statement.setLong(1, ingredient.getId());
                try (ResultSet rows = statement.executeQuery()) {
                    exists = rows.next();
                }
            }
            if (exists) {
                updateIngredient(connection, ingredient);
            } else {
                insertIngredient(connection, ingredient);
            }
            return ingredient;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to save ingredient.", ex);
        }
    }

    private void insertIngredient(Connection connection, IngredientRecord ingredient) throws SQLException {
        String sql = "INSERT INTO ingredient (ingredient_id, ingredient_name, unit, current_quantity, "
                + "reorder_level, unit_cost, supplier_name, last_updated) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindIngredient(statement, ingredient);
            statement.executeUpdate();
        }
    }

    private void updateIngredient(Connection connection, IngredientRecord ingredient) throws SQLException {
        String sql = "UPDATE ingredient SET ingredient_name = ?, unit = ?, current_quantity = ?, "
                + "reorder_level = ?, unit_cost = ?, supplier_name = ?, last_updated = ? "
                + "WHERE ingredient_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ingredient.getName());
            statement.setString(2, ingredient.getUnit());
            statement.setBigDecimal(3, ingredient.getCurrentQuantity());
            statement.setBigDecimal(4, ingredient.getReorderLevel());
            statement.setBigDecimal(5, ingredient.getUnitCost());
            statement.setString(6, ingredient.getSupplierName());
            statement.setTimestamp(7, Timestamp.valueOf(ingredient.getLastUpdated()));
            statement.setLong(8, ingredient.getId());
            statement.executeUpdate();
        }
    }

    private void bindIngredient(PreparedStatement statement, IngredientRecord ingredient) throws SQLException {
        statement.setLong(1, ingredient.getId());
        statement.setString(2, ingredient.getName());
        statement.setString(3, ingredient.getUnit());
        statement.setBigDecimal(4, ingredient.getCurrentQuantity());
        statement.setBigDecimal(5, ingredient.getReorderLevel());
        statement.setBigDecimal(6, ingredient.getUnitCost());
        statement.setString(7, ingredient.getSupplierName());
        statement.setTimestamp(8, Timestamp.valueOf(ingredient.getLastUpdated()));
    }

    @Override
    public boolean deleteIngredient(long id) {
        String sql = "DELETE FROM ingredient WHERE ingredient_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to delete ingredient.", ex);
        }
    }

    @Override
    public StockTransactionRecord saveStockTransaction(StockTransactionRecord transaction) {
        String sql = "INSERT INTO stock_transaction (transaction_id, ingredient_id, transaction_type, "
                + "quantity, reference_note, performed_by_name, transaction_time) VALUES (?,?,?,?,?,?,?)";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, transaction.getId());
            statement.setLong(2, transaction.getIngredientId());
            statement.setString(3, transaction.getTransactionType());
            statement.setBigDecimal(4, transaction.getQuantity());
            statement.setString(5, transaction.getReferenceNote());
            statement.setString(6, transaction.getPerformedBy());
            statement.setTimestamp(7, Timestamp.valueOf(transaction.getTransactionTime()));
            statement.executeUpdate();
            return transaction;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to record stock transaction.", ex);
        }
    }

    @Override
    public List<StockTransactionRecord> findTransactionsForIngredient(long ingredientId) {
        String sql = "SELECT st.transaction_id, st.ingredient_id, i.ingredient_name, st.transaction_type, "
                + "st.quantity, st.reference_note, st.performed_by_name, st.transaction_time "
                + "FROM stock_transaction st JOIN ingredient i ON i.ingredient_id = st.ingredient_id "
                + "WHERE st.ingredient_id = ? ORDER BY st.transaction_time DESC, st.transaction_id DESC";
        List<StockTransactionRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ingredientId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapTransaction(rows));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load stock history.", ex);
        }
        return result;
    }

    @Override
    public List<StockTransactionRecord> findAllTransactions() {
        String sql = "SELECT st.transaction_id, st.ingredient_id, i.ingredient_name, st.transaction_type, "
                + "st.quantity, st.reference_note, st.performed_by_name, st.transaction_time "
                + "FROM stock_transaction st JOIN ingredient i ON i.ingredient_id = st.ingredient_id "
                + "ORDER BY st.transaction_time DESC, st.transaction_id DESC";
        List<StockTransactionRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapTransaction(rows));
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load stock history.", ex);
        }
        return result;
    }

    @Override
    public boolean hasTransactions(long ingredientId) {
        String sql = "SELECT COUNT(*) FROM stock_transaction WHERE ingredient_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ingredientId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to check stock history.", ex);
        }
    }

    @Override
    public long nextIngredientId() {
        return nextId("ingredient", "ingredient_id");
    }

    @Override
    public long nextTransactionId() {
        return nextId("stock_transaction", "transaction_id");
    }

    private long nextId(String table, String column) {
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) + 1 FROM " + table;
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            return rows.next() ? rows.getLong(1) : 1;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to allocate a database identifier.", ex);
        }
    }

    private IngredientRecord mapIngredient(ResultSet rows) throws SQLException {
        java.sql.Timestamp updated = rows.getTimestamp("last_updated");
        return new IngredientRecord(
                rows.getLong("ingredient_id"),
                rows.getString("ingredient_name"),
                rows.getString("unit"),
                rows.getBigDecimal("current_quantity"),
                rows.getBigDecimal("reorder_level"),
                rows.getBigDecimal("unit_cost"),
                rows.getString("supplier_name"),
                updated == null ? LocalDateTime.now() : updated.toLocalDateTime());
    }

    private StockTransactionRecord mapTransaction(ResultSet rows) throws SQLException {
        BigDecimal quantity = rows.getBigDecimal("quantity");
        return new StockTransactionRecord(
                rows.getLong("transaction_id"),
                rows.getLong("ingredient_id"),
                rows.getString("ingredient_name"),
                rows.getString("transaction_type"),
                quantity,
                rows.getString("reference_note"),
                rows.getString("performed_by_name"),
                rows.getTimestamp("transaction_time").toLocalDateTime());
    }

    private static IllegalStateException repositoryFailure(String message, SQLException ex) {
        return new IllegalStateException(message + " " + ex.getMessage(), ex);
    }
}
