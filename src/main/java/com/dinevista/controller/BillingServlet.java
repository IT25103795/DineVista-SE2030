package com.dinevista.controller;

import com.dinevista.model.FoodOrderRecord;
import com.dinevista.model.EventBookingRecord;
import com.dinevista.model.EventQuoteRecord;
import com.dinevista.model.InvoiceRecord;
import com.dinevista.model.PromotionRecord;
import com.dinevista.service.BillingService;
import com.dinevista.service.EventBookingService;
import com.dinevista.service.OperationResult;
import com.dinevista.service.ReservationOrderService;
import com.dinevista.util.BillingContext;
import com.dinevista.util.EventBookingContext;
import com.dinevista.util.FlashUtil;
import com.dinevista.util.RequestUtil;
import com.dinevista.util.ReservationOrderContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Owner: Nawarathna N. M. I. N. (IT25103797) — Billing Management with
 * Promotions and Discounts.
 * Staff-only CRUD for invoices, recorded/verified payments, voids/refunds and
 * the promotion catalogue (UC-BIL-01, FR15/FR16/FR22/FR23).
 * Routes:
 *   GET  /staff/billing                        invoice list + search/status filter + finance summary
 *   GET  /staff/billing/new                     new-invoice form (?orderReference= pre-fills a food order)
 *   GET  /staff/billing/view                    invoice detail, payment history, pay/void/cancel forms (?id=)
 *   POST /staff/billing/generate                create an invoice from confirmed charges
 *   POST /staff/billing/pay                     record + verify a simulated payment
 *   POST /staff/billing/void                    void/refund a verified payment
 *   POST /staff/billing/cancel                  cancel an unpaid invoice
 *   GET  /staff/billing/promotions              promotion catalogue
 *   GET  /staff/billing/promotions/new          blank create form
 *   GET  /staff/billing/promotions/edit         edit form (?id=)
 *   POST /staff/billing/promotions/save         create or update a promotion
 *   POST /staff/billing/promotions/delete       delete (blocked once usage exists)
 */
@WebServlet(urlPatterns = {"/staff/billing", "/staff/billing/*"})
public class BillingServlet extends HttpServlet {
    private BillingService billingService;
    private EventBookingService eventBookingService;

    @Override
    public void init() {
        billingService = BillingContext.service(getServletContext());
        eventBookingService = EventBookingContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        FlashUtil.expose(request);
        String path = path(request);

        switch (path) {
            case "/new": {
                renderNew(request, response);
                return;
            }
            case "/view": {
                long id = RequestUtil.longValue(request, "id", 0);
                Optional<InvoiceRecord> invoice = billingService.invoice(id);
                if (invoice.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("invoice", invoice.get());
                request.setAttribute("payments", billingService.paymentsFor(id));
                request.getRequestDispatcher("/WEB-INF/views/staff-billing-detail.jsp").forward(request, response);
                return;
            }
            case "/promotions": {
                request.setAttribute("promotions", billingService.allPromotions());
                request.getRequestDispatcher("/WEB-INF/views/staff-billing-promotions.jsp").forward(request, response);
                return;
            }
            case "/promotions/new":
                request.getRequestDispatcher("/WEB-INF/views/staff-billing-promotion-form.jsp").forward(request, response);
                return;
            case "/promotions/edit": {
                Optional<PromotionRecord> promotion = billingService.promotion(RequestUtil.longValue(request, "id", 0));
                if (promotion.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("promotion", promotion.get());
                request.setAttribute("usageCount", billingService.usageCount(promotion.get().getId()));
                request.getRequestDispatcher("/WEB-INF/views/staff-billing-promotion-form.jsp").forward(request, response);
                return;
            }
            case "": {
                String status = RequestUtil.clean(request, "status");
                String search = RequestUtil.clean(request, "search");
                request.setAttribute("billingStatus", status);
                request.setAttribute("billingSearch", search);
                request.setAttribute("invoices", billingService.allInvoices(status, search));
                request.setAttribute("financeSummary", billingService.financeSummary());
                request.getRequestDispatcher("/WEB-INF/views/staff-billing.jsp").forward(request, response);
                return;
            }
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        String path = path(request);
        String staffName = ReservationOrderContext.displayName(request);

        if ("/generate".equals(path)) {
            String sourceType = RequestUtil.clean(request, "sourceType");
            OperationResult<InvoiceRecord> result;
            if ("EVENT_BOOKING".equals(sourceType)) {
                String reference = RequestUtil.clean(request, "sourceReference");
                EventBookingRecord booking = eventBookingService.booking(reference).orElse(null);
                EventQuoteRecord acceptedQuote = booking == null ? null : eventBookingService.quotes(reference).stream()
                        .findFirst().filter(EventQuoteRecord::isAccepted).orElse(null);
                result = billingService.generateEventInvoice(booking, acceptedQuote,
                        request.getParameterValues("description"), request.getParameterValues("quantity"),
                        request.getParameterValues("unitPrice"), RequestUtil.clean(request, "promotionCode"), staffName);
            } else {
                result = billingService.generateInvoice(sourceType,
                        RequestUtil.clean(request, "sourceReference"), RequestUtil.clean(request, "customerKey"),
                        RequestUtil.clean(request, "customerName"), RequestUtil.clean(request, "customerEmail"),
                        request.getParameterValues("description"), request.getParameterValues("quantity"),
                        request.getParameterValues("unitPrice"), RequestUtil.clean(request, "promotionCode"), staffName);
            }
            if (result.isSuccess()) {
                FlashUtil.success(request, "Invoice " + result.getValue().getInvoiceNumber() + " was generated.");
                response.sendRedirect(request.getContextPath() + "/staff/billing/view?id=" + result.getValue().getId());
            } else {
                request.setAttribute("errors", result.getErrors());
                renderNew(request, response);
            }
            return;
        }

        if ("/pay".equals(path)) {
            long invoiceId = RequestUtil.longValue(request, "invoiceId", 0);
            OperationResult<?> result = billingService.recordPayment(
                    invoiceId,
                    RequestUtil.clean(request, "method"),
                    RequestUtil.clean(request, "amount"),
                    RequestUtil.clean(request, "reference"),
                    RequestUtil.clean(request, "note"),
                    staffName);
            if (result.isSuccess()) {
                FlashUtil.success(request, "Payment recorded and verified.");
            } else {
                FlashUtil.errors(request, result.getErrors());
            }
            response.sendRedirect(request.getContextPath() + "/staff/billing/view?id=" + invoiceId);
            return;
        }

        if ("/void".equals(path)) {
            long paymentId = RequestUtil.longValue(request, "paymentId", 0);
            long invoiceId = RequestUtil.longValue(request, "invoiceId", 0);
            OperationResult<Void> result = billingService.voidPayment(
                    paymentId, RequestUtil.clean(request, "reason"), staffName);
            if (result.isSuccess()) {
                FlashUtil.success(request, "Payment was voided and reversed on the invoice.");
            } else {
                FlashUtil.errors(request, result.getErrors());
            }
            response.sendRedirect(request.getContextPath() + "/staff/billing/view?id=" + invoiceId);
            return;
        }

        if ("/cancel".equals(path)) {
            long invoiceId = RequestUtil.longValue(request, "invoiceId", 0);
            OperationResult<Void> result = billingService.cancelInvoice(
                    invoiceId, RequestUtil.clean(request, "reason"), staffName);
            if (result.isSuccess()) {
                FlashUtil.success(request, "Invoice was cancelled.");
            } else {
                FlashUtil.errors(request, result.getErrors());
            }
            response.sendRedirect(request.getContextPath() + "/staff/billing/view?id=" + invoiceId);
            return;
        }

        if ("/delete".equals(path)) {
            long invoiceId = RequestUtil.longValue(request, "invoiceId", 0);
            OperationResult<Void> result = billingService.deleteInvoice(
                    invoiceId, RequestUtil.clean(request, "reason"), staffName);
            if (result.isSuccess()) {
                FlashUtil.success(request, "Invoice was permanently deleted.");
                response.sendRedirect(request.getContextPath() + "/staff/billing");
            } else {
                FlashUtil.errors(request, result.getErrors());
                response.sendRedirect(request.getContextPath() + "/staff/billing/view?id=" + invoiceId);
            }
            return;
        }

        if ("/promotions/save".equals(path)) {
            long id = RequestUtil.longValue(request, "id", 0);
            OperationResult<PromotionRecord> result = billingService.savePromotion(
                    id,
                    RequestUtil.clean(request, "code"),
                    RequestUtil.clean(request, "name"),
                    RequestUtil.clean(request, "discountType"),
                    RequestUtil.clean(request, "discountValue"),
                    RequestUtil.clean(request, "minimumSpend"),
                    RequestUtil.clean(request, "startDate"),
                    RequestUtil.clean(request, "endDate"),
                    RequestUtil.clean(request, "usageLimit"),
                    "1".equals(RequestUtil.clean(request, "active")));
            if (result.isSuccess()) {
                FlashUtil.success(request, id <= 0
                        ? "Promotion \"" + result.getValue().getCode() + "\" was created."
                        : "Promotion \"" + result.getValue().getCode() + "\" was updated.");
                response.sendRedirect(request.getContextPath() + "/staff/billing/promotions");
            } else {
                FlashUtil.errors(request, result.getErrors());
                response.sendRedirect(request.getContextPath()
                        + (id <= 0 ? "/staff/billing/promotions/new" : "/staff/billing/promotions/edit?id=" + id));
            }
            return;
        }

        if ("/promotions/delete".equals(path)) {
            long id = RequestUtil.longValue(request, "id", 0);
            OperationResult<Void> result = billingService.deletePromotion(id);
            if (result.isSuccess()) {
                FlashUtil.success(request, "Promotion was deleted.");
            } else {
                FlashUtil.errors(request, result.getErrors());
            }
            response.sendRedirect(request.getContextPath() + "/staff/billing/promotions");
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private boolean requireManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (ReservationOrderContext.isManager(request)) return true;
        response.sendRedirect(request.getContextPath() + "/manager/login?required=manager");
        return false;
    }

    private void renderNew(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String orderReference = RequestUtil.clean(request, "orderReference");
        if (!orderReference.isEmpty()) {
            ReservationOrderService orderService = ReservationOrderContext.service(getServletContext());
            Optional<FoodOrderRecord> order = orderService.order(orderReference);
            if (order.isPresent()) {
                request.setAttribute("prefillOrder", order.get());
                request.setAttribute("existingInvoice",
                        billingService.existingInvoiceForSource("FOOD_ORDER", orderReference).orElse(null));
            }
        }
        String eventReference = RequestUtil.clean(request, "sourceReference");
        EventBookingRecord selectedEvent = "EVENT_BOOKING".equals(RequestUtil.clean(request, "sourceType"))
                ? eventBookingService.booking(eventReference).filter(this::readyToInvoice).orElse(null) : null;
        if (selectedEvent != null) {
            request.setAttribute("selectedEventBooking", selectedEvent);
            request.setAttribute("existingInvoice", billingService
                    .existingInvoiceForSource("EVENT_BOOKING", selectedEvent.getReference()).orElse(null));
        }
        List<EventBookingRecord> completed = new ArrayList<>();
        Set<String> invoiced = new HashSet<>();
        for (EventBookingRecord booking : eventBookingService.allBookings("")) {
            if (!readyToInvoice(booking)) continue;
            completed.add(booking);
            if (billingService.existingInvoiceForSource("EVENT_BOOKING", booking.getReference()).isPresent()) {
                invoiced.add(booking.getReference());
            }
        }
        request.setAttribute("completedEventBookings", completed);
        request.setAttribute("invoicedEventReferences", invoiced);
        List<PromotionRecord> promotions = billingService.allPromotions();
        Map<Long, Integer> usageCounts = new HashMap<>();
        for (PromotionRecord promotion : promotions) {
            usageCounts.put(promotion.getId(), billingService.usageCount(promotion.getId()));
        }
        request.setAttribute("promotions", promotions);
        request.setAttribute("promotionUsageCounts", usageCounts);
        request.getRequestDispatcher("/WEB-INF/views/staff-billing-form.jsp").forward(request, response);
    }

    private boolean readyToInvoice(EventBookingRecord booking) {
        if (!("QUOTED".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())
                || "COMPLETED".equals(booking.getStatus()))) return false;
        return eventBookingService.quotes(booking.getReference()).stream()
                .findFirst().filter(EventQuoteRecord::isAccepted).isPresent();
    }

    private String path(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || "/".equals(path) ? "" : path;
    }
}
