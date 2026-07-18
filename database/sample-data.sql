USE dinevista;

INSERT INTO role (role_name, description) VALUES
('ADMIN', 'System administrator'),
('MANAGER', 'Restaurant and event operations manager'),
('STAFF', 'Restaurant or event staff member'),
('CUSTOMER', 'Registered DineVista customer');

INSERT INTO menu_category (category_name, description, display_order) VALUES
('Signature', 'Chef-crafted DineVista dishes', 1),
('Sri Lankan', 'Modern local favourites', 2),
('Seafood', 'Fresh fish and lagoon seafood', 3),
('Vegetarian', 'Plant-forward mains and bowls', 4),
('Desserts', 'Sweet finishes and pastries', 5);

INSERT INTO menu_item (category_id, item_name, description, price, dietary_type, spice_level) VALUES
(1, 'Fire-Roasted Chicken', 'Herb-marinated chicken with coconut pepper sauce.', 2450.00, 'REGULAR', 'MEDIUM'),
(2, 'Island Curry Collection', 'Chicken curry, dhal, vegetables, rice, and sambols.', 2150.00, 'REGULAR', 'MEDIUM'),
(3, 'Lagoon Grilled Fish', 'Daily catch with lime butter and herb rice.', 2850.00, 'GLUTEN_AWARE', 'MILD'),
(4, 'Garden Harvest Bowl', 'Roasted vegetables, chickpeas, avocado, and red rice.', 1850.00, 'VEGAN', 'MILD'),
(5, 'Ceylon Cocoa Slice', 'Dark chocolate mousse with vanilla cream and berries.', 1150.00, 'VEGETARIAN', 'NONE');

INSERT INTO restaurant_table (table_code, seating_area, capacity) VALUES
('I-01', 'INDOOR', 2),
('I-02', 'INDOOR', 4),
('G-01', 'GARDEN', 4),
('G-02', 'GARDEN', 6),
('P-01', 'PRIVATE_DINING', 12),
('C-01', 'CHEF_COUNTER', 2);

INSERT INTO event_package (package_name, event_category, description, base_price_per_guest, minimum_guests, maximum_guests, inclusions) VALUES
('Joyful Gatherings', 'BIRTHDAY', 'Birthday and family celebration package.', 4500.00, 20, 180, 'Buffet or set menu, basic styling, welcome beverage, service staff'),
('Everlasting Elegance', 'WEDDING', 'Premium wedding reception package.', 7900.00, 50, 350, 'Premium menu, venue styling, coordinator, bridal table, cake service'),
('Professional Impact', 'CORPORATE', 'Corporate meeting and launch package.', 5800.00, 20, 300, 'Meeting setup, food service, audio-visual essentials, registration support');

INSERT INTO event_venue (venue_name, venue_type, capacity, base_fee, description) VALUES
('Garden Pavilion', 'OUTDOOR', 220, 150000.00, 'Landscaped outdoor event venue with weather backup options.'),
('Vista Grand Hall', 'INDOOR', 350, 250000.00, 'Climate-controlled hall with stage and projection facilities.'),
('Private Dining Suite', 'PRIVATE_ROOM', 40, 60000.00, 'Private room for intimate celebrations and executive dinners.');
