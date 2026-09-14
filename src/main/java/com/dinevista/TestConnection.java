package com.dinevista;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/dinevistadb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, "root", "NooB4002$$$tiilS");
            System.out.println("✅ Database connected successfully!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            if(rs.next()) {
                System.out.println("✅ Test query successful!");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Connection failed: " + e.getMessage());
        }
    }
}