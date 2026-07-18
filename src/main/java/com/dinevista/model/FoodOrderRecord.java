package com.dinevista.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FoodOrderRecord implements Serializable {
    private final long id;
    private final String reference;
    private final String customerKey;
    private final String customerName;
    private final String email;
    private final String phone;
    private final String orderType;
    private final String reservationReference;
    private final LocalDateTime requestedFor;
    private final String orderNotes;
    private String status;
    private final List<OrderItemRecord> items;
    private final BigDecimal subtotal;
    private final BigDecimal serviceCharge;
    private final BigDecimal totalAmount;
    private String staffNote;
    private String cancellationReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<StatusHistoryRecord> history = new ArrayList<>();

    public FoodOrderRecord(long id, String reference, String customerKey,
                           String customerName, String email, String phone,
                           String orderType, String reservationReference,
                           LocalDateTime requestedFor, String orderNotes,
                           List<OrderItemRecord> items) {
        this.id = id;
        this.reference = reference;
        this.customerKey = customerKey;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.orderType = orderType;
        this.reservationReference = reservationReference;
        this.requestedFor = requestedFor;
        this.orderNotes = orderNotes == null ? "" : orderNotes;
        this.items = new ArrayList<>(items);
        this.subtotal = items.stream()
                .map(OrderItemRecord::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        this.serviceCharge = "DINE_IN".equals(orderType)
                ? subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.totalAmount = subtotal.add(serviceCharge).setScale(2, RoundingMode.HALF_UP);
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
        addHistory("PENDING", "Food order created.", customerName);
    }


    public FoodOrderRecord(long id, String reference, String customerKey,
                           String customerName, String email, String phone,
                           String orderType, String reservationReference,
                           LocalDateTime requestedFor, String orderNotes,
                           String status, List<OrderItemRecord> items,
                           BigDecimal subtotal, BigDecimal serviceCharge,
                           BigDecimal totalAmount, String staffNote,
                           String cancellationReason, LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           List<StatusHistoryRecord> restoredHistory) {
        this.id = id;
        this.reference = reference;
        this.customerKey = customerKey;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.orderType = orderType;
        this.reservationReference = reservationReference == null ? "" : reservationReference;
        this.requestedFor = requestedFor;
        this.orderNotes = orderNotes == null ? "" : orderNotes;
        this.status = status;
        this.items = new ArrayList<>(items == null ? Collections.emptyList() : items);
        this.subtotal = subtotal == null ? BigDecimal.ZERO.setScale(2) : subtotal;
        this.serviceCharge = serviceCharge == null ? BigDecimal.ZERO.setScale(2) : serviceCharge;
        this.totalAmount = totalAmount == null ? this.subtotal.add(this.serviceCharge) : totalAmount;
        this.staffNote = staffNote == null ? "" : staffNote;
        this.cancellationReason = cancellationReason == null ? "" : cancellationReason;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        if (restoredHistory != null) this.history.addAll(restoredHistory);
    }

    public long getId() { return id; }
    public String getReference() { return reference; }
    public String getCustomerKey() { return customerKey; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getOrderType() { return orderType; }
    public String getReservationReference() { return reservationReference; }
    public LocalDateTime getRequestedFor() { return requestedFor; }
    public String getOrderNotes() { return orderNotes; }
    public String getStatus() { return status; }
    public List<OrderItemRecord> getItems() { return Collections.unmodifiableList(items); }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getServiceCharge() { return serviceCharge; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStaffNote() { return staffNote; }
    public String getCancellationReason() { return cancellationReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<StatusHistoryRecord> getHistory() { return Collections.unmodifiableList(history); }

    public void changeStatus(String newStatus, String note, String changedBy) {
        this.status = newStatus;
        this.staffNote = note == null ? "" : note;
        if ("CANCELLED".equals(newStatus) || "REJECTED".equals(newStatus)) {
            this.cancellationReason = this.staffNote;
        }
        this.updatedAt = LocalDateTime.now();
        addHistory(newStatus, note, changedBy);
    }

    private void addHistory(String status, String note, String changedBy) {
        history.add(0, new StatusHistoryRecord(status, note, changedBy, LocalDateTime.now()));
    }

    public String getOrderTypeDisplay() {
        return orderType == null ? "" : orderType.replace('_', ' ');
    }

    public String getRequestedForDisplay() {
        return requestedFor == null ? "As soon as possible"
                : requestedFor.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public String getCreatedAtDisplay() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public String getSubtotalDisplay() {
        return "LKR " + String.format("%,.0f", subtotal);
    }

    public String getServiceChargeDisplay() {
        return "LKR " + String.format("%,.0f", serviceCharge);
    }

    public String getTotalAmountDisplay() {
        return "LKR " + String.format("%,.0f", totalAmount);
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(OrderItemRecord::getQuantity).sum();
    }

    public String getStatusCss() {
        String value = status == null ? "" : status.toLowerCase();
        if ("completed".equals(value) || "served".equals(value) || "ready".equals(value)) return "confirmed";
        if ("cancelled".equals(value) || "rejected".equals(value)) return "cancelled";
        if ("pending".equals(value)) return "pending";
        return "processing";
    }
}
