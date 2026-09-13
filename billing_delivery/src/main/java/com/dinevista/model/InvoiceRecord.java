package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A generated invoice owned by the Billing, Promotions & Discounts module
 * (Nawarathna N. M. I. N. / IT25103797). Mirrors the {@code invoice} table in
 * database/schema.sql (plus a small set of demo-friendly columns — customer
 * name/key, notes, amount paid — that are auto-migrated in at startup by
 * {@link com.dinevista.repository.JdbcBillingRepository}, the same pattern
 * {@code JdbcInventoryRepository} uses for {@code performed_by_name}).
 *
 * invoiceStatus is one of DRAFT, ISSUED, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED.
 * sourceType is one of FOOD_ORDER, EVENT_BOOKING, OTHER (UC-BIL-01 / FR15).
 */
public class InvoiceRecord implements Serializable {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final long id;
    private final String invoiceNumber;
    private final String sourceType;
    private final String sourceReference;
    private final String customerKey;
    private final String customerName;
    private final String customerEmail;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private final List<InvoiceItemRecord> items;
    private final BigDecimal subtotal;
    private final BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private String status;
    private String promotionCode;
    private String notes;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<StatusHistoryRecord> history = new ArrayList<>();

    public InvoiceRecord(long id, String invoiceNumber, String sourceType, String sourceReference,
                          String customerKey, String customerName, String customerEmail,
                          LocalDate issueDate, LocalDate dueDate, List<InvoiceItemRecord> items,
                          BigDecimal subtotal, BigDecimal taxAmount, BigDecimal discountAmount,
                          BigDecimal totalAmount, BigDecimal amountPaid, String status,
                          String promotionCode, String notes, String createdBy,
                          LocalDateTime createdAt, LocalDateTime updatedAt,
                          List<StatusHistoryRecord> restoredHistory) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.sourceType = sourceType;
        this.sourceReference = sourceReference == null ? "" : sourceReference;
        this.customerKey = customerKey == null ? "" : customerKey;
        this.customerName = customerName == null ? "Walk-in customer" : customerName;
        this.customerEmail = customerEmail == null ? "" : customerEmail;
        this.issueDate = issueDate == null ? LocalDate.now() : issueDate;
        this.dueDate = dueDate;
        this.items = new ArrayList<>(items == null ? Collections.emptyList() : items);
        this.subtotal = scale(subtotal);
        this.taxAmount = scale(taxAmount);
        this.discountAmount = scale(discountAmount);
        this.totalAmount = scale(totalAmount);
        this.amountPaid = scale(amountPaid);
        this.status = status == null ? "ISSUED" : status;
        this.promotionCode = promotionCode;
        this.notes = notes == null ? "" : notes;
        this.createdBy = createdBy == null ? "System" : createdBy;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        if (restoredHistory != null) this.history.addAll(restoredHistory);
    }

    private static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    public long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getSourceType() { return sourceType; }
    public String getSourceReference() { return sourceReference; }
    public String getCustomerKey() { return customerKey; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public List<InvoiceItemRecord> getItems() { return Collections.unmodifiableList(items); }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public String getStatus() { return status; }
    public String getPromotionCode() { return promotionCode; }
    public String getNotes() { return notes; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<StatusHistoryRecord> getHistory() { return Collections.unmodifiableList(history); }

    public BigDecimal getBalance() {
        BigDecimal balance = totalAmount.subtract(amountPaid);
        return balance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2) : balance;
    }

    public boolean isFullyPaid() { return getBalance().compareTo(BigDecimal.ZERO) <= 0; }
    public boolean isCancelled() { return "CANCELLED".equals(status); }

    /** Applies a verified payment amount and recomputes the invoice status (FR16). */
    public void applyPayment(BigDecimal amount, String note, String changedBy) {
        this.amountPaid = amountPaid.add(scale(amount));
        this.status = isFullyPaid() ? "PAID" : "PARTIALLY_PAID";
        this.updatedAt = LocalDateTime.now();
        addHistory(status, note, changedBy);
    }

    /** Reverses a previously verified payment amount, e.g. a refund/void (FR23). */
    public void reversePayment(BigDecimal amount, String note, String changedBy) {
        BigDecimal newPaid = amountPaid.subtract(scale(amount));
        this.amountPaid = newPaid.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2) : newPaid;
        this.status = amountPaid.compareTo(BigDecimal.ZERO) <= 0 ? "ISSUED" : "PARTIALLY_PAID";
        this.updatedAt = LocalDateTime.now();
        addHistory(status, note, changedBy);
    }

    public void cancel(String note, String changedBy) {
        this.status = "CANCELLED";
        this.updatedAt = LocalDateTime.now();
        addHistory(status, note, changedBy);
    }

    public void appendNote(String extra) {
        this.notes = notes.isEmpty() ? extra : notes + " | " + extra;
        this.updatedAt = LocalDateTime.now();
    }

    public void clearPromotion() { this.promotionCode = null; }

    public void setDiscountAndTotal(BigDecimal discountAmount, BigDecimal totalAmount) {
        this.discountAmount = scale(discountAmount);
        this.totalAmount = scale(totalAmount);
        this.updatedAt = LocalDateTime.now();
    }

    public void addHistory(String status, String note, String changedBy) {
        history.add(0, new StatusHistoryRecord(status, note, changedBy, LocalDateTime.now()));
    }

    public String getSourceTypeDisplay() {
        return sourceType == null ? "" : sourceType.replace('_', ' ');
    }

    public String getIssueDateDisplay() { return issueDate.format(DATE_FORMAT); }
    public String getDueDateDisplay() { return dueDate == null ? "—" : dueDate.format(DATE_FORMAT); }
    public String getCreatedAtDisplay() { return createdAt.format(DATE_TIME_FORMAT); }

    public String getSubtotalDisplay() { return "LKR " + String.format("%,.2f", subtotal); }
    public String getTaxAmountDisplay() { return "LKR " + String.format("%,.2f", taxAmount); }
    public String getDiscountAmountDisplay() { return "LKR " + String.format("%,.2f", discountAmount); }
    public String getTotalAmountDisplay() { return "LKR " + String.format("%,.2f", totalAmount); }
    public String getAmountPaidDisplay() { return "LKR " + String.format("%,.2f", amountPaid); }
    public String getBalanceDisplay() { return "LKR " + String.format("%,.2f", getBalance()); }

    public String getStatusCss() {
        switch (status == null ? "" : status) {
            case "PAID": return "confirmed";
            case "PARTIALLY_PAID": return "processing";
            case "CANCELLED": return "cancelled";
            case "OVERDUE": return "cancelled";
            default: return "pending";
        }
    }

    public String getStatusLabel() {
        return status == null ? "" : status.replace('_', ' ');
    }
}
