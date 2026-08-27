package com.dinevista.controller;

import com.dinevista.model.FoodOrderRecord;
import com.dinevista.service.OperationResult;
import com.dinevista.service.ReservationOrderService;
import com.dinevista.util.FlashUtil;
import com.dinevista.util.RequestUtil;
import com.dinevista.util.ReservationOrderContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet(urlPatterns = {"/staff/orders", "/staff/orders/*"})
public class StaffOrderServlet extends HttpServlet {
    private ReservationOrderService service;

    @Override
    public void init() {
        service = ReservationOrderContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        FlashUtil.expose(request);

        if ("/view".equals(path(request))) {
            String reference = RequestUtil.clean(request, "reference");
            Optional<FoodOrderRecord> order = service.order(reference);
            if (order.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("foodOrder", order.get());
            request.getRequestDispatcher("/WEB-INF/views/staff-order-detail.jsp")
                    .forward(request, response);
            return;
        }

        if (path(request).isEmpty()) {
            String status = RequestUtil.clean(request, "status");
            String type = RequestUtil.clean(request, "type");
            request.setAttribute("orderFilterStatus", status);
            request.setAttribute("orderFilterType", type);
            request.setAttribute("staffOrders", service.allOrders(status, type));
            request.getRequestDispatcher("/WEB-INF/views/staff-orders.jsp")
                    .forward(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!requireManager(request, response)) return;
        if (!"/update".equals(path(request))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String reference = RequestUtil.clean(request, "reference");
        OperationResult<FoodOrderRecord> result = service.staffUpdateOrder(
                reference,
                RequestUtil.clean(request, "status"),
                RequestUtil.clean(request, "note"),
                ReservationOrderContext.displayName(request));

        if (result.isSuccess()) {
            FlashUtil.success(request, "Order " + reference + " was updated.");
        } else {
            FlashUtil.errors(request, result.getErrors());
        }
        response.sendRedirect(request.getContextPath()
                + "/staff/orders/view?reference=" + reference);
    }

    private boolean requireManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (ReservationOrderContext.isManager(request)) return true;
        response.sendRedirect(request.getContextPath() + "/manager/login?required=manager");
        return false;
    }

    private String path(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || "/".equals(path) ? "" : path;
    }
}
