package com.dinevista.controller;

import com.dinevista.model.MenuCategoryRecord;
import com.dinevista.model.MenuItemAdminRecord;
import com.dinevista.service.MenuService;
import com.dinevista.service.OperationResult;
import com.dinevista.util.FlashUtil;
import com.dinevista.util.MenuContext;
import com.dinevista.util.RequestUtil;
import com.dinevista.util.ReservationOrderContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Owner: M. A. I. Samarasinghe (IT25103794) — Menu Management.
 * Implements UC-MEN-02 (Maintain Menu Item) and Category lifecycle.
 *
 * Routes:
 *   GET  /staff/menu                     List menu items with search and category filters
 *   GET  /staff/menu/new                 Create form for a new dish
 *   GET  /staff/menu/edit?id=...         Edit form for an existing dish
 *   POST /staff/menu/save                Save (insert/update) a menu item
 *   POST /staff/menu/delete              Safe delete or archive an item
 *   POST /staff/menu/toggle-status       Quick 1-click availability toggle
 *   POST /staff/menu/category/save       Create or edit a category
 *   POST /staff/menu/category/delete     Delete an empty category
 */
@WebServlet(urlPatterns = {"/staff/menu", "/staff/menu/*"})
public class StaffMenuServlet extends HttpServlet {
    private MenuService service;

    @Override
    public void init() {
        service = MenuContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        FlashUtil.expose(request);
        String path = path(request);

        switch (path) {
            case "/new": {
                List<MenuCategoryRecord> categories = service.activeCategories();
                request.setAttribute("categories", categories);
                request.getRequestDispatcher("/WEB-INF/views/staff-menu-form.jsp")
                        .forward(request, response);
                return;
            }
            case "/edit": {
                long id = RequestUtil.longValue(request, "id", 0);
                Optional<MenuItemAdminRecord> item = service.item(id);
                if (item.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                List<MenuCategoryRecord> categories = service.allCategories();
                request.setAttribute("item", item.get());
                request.setAttribute("categories", categories);
                request.getRequestDispatcher("/WEB-INF/views/staff-menu-form.jsp")
                        .forward(request, response);
                return;
            }
            case "":
            case "/": {
                String search = RequestUtil.clean(request, "search");
                long categoryId = RequestUtil.longValue(request, "category", 0);
                String status = RequestUtil.clean(request, "status");

                List<MenuItemAdminRecord> items = service.allItems(search, categoryId, status);
                List<MenuCategoryRecord> categories = service.allCategories();

                request.setAttribute("items", items);
                request.setAttribute("categories", categories);
                request.setAttribute("menuSearch", search == null ? "" : search);
                request.setAttribute("selectedCategory", categoryId);
                request.setAttribute("selectedStatus", status == null ? "ALL" : status);

                request.setAttribute("totalItemsCount", service.totalItemsCount());
                request.setAttribute("availableCount", service.availableItemsCount());
                request.setAttribute("unavailableCount", service.soldOutOrUnavailableCount());
                request.setAttribute("categoriesCount", service.totalCategoriesCount());

                request.getRequestDispatcher("/WEB-INF/views/staff-menu.jsp")
                        .forward(request, response);
                return;
            }
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        String path = path(request);

        switch (path) {
            case "/save": {
                long id = RequestUtil.longValue(request, "id", 0);
                long categoryId = RequestUtil.longValue(request, "categoryId", 0);
                String name = RequestUtil.clean(request, "name");
                String description = RequestUtil.clean(request, "description");
                String price = RequestUtil.clean(request, "price");
                String imagePath = RequestUtil.clean(request, "imagePath");
                String preparationMinutes = RequestUtil.clean(request, "preparationMinutes");
                String dietaryType = RequestUtil.clean(request, "dietaryType");
                String spiceLevel = RequestUtil.clean(request, "spiceLevel");
                String availabilityStatus = RequestUtil.clean(request, "availabilityStatus");

                OperationResult<MenuItemAdminRecord> result = service.saveItem(
                        id, categoryId, name, description, price, imagePath,
                        preparationMinutes, dietaryType, spiceLevel, availabilityStatus
                );

                String returnTo = RequestUtil.clean(request, "returnTo");

                if (result.isSuccess()) {
                    String actionText = id > 0 ? "updated" : "added";
                    FlashUtil.success(request, "Menu item '" + result.getValue().getName() + "' was successfully " + actionText + ".");
                    response.sendRedirect(request.getContextPath() + (returnTo != null && !returnTo.isBlank() ? returnTo : "/staff/menu"));
                } else {
                    FlashUtil.errors(request, result.getErrors());
                    if (returnTo != null && !returnTo.isBlank()) {
                        response.sendRedirect(request.getContextPath() + returnTo);
                    } else if (id > 0) {
                        response.sendRedirect(request.getContextPath() + "/staff/menu/edit?id=" + id);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/staff/menu/new");
                    }
                }
                return;
            }
            case "/delete": {
                long id = RequestUtil.longValue(request, "id", 0);
                MenuService.DeleteOutcome outcome = service.deleteOrArchiveItem(id);
                if (outcome.isSuccess()) {
                    FlashUtil.success(request, outcome.getMessage());
                } else {
                    FlashUtil.error(request, outcome.getMessage());
                }
                response.sendRedirect(request.getContextPath() + "/staff/menu");
                return;
            }
            case "/toggle-status": {
                long id = RequestUtil.longValue(request, "id", 0);
                OperationResult<MenuItemAdminRecord> result = service.toggleAvailability(id);
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Availability updated for '" + result.getValue().getName()
                            + "' to " + result.getValue().getStatusLabel() + ".");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/menu");
                return;
            }
            case "/category/save": {
                long id = RequestUtil.longValue(request, "categoryId", 0);
                String name = RequestUtil.clean(request, "categoryName");
                String description = RequestUtil.clean(request, "categoryDescription");
                int displayOrder = (int) RequestUtil.longValue(request, "displayOrder", 0);
                boolean active = "1".equals(request.getParameter("active")) || "true".equalsIgnoreCase(request.getParameter("active"));

                OperationResult<MenuCategoryRecord> result = service.saveCategory(id, name, description, displayOrder, active);
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Menu category '" + result.getValue().getName() + "' saved successfully.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/menu");
                return;
            }
            case "/category/delete": {
                long id = RequestUtil.longValue(request, "categoryId", 0);
                OperationResult<Void> result = service.deleteCategory(id);
                if (result.isSuccess()) {
                    FlashUtil.success(request, "Menu category was deleted.");
                } else {
                    FlashUtil.errors(request, result.getErrors());
                }
                response.sendRedirect(request.getContextPath() + "/staff/menu");
                return;
            }
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
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
