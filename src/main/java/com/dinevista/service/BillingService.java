package com.dinevista.service;

import com.dinevista.model.InvoiceItemRecord;
import com.dinevista.model.InvoiceRecord;
import com.dinevista.model.PaymentRecord;
import com.dinevista.model.PromotionRecord;
import com.dinevista.model.PromotionUsageRecord;
import com.dinevista.repository.BillingRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Business logic for the Billing, Promotions & Discounts module
 * (Nawarathna N. M. I. N. / IT25103797), covering UC-BIL-01 (Generate and
 * Settle Invoice with Eligible Promotion) plus promotion CRUD.
 *
 * Enforces:
 *  - BR-BIL-01: totals are always derived from the confirmed source records / entered charges.
 *  - BR-PRO-01: discounts are applied only when every eligibility rule passes.
 *  - BR-PAY-01: payments are recorded and verified locally; there is no live payment gateway.
 *  - BR-AUD-01: finance history is never hard-deleted — cancellations, voids and refunds are
 *    recorded as explicit, auditable status/history entries instead.
 */
public class BillingService {
    /** Simple flat service tax applied to every invoice subtotal (demo rate). */
    public static final BigDecimal TAX_RATE = new BigDecimal("0.025");
    private static final Set<String> SOURCE_TYPES = Set.of("FOOD_ORDER", "EVENT_BOOKING", "OTHER");
    private static final Set<String> PAYMENT_METHODS = Set.of("CASH", "CARD", "BANK_TRANSFER", "ONLINE");
    private static final Set<String> DISCOUNT_TYPES = Set.of("PERCENTAGE", "FIXED_AMOUNT");

    private final BillingRepository repository;

    public BillingService(BillingRepository repository) {
        this.repository = repository;
    }

    // --------------------------------------------------------------- invoices

    public List<InvoiceRecord> allInvoices(String status, String search) {
        String needle = search == null ? "" : search.trim().toLowerCase();
        String cleanStatus = status == null ? "" : status.trim().toUpperCase();
        List<InvoiceRecord> result = new ArrayList<>();
        for (InvoiceRecord invoice : repository.findAllInvoices()) {
            if (!cleanStatus.isEmpty() && !cleanStatus.equals(invoice.getStatus())) continue;
            if (!needle.isEmpty()
                    && !invoice.getInvoiceNumber().toLowerCase().contains(needle)
                    && !invoice.getCustomerName().toLowerCase().contains(needle)
                    && !invoice.getSourceReference().toLowerCase().contains(needle)) continue;
            result.add(invoice);
        }
        return result;
    }

    public Optional<InvoiceRecord> invoice(long id) {
        return repository.findInvoice(id);
    }

    public List<InvoiceRecord> invoicesForCustomer(String customerKey) {
        return repository.findInvoicesForCustomer(customerKey);
    }

    public List<PaymentRecord> paymentsFor(long invoiceId) {
        return repository.findPaymentsForInvoice(invoiceId);
    }

    /** True once a confirmed source transaction already has a live (non-cancelled) invoice. */
    public Optional<InvoiceRecord> existingInvoiceForSource(String sourceType, String sourceReference) {
        return repository.findInvoiceBySource(sourceType, sourceReference);
    }

    /**
     * Generates an invoice from one or more billable lines (FR15). Line arrays must be the
     * same length; blank rows are ignored. Applies an eligible promotion code when one is
     * supplied (FR22) — an invalid or ineligible code does not fail the invoice, it simply
     * leaves the amount undiscounted and explains why in the invoice notes (A1).
     */
    public synchronized OperationResult<InvoiceRecord> generateInvoice(
            String sourceType, String sourceReference, String customerKey, String customerName,
            String customerEmail, String[] descriptions, String[] quantities, String[] unitPrices,
            String promotionCode, String issuedBy) {

        List<String> errors = new ArrayList<>();
        String cleanSourceType = SOURCE_TYPES.contains(sourceType) ? sourceType : "OTHER";
        String cleanReference = sourceReference == null ? "" : sourceReference.trim();
        String cleanCustomerName = (customerName == null || customerName.trim().isEmpty())
                ? "Walk-in customer" : customerName.trim();

        if (!cleanReference.isEmpty()
                && repository.findInvoiceBySource(cleanSourceType, cleanReference).isPresent()) {
            return OperationResult.failure(
                    "An active invoice already exists for " + cleanReference + ".");
        }

        List<InvoiceItemRecord> items = new ArrayList<>();
        int rows = descriptions == null ? 0 : descriptions.length;
        for (int i = 0; i < rows; i++) {
            String description = descriptions[i] == null ? "" : descriptions[i].trim();
            if (description.isEmpty()) continue;
            BigDecimal quantity = parsePositive(value(quantities, i, "1"), "Quantity for \"" + description + "\"", errors);
            BigDecimal unitPrice = parseNonNegative(value(unitPrices, i, "0"), "Unit price for \"" + description + "\"", errors);
            items.add(new InvoiceItemRecord(repository.nextInvoiceItemId(), description, quantity, unitPrice));
        }
        if (items.isEmpty()) errors.add("Add at least one billable line before generating an invoice.");
        if (!errors.isEmpty()) return OperationResult.failure(errors);

        BigDecimal subtotal = items.stream()
                .map(InvoiceItemRecord::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        String appliedPromotionCode = null;
        PromotionRecord eligiblePromotion = null;
        StringBuilder promotionNote = new StringBuilder();

        String cleanCode = promotionCode == null ? "" : promotionCode.trim().toUpperCase();
        if (!cleanCode.isEmpty()) {
            Optional<PromotionRecord> promotionOpt = repository.findPromotionByCode(cleanCode);
            if (promotionOpt.isEmpty()) {
                promotionNote.append("Promotion \"").append(cleanCode).append("\" was not found; invoice issued at full price.");
            } else {
                PromotionRecord promotion = promotionOpt.get();
                String reason = ineligibilityReason(promotion, subtotal);
                if (reason != null) {
                    promotionNote.append("Promotion \"").append(cleanCode).append("\" was not applied: ").append(reason);
                } else {
                    discountAmount = promotion.calculateDiscount(subtotal);
                    appliedPromotionCode = promotion.getCode();
                    eligiblePromotion = promotion;
                }
            }
        }

        BigDecimal totalAmount = subtotal.add(taxAmount).subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO.setScale(2);

        long invoiceId = repository.nextInvoiceId();
        InvoiceRecord invoice = new InvoiceRecord(
                invoiceId, repository.nextInvoiceNumber(), cleanSourceType, cleanReference,
                customerKey == null ? "" : customerKey.trim(), cleanCustomerName,
                customerEmail == null ? "" : customerEmail.trim(),
                LocalDate.now(), LocalDate.now().plusDays(7), items,
                subtotal, taxAmount, discountAmount, totalAmount, BigDecimal.ZERO,
                "ISSUED", appliedPromotionCode, promotionNote.toString(), issuedBy,
                null, null, null);
        invoice.addHistory("ISSUED", "Invoice generated from " + cleanSourceType.replace('_', ' ') + ".", issuedBy);

        InvoiceRecord saved = repository.saveInvoice(invoice);

        if (eligiblePromotion != null) {
            repository.savePromotionUsage(new PromotionUsageRecord(
                    repository.nextPromotionUsageId(), eligiblePromotion.getId(), eligiblePromotion.getCode(),
                    saved.getId(), saved.getCustomerKey(), discountAmount, null));
        }

        return OperationResult.success(saved);
    }

    private String ineligibilityReason(PromotionRecord promotion, BigDecimal subtotal) {
        if (!promotion.isActive()) return "the code is no longer active.";
        if (!promotion.isCurrentlyValid()) return "the code is outside its valid date range.";
        if (subtotal.compareTo(promotion.getMinimumSpend()) < 0) {
            return "the order does not meet the minimum spend of LKR "
                    + String.format("%,.2f", promotion.getMinimumSpend()) + ".";
        }
        Integer limit = promotion.getUsageLimit();
        if (limit != null && repository.countPromotionUsage(promotion.getId()) >= limit) {
            return "the usage limit for this code has been reached.";
        }
        return null;
    }

    public synchronized OperationResult<Void> cancelInvoice(long invoiceId, String reason, String actor) {
        Optional<InvoiceRecord> invoiceOpt = repository.findInvoice(invoiceId);
        if (invoiceOpt.isEmpty()) return OperationResult.failure("Invoice could not be found.");
        InvoiceRecord invoice = invoiceOpt.get();
        if (invoice.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            return OperationResult.failure(
                    "This invoice has recorded payments and cannot be cancelled; void the payments first.");
        }
        invoice.cancel(reason == null || reason.trim().isEmpty() ? "Invoice cancelled." : reason.trim(), actor);
        repository.saveInvoice(invoice);
        return OperationResult.success(null);
    }

    // --------------------------------------------------------------- payments

    /**
     * Records and verifies a simulated payment in one controlled step (FR16). Duplicate
     * payment references are blocked (A3) and a payment can never push the balance negative.
     */
    public synchronized OperationResult<PaymentRecord> recordPayment(
            long invoiceId, String method, String amountRaw, String referenceRaw, String note, String staffName) {

        List<String> errors = new ArrayList<>();
        Optional<InvoiceRecord> invoiceOpt = repository.findInvoice(invoiceId);
        if (invoiceOpt.isEmpty()) return OperationResult.failure("Invoice could not be found.");
        InvoiceRecord invoice = invoiceOpt.get();

        if (invoice.isCancelled()) return OperationResult.failure("This invoice has been cancelled.");
        if (invoice.isFullyPaid()) return OperationResult.failure("This invoice is already fully paid.");

        String cleanMethod = method == null ? "" : method.trim().toUpperCase();
        if (!PAYMENT_METHODS.contains(cleanMethod)) errors.add("Select a valid payment method.");

        BigDecimal amount = parsePositive(amountRaw, "Payment amount", errors);
        BigDecimal balance = invoice.getBalance();
        if (errors.isEmpty() && amount.compareTo(balance) > 0) {
            errors.add("Payment of LKR " + String.format("%,.2f", amount)
                    + " exceeds the outstanding balance of LKR " + String.format("%,.2f", balance) + ".");
        }

        String reference = referenceRaw == null ? "" : referenceRaw.trim();
        if (reference.isEmpty()) {
            reference = repository.nextPaymentReference();
        } else if (repository.findPaymentByReference(reference).isPresent()) {
            errors.add("Payment reference \"" + reference + "\" has already been recorded; duplicate settlement blocked.");
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        PaymentRecord payment = new PaymentRecord(
                repository.nextPaymentId(), invoiceId, reference, cleanMethod, amount,
                "SUCCESS", java.time.LocalDateTime.now(), staffName,
                note == null ? "" : note.trim(), null);
        repository.savePayment(payment);

        invoice.applyPayment(amount, "Payment " + reference + " verified.", staffName);
        repository.saveInvoice(invoice);

        return OperationResult.success(payment);
    }

    /**
     * Voids/refunds a previously verified payment (A4 / FR23). Requires a reason and marks
     * the original payment REFUNDED rather than deleting it, keeping the audit trail intact.
     */
    public synchronized OperationResult<Void> voidPayment(long paymentId, String reason, String staffName) {
        if (reason == null || reason.trim().isEmpty()) {
            return OperationResult.failure("A reason is required to void or refund a payment.");
        }
        Optional<PaymentRecord> paymentOpt = repository.findPayment(paymentId);
        if (paymentOpt.isEmpty()) return OperationResult.failure("Payment could not be found.");
        PaymentRecord payment = paymentOpt.get();
        if (!"SUCCESS".equals(payment.getStatus())) {
            return OperationResult.failure("Only a verified payment can be voided or refunded.");
        }
        Optional<InvoiceRecord> invoiceOpt = repository.findInvoice(payment.getInvoiceId());
        if (invoiceOpt.isEmpty()) return OperationResult.failure("The linked invoice could not be found.");
        InvoiceRecord invoice = invoiceOpt.get();

        payment.markRefunded(reason.trim());
        repository.savePayment(payment);

        invoice.reversePayment(payment.getAmount(),
                "Payment " + payment.getPaymentReference() + " refunded: " + reason.trim(), staffName);
        repository.saveInvoice(invoice);

        return OperationResult.success(null);
    }

    // ------------------------------------------------------------- promotions

    public List<PromotionRecord> allPromotions() {
        return repository.findAllPromotions();
    }

    public Optional<PromotionRecord> promotion(long id) {
        return repository.findPromotion(id);
    }

    public List<PromotionUsageRecord> usageFor(long promotionId) {
        return repository.findUsageForPromotion(promotionId);
    }

    public int usageCount(long promotionId) {
        return repository.countPromotionUsage(promotionId);
    }

    /** Creates a new promotion (id == 0) or updates an existing one. */
    public OperationResult<PromotionRecord> savePromotion(
            long id, String code, String name, String discountType, String discountValueRaw,
            String minimumSpendRaw, String startDateRaw, String endDateRaw, String usageLimitRaw, boolean active) {

        List<String> errors = new ArrayList<>();
        String cleanCode = code == null ? "" : code.trim().toUpperCase();
        String cleanName = name == null ? "" : name.trim();
        String cleanType = DISCOUNT_TYPES.contains(discountType) ? discountType : null;

        if (cleanCode.isEmpty()) errors.add("Promotion code is required.");
        if (cleanName.isEmpty()) errors.add("Promotion name is required.");
        if (cleanType == null) errors.add("Select a valid discount type.");

        BigDecimal discountValue = parsePositive(discountValueRaw, "Discount value", errors);
        if ("PERCENTAGE".equals(cleanType) && discountValue.compareTo(new BigDecimal("100")) > 0) {
            errors.add("A percentage discount cannot exceed 100.");
        }
        BigDecimal minimumSpend = parseNonNegative(
                minimumSpendRaw == null || minimumSpendRaw.trim().isEmpty() ? "0" : minimumSpendRaw,
                "Minimum spend", errors);

        LocalDate startDate = parseDate(startDateRaw, "Start date", errors);
        LocalDate endDate = parseDate(endDateRaw, "End date", errors);
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            errors.add("End date cannot be before the start date.");
        }

        Integer usageLimit = null;
        if (usageLimitRaw != null && !usageLimitRaw.trim().isEmpty()) {
            try {
                usageLimit = Integer.parseInt(usageLimitRaw.trim());
                if (usageLimit <= 0) errors.add("Usage limit must be greater than zero.");
            } catch (NumberFormatException ex) {
                errors.add("Usage limit must be a whole number.");
            }
        }

        Optional<PromotionRecord> existingByCode = repository.findPromotionByCode(cleanCode);
        if (existingByCode.isPresent() && existingByCode.get().getId() != id) {
            errors.add("Another promotion already uses the code \"" + cleanCode + "\".");
        }

        if (!errors.isEmpty()) return OperationResult.failure(errors);

        PromotionRecord promotion;
        if (id <= 0) {
            promotion = new PromotionRecord(repository.nextPromotionId(), cleanCode, cleanName, cleanType,
                    discountValue, minimumSpend, startDate, endDate, usageLimit, active);
        } else {
            Optional<PromotionRecord> existing = repository.findPromotion(id);
            if (existing.isEmpty()) return OperationResult.failure("Promotion could not be found.");
            promotion = existing.get();
            promotion.setCode(cleanCode);
            promotion.setName(cleanName);
            promotion.setDiscountType(cleanType);
            promotion.setDiscountValue(discountValue);
            promotion.setMinimumSpend(minimumSpend);
            promotion.setStartDate(startDate);
            promotion.setEndDate(endDate);
            promotion.setUsageLimit(usageLimit);
            promotion.setActive(active);
        }
        return OperationResult.success(repository.savePromotion(promotion));
    }

    /** Deletes a promotion only once it has never been used, preserving discount audit history. */
    public OperationResult<Void> deletePromotion(long id) {
        if (repository.countPromotionUsage(id) > 0) {
            return OperationResult.failure(
                    "This promotion has recorded usage history and cannot be deleted; deactivate it instead.");
        }
        if (!repository.deletePromotion(id)) return OperationResult.failure("Promotion could not be found.");
        return OperationResult.success(null);
    }

    // ---------------------------------------------------------------- reports

    public FinanceSummary financeSummary() {
        BigDecimal invoiced = BigDecimal.ZERO;
        BigDecimal collected = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        long paidCount = 0, openCount = 0, cancelledCount = 0;
        for (InvoiceRecord invoice : repository.findAllInvoices()) {
            if (invoice.isCancelled()) { cancelledCount++; continue; }
            invoiced = invoiced.add(invoice.getTotalAmount());
            collected = collected.add(invoice.getAmountPaid());
            outstanding = outstanding.add(invoice.getBalance());
            if (invoice.isFullyPaid()) paidCount++; else openCount++;
        }
        return new FinanceSummary(invoiced, collected, outstanding, paidCount, openCount, cancelledCount);
    }

    public static final class FinanceSummary {
        public final BigDecimal totalInvoiced;
        public final BigDecimal totalCollected;
        public final BigDecimal totalOutstanding;
        public final long paidCount;
        public final long openCount;
        public final long cancelledCount;

        FinanceSummary(BigDecimal totalInvoiced, BigDecimal totalCollected, BigDecimal totalOutstanding,
                       long paidCount, long openCount, long cancelledCount) {
            this.totalInvoiced = totalInvoiced.setScale(2, RoundingMode.HALF_UP);
            this.totalCollected = totalCollected.setScale(2, RoundingMode.HALF_UP);
            this.totalOutstanding = totalOutstanding.setScale(2, RoundingMode.HALF_UP);
            this.paidCount = paidCount;
            this.openCount = openCount;
            this.cancelledCount = cancelledCount;
        }

        public String getTotalInvoicedDisplay() { return "LKR " + String.format("%,.2f", totalInvoiced); }
        public String getTotalCollectedDisplay() { return "LKR " + String.format("%,.2f", totalCollected); }
        public String getTotalOutstandingDisplay() { return "LKR " + String.format("%,.2f", totalOutstanding); }
    }

    // ------------------------------------------------------------------ utils

    private String value(String[] array, int index, String fallback) {
        if (array == null || index >= array.length || array[index] == null || array[index].trim().isEmpty()) {
            return fallback;
        }
        return array[index].trim();
    }

    private BigDecimal parseNonNegative(String raw, String label, List<String> errors) {
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(label + " cannot be negative.");
                return BigDecimal.ZERO;
            }
            return value;
        } catch (Exception ex) {
            errors.add(label + " must be a valid number.");
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal parsePositive(String raw, String label, List<String> errors) {
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(label + " must be greater than zero.");
                return BigDecimal.ZERO;
            }
            return value;
        } catch (Exception ex) {
            errors.add(label + " must be a valid number.");
            return BigDecimal.ZERO;
        }
    }

    private LocalDate parseDate(String raw, String label, List<String> errors) {
        if (raw == null || raw.trim().isEmpty()) {
            errors.add(label + " is required.");
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception ex) {
            errors.add(label + " must be a valid date.");
            return null;
        }
    }
}
