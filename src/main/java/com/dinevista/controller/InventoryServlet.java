package com.dinevista.controller;

import com.dinevista.model.IngredientRecord;
import com.dinevista.service.InventoryService;
import com.dinevista.service.OperationResult;
import com.dinevista.util.FlashUtil;
import com.dinevista.util.InventoryContext;
import com.dinevista.util.RequestUtil;
import com.dinevista.util.ReservationOrderContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Owner: Hansaka A. K. (IT25103798) — Inventory Management.
 * Staff-only CRUD for ingredients plus an auditable stock-transaction ledger.
 * Routes:
 *   GET  /staff/inventory              list + search + low-stock filter
 *   GET  /staff/inventory/new          blank create form
 *   GET  /staff/inventory/edit         edit form (?id=)
 *   GET  /staff/inventory/view         detail + stock history (?id=)
 *   POST /staff/inventory/save         create or update an ingredient
 *   POST /staff/inventory/delete       delete (blocked once history exists)
 *   POST /staff/inventory/stock        record a stock transaction
 */
@WebServlet(urlPatterns = {"/staff/inventory", "/staff/inventory/*"})
public class InventoryServlet extends HttpServlet {
    private InventoryService service;

    @Override
    public void init() {
        service = InventoryContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        FlashUtil.expose(request);
        String path = path(request);

        switch (path) {
            case "/new":
                request.getRequestDispatcher("/WEB-INF/views/staff-inventory-form.jsp")
                        .forward(request, response);
                return;
            case "/edit": {
                Optional<IngredientRecord> ingredient = service.ingredient(
                        RequestUtil.longValue(request, "id", 0));
                if (ingredient.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("ingredient", ingredient.get());
                request.getRequestDispatcher("/WEB-INF/views/staff-inventory-form.jsp")
                        .forward(request, response);
                return;
            }
            case "/view": {
                long id = RequestUtil.longValue(request, "id", 0);
                Optional<IngredientRecord> ingredient = service.ingredient(id);
                if (ingredient.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("ingredient", ingredient.get());
                request.setAttribute("stockHistory", service.historyFor(id));
                request.getRequestDispatcher("/WEB-INF/views/staff-inventory-detail.jsp")
                        .forward(request, response);
                return;
            }
            case "": {
                String search = RequestUtil.clean(request, "search");
                boolean lowStockOnly = "1".equals(RequestUtil.clean(request, "lowStock"));
                request.setAttribute("inventorySearch", search);
                request.setAttribute("inventoryLowStockOnly", lowStockOnly);
                request.setAttribute("ingredients", service.allIngredients(search, lowStockOnly));
                request.setAttribute("lowStockCount", service.lowStockCount());
                request.getRequestDispatcher("/WEB-INF/views/staff-inventory.jsp")
                        .forward(request, response);
                return;
            }
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!requireManager(request, response)) return;
        String path = path(request);

        if ("/save".equals(path)) {
            long id = RequestUtil.longValue(request, "id", 0);
            OperationResult<IngredientRecord> result = service.saveIngredient(
                    id,
                    RequestUtil.clean(request, "name"),
                    RequestUtil.clean(request, "unit"),
                    RequestUtil.clean(request, "reorderLevel"),
                    RequestUtil.clean(request, "unitCost"),
                    RequestUtil.clean(request, "supplierName"));
            if (result.isSuccess()) {
                FlashUtil.success(request, id <= 0
                        ? "Ingredient \"" + result.getValue().getName() + "\" was added."
                        : "Ingredient \"" + result.getValue().getName() + "\" was updated.");
                response.sendRedirect(request.getContextPath()
                        + "/staff/inventory/view?id=" + result.getValue().getId());
            } else {
                FlashUtil.errors(request, result.getErrors());
                response.sendRedirect(request.getContextPath()
                        + (id <= 0 ? "/staff/inventory/new" : "/staff/inventory/edit?id=" + id));
            }
            return;
        }

        if ("/stock".equals(path)) {
            long ingredientId = RequestUtil.longValue(request, "ingredientId", 0);
            OperationResult<?> result = service.recordTransaction(
                    ingredientId,
                    RequestUtil.clean(request, "transactionType"),
                    RequestUtil.clean(request, "quantity"),
                    RequestUtil.clean(request, "note"),
                    ReservationOrderContext.displayName(request));
            if (result.isSuccess()) {
                FlashUtil.success(request, "Stock transaction recorded.");
            } else {
                FlashUtil.errors(request, result.getErrors());
            }
            response.sendRedirect(request.getContextPath()
                    + "/staff/inventory/view?id=" + ingredientId);
            return;
        }

        if ("/delete".equals(path)) {
            long id = RequestUtil.longValue(request, "id", 0);
            OperationResult<Void> result = service.deleteIngredient(id);
            if (result.isSuccess()) {
                FlashUtil.success(request, "Ingredient was deleted.");
                response.sendRedirect(request.getContextPath() + "/staff/inventory");
            } else {
                FlashUtil.errors(request, result.getErrors());
                response.sendRedirect(request.getContextPath() + "/staff/inventory/view?id=" + id);
            }
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
