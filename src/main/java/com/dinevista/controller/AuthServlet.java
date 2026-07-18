package com.dinevista.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class AuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = value(request.getParameter("email"));
        String password = value(request.getParameter("password"));
        String role = value(request.getParameter("role"));

        if (email.isEmpty() || password.isEmpty()) {
            request.setAttribute("loginError", "Enter your email and password.");
            request.setAttribute("formEmail", email);
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        boolean manager = "manager".equalsIgnoreCase(role);
        request.getSession().setAttribute("demoRole", manager ? "manager" : "customer");
        request.getSession().setAttribute("displayName", manager ? "Operations Manager" : "DineVista Guest");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
