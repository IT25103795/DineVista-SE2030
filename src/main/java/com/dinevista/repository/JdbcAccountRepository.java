package com.dinevista.repository;

import com.dinevista.model.UserAccountRecord;
import com.dinevista.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class JdbcAccountRepository implements AccountRepository {
    private final DatabaseConfig config;

    public JdbcAccountRepository(DatabaseConfig config) throws SQLException {
        this.config = config;
        String sql = "SELECT 1 FROM user_account ua JOIN role r ON r.role_id = ua.role_id LIMIT 1";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeQuery();
        }
    }

    @Override
    public Optional<UserAccountRecord> findByEmailAndRole(String email, String role) {
        String sql = "SELECT ua.user_id, r.role_name, ua.first_name, ua.last_name, ua.email, "
                + "ua.password_hash, ua.account_status FROM user_account ua "
                + "JOIN role r ON r.role_id = ua.role_id "
                + "WHERE LOWER(ua.email) = LOWER(?) AND r.role_name = ? LIMIT 1";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, role);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw failure("Unable to read the account.", ex);
        }
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM user_account WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw failure("Unable to check the email address.", ex);
        }
    }

    @Override
    public UserAccountRecord create(String role, String firstName, String lastName,
                                    String email, String phone, String passwordHash) {
        String userSql = "INSERT INTO user_account "
                + "(role_id, first_name, last_name, email, phone, password_hash, account_status) "
                + "SELECT role_id, ?, ?, ?, ?, ?, 'ACTIVE' FROM role WHERE role_name = ?";
        try (Connection connection = config.openConnection()) {
            connection.setAutoCommit(false);
            try {
                long userId;
                try (PreparedStatement statement = connection.prepareStatement(
                        userSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, firstName);
                    statement.setString(2, lastName);
                    statement.setString(3, email);
                    statement.setString(4, phone);
                    statement.setString(5, passwordHash);
                    statement.setString(6, role);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("The required database role is missing: " + role);
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No user identifier was generated.");
                        userId = keys.getLong(1);
                    }
                }

                if ("MANAGER".equals(role)) {
                    insertManagerProfile(connection, userId);
                } else {
                    insertCustomerProfile(connection, userId);
                }
                connection.commit();
                return new UserAccountRecord(userId, role, firstName, lastName,
                        email, passwordHash, "ACTIVE");
            } catch (SQLException ex) {
                connection.rollback();
                if ("23000".equals(ex.getSQLState())) throw new DuplicateEmailException();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (DuplicateEmailException ex) {
            throw ex;
        } catch (SQLException ex) {
            throw failure("Unable to create the account.", ex);
        }
    }

    private void insertCustomerProfile(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO customer_profile (user_id) VALUES (?)")) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    private void insertManagerProfile(Connection connection, long userId) throws SQLException {
        String sql = "INSERT INTO staff_profile "
                + "(user_id, employee_code, job_title, department, hire_date) "
                + "VALUES (?, ?, 'Manager', 'Operations', CURRENT_DATE)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, "MGR-" + userId);
            statement.executeUpdate();
        }
    }

    private UserAccountRecord map(ResultSet rows) throws SQLException {
        return new UserAccountRecord(
                rows.getLong("user_id"), rows.getString("role_name"),
                rows.getString("first_name"), rows.getString("last_name"),
                rows.getString("email"), rows.getString("password_hash"),
                rows.getString("account_status"));
    }

    private static IllegalStateException failure(String message, SQLException ex) {
        return new IllegalStateException(message + " " + ex.getMessage(), ex);
    }
}
