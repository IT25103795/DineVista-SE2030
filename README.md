# DineVista

## Web-Based Restaurant and Event Management System

DineVista is a responsive Java web application developed for the **SE2030 Software Engineering** group project. It combines restaurant operations and event management in one professional platform.

## Current Release

The current release includes a complete **Table Reservation and Food Order Management** module for both customers and restaurant staff.

### Customer functions

- Browse available restaurant tables by date, time, party size, and seating area
- Create, view, edit, and cancel eligible table reservations
- Browse and filter the restaurant menu
- Add, update, and remove food items in a server-side cart
- Create dine-in, takeaway, and reservation-linked pre-orders
- View current and previous reservations and orders
- Track reservation and order status histories
- Receive manager confirmation and lifecycle updates from the notification bell
- Cancel eligible orders before kitchen preparation begins

### Customer experience enhancements

- Theme-aware light and dark appearance across navigation, forms, cards, tables,
  detail records, and the footer
- Local illustrated hero backgrounds with readable overlays on all public pages
- Three-step reservation form with a synchronized review summary
- Floor-style table availability preview that safely pre-fills the existing form
- Slide-out food cart backed by the original server-side update/remove/checkout forms
- Clear confirmation cards, copyable references, and live status-progress tracks
- Mobile bottom navigation, keyboard-friendly dialogs, focus handling, and
  reduced-motion support

### Restaurant-staff functions

- View and filter incoming reservations and orders
- Review complete customer details
- Assign a suitable available table
- Confirm, reject, seat, complete, cancel, or mark reservations as no-show
- Send accepted food orders through the kitchen workflow
- Update orders from pending to confirmed, preparing, ready, served, and completed
- Receive new, edited, and cancelled customer activity from the notification bell
- Record compulsory operational notes and cancellation or rejection reasons
- Prevent overlapping table allocations and invalid order quantities

The module runs in **memory mode by default**, so it works immediately without MySQL. A complete JDBC repository is included and can be enabled using the provided database configuration after importing the SQL scripts.

## Team Members

| Student ID | Student Name | Assigned Major Function |
|---|---|---|
| IT25103799 | Wijesuriya W. A. T. D. | Event Resource and Staff Scheduling Management |
| IT25103794 | Samarasinghe M. A. I. | Menu and Inventory Management |
| IT25103795 | De Silva K. H. B. N. | Table Reservation and Food Order Management |
| IT25103796 | Manzab M. G. M. | Event Package and Booking Management |
| IT25103797 | Nawarathna N. M. I. N. | Payment, Billing and Promotion Management |
| IT25103798 | Hansaka A. K. | User, Customer and Staff Management |

## Technology Stack

- Java 11
- Java Servlets 4.0
- JSP
- JDBC
- MySQL 8
- Maven WAR
- Apache Tomcat 9
- HTML5, CSS3, and vanilla JavaScript

## Project Structure

```text
DineVista/
├── src/main/java/com/dinevista/
│   ├── controller/
│   ├── filter/
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── util/
├── src/main/resources/
│   └── database.example.properties
├── src/main/webapp/
│   ├── assets/
│   ├── WEB-INF/views/
│   └── index.jsp
├── database/
│   ├── schema.sql
│   └── sample-data.sql
├── docs/
│   ├── proposal/
│   ├── diagrams/
│   └── TABLE_RESERVATION_AND_FOOD_ORDER_MODULE.md
├── pom.xml
└── README.md
```

## Main Module Routes

| Route | Purpose |
|---|---|
| `/menu` | Browse and filter menu items |
| `/reservations` | Search availability and manage customer reservations |
| `/reservations/view?reference=...` | View one reservation and its timeline |
| `/reservations/edit?reference=...` | Edit an eligible reservation |
| `/orders` | Manage the cart, checkout, and customer orders |
| `/orders/view?reference=...` | View one order and its status timeline |
| `/staff/reservations` | Staff reservation operations dashboard |
| `/staff/reservations/view?reference=...` | Staff reservation review and status actions |
| `/staff/orders` | Staff kitchen/order operations dashboard |
| `/staff/orders/view?reference=...` | Staff order review and workflow actions |
| `/dashboard` | Role-based customer or manager dashboard |
| `/notifications/open?id=...` | Mark an owned notification read and open its related record |
| `/notifications/read-all` | Mark the signed-in user's notifications as read |
| `/notifications/clear-all` | Permanently clear only the signed-in user's notifications |
| `/health` | JSON health response |

## Run in IntelliJ IDEA

1. Open this folder as a Maven project.
2. Set the Project SDK to Java 11 or later.
3. Allow Maven to download dependencies.
4. Add an Apache Tomcat 9 local configuration.
5. Deploy the artifact `DineVista:war exploded`.
6. Open `http://localhost:8080/DineVista/`.

## Demo Login

The current sign-in screen supports role-based demonstration:

- Choose **Customer** to open customer reservations and orders.
- Choose **Manager** to open staff reservation and order operations.
- Enter any valid-looking email and any password with at least four characters.

## Persistence Modes

### Memory mode — default

No database setup is required. Demo data remains available while the application is running.

### MySQL mode

1. Import `database/schema.sql`.
2. Import `database/sample-data.sql`.
3. Copy `src/main/resources/database.example.properties` to `src/main/resources/database.properties`.
4. Change `storage.mode=memory` to `storage.mode=mysql`.
5. Enter your local MySQL username and password.
6. Restart Tomcat.

Never commit `database.properties`; it is already excluded by `.gitignore`.

## Documentation

Detailed implementation, business rules, routes, status transitions, testing steps, and viva points are available in:

```text
docs/TABLE_RESERVATION_AND_FOOD_ORDER_MODULE.md
```

The latest audit results, including the 107-check CRUD and notification suite and UI interaction
verification, are recorded in `docs/CRUD_AUDIT_RESULTS.md`.

## Git Workflow

This repository currently uses only the `main` branch.

- `main` — active development and stable project source

Commit approved project changes directly to `main`.
