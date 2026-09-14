package com.dinevista;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        try {
            com.dinevista.util.DatabaseConfig config = com.dinevista.util.DatabaseConfig.load();
            System.out.println("Connecting using mode: " + (config.isMysqlEnabled() ? "mysql" : "memory"));
            Connection conn = config.openConnection();
            System.out.println("✅ Database connected successfully!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            if (rs.next()) {
                System.out.println("✅ Test query successful!");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Connection failed: " + e.getMessage());
        }
    }
}