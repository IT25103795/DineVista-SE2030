package com.dinevista.repository;

import com.dinevista.model.FoodOrderRecord;
import com.dinevista.model.MenuItemRecord;
import com.dinevista.model.OrderItemRecord;
import com.dinevista.model.RestaurantTableRecord;
import com.dinevista.model.StatusHistoryRecord;
import com.dinevista.model.TableReservationRecord;
import com.dinevista.util.DatabaseConfig;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JdbcReservationOrderRepository implements ReservationOrderRepository {
    private final DatabaseConfig config;

    public JdbcReservationOrderRepository(DatabaseConfig config) throws SQLException {
        this.config = config;
        try (Connection ignored = config.openConnection()) {
            // Fail early so the application can safely fall back to memory mode.
        }
    }

    @Override
    public List<MenuItemRecord> findAllMenuItems() {
        String sql = "SELECT m.menu_item_id, c.category_name, m.item_name, m.description, "
                + "m.price, m.image_path, m.dietary_type, m.spice_level, m.availability_status "
                + "FROM menu_item m JOIN menu_category c ON c.category_id = m.category_id "
                + "WHERE c.is_active = TRUE ORDER BY c.display_order, m.item_name";
        List<MenuItemRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                result.add(new MenuItemRecord(
                        rows.getLong("menu_item_id"),
                        rows.getString("category_name"),
                        rows.getString("item_name"),
                        rows.getString("description"),
                        rows.getBigDecimal("price"),
                        imageName(rows.getString("image_path")),
                        rows.getString("dietary_type"),
                        rows.getString("spice_level"),
                        "AVAILABLE".equals(rows.getString("availability_status"))));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load menu items.", ex);
        }
        return result;
    }

    @Override
    public Optional<MenuItemRecord> findMenuItem(long id) {
        return findAllMenuItems().stream().filter(item -> item.getId() == id).findFirst();
    }

    @Override
    public List<RestaurantTableRecord> findAllTables() {
        String sql = "SELECT table_id, table_code, seating_area, capacity, table_status "
                + "FROM restaurant_table ORDER BY capacity, table_code";
        List<RestaurantTableRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                result.add(new RestaurantTableRecord(
                        rows.getLong("table_id"),
                        rows.getString("table_code"),
                        rows.getString("seating_area"),
                        rows.getInt("capacity"),
                        rows.getString("table_status")));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load restaurant tables.", ex);
        }
        return result;
    }

    @Override
    public Optional<RestaurantTableRecord> findTable(long id) {
        return findAllTables().stream().filter(table -> table.getId() == id).findFirst();
    }

    @Override
    public TableReservationRecord saveReservation(TableReservationRecord reservation) {
        String existsSql = "SELECT reservation_id FROM table_reservation WHERE reservation_reference = ?";
        try (Connection connection = config.openConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean exists;
                try (PreparedStatement statement = connection.prepareStatement(existsSql)) {
                    statement.setString(1, reservation.getReference());
                    try (ResultSet rows = statement.executeQuery()) {
                        exists = rows.next();
                    }
                }

                if (exists) updateReservation(connection, reservation);
                else insertReservation(connection, reservation);
                replaceReservationHistory(connection, reservation);
                connection.commit();
                return reservation;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to save reservation.", ex);
        }
    }

    private void insertReservation(Connection connection, TableReservationRecord reservation)
            throws SQLException {
        String sql = "INSERT INTO table_reservation "
                + "(reservation_id, reservation_reference, customer_key, table_id, guest_name, "
                + "email, phone, reservation_date, reservation_time, party_size, seating_preference, "
                + "occasion_notes, reservation_status, staff_note, cancellation_reason, created_at, updated_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindReservation(statement, reservation, true);
            statement.executeUpdate();
        }
    }

    private void updateReservation(Connection connection, TableReservationRecord reservation)
            throws SQLException {
        String sql = "UPDATE table_reservation SET customer_key=?, table_id=?, guest_name=?, "
                + "email=?, phone=?, reservation_date=?, reservation_time=?, party_size=?, "
                + "seating_preference=?, occasion_notes=?, reservation_status=?, staff_note=?, "
                + "cancellation_reason=?, updated_at=? WHERE reservation_reference=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, reservation.getCustomerKey());
            nullableLong(statement, index++, reservation.getTableId());
            statement.setString(index++, reservation.getGuestName());
            statement.setString(index++, reservation.getEmail());
            statement.setString(index++, reservation.getPhone());
            statement.setDate(index++, Date.valueOf(reservation.getReservationDate()));
            statement.setTime(index++, Time.valueOf(reservation.getReservationTime()));
            statement.setInt(index++, reservation.getPartySize());
            statement.setString(index++, reservation.getSeatingPreference());
            statement.setString(index++, reservation.getOccasionNotes());
            statement.setString(index++, reservation.getStatus());
            statement.setString(index++, reservation.getStaffNote());
            statement.setString(index++, reservation.getCancellationReason());
            statement.setTimestamp(index++, Timestamp.valueOf(reservation.getUpdatedAt()));
            statement.setString(index, reservation.getReference());
            statement.executeUpdate();
        }
    }

    private void bindReservation(PreparedStatement statement, TableReservationRecord reservation,
                                 boolean includeIdentity) throws SQLException {
        int index = 1;
        if (includeIdentity) {
            statement.setLong(index++, reservation.getId());
            statement.setString(index++, reservation.getReference());
        }
        statement.setString(index++, reservation.getCustomerKey());
        nullableLong(statement, index++, reservation.getTableId());
        statement.setString(index++, reservation.getGuestName());
        statement.setString(index++, reservation.getEmail());
        statement.setString(index++, reservation.getPhone());
        statement.setDate(index++, Date.valueOf(reservation.getReservationDate()));
        statement.setTime(index++, Time.valueOf(reservation.getReservationTime()));
        statement.setInt(index++, reservation.getPartySize());
        statement.setString(index++, reservation.getSeatingPreference());
        statement.setString(index++, reservation.getOccasionNotes());
        statement.setString(index++, reservation.getStatus());
        statement.setString(index++, reservation.getStaffNote());
        statement.setString(index++, reservation.getCancellationReason());
        statement.setTimestamp(index++, Timestamp.valueOf(reservation.getCreatedAt()));
        statement.setTimestamp(index, Timestamp.valueOf(reservation.getUpdatedAt()));
    }

    private void replaceReservationHistory(Connection connection, TableReservationRecord reservation)
            throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM reservation_status_history WHERE reservation_id=?")) {
            delete.setLong(1, reservation.getId());
            delete.executeUpdate();
        }
        String sql = "INSERT INTO reservation_status_history "
                + "(reservation_id, status, changed_by_name, note, changed_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            List<StatusHistoryRecord> historyItems = reservation.getHistory();
            // Models keep newest entries first. Insert oldest first so the auto-increment
            // id remains a reliable newest-first tiebreaker when MySQL timestamps match.
            for (int i = historyItems.size() - 1; i >= 0; i--) {
                StatusHistoryRecord history = historyItems.get(i);
                insert.setLong(1, reservation.getId());
                insert.setString(2, history.getStatus());
                insert.setString(3, history.getChangedBy());
                insert.setString(4, history.getNote());
                insert.setTimestamp(5, Timestamp.valueOf(history.getChangedAt()));
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    @Override
    public Optional<TableReservationRecord> findReservationByReference(String reference) {
        String sql = reservationSelect() + " WHERE r.reservation_reference=?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reference);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) return Optional.empty();
                return Optional.of(mapReservation(connection, rows));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load reservation.", ex);
        }
    }

    @Override
    public List<TableReservationRecord> findReservationsByCustomer(String customerKey) {
        return loadReservations(" WHERE r.customer_key=? ORDER BY r.reservation_date DESC, r.reservation_time DESC",
                statement -> statement.setString(1, customerKey));
    }

    @Override
    public List<TableReservationRecord> findAllReservations() {
        return loadReservations(" ORDER BY r.reservation_date, r.reservation_time", statement -> {});
    }

    private List<TableReservationRecord> loadReservations(String suffix, SqlBinder binder) {
        List<TableReservationRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(reservationSelect() + suffix)) {
            binder.bind(statement);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapReservation(connection, rows));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load reservations.", ex);
        }
        return result;
    }

    private String reservationSelect() {
        return "SELECT r.*, t.table_code FROM table_reservation r "
                + "LEFT JOIN restaurant_table t ON t.table_id=r.table_id";
    }

    private TableReservationRecord mapReservation(Connection connection, ResultSet rows)
            throws SQLException {
        Long tableId = nullableLong(rows, "table_id");
        return new TableReservationRecord(
                rows.getLong("reservation_id"),
                rows.getString("reservation_reference"),
                rows.getString("customer_key"),
                rows.getString("guest_name"),
                rows.getString("email"),
                rows.getString("phone"),
                rows.getDate("reservation_date").toLocalDate(),
                rows.getTime("reservation_time").toLocalTime(),
                rows.getInt("party_size"),
                rows.getString("seating_preference"),
                rows.getString("occasion_notes"),
                rows.getString("reservation_status"),
                tableId,
                rows.getString("table_code"),
                rows.getString("staff_note"),
                rows.getString("cancellation_reason"),
                rows.getTimestamp("created_at").toLocalDateTime(),
                rows.getTimestamp("updated_at").toLocalDateTime(),
                loadReservationHistory(connection, rows.getLong("reservation_id")));
    }

    private List<StatusHistoryRecord> loadReservationHistory(Connection connection, long id)
            throws SQLException {
        String sql = "SELECT status, note, changed_by_name, changed_at "
                + "FROM reservation_status_history WHERE reservation_id=? ORDER BY changed_at DESC, history_id DESC";
        List<StatusHistoryRecord> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new StatusHistoryRecord(
                            rows.getString("status"), rows.getString("note"),
                            rows.getString("changed_by_name"),
                            rows.getTimestamp("changed_at").toLocalDateTime()));
                }
            }
        }
        return result;
    }

    @Override
    public boolean isTableAvailable(long tableId, LocalDate date, LocalTime time,
                                    int slotMinutes, String excludingReference) {
        String sql = "SELECT reservation_reference, reservation_time FROM table_reservation "
                + "WHERE table_id=? AND reservation_date=? "
                + "AND reservation_status NOT IN ('COMPLETED','CANCELLED','REJECTED','NO_SHOW')";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, tableId);
            statement.setDate(2, Date.valueOf(date));
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    if (excludingReference != null
                            && excludingReference.equals(rows.getString("reservation_reference"))) continue;
                    long minutes = Math.abs(Duration.between(
                            rows.getTime("reservation_time").toLocalTime(), time).toMinutes());
                    if (minutes < slotMinutes) return false;
                }
            }
            return true;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to check table availability.", ex);
        }
    }

    @Override
    public FoodOrderRecord saveOrder(FoodOrderRecord order) {
        String existsSql = "SELECT order_id FROM food_order WHERE order_reference=?";
        try (Connection connection = config.openConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean exists;
                try (PreparedStatement statement = connection.prepareStatement(existsSql)) {
                    statement.setString(1, order.getReference());
                    try (ResultSet rows = statement.executeQuery()) {
                        exists = rows.next();
                    }
                }
                if (exists) updateOrder(connection, order);
                else insertOrder(connection, order);
                replaceOrderItems(connection, order);
                replaceOrderHistory(connection, order);
                connection.commit();
                return order;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to save food order.", ex);
        }
    }

    private void insertOrder(Connection connection, FoodOrderRecord order) throws SQLException {
        String sql = "INSERT INTO food_order "
                + "(order_id, order_reference, customer_key, reservation_id, customer_name, email, "
                + "phone, order_type, order_status, requested_for, subtotal, service_charge, "
                + "total_amount, order_notes, staff_note, cancellation_reason, created_at, updated_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setLong(index++, order.getId());
            statement.setString(index++, order.getReference());
            statement.setString(index++, order.getCustomerKey());
            nullableLong(statement, index++, reservationId(connection, order.getReservationReference()));
            statement.setString(index++, order.getCustomerName());
            statement.setString(index++, order.getEmail());
            statement.setString(index++, order.getPhone());
            statement.setString(index++, order.getOrderType());
            statement.setString(index++, order.getStatus());
            nullableTimestamp(statement, index++, order.getRequestedFor());
            statement.setBigDecimal(index++, order.getSubtotal());
            statement.setBigDecimal(index++, order.getServiceCharge());
            statement.setBigDecimal(index++, order.getTotalAmount());
            statement.setString(index++, order.getOrderNotes());
            statement.setString(index++, order.getStaffNote());
            statement.setString(index++, order.getCancellationReason());
            statement.setTimestamp(index++, Timestamp.valueOf(order.getCreatedAt()));
            statement.setTimestamp(index, Timestamp.valueOf(order.getUpdatedAt()));
            statement.executeUpdate();
        }
    }

    private void updateOrder(Connection connection, FoodOrderRecord order) throws SQLException {
        String sql = "UPDATE food_order SET order_status=?, staff_note=?, cancellation_reason=?, "
                + "updated_at=? WHERE order_reference=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, order.getStatus());
            statement.setString(2, order.getStaffNote());
            statement.setString(3, order.getCancellationReason());
            statement.setTimestamp(4, Timestamp.valueOf(order.getUpdatedAt()));
            statement.setString(5, order.getReference());
            statement.executeUpdate();
        }
    }

    private void replaceOrderItems(Connection connection, FoodOrderRecord order) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM order_item WHERE order_id=?")) {
            delete.setLong(1, order.getId());
            delete.executeUpdate();
        }
        String sql = "INSERT INTO order_item "
                + "(order_id, menu_item_id, quantity, unit_price, item_notes, line_total) "
                + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            for (OrderItemRecord item : order.getItems()) {
                insert.setLong(1, order.getId());
                insert.setLong(2, item.getMenuItemId());
                insert.setInt(3, item.getQuantity());
                insert.setBigDecimal(4, item.getUnitPrice());
                insert.setString(5, item.getItemNotes());
                insert.setBigDecimal(6, item.getLineTotal());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private void replaceOrderHistory(Connection connection, FoodOrderRecord order) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM order_status_history WHERE order_id=?")) {
            delete.setLong(1, order.getId());
            delete.executeUpdate();
        }
        String sql = "INSERT INTO order_status_history "
                + "(order_id, status, changed_by_name, note, changed_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            List<StatusHistoryRecord> historyItems = order.getHistory();
            // Preserve newest-first ordering even when several changes share one SQL second.
            for (int i = historyItems.size() - 1; i >= 0; i--) {
                StatusHistoryRecord history = historyItems.get(i);
                insert.setLong(1, order.getId());
                insert.setString(2, history.getStatus());
                insert.setString(3, history.getChangedBy());
                insert.setString(4, history.getNote());
                insert.setTimestamp(5, Timestamp.valueOf(history.getChangedAt()));
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    @Override
    public Optional<FoodOrderRecord> findOrderByReference(String reference) {
        String sql = orderSelect() + " WHERE o.order_reference=?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reference);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) return Optional.empty();
                return Optional.of(mapOrder(connection, rows));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load food order.", ex);
        }
    }

    @Override
    public List<FoodOrderRecord> findOrdersByCustomer(String customerKey) {
        return loadOrders(" WHERE o.customer_key=? ORDER BY o.created_at DESC",
                statement -> statement.setString(1, customerKey));
    }

    @Override
    public List<FoodOrderRecord> findAllOrders() {
        return loadOrders(" ORDER BY o.created_at DESC", statement -> {});
    }

    private List<FoodOrderRecord> loadOrders(String suffix, SqlBinder binder) {
        List<FoodOrderRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(orderSelect() + suffix)) {
            binder.bind(statement);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapOrder(connection, rows));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load food orders.", ex);
        }
        return result;
    }

    private String orderSelect() {
        return "SELECT o.*, r.reservation_reference FROM food_order o "
                + "LEFT JOIN table_reservation r ON r.reservation_id=o.reservation_id";
    }

    private FoodOrderRecord mapOrder(Connection connection, ResultSet rows) throws SQLException {
        long orderId = rows.getLong("order_id");
        Timestamp requestedFor = rows.getTimestamp("requested_for");
        return new FoodOrderRecord(
                orderId,
                rows.getString("order_reference"),
                rows.getString("customer_key"),
                rows.getString("customer_name"),
                rows.getString("email"),
                rows.getString("phone"),
                rows.getString("order_type"),
                nullableString(rows.getString("reservation_reference")),
                requestedFor == null ? null : requestedFor.toLocalDateTime(),
                rows.getString("order_notes"),
                rows.getString("order_status"),
                loadOrderItems(connection, orderId),
                rows.getBigDecimal("subtotal"),
                rows.getBigDecimal("service_charge"),
                rows.getBigDecimal("total_amount"),
                rows.getString("staff_note"),
                rows.getString("cancellation_reason"),
                rows.getTimestamp("created_at").toLocalDateTime(),
                rows.getTimestamp("updated_at").toLocalDateTime(),
                loadOrderHistory(connection, orderId));
    }

    private List<OrderItemRecord> loadOrderItems(Connection connection, long orderId)
            throws SQLException {
        String sql = "SELECT oi.menu_item_id, m.item_name, oi.quantity, oi.unit_price, oi.item_notes "
                + "FROM order_item oi JOIN menu_item m ON m.menu_item_id=oi.menu_item_id "
                + "WHERE oi.order_id=? ORDER BY oi.order_item_id";
        List<OrderItemRecord> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new OrderItemRecord(
                            rows.getLong("menu_item_id"), rows.getString("item_name"),
                            rows.getInt("quantity"), rows.getBigDecimal("unit_price"),
                            rows.getString("item_notes")));
                }
            }
        }
        return result;
    }

    private List<StatusHistoryRecord> loadOrderHistory(Connection connection, long orderId)
            throws SQLException {
        String sql = "SELECT status, note, changed_by_name, changed_at "
                + "FROM order_status_history WHERE order_id=? ORDER BY changed_at DESC, history_id DESC";
        List<StatusHistoryRecord> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new StatusHistoryRecord(
                            rows.getString("status"), rows.getString("note"),
                            rows.getString("changed_by_name"),
                            rows.getTimestamp("changed_at").toLocalDateTime()));
                }
            }
        }
        return result;
    }

    private Long reservationId(Connection connection, String reference) throws SQLException {
        if (reference == null || reference.isBlank()) return null;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT reservation_id FROM table_reservation WHERE reservation_reference=?")) {
            statement.setString(1, reference);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getLong(1) : null;
            }
        }
    }

    @Override
    public long nextReservationId() {
        return nextId("table_reservation", "reservation_id");
    }

    @Override
    public long nextOrderId() {
        return nextId("food_order", "order_id");
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

    private static String imageName(String value) {
        if (value == null || value.isBlank()) return "dish-signature.svg";
        int slash = Math.max(value.lastIndexOf('/'), value.lastIndexOf('\\'));
        return slash >= 0 ? value.substring(slash + 1) : value;
    }

    private static void nullableLong(PreparedStatement statement, int index, Long value)
            throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.BIGINT);
        else statement.setLong(index, value);
    }

    private static Long nullableLong(ResultSet rows, String column) throws SQLException {
        long value = rows.getLong(column);
        return rows.wasNull() ? null : value;
    }

    private static void nullableTimestamp(PreparedStatement statement, int index,
                                          LocalDateTime value) throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.TIMESTAMP);
        else statement.setTimestamp(index, Timestamp.valueOf(value));
    }

    private static String nullableString(String value) {
        return value == null ? "" : value;
    }

    private static IllegalStateException repositoryFailure(String message, SQLException ex) {
        return new IllegalStateException(message + " " + ex.getMessage(), ex);
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
