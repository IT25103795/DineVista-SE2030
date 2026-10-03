package com.dinevista.repository;

import com.dinevista.model.MenuCategoryRecord;
import com.dinevista.model.MenuItemAdminRecord;
import com.dinevista.model.MenuRecipeIngredientRecord;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for the Menu Management module (menu categories and menu
 * items). Reads and writes the same {@code menu_category} and {@code menu_item}
 * tables used by the Reservation/Order module's read-only menu lookup, so items
 * and categories created or edited here appear immediately on the public menu.
 */
public interface MenuRepository {
    // Categories
    List<MenuCategoryRecord> findAllCategories();
    List<MenuCategoryRecord> findActiveCategories();
    Optional<MenuCategoryRecord> findCategory(long id);
    Optional<MenuCategoryRecord> findCategoryByName(String name);
    MenuCategoryRecord saveCategory(MenuCategoryRecord category);
    boolean deleteCategory(long id);
    boolean categoryHasItems(long categoryId);
    long nextCategoryId();

    // Items
    List<MenuItemAdminRecord> findAllItems();
    Optional<MenuItemAdminRecord> findItem(long id);
    Optional<MenuItemAdminRecord> findItemByNameInCategory(String name, long categoryId);
    MenuItemAdminRecord saveItem(MenuItemAdminRecord item);
    boolean deleteItem(long id);
    boolean itemReferencedByOrders(long id);
    long nextItemId();

    /** Recipe management is available in the MySQL-backed application. */
    default List<MenuRecipeIngredientRecord> findRecipe(long menuItemId) { return List.of(); }
    default void upsertRecipeIngredient(long menuItemId, long ingredientId, BigDecimal amount) {
        throw new UnsupportedOperationException("Recipe management requires MySQL storage.");
    }
    default boolean removeRecipeIngredient(long menuItemId, long ingredientId) {
        throw new UnsupportedOperationException("Recipe management requires MySQL storage.");
    }
}
