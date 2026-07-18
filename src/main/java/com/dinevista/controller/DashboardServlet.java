package com.dinevista.controller;

import com.dinevista.service.ReservationOrderService;
import com.dinevista.util.ReservationOrderContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private ReservationOrderService service;

    @Override
    public void init() {
        service = ReservationOrderContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object role = request.getSession().getAttribute("demoRole");
        if ("manager".equals(role)) {
            request.setAttribute("managerReservations", service.allReservations("", ""));
            request.setAttribute("managerOrders", service.allOrders("", ""));
            request.setAttribute("activeReservationCount", service.countActiveReservations());
            request.setAttribute("activeOrderCount", service.countActiveOrders());
            request.getRequestDispatcher("/WEB-INF/views/manager-dashboard.jsp").forward(request, response);
        } else {
            String customerKey = ReservationOrderContext.customerKey(request);
            request.setAttribute("customerReservations", service.reservationsForCustomer(customerKey));
            request.setAttribute("customerOrders", service.ordersForCustomer(customerKey));
            request.getRequestDispatcher("/WEB-INF/views/customer-dashboard.jsp").forward(request, response);
        }
    }
}
