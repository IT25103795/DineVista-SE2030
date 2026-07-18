package com.dinevista.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class AuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("manager".equalsIgnoreCase(request.getParameter("required"))) {
            request.setAttribute("loginNotice", "Sign in as Manager to access restaurant operations.");
        }
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
        String emailName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String displayName = manager ? "Operations Manager" : friendlyName(emailName);

        request.getSession().invalidate();
        HttpSession session = request.getSession(true);
        session.setAttribute("demoRole", manager ? "manager" : "customer");
        session.setAttribute("demoEmail", email.toLowerCase());
        session.setAttribute("displayName", displayName);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private String friendlyName(String value) {
        String clean = value.replace('.', ' ').replace('_', ' ').replace('-', ' ').trim();
        if (clean.isEmpty()) return "DineVista Guest";
        StringBuilder result = new StringBuilder();
        for (String word : clean.split("\\s+")) {
            if (word.isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) result.append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
