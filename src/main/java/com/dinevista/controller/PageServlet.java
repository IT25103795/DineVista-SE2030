package com.dinevista.controller;

import com.dinevista.service.ReservationOrderService;
import com.dinevista.util.ReservationOrderContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/menu", "/events", "/about"})
public class PageServlet extends HttpServlet {
    private final Map<String, String> views = new HashMap<>();
    private ReservationOrderService reservationOrderService;

    @Override
    public void init() {
        views.put("/menu", "/WEB-INF/views/menu.jsp");
        views.put("/events", "/WEB-INF/views/events.jsp");
        views.put("/about", "/WEB-INF/views/about.jsp");
        reservationOrderService = ReservationOrderContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        String view = views.get(servletPath);
        if (view == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if ("/menu".equals(servletPath)) {
            request.setAttribute("menuItems", reservationOrderService.menuItems());
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(view);
        dispatcher.forward(request, response);
    }
}
