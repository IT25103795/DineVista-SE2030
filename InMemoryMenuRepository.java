package com.dinevista.repository;

import com.dinevista.model.MenuCategoryRecord;
import com.dinevista.model.MenuItemAdminRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory {@link MenuRepository} used in the default demo storage
 * mode. Seeded with the same categories and dishes as
 * {@link InMemoryReservationOrderRepository} so the customer menu and the staff
 * Menu Management workspace show consistent data when MySQL is not configured.
 */
public class InMemoryMenuRepository implements MenuRepository {
    private final Map<Long, MenuCategoryRecord> categories = new ConcurrentHashMap<>();
    private final Map<Long, MenuItemAdminRecord> items = new ConcurrentHashMap<>();
    private final AtomicLong categorySequence = new AtomicLong(0);
    private final AtomicLong itemSequence = new AtomicLong(0);

    public InMemoryMenuRepository() {
        seed();
    }

    private void seed() {
        long signature = seedCategory("Signature", "Chef-crafted DineVista dishes", 1);
        long sriLankan = seedCategory("Sri Lankan", "Modern local favourites", 2);
        long seafood = seedCategory("Seafood", "Fresh fish and lagoon seafood", 3);
        long vegetarian = seedCategory("Vegetarian", "Plant-forward plates", 4);
        long desserts = seedCategory("Desserts", "Sweet finishes", 5);

        seedItem(signature, "Fire-Roasted Chicken", "Herb-marinated chicken with coconut pepper sauce.",
                "2450", "dish-signature.svg", 28, "REGULAR", "MEDIUM");
        seedItem(sriLankan, "Island Curry Collection", "A trio of slow-cooked curries with rice and sambols.",
                "3200", "dish-curry.svg", 30, "REGULAR", "HOT");
        seedItem(seafood, "Lagoon Grilled Fish", "Line-caught fish grilled with citrus and herbs.",
                "3600", "dish-seafood.svg", 25, "REGULAR", "MILD");
        seedItem(vegetarian, "Garden Harvest Bowl", "Seasonal vegetables, grains, and a tahini dressing.",
                "2100", "dish-signature.svg", 20, "VEGAN", "NONE");
        seedItem(desserts, "Ceylon Cocoa Slice", "Dark cocoa cake with a Ceylon cinnamon crumb.",
                "1450", "dish-dessert.svg", 15, "VEGETARIAN", "NONE");
        seedItem(signature, "DineVista Sharing Platter", "A signature spread built for the table.",
                "5200", "dish-signature.svg", 35, "REGULAR", "MEDIUM");
        seedItem(sriLankan, "Kottu Vista", "Chopped roti stir-fry with egg and a smoky curry base.",
                "2650", "dish-curry.svg", 22, "REGULAR", "MEDIUM");
        seedItem(desserts, "Tropical Pavlova", "Meringue, passionfruit, and fresh cream.",
                "1600", "dish-dessert.svg", 15, "VEGETARIAN", "NONE");
    }

    private long seedCategory(String name, String description, int order) {
        long id = nextCategoryId();
        categories.put(id, new MenuCategoryRecord(id, name, description, order, true));
        return id;
    }

    private void seedItem(long categoryId, String name, String description, String price,
                           String image, int prepMinutes, String dietaryType, String spiceLevel) {
        long id = nextItemId();
        String categoryName = categories.get(categoryId).getName();
        items.put(id, new MenuItemAdminRecord(id, categoryId, categoryName, name, description,
                new BigDecimal(price), image, prepMinutes, dietaryType, spiceLevel,
                "AVAILABLE", LocalDateTime.now(), LocalDateTime.now()));
    }

    // ---- Categories ----

    @Override
    public List<MenuCategoryRecord> findAllCategories() {
        List<MenuCategoryRecord> list = new ArrayList<>(categories.values());
        list.sort(Comparator.comparingInt(MenuCategoryRecord::getDisplayOrder)
                .thenComparing(MenuCategoryRecord::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    @Override
    public List<MenuCategoryRecord> findActiveCategories() {
        List<MenuCategoryRecord> list = new ArrayList<>();
        for (MenuCategoryRecord category : findAllCategories()) {
            if (category.isActive()) list.add(category);
        }
        return list;
    }

    @Override
    public Optional<MenuCategoryRecord> findCategory(long id) {
        return Optional.ofNullable(categories.get(id));
    }

    @Override
    public Optional<MenuCategoryRecord> findCategoryByName(String name) {
        return categories.values().stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public MenuCategoryRecord saveCategory(MenuCategoryRecord category) {
        categories.put(category.getId(), category);
        return category;
    }

    @Override
    public boolean deleteCategory(long id) {
        return categories.remove(id) != null;
    }

    @Override
    public boolean categoryHasItems(long categoryId) {
        return items.values().stream().anyMatch(item -> item.getCategoryId() == categoryId);
    }

    @Override
    public long nextCategoryId() {
        return categorySequence.incrementAndGet();
    }

    // ---- Items ----

    @Override
    public List<MenuItemAdminRecord> findAllItems() {
        List<MenuItemAdminRecord> list = new ArrayList<>(items.values());
        list.sort(Comparator.comparing(MenuItemAdminRecord::getCategoryName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(MenuItemAdminRecord::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    @Override
    public Optional<MenuItemAdminRecord> findItem(long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Optional<MenuItemAdminRecord> findItemByNameInCategory(String name, long categoryId) {
        return items.values().stream()
                .filter(item -> item.getCategoryId() == categoryId && item.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public MenuItemAdminRecord saveItem(MenuItemAdminRecord item) {
        Optional<MenuCategoryRecord> category = findCategory(item.getCategoryId());
        category.ifPresent(c -> item.setCategoryName(c.getName()));
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public boolean deleteItem(long id) {
        return items.remove(id) != null;
    }

    @Override
    public boolean itemReferencedByOrders(long id) {
        // The in-memory demo mode does not persist orders across a JVM restart and has
        // no order_item table to check, so items are never treated as referenced here.
        return false;
    }

    @Override
    public long nextItemId() {
        return itemSequence.incrementAndGet();
    }
}
