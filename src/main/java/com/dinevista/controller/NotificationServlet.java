package com.dinevista.controller;

import com.dinevista.service.ReservationOrderService;
import com.dinevista.util.ReservationOrderContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(urlPatterns = {"/notifications", "/notifications/*"})
public class NotificationServlet extends HttpServlet {
    private ReservationOrderService service;

    @Override
    public void init() {
        service = ReservationOrderContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ReservationOrderContext.isSignedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!"/open".equals(request.getPathInfo())) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        long notificationId = parseLong(request.getParameter("id"));
        String recipientKey = ReservationOrderContext.notificationRecipientKey(request);
        Optional<String> actionPath = service.openNotification(notificationId, recipientKey);
        response.sendRedirect(request.getContextPath() + actionPath.orElse("/dashboard"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!ReservationOrderContext.isSignedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("/read-all".equals(request.getPathInfo())) {
            service.markAllNotificationsRead(
                    ReservationOrderContext.notificationRecipientKey(request));
        } else if ("/clear-all".equals(request.getPathInfo())) {
            service.clearNotifications(
                    ReservationOrderContext.notificationRecipientKey(request));
        }
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value == null ? "" : value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
