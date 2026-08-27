package com.dinevista.repository;

import com.dinevista.model.EventBookingRecord;
import com.dinevista.util.DatabaseConfig;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** MySQL persistence for the public event consultation form. */
public class JdbcEventBookingRepository implements EventBookingRepository {
    private final DatabaseConfig config;

    public JdbcEventBookingRepository(DatabaseConfig config) throws SQLException {
        this.config = config;
        try (Connection connection = config.openConnection()) {
            requireTable(connection, "event_booking");
            requireTable(connection, "event_booking_status_history");
        }
    }

    @Override
    public EventBookingRecord save(EventBookingRecord booking) {
        String insertBooking = "INSERT INTO event_booking "
                + "(event_reference, package_id, venue_id, contact_name, email, phone, event_type, "
                + "event_date, guest_count, requirements_summary, booking_status, estimated_amount) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        String insertHistory = "INSERT INTO event_booking_status_history "
                + "(event_booking_id, status, note) VALUES (?,?,?)";

        try (Connection connection = config.openConnection()) {
            connection.setAutoCommit(false);
            try {
                Long packageId = findId(connection, "event_package", "package_id", "package_name",
                        booking.getPackageName());
                Long venueId = findId(connection, "event_venue", "venue_id", "venue_name",
                        booking.getVenue());
                BigDecimal estimate = estimate(connection, packageId, venueId, booking.getGuestCount());
                long bookingId;

                try (PreparedStatement statement = connection.prepareStatement(
                        insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                    int index = 1;
                    statement.setString(index++, booking.getReference());
                    nullableLong(statement, index++, packageId);
                    nullableLong(statement, index++, venueId);
                    statement.setString(index++, booking.getCustomerName());
                    statement.setString(index++, booking.getEmail());
                    statement.setString(index++, booking.getPhone());
                    statement.setString(index++, booking.getEventType());
                    statement.setDate(index++, Date.valueOf(booking.getEventDate()));
                    statement.setInt(index++, booking.getGuestCount());
                    statement.setString(index++, requirementsSummary(booking));
                    statement.setString(index++, "INQUIRY");
                    statement.setBigDecimal(index, estimate);
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No event booking identifier was generated.");
                        bookingId = keys.getLong(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(insertHistory)) {
                    statement.setLong(1, bookingId);
                    statement.setString(2, "INQUIRY");
                    statement.setString(3, "Event consultation requested through the website.");
                    statement.executeUpdate();
                }

                connection.commit();
                return booking;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to save the event booking to MySQL. "
                    + ex.getMessage(), ex);
        }
    }

    private static void requireTable(Connection connection, String table) throws SQLException {
        try (ResultSet tables = connection.getMetaData().getTables(
                connection.getCatalog(), null, table, new String[]{"TABLE"})) {
            if (!tables.next()) throw new SQLException("Required table " + table + " does not exist.");
        }
    }

    private static Long findId(Connection connection, String table, String idColumn,
                               String nameColumn, String name) throws SQLException {
        String sql = "SELECT " + idColumn + " FROM " + table + " WHERE " + nameColumn + "=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getLong(1) : null;
            }
        }
    }

    private static BigDecimal estimate(Connection connection, Long packageId, Long venueId,
                                       int guestCount) throws SQLException {
        BigDecimal perGuest = BigDecimal.ZERO;
        BigDecimal venueFee = BigDecimal.ZERO;
        if (packageId != null) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT base_price_per_guest FROM event_package WHERE package_id=?")) {
                statement.setLong(1, packageId);
                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) perGuest = rows.getBigDecimal(1);
                }
            }
        }
        if (venueId != null) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT base_fee FROM event_venue WHERE venue_id=?")) {
                statement.setLong(1, venueId);
                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) venueFee = rows.getBigDecimal(1);
                }
            }
        }
        return perGuest.multiply(BigDecimal.valueOf(guestCount)).add(venueFee);
    }

    private static String requirementsSummary(EventBookingRecord booking) {
        StringBuilder summary = new StringBuilder()
                .append("Package: ").append(booking.getPackageName())
                .append("; Venue: ").append(booking.getVenue());
        if (!booking.getNotes().isEmpty()) summary.append("; Notes: ").append(booking.getNotes());
        return summary.toString();
    }

    private static void nullableLong(PreparedStatement statement, int index, Long value)
            throws SQLException {
        if (value == null) statement.setNull(index, java.sql.Types.BIGINT);
        else statement.setLong(index, value);
    }
}
