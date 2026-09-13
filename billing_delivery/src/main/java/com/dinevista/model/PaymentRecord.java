package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A recorded (simulated) settlement against one {@link InvoiceRecord}, owned by
 * the Billing, Promotions & Discounts module (Nawarathna N. M. I. N. / IT25103797).
 * Mirrors the {@code payment} table in database/schema.sql. There is no live
 * payment gateway in this prototype (BR-PAY-01) — payments are recorded and
 * verified by finance staff, never sent to an external processor.
 *
 * paymentMethod is one of CASH, CARD, BANK_TRANSFER, ONLINE.
 * paymentStatus is one of PENDING, SUCCESS, FAILED, REFUNDED.
 */
public class PaymentRecord implements Serializable {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final long id;
    private final long invoiceId;
    private final String paymentReference;
    private final String paymentMethod;
    private final BigDecimal amount;
    private String status;
    private final LocalDateTime paidAt;
    private final String verifiedBy;
    private String note;
    private final LocalDateTime createdAt;

    public PaymentRecord(long id, long invoiceId, String paymentReference, String paymentMethod,
                          BigDecimal amount, String status, LocalDateTime paidAt, String verifiedBy,
                          String note, LocalDateTime createdAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.paymentReference = paymentReference;
        this.paymentMethod = paymentMethod;
        this.amount = (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP);
        this.status = status == null ? "SUCCESS" : status;
        this.paidAt = paidAt;
        this.verifiedBy = verifiedBy == null ? "" : verifiedBy;
        this.note = note == null ? "" : note;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public long getId() { return id; }
    public long getInvoiceId() { return invoiceId; }
    public String getPaymentReference() { return paymentReference; }
    public String getPaymentMethod() { return paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getVerifiedBy() { return verifiedBy; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void markRefunded(String reason) {
        this.status = "REFUNDED";
        this.note = (note == null || note.isEmpty()) ? "Refunded: " + reason : note + " | Refunded: " + reason;
    }

    public String getMethodDisplay() {
        return paymentMethod == null ? "" : paymentMethod.replace('_', ' ');
    }

    public String getAmountDisplay() { return "LKR " + String.format("%,.2f", amount); }

    public String getPaidAtDisplay() {
        return paidAt == null ? "—" : paidAt.format(DISPLAY_FORMAT);
    }

    public String getCreatedAtDisplay() { return createdAt.format(DISPLAY_FORMAT); }

    public String getStatusCss() {
        switch (status == null ? "" : status) {
            case "SUCCESS": return "confirmed";
            case "REFUNDED": return "cancelled";
            case "FAILED": return "cancelled";
            default: return "pending";
        }
    }
}
