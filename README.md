# DineVista

## Web-Based Restaurant and Event Management System

DineVista is a responsive Java web application developed for the **SE2030 Software Engineering** group project. It combines restaurant operations and event management in one professional platform.

## Current Release

This repository contains the polished frontend baseline and working demo flows for:

- Restaurant menu browsing and filtering
- Food ordering and a browser-based shopping cart
- Table reservation submission and confirmation
- Event package exploration and booking requests
- Customer and manager dashboards
- Login, registration, validation, error pages, and UTF-8 support

The demo forms use session-based temporary records. The architecture is ready for JDBC/MySQL implementation during the CRUD phase.

## Team Members

| Student ID | Student Name | Assigned Major Function |
|---|---|---|
| IT25103799 | Wijesuriya W. A. T. D. | User, Customer and Staff Management |
| IT25103794 | Samarasinghe M. A. I. | Menu and Inventory Management |
| IT25103795 | De Silva K. H. B. N. | Table Reservation and Food Order Management |
| IT25103796 | Manzab M. G. M. | Event Package and Booking Management |
| IT25103797 | Nawarathna N. M. I. N. | Payment, Billing and Promotion Management |
| IT25103798 | Hansaka A. K. | Event Resource and Staff Scheduling Management |

## Technology Stack

- Java 11
- Java Servlets 4.0
- JSP
- JDBC-ready architecture
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
│   └── model/
├── src/main/webapp/
│   ├── assets/
│   ├── WEB-INF/views/
│   └── index.jsp
├── database/
│   ├── schema.sql
│   └── sample-data.sql
├── docs/
│   ├── proposal/
│   └── diagrams/
├── pom.xml
└── README.md
```

## Run in IntelliJ IDEA

1. Open this folder as a Maven project.
2. Set the Project SDK to Java 11 or later.
3. Add an Apache Tomcat 9 local configuration.
4. Deploy the artifact `DineVista:war exploded`.
5. Open `http://localhost:8080/DineVista/`.

## Demo Login

The current authentication flow is for UI demonstration:

- Choose **Customer** to open the customer dashboard.
- Choose **Manager** to open the operations dashboard.
- Any non-empty email and password are accepted until database authentication is connected.

## Git Workflow

This repository currently uses only the `main` branch.

- `main` - active development and stable project source

All approved project changes should be committed and pushed directly to `main`.
