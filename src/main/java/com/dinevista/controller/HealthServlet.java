package com.dinevista.controller;

import com.dinevista.util.DatabaseConfig;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/health")
public class HealthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        DatabaseConfig config = DatabaseConfig.load();
        if (!config.isMysqlEnabled()) {
            response.getWriter().write("{\"status\":\"ok\",\"application\":\"DineVista\","
                    + "\"persistence\":\"memory\"}");
            return;
        }

        try (Connection connection = config.openConnection()) {
            if (!connection.isValid(2)) throw new SQLException("Connection validation failed.");
            response.getWriter().write("{\"status\":\"ok\",\"application\":\"DineVista\","
                    + "\"persistence\":\"mysql\",\"database\":\"connected\"}");
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"status\":\"degraded\",\"application\":\"DineVista\","
                    + "\"persistence\":\"mysql\",\"database\":\"unavailable\"}");
        }
    }
}
