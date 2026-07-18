-- DineVista MySQL 8 schema
-- Web-Based Restaurant and Event Management System

CREATE DATABASE IF NOT EXISTS dinevista
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE dinevista;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS promotion_usage;
DROP TABLE IF EXISTS promotion;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS invoice_item;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS event_staff_assignment;
DROP TABLE IF EXISTS staff_schedule;
DROP TABLE IF EXISTS resource_booking;
DROP TABLE IF EXISTS event_resource;
DROP TABLE IF EXISTS event_requirement;
DROP TABLE IF EXISTS event_booking_status_history;
DROP TABLE IF EXISTS event_booking;
DROP TABLE IF EXISTS event_venue;
DROP TABLE IF EXISTS event_package;
DROP TABLE IF EXISTS order_status_history;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS food_order;
DROP TABLE IF EXISTS table_reservation;
DROP TABLE IF EXISTS restaurant_table;
DROP TABLE IF EXISTS stock_transaction;
DROP TABLE IF EXISTS menu_item_ingredient;
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS menu_item;
DROP TABLE IF EXISTS menu_category;
DROP TABLE IF EXISTS staff_profile;
DROP TABLE IF EXISTS customer_profile;
DROP TABLE IF EXISTS user_account;
DROP TABLE IF EXISTS role;

CREATE TABLE role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(40) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE user_account (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    account_status ENUM('ACTIVE','INACTIVE','SUSPENDED','PENDING') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(role_id)
) ENGINE=InnoDB;

CREATE TABLE customer_profile (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    address_line VARCHAR(255),
    city VARCHAR(100),
    dietary_notes VARCHAR(500),
    loyalty_points INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE staff_profile (
    staff_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    employee_code VARCHAR(40) NOT NULL UNIQUE,
    job_title VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    hire_date DATE,
    hourly_rate DECIMAL(10,2),
    availability_status ENUM('AVAILABLE','UNAVAILABLE','ON_LEAVE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE menu_category (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE menu_item (
    menu_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    item_name VARCHAR(140) NOT NULL,
    description VARCHAR(600),
    price DECIMAL(10,2) NOT NULL,
    image_path VARCHAR(255),
    preparation_minutes INT NOT NULL DEFAULT 20,
    dietary_type ENUM('REGULAR','VEGETARIAN','VEGAN','GLUTEN_AWARE') NOT NULL DEFAULT 'REGULAR',
    spice_level ENUM('NONE','MILD','MEDIUM','HOT') NOT NULL DEFAULT 'NONE',
    availability_status ENUM('AVAILABLE','UNAVAILABLE','SOLD_OUT') NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_category FOREIGN KEY (category_id) REFERENCES menu_category(category_id)
) ENGINE=InnoDB;

CREATE TABLE ingredient (
    ingredient_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ingredient_name VARCHAR(140) NOT NULL UNIQUE,
    unit VARCHAR(30) NOT NULL,
    current_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    reorder_level DECIMAL(12,3) NOT NULL DEFAULT 0,
    unit_cost DECIMAL(10,2),
    supplier_name VARCHAR(160),
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE menu_item_ingredient (
    menu_item_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity_required DECIMAL(10,3) NOT NULL,
    PRIMARY KEY (menu_item_id, ingredient_id),
    CONSTRAINT fk_recipe_item FOREIGN KEY (menu_item_id) REFERENCES menu_item(menu_item_id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
) ENGINE=InnoDB;

CREATE TABLE stock_transaction (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ingredient_id BIGINT NOT NULL,
    transaction_type ENUM('PURCHASE','USAGE','ADJUSTMENT','WASTE','RETURN') NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    reference_note VARCHAR(255),
    performed_by BIGINT,
    transaction_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id),
    CONSTRAINT fk_stock_user FOREIGN KEY (performed_by) REFERENCES user_account(user_id)
) ENGINE=InnoDB;

CREATE TABLE restaurant_table (
    table_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    table_code VARCHAR(30) NOT NULL UNIQUE,
    seating_area ENUM('INDOOR','GARDEN','PRIVATE_DINING','CHEF_COUNTER') NOT NULL,
    capacity INT NOT NULL,
    table_status ENUM('AVAILABLE','RESERVED','OCCUPIED','OUT_OF_SERVICE') NOT NULL DEFAULT 'AVAILABLE'
) ENGINE=InnoDB;

CREATE TABLE table_reservation (
    reservation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_reference VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT,
    table_id BIGINT,
    guest_name VARCHAR(160) NOT NULL,
    email VARCHAR(160) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    party_size INT NOT NULL,
    seating_preference VARCHAR(100),
    occasion_notes VARCHAR(500),
    reservation_status ENUM('PENDING','CONFIRMED','SEATED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_customer FOREIGN KEY (customer_id) REFERENCES customer_profile(customer_id),
    CONSTRAINT fk_reservation_table FOREIGN KEY (table_id) REFERENCES restaurant_table(table_id)
) ENGINE=InnoDB;

CREATE TABLE food_order (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_reference VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT,
    reservation_id BIGINT,
    order_type ENUM('DINE_IN','TAKEAWAY','DELIVERY') NOT NULL,
    order_status ENUM('PENDING','CONFIRMED','PREPARING','READY','OUT_FOR_DELIVERY','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    delivery_address VARCHAR(500),
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    service_charge DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    order_notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer_profile(customer_id),
    CONSTRAINT fk_order_reservation FOREIGN KEY (reservation_id) REFERENCES table_reservation(reservation_id)
) ENGINE=InnoDB;

CREATE TABLE order_item (
    order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    item_notes VARCHAR(255),
    line_total DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES food_order(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_menu FOREIGN KEY (menu_item_id) REFERENCES menu_item(menu_item_id)
) ENGINE=InnoDB;

CREATE TABLE order_status_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    changed_by BIGINT,
    note VARCHAR(255),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_history_order FOREIGN KEY (order_id) REFERENCES food_order(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_history_user FOREIGN KEY (changed_by) REFERENCES user_account(user_id)
) ENGINE=InnoDB;

CREATE TABLE event_package (
    package_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_name VARCHAR(140) NOT NULL UNIQUE,
    event_category ENUM('WEDDING','CORPORATE','BIRTHDAY','ANNIVERSARY','PRIVATE','CUSTOM') NOT NULL,
    description VARCHAR(800),
    base_price_per_guest DECIMAL(10,2) NOT NULL,
    minimum_guests INT NOT NULL DEFAULT 10,
    maximum_guests INT NOT NULL DEFAULT 500,
    inclusions TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE event_venue (
    venue_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_name VARCHAR(140) NOT NULL UNIQUE,
    venue_type ENUM('INDOOR','OUTDOOR','PRIVATE_ROOM','OFF_SITE') NOT NULL,
    capacity INT NOT NULL,
    base_fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    description VARCHAR(600),
    availability_status ENUM('AVAILABLE','UNAVAILABLE','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE'
) ENGINE=InnoDB;

CREATE TABLE event_booking (
    event_booking_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_reference VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT,
    package_id BIGINT,
    venue_id BIGINT,
    contact_name VARCHAR(160) NOT NULL,
    email VARCHAR(160) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_date DATE NOT NULL,
    guest_count INT NOT NULL,
    requirements_summary TEXT,
    booking_status ENUM('INQUIRY','CONSULTATION','QUOTED','CONFIRMED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'INQUIRY',
    estimated_amount DECIMAL(14,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_customer FOREIGN KEY (customer_id) REFERENCES customer_profile(customer_id),
    CONSTRAINT fk_event_package FOREIGN KEY (package_id) REFERENCES event_package(package_id),
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES event_venue(venue_id)
) ENGINE=InnoDB;

CREATE TABLE event_booking_status_history (
    event_history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_booking_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    changed_by BIGINT,
    note VARCHAR(500),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_history_booking FOREIGN KEY (event_booking_id) REFERENCES event_booking(event_booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_event_history_user FOREIGN KEY (changed_by) REFERENCES user_account(user_id)
) ENGINE=InnoDB;

CREATE TABLE event_requirement (
    requirement_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_booking_id BIGINT NOT NULL,
    requirement_type ENUM('CATERING','DECOR','AUDIO_VISUAL','SEATING','LIGHTING','TRANSPORT','OTHER') NOT NULL,
    description VARCHAR(800) NOT NULL,
    quantity INT,
    estimated_cost DECIMAL(12,2),
    requirement_status ENUM('REQUESTED','APPROVED','ALLOCATED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'REQUESTED',
    CONSTRAINT fk_requirement_event FOREIGN KEY (event_booking_id) REFERENCES event_booking(event_booking_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE event_resource (
    resource_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_name VARCHAR(160) NOT NULL,
    resource_category ENUM('FURNITURE','AUDIO_VISUAL','DECOR','LIGHTING','KITCHEN','TRANSPORT','OTHER') NOT NULL,
    total_quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    unit_cost DECIMAL(10,2),
    resource_status ENUM('AVAILABLE','MAINTENANCE','RETIRED') NOT NULL DEFAULT 'AVAILABLE'
) ENGINE=InnoDB;

CREATE TABLE resource_booking (
    resource_booking_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_booking_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    quantity_reserved INT NOT NULL,
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    allocation_status ENUM('REQUESTED','ALLOCATED','RETURNED','CANCELLED') NOT NULL DEFAULT 'REQUESTED',
    CONSTRAINT fk_resource_booking_event FOREIGN KEY (event_booking_id) REFERENCES event_booking(event_booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_resource_booking_resource FOREIGN KEY (resource_id) REFERENCES event_resource(resource_id)
) ENGINE=InnoDB;

CREATE TABLE staff_schedule (
    schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    staff_id BIGINT NOT NULL,
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    shift_type ENUM('RESTAURANT','KITCHEN','EVENT','DELIVERY','ADMIN') NOT NULL,
    schedule_status ENUM('SCHEDULED','CONFIRMED','COMPLETED','ABSENT','CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    CONSTRAINT fk_schedule_staff FOREIGN KEY (staff_id) REFERENCES staff_profile(staff_id),
    UNIQUE KEY uq_staff_shift (staff_id, shift_date, start_time)
) ENGINE=InnoDB;

CREATE TABLE event_staff_assignment (
    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_booking_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    assignment_role VARCHAR(100) NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    assignment_status ENUM('ASSIGNED','CONFIRMED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'ASSIGNED',
    CONSTRAINT fk_assignment_event FOREIGN KEY (event_booking_id) REFERENCES event_booking(event_booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_staff FOREIGN KEY (staff_id) REFERENCES staff_profile(staff_id)
) ENGINE=InnoDB;

CREATE TABLE invoice (
    invoice_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_number VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT,
    order_id BIGINT,
    event_booking_id BIGINT,
    invoice_type ENUM('FOOD_ORDER','EVENT_BOOKING','OTHER') NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE,
    subtotal DECIMAL(14,2) NOT NULL,
    tax_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(14,2) NOT NULL,
    invoice_status ENUM('DRAFT','ISSUED','PARTIALLY_PAID','PAID','OVERDUE','CANCELLED') NOT NULL DEFAULT 'ISSUED',
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customer_id) REFERENCES customer_profile(customer_id),
    CONSTRAINT fk_invoice_order FOREIGN KEY (order_id) REFERENCES food_order(order_id),
    CONSTRAINT fk_invoice_event FOREIGN KEY (event_booking_id) REFERENCES event_booking(event_booking_id)
) ENGINE=InnoDB;

CREATE TABLE invoice_item (
    invoice_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL,
    line_total DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_invoice_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE payment (
    payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_id BIGINT NOT NULL,
    payment_reference VARCHAR(40) NOT NULL UNIQUE,
    payment_method ENUM('CASH','CARD','BANK_TRANSFER','ONLINE') NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    payment_status ENUM('PENDING','SUCCESS','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    proof_path VARCHAR(255),
    verified_by BIGINT,
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id),
    CONSTRAINT fk_payment_verifier FOREIGN KEY (verified_by) REFERENCES user_account(user_id)
) ENGINE=InnoDB;

CREATE TABLE promotion (
    promotion_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    promotion_code VARCHAR(40) NOT NULL UNIQUE,
    promotion_name VARCHAR(160) NOT NULL,
    discount_type ENUM('PERCENTAGE','FIXED_AMOUNT') NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    minimum_spend DECIMAL(12,2) NOT NULL DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    usage_limit INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE promotion_usage (
    promotion_usage_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    promotion_id BIGINT NOT NULL,
    customer_id BIGINT,
    order_id BIGINT,
    event_booking_id BIGINT,
    discount_applied DECIMAL(12,2) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usage_promotion FOREIGN KEY (promotion_id) REFERENCES promotion(promotion_id),
    CONSTRAINT fk_usage_customer FOREIGN KEY (customer_id) REFERENCES customer_profile(customer_id),
    CONSTRAINT fk_usage_order FOREIGN KEY (order_id) REFERENCES food_order(order_id),
    CONSTRAINT fk_usage_event FOREIGN KEY (event_booking_id) REFERENCES event_booking(event_booking_id)
) ENGINE=InnoDB;

CREATE TABLE notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(60) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message VARCHAR(800) NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_reservation_date_status ON table_reservation(reservation_date, reservation_status);
CREATE INDEX idx_order_status_created ON food_order(order_status, created_at);
CREATE INDEX idx_event_date_status ON event_booking(event_date, booking_status);
CREATE INDEX idx_stock_reorder ON ingredient(current_quantity, reorder_level);
CREATE INDEX idx_staff_schedule_date ON staff_schedule(shift_date, shift_type);

SET FOREIGN_KEY_CHECKS = 1;
