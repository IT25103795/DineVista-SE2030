package com.dinevista.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private final String storageMode;
    private final String url;
    private final String username;
    private final String password;

    private DatabaseConfig(String storageMode, String url, String username, String password) {
        this.storageMode = storageMode;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConfig load() {
        Properties properties = new Properties();
        try (InputStream stream = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (stream != null) properties.load(stream);
        } catch (IOException ignored) {
            // Environment variables and defaults are used when the file cannot be read.
        }

        String mode = value("DINEVISTA_STORAGE_MODE", properties.getProperty("storage.mode", "memory"));
        String url = value("DINEVISTA_DB_URL",
                properties.getProperty("db.url", "jdbc:mysql://localhost:3306/dinevista?useSSL=false&serverTimezone=Asia/Colombo&characterEncoding=UTF-8"));
        String user = value("DINEVISTA_DB_USERNAME", properties.getProperty("db.username", "root"));
        String password = value("DINEVISTA_DB_PASSWORD", properties.getProperty("db.password", ""));
        return new DatabaseConfig(mode, url, user, password);
    }

    public boolean isMysqlEnabled() {
        return "mysql".equalsIgnoreCase(storageMode);
    }

    public Connection openConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL Connector/J is not available.", ex);
        }
        return DriverManager.getConnection(url, username, password);
    }

    private static String value(String environmentName, String fallback) {
        String environment = System.getenv(environmentName);
        return environment == null || environment.isBlank() ? fallback : environment.trim();
    }
}
