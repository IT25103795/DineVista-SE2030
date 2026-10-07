package com.dinevista.util;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks which customer requests have been opened in the shared manager portal. */
public final class ManagerRequestReadState {
    public static final String RESERVATION = "RESERVATION";
    public static final String ORDER = "ORDER";
    public static final String EVENT = "EVENT";
    private static final String CONTEXT_KEY = ManagerRequestReadState.class.getName();

    private final DatabaseConfig config;
    private final Set<String> openedInMemory = ConcurrentHashMap.newKeySet();

    private ManagerRequestReadState(DatabaseConfig config) throws SQLException {
        this.config = config;
        if (config.isMysqlEnabled()) initializeTable();
    }

    public static ManagerRequestReadState get(ServletContext context) {
        synchronized (context) {
            ManagerRequestReadState state = (ManagerRequestReadState) context.getAttribute(CONTEXT_KEY);
            if (state == null) {
                try {
                    state = new ManagerRequestReadState(DatabaseConfig.load());
                } catch (SQLException ex) {
                    throw new IllegalStateException("Manager request read state could not start.", ex);
                }
                context.setAttribute(CONTEXT_KEY, state);
            }
            return state;
        }
    }

    public Set<String> openedReferences(String type) {
        validateType(type);
        if (!config.isMysqlEnabled()) {
            Set<String> result = new HashSet<>();
            for (String key : openedInMemory) {
                if (key.startsWith(type + ":")) result.add(key.substring(type.length() + 1));
            }
            return Collections.unmodifiableSet(result);
        }
        Set<String> result = new HashSet<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT request_reference FROM manager_request_read WHERE request_type=?")) {
            statement.setString(1, type);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(rows.getString(1));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to load manager request read state.", ex);
        }
        return result;
    }

    public void markOpened(String type, String reference) {
        validateType(type);
        if (reference == null || reference.isBlank()) return;
        if (!config.isMysqlEnabled()) {
            openedInMemory.add(type + ":" + reference);
            return;
        }
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT IGNORE INTO manager_request_read (request_type,request_reference) VALUES (?,?)")) {
            statement.setString(1, type);
            statement.setString(2, reference);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to mark manager request as opened.", ex);
        }
    }

    private void initializeTable() throws SQLException {
        try (Connection connection = config.openConnection()) {
            boolean existed;
            try (PreparedStatement check = connection.prepareStatement(
                    "SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() "
                    + "AND table_name='manager_request_read' LIMIT 1");
                 ResultSet rows = check.executeQuery()) {
                existed = rows.next();
            }
            try (PreparedStatement create = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS manager_request_read ("
                    + "request_type VARCHAR(20) NOT NULL,request_reference VARCHAR(30) NOT NULL,"
                    + "opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY(request_type,request_reference)) ENGINE=InnoDB")) {
                create.executeUpdate();
            }
            // Existing records predate unread tracking, so do not label the entire old queue NEW.
            if (!existed) {
                seedOpened(connection, RESERVATION, "table_reservation", "reservation_reference");
                seedOpened(connection, ORDER, "food_order", "order_reference");
                seedOpened(connection, EVENT, "event_booking", "event_reference");
            }
        }
    }

    private void seedOpened(Connection connection, String type, String table, String referenceColumn)
            throws SQLException {
        try (PreparedStatement seed = connection.prepareStatement(
                "INSERT IGNORE INTO manager_request_read (request_type,request_reference) "
                + "SELECT ?," + referenceColumn + " FROM " + table)) {
            seed.setString(1, type);
            seed.executeUpdate();
        }
    }

    private static void validateType(String type) {
        if (!RESERVATION.equals(type) && !ORDER.equals(type) && !EVENT.equals(type)) {
            throw new IllegalArgumentException("Unknown manager request type.");
        }
    }
}
