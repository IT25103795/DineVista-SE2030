# Billing, Promotions & Discounts module — code files

Owner: Nawarathna N. M. I. N. (IT25103797)

These files implement your module inside the existing DineVista Maven/Tomcat
project, following the exact same conventions as the other five modules
(record models, repository interface + in-memory/JDBC implementations,
service with `OperationResult`, a `@WebServlet` controller, and JSP views
using the shared header/footer fragments).

## Where each file goes (paths are relative to the project's `Dine Vista/` folder)

```
src/main/java/com/dinevista/model/InvoiceRecord.java
src/main/java/com/dinevista/model/InvoiceItemRecord.java
src/main/java/com/dinevista/model/PaymentRecord.java
src/main/java/com/dinevista/model/PromotionRecord.java
src/main/java/com/dinevista/model/PromotionUsageRecord.java

src/main/java/com/dinevista/repository/BillingRepository.java
src/main/java/com/dinevista/repository/InMemoryBillingRepository.java
src/main/java/com/dinevista/repository/JdbcBillingRepository.java

src/main/java/com/dinevista/service/BillingService.java

src/main/java/com/dinevista/util/BillingContext.java

src/main/java/com/dinevista/controller/BillingServlet.java

src/main/webapp/WEB-INF/views/staff-billing.jsp
src/main/webapp/WEB-INF/views/staff-billing-form.jsp
src/main/webapp/WEB-INF/views/staff-billing-detail.jsp
src/main/webapp/WEB-INF/views/staff-billing-promotions.jsp
src/main/webapp/WEB-INF/views/staff-billing-promotion-form.jsp

src/main/webapp/WEB-INF/views/fragments/header.jspf   (already patched — only adds one <a> nav link for "Billing")
```

No changes are needed to `database/schema.sql` — the `invoice`, `invoice_item`,
`payment`, `promotion` and `promotion_usage` tables it already defines are
used as-is. `JdbcBillingRepository` auto-adds a handful of extra columns
(customer name/key/email on `invoice`, a note/verifier on `payment`, an
invoice link on `promotion_usage`) the first time it runs, the same way
`JdbcInventoryRepository` adds `performed_by_name` — so it's safe to run
against a database that was already created from schema.sql.

## What it covers (UC-BIL-01 + promotion CRUD)

- **Generate an invoice** from confirmed charges (`/staff/billing/new`) — pick
  up a food order by reference (auto-fills its line items + service charge)
  or enter lines manually for an event booking / other charge. Totals
  (subtotal, a 2.5% service tax, discount, grand total) are always
  calculated from the lines, never typed in directly.
- **Apply an eligible promotion code** — checked against active/inactive,
  validity window, minimum spend and usage limit. An invalid or ineligible
  code never blocks the invoice; it's simply not applied, and the reason is
  shown on the invoice.
- **Record and verify a simulated payment** (`/staff/billing/pay`) — no live
  payment gateway; duplicate references are blocked and a payment can never
  exceed the outstanding balance.
- **Void / refund a payment** and **cancel an unpaid invoice** — both require
  a reason and are kept as auditable history rather than deleted.
- **Promotion catalogue CRUD** (`/staff/billing/promotions`) — create, edit,
  and delete (blocked once a promotion has recorded usage, to preserve the
  audit trail).
- A small finance summary (total invoiced / collected / outstanding) on the
  billing list page.

## Access

Routes are staff-only, gated the same way as `/staff/inventory` (manager
session required); a link is in the top navigation.
