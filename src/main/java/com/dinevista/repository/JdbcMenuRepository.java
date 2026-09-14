package com.dinevista.repository;

import com.dinevista.model.MenuCategoryRecord;
import com.dinevista.model.MenuItemAdminRecord;
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
 * MySQL-backed {@link MenuRepository} for the Menu Management module.
 * Persists against the {@code menu_category} and {@code menu_item} tables in
 * database/schema.sql, following the same connection and error-handling pattern
 * as {@link JdbcInventoryRepository}. These are the same tables the
 * Reservation/Order module reads from via {@code findAllMenuItems()}, so writes
 * made here are visible on the public menu immediately.
 */
public class JdbcMenuRepository implements MenuRepository {
    private static final String ITEM_SELECT =
            "SELECT m.menu_item_id, m.category_id, c.category_name, m.item_name, m.description, "
            + "m.price, m.image_path, m.preparation_minutes, m.dietary_type, m.spice_level, "
            + "m.availability_status, m.created_at, m.updated_at "
            + "FROM menu_item m JOIN menu_category c ON c.category_id = m.category_id ";

    private final DatabaseConfig config;

    public JdbcMenuRepository(DatabaseConfig config) throws SQLException {
        this.config = config;
        try (Connection ignored = config.openConnection()) {
            // Fail early so the application can safely fall back to memory mode.
        }
    }

    // ---- Categories ----

    @Override
    public List<MenuCategoryRecord> findAllCategories() {
        String sql = "SELECT category_id, category_name, description, display_order, is_active "
                + "FROM menu_category ORDER BY display_order, category_name";
        List<MenuCategoryRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapCategory(rows));
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load menu categories.", ex);
        }
        return result;
    }

    @Override
    public List<MenuCategoryRecord> findActiveCategories() {
        String sql = "SELECT category_id, category_name, description, display_order, is_active "
                + "FROM menu_category WHERE is_active = TRUE ORDER BY display_order, category_name";
        List<MenuCategoryRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapCategory(rows));
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load menu categories.", ex);
        }
        return result;
    }

    @Override
    public Optional<MenuCategoryRecord> findCategory(long id) {
        String sql = "SELECT category_id, category_name, description, display_order, is_active "
                + "FROM menu_category WHERE category_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapCategory(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load menu category.", ex);
        }
    }

    @Override
    public Optional<MenuCategoryRecord> findCategoryByName(String name) {
        String sql = "SELECT category_id, category_name, description, display_order, is_active "
                + "FROM menu_category WHERE category_name = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapCategory(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load menu category.", ex);
        }
    }

    @Override
    public MenuCategoryRecord saveCategory(MenuCategoryRecord category) {
        String existsSql = "SELECT category_id FROM menu_category WHERE category_id = ?";
        try (Connection connection = config.openConnection()) {
            boolean exists;
            try (PreparedStatement statement = connection.prepareStatement(existsSql)) {
                statement.setLong(1, category.getId());
                try (ResultSet rows = statement.executeQuery()) {
                    exists = rows.next();
                }
            }
            if (exists) {
                updateCategory(connection, category);
            } else {
                insertCategory(connection, category);
            }
            return category;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to save menu category.", ex);
        }
    }

    private void insertCategory(Connection connection, MenuCategoryRecord category) throws SQLException {
        String sql = "INSERT INTO menu_category (category_id, category_name, description, "
                + "display_order, is_active) VALUES (?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, category.getId());
            statement.setString(2, category.getName());
            statement.setString(3, category.getDescription());
            statement.setInt(4, category.getDisplayOrder());
            statement.setBoolean(5, category.isActive());
            statement.executeUpdate();
        }
    }

    private void updateCategory(Connection connection, MenuCategoryRecord category) throws SQLException {
        String sql = "UPDATE menu_category SET category_name = ?, description = ?, "
                + "display_order = ?, is_active = ? WHERE category_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, category.getDisplayOrder());
            statement.setBoolean(4, category.isActive());
            statement.setLong(5, category.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean deleteCategory(long id) {
        String sql = "DELETE FROM menu_category WHERE category_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to delete menu category.", ex);
        }
    }

    @Override
    public boolean categoryHasItems(long categoryId) {
        String sql = "SELECT COUNT(*) FROM menu_item WHERE category_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, categoryId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to check menu category usage.", ex);
        }
    }

    @Override
    public long nextCategoryId() {
        return nextId("menu_category", "category_id");
    }

    // ---- Items ----

    @Override
    public List<MenuItemAdminRecord> findAllItems() {
        String sql = ITEM_SELECT + "ORDER BY c.display_order, m.item_name";
        List<MenuItemAdminRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) result.add(mapItem(rows));
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load menu items.", ex);
        }
        return result;
    }

    @Override
    public Optional<MenuItemAdminRecord> findItem(long id) {
        String sql = ITEM_SELECT + "WHERE m.menu_item_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapItem(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load menu item.", ex);
        }
    }

    @Override
    public Optional<MenuItemAdminRecord> findItemByNameInCategory(String name, long categoryId) {
        String sql = ITEM_SELECT + "WHERE m.category_id = ? AND m.item_name = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, categoryId);
            statement.setString(2, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapItem(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load menu item.", ex);
        }
    }

    @Override
    public MenuItemAdminRecord saveItem(MenuItemAdminRecord item) {
        String existsSql = "SELECT menu_item_id FROM menu_item WHERE menu_item_id = ?";
        try (Connection connection = config.openConnection()) {
            boolean exists;
            try (PreparedStatement statement = connection.prepareStatement(existsSql)) {
                statement.setLong(1, item.getId());
                try (ResultSet rows = statement.executeQuery()) {
                    exists = rows.next();
                }
            }
            if (exists) {
                updateItem(connection, item);
            } else {
                insertItem(connection, item);
            }
            return findItem(item.getId()).orElse(item);
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to save menu item.", ex);
        }
    }

    private void insertItem(Connection connection, MenuItemAdminRecord item) throws SQLException {
        String sql = "INSERT INTO menu_item (menu_item_id, category_id, item_name, description, "
                + "price, image_path, preparation_minutes, dietary_type, spice_level, "
                + "availability_status, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        LocalDateTime now = LocalDateTime.now();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, item.getId());
            statement.setLong(2, item.getCategoryId());
            statement.setString(3, item.getName());
            statement.setString(4, item.getDescription());
            statement.setBigDecimal(5, item.getPrice());
            statement.setString(6, item.getImagePath());
            statement.setInt(7, item.getPreparationMinutes());
            statement.setString(8, item.getDietaryType());
            statement.setString(9, item.getSpiceLevel());
            statement.setString(10, item.getAvailabilityStatus());
            statement.setTimestamp(11, Timestamp.valueOf(now));
            statement.setTimestamp(12, Timestamp.valueOf(now));
            statement.executeUpdate();
        }
    }

    private void updateItem(Connection connection, MenuItemAdminRecord item) throws SQLException {
        String sql = "UPDATE menu_item SET category_id = ?, item_name = ?, description = ?, "
                + "price = ?, image_path = ?, preparation_minutes = ?, dietary_type = ?, "
                + "spice_level = ?, availability_status = ?, updated_at = ? WHERE menu_item_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, item.getCategoryId());
            statement.setString(2, item.getName());
            statement.setString(3, item.getDescription());
            statement.setBigDecimal(4, item.getPrice());
            statement.setString(5, item.getImagePath());
            statement.setInt(6, item.getPreparationMinutes());
            statement.setString(7, item.getDietaryType());
            statement.setString(8, item.getSpiceLevel());
            statement.setString(9, item.getAvailabilityStatus());
            statement.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(11, item.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public boolean deleteItem(long id) {
        try (Connection connection = config.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement s1 = connection.prepareStatement(
                        "DELETE FROM menu_item_ingredient WHERE menu_item_id = ?")) {
                    s1.setLong(1, id);
                    s1.executeUpdate();
                }
                try (PreparedStatement s2 = connection.prepareStatement(
                        "DELETE FROM order_item WHERE menu_item_id = ?")) {
                    s2.setLong(1, id);
                    s2.executeUpdate();
                }
                int rows;
                try (PreparedStatement s3 = connection.prepareStatement(
                        "DELETE FROM menu_item WHERE menu_item_id = ?")) {
                    s3.setLong(1, id);
                    rows = s3.executeUpdate();
                }
                connection.commit();
                return rows > 0;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to delete menu item.", ex);
        }
    }

    @Override
    public boolean itemReferencedByOrders(long id) {
        String sql = "SELECT COUNT(*) FROM order_item WHERE menu_item_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to check menu item order history.", ex);
        }
    }

    @Override
    public long nextItemId() {
        return nextId("menu_item", "menu_item_id");
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

    private MenuCategoryRecord mapCategory(ResultSet rows) throws SQLException {
        return new MenuCategoryRecord(
                rows.getLong("category_id"),
                rows.getString("category_name"),
                rows.getString("description"),
                rows.getInt("display_order"),
                rows.getBoolean("is_active"));
    }

    private MenuItemAdminRecord mapItem(ResultSet rows) throws SQLException {
        BigDecimal price = rows.getBigDecimal("price");
        Timestamp created = rows.getTimestamp("created_at");
        Timestamp updated = rows.getTimestamp("updated_at");
        return new MenuItemAdminRecord(
                rows.getLong("menu_item_id"),
                rows.getLong("category_id"),
                rows.getString("category_name"),
                rows.getString("item_name"),
                rows.getString("description"),
                price,
                rows.getString("image_path"),
                rows.getInt("preparation_minutes"),
                rows.getString("dietary_type"),
                rows.getString("spice_level"),
                rows.getString("availability_status"),
                created == null ? LocalDateTime.now() : created.toLocalDateTime(),
                updated == null ? LocalDateTime.now() : updated.toLocalDateTime());
    }

    private static IllegalStateException repositoryFailure(String message, SQLException ex) {
        return new IllegalStateException(message + " " + ex.getMessage(), ex);
    }
}
