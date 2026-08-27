package com.dinevista.controller;

import com.dinevista.model.CartLineRecord;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(urlPatterns = {"/orders", "/orders/*"})
public class FoodOrderServlet extends HttpServlet {
    private ReservationOrderService service;

    @Override
    public void init() {
        service = ReservationOrderContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireSignIn(request, response)) return;
        FlashUtil.expose(request);
        String path = path(request);
        if ("/view".equals(path)) {
            showDetails(request, response);
            return;
        }
        if (path.isEmpty()) {
            renderMain(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (requireSignIn(request, response)) return;
        switch (path(request)) {
            case "/cart/add":
                addToCart(request, response);
                break;
            case "/cart/update":
                updateCart(request, response);
                break;
            case "/cart/remove":
                removeFromCart(request, response);
                break;
            case "/checkout":
                checkout(request, response);
                break;
            case "/items/add":
                addPendingItem(request, response);
                break;
            case "/items/update":
                updatePendingItem(request, response);
                break;
            case "/items/remove":
                removePendingItem(request, response);
                break;
            case "/cancel":
                cancel(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /** Returns true (and sends redirect) when the user is not signed in. */
    private boolean requireSignIn(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!ReservationOrderContext.isSignedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return true;
        }
        return false;
    }

    private void renderMain(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String customerKey = ReservationOrderContext.customerKey(request);
        Map<Long, Integer> cart = ReservationOrderContext.cart(request);
        List<CartLineRecord> cartLines = service.cartLines(cart);
        BigDecimal subtotal = service.cartSubtotal(cart);

        request.setAttribute("menuItems", service.menuItems());
        request.setAttribute("cartLines", cartLines);
        request.setAttribute("cartSubtotal", subtotal);
        request.setAttribute("cartServiceCharge", subtotal.multiply(new BigDecimal("0.05")));
        request.setAttribute("customerOrders", service.ordersForCustomer(customerKey));
        request.setAttribute("preOrderReservations",
                service.eligiblePreOrderReservations(customerKey));
        request.setAttribute("dineInReservations",
                service.eligibleDineInReservations(customerKey));
        request.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(request, response);
    }

    private void addToCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long itemId = RequestUtil.longValue(request, "menuItemId", -1);
        int quantity = RequestUtil.integer(request, "quantity", -1);
        OperationResult<Integer> result = service.addCartItem(
                ReservationOrderContext.cart(request), itemId, quantity);
        if (result.isSuccess()) {
            FlashUtil.success(request, "Menu item added to your order.");
        } else {
            FlashUtil.errors(request, result.getErrors());
        }
        response.sendRedirect(request.getContextPath() + "/orders#food-menu");
    }

    private void updateCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long itemId = RequestUtil.longValue(request, "menuItemId", -1);
        int quantity = RequestUtil.integer(request, "quantity", -1);
        OperationResult<Integer> result = service.updateCartItem(
                ReservationOrderContext.cart(request), itemId, quantity);
        if (result.isSuccess()) {
            FlashUtil.success(request, quantity <= 0
                    ? "Item removed from your order."
                    : "Cart quantity updated.");
        } else {
            FlashUtil.errors(request, result.getErrors());
        }
        response.sendRedirect(request.getContextPath() + "/orders#order-cart");
    }

    private void removeFromCart(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long itemId = RequestUtil.longValue(request, "menuItemId", -1);
        OperationResult<Integer> result = service.updateCartItem(
                ReservationOrderContext.cart(request), itemId, 0);
        if (result.isSuccess()) {
            FlashUtil.success(request, "Item removed from your order.");
        } else {
            FlashUtil.errors(request, result.getErrors());
        }
        response.sendRedirect(request.getContextPath() + "/orders#order-cart");
    }

    private void checkout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String customerKey = ReservationOrderContext.customerKey(request);
        String orderType = RequestUtil.clean(request, "orderType");
        String linkedReservation = postedReservationReference(request, orderType);
        OperationResult<FoodOrderRecord> result = service.createOrder(
                customerKey,
                RequestUtil.clean(request, "customerName"),
                RequestUtil.clean(request, "email"),
                RequestUtil.clean(request, "phone"),
                orderType,
                linkedReservation,
                RequestUtil.dateTime(request, "requestedFor"),
                RequestUtil.clean(request, "orderNotes"),
                ReservationOrderContext.cart(request));

        if (!result.isSuccess()) {
            request.setAttribute("errors", result.getErrors());
            request.setAttribute("checkoutName", RequestUtil.clean(request, "customerName"));
            request.setAttribute("checkoutEmail", RequestUtil.clean(request, "email"));
            request.setAttribute("checkoutPhone", RequestUtil.clean(request, "phone"));
            request.setAttribute("checkoutOrderType", orderType);
            if ("DINE_IN".equals(orderType)) {
                request.setAttribute("checkoutDineInReservation", linkedReservation);
            } else if ("PRE_ORDER".equals(orderType)) {
                request.setAttribute("checkoutPreOrderReservation", linkedReservation);
            }
            request.setAttribute("checkoutRequestedFor", RequestUtil.clean(request, "requestedFor"));
            request.setAttribute("checkoutNotes", RequestUtil.clean(request, "orderNotes"));
            renderMain(request, response);
            return;
        }

        FlashUtil.success(request, "Order " + result.getValue().getReference()
                + " was created successfully and sent to restaurant staff.");
        response.sendRedirect(request.getContextPath() + "/orders/view?reference="
                + result.getValue().getReference());
    }

    private String postedReservationReference(HttpServletRequest request, String orderType) {
        String fieldName = "DINE_IN".equals(orderType)
                ? "dineInReservationReference"
                : ("PRE_ORDER".equals(orderType)
                ? "preOrderReservationReference" : "");
        String specificReference = fieldName.isEmpty()
                ? "" : RequestUtil.clean(request, fieldName);
        return specificReference.isEmpty()
                ? RequestUtil.clean(request, "reservationReference")
                : specificReference;
    }

    private void cancel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String reference = RequestUtil.clean(request, "reference");
        OperationResult<FoodOrderRecord> result = service.cancelOrder(
                ReservationOrderContext.customerKey(request), reference,
                RequestUtil.clean(request, "reason"));

        if (result.isSuccess()) {
            FlashUtil.success(request, "Order " + reference + " was cancelled.");
        } else {
            FlashUtil.errors(request, result.getErrors());
        }
        response.sendRedirect(request.getContextPath() + "/orders/view?reference=" + reference);
    }

    private void addPendingItem(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String reference = RequestUtil.clean(request, "reference");
        OperationResult<FoodOrderRecord> result = service.addPendingOrderItem(
                ReservationOrderContext.customerKey(request), reference,
                RequestUtil.longValue(request, "menuItemId", -1),
                RequestUtil.integer(request, "quantity", -1));
        exposeItemResult(request, result, "Item added to the pending order.");
        response.sendRedirect(request.getContextPath() + "/orders/view?reference=" + reference
                + "&updated=items");
    }

    private void updatePendingItem(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String reference = RequestUtil.clean(request, "reference");
        OperationResult<FoodOrderRecord> result = service.updatePendingOrderItem(
                ReservationOrderContext.customerKey(request), reference,
                RequestUtil.longValue(request, "menuItemId", -1),
                RequestUtil.integer(request, "quantity", -1));
        exposeItemResult(request, result, "Pending order quantity updated.");
        response.sendRedirect(request.getContextPath() + "/orders/view?reference=" + reference
                + "&updated=items");
    }

    private void removePendingItem(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String reference = RequestUtil.clean(request, "reference");
        OperationResult<FoodOrderRecord> result = service.removePendingOrderItem(
                ReservationOrderContext.customerKey(request), reference,
                RequestUtil.longValue(request, "menuItemId", -1));
        exposeItemResult(request, result, "Item removed from the pending order.");
        response.sendRedirect(request.getContextPath() + "/orders/view?reference=" + reference
                + "&updated=items");
    }

    private void exposeItemResult(HttpServletRequest request,
                                  OperationResult<FoodOrderRecord> result,
                                  String successMessage) {
        if (result.isSuccess()) FlashUtil.success(request, successMessage);
        else FlashUtil.errors(request, result.getErrors());
    }

    private void showDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String reference = RequestUtil.clean(request, "reference");
        Optional<FoodOrderRecord> record = service.order(reference);
        boolean manager = ReservationOrderContext.isManager(request);
        String customerKey = ReservationOrderContext.customerKey(request);

        if (record.isEmpty()
                || (!manager && !record.get().getCustomerKey().equals(customerKey))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("foodOrder", record.get());
        request.setAttribute("managerView", manager);
        request.setAttribute("menuItems", service.menuItems());
        request.getRequestDispatcher("/WEB-INF/views/order-detail.jsp").forward(request, response);
    }

    private String path(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || "/".equals(path) ? "" : path;
    }
}
