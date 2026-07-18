package com.dinevista.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/menu", "/orders", "/events", "/register", "/about"})
public class PageServlet extends HttpServlet {
    private final Map<String, String> views = new HashMap<>();

    @Override
    public void init() {
        views.put("/menu", "/WEB-INF/views/menu.jsp");
        views.put("/orders", "/WEB-INF/views/orders.jsp");
        views.put("/events", "/WEB-INF/views/events.jsp");
        views.put("/register", "/WEB-INF/views/register.jsp");
        views.put("/about", "/WEB-INF/views/about.jsp");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view = views.get(request.getServletPath());
        if (view == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher(view);
        dispatcher.forward(request, response);
    }
}
