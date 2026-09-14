package com.dinevista.repository;

import com.dinevista.model.InvoiceRecord;
import com.dinevista.model.PaymentRecord;
import com.dinevista.model.PromotionRecord;
import com.dinevista.model.PromotionUsageRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory {@link BillingRepository} used in the default demo
 * storage mode. Seeded with a couple of representative promotions so the
 * discount workflow is demonstrable immediately.
 */
public class InMemoryBillingRepository implements BillingRepository {
    private final Map<Long, InvoiceRecord> invoices = new ConcurrentHashMap<>();
    private final Map<Long, PaymentRecord> payments = new ConcurrentHashMap<>();
    private final Map<Long, PromotionRecord> promotions = new ConcurrentHashMap<>();
    private final Map<Long, PromotionUsageRecord> promotionUsage = new ConcurrentHashMap<>();

    private final AtomicLong invoiceSequence = new AtomicLong(0);
    private final AtomicLong invoiceItemSequence = new AtomicLong(0);
    private final AtomicLong paymentSequence = new AtomicLong(0);
    private final AtomicLong promotionSequence = new AtomicLong(0);
    private final AtomicLong promotionUsageSequence = new AtomicLong(0);

    public InMemoryBillingRepository() {
        seedPromotions();
    }

    private void seedPromotions() {
        seedPromotion("WELCOME10", "New customer welcome discount", "PERCENTAGE",
                "10", "0", LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(6), 200, true);
        seedPromotion("EVENT500", "Flat event booking discount", "FIXED_AMOUNT",
                "500", "5000", LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(6), 100, true);
    }

    private void seedPromotion(String code, String name, String type, String value, String minSpend,
                                LocalDate start, LocalDate end, int limit, boolean active) {
        long id = nextPromotionId();
        promotions.put(id, new PromotionRecord(id, code, name, type, new BigDecimal(value),
                new BigDecimal(minSpend), start, end, limit, active));
    }

    @Override
    public List<InvoiceRecord> findAllInvoices() {
        List<InvoiceRecord> list = new ArrayList<>(invoices.values());
        list.sort(Comparator.comparing(InvoiceRecord::getCreatedAt).reversed());
        return list;
    }

    @Override
    public Optional<InvoiceRecord> findInvoice(long id) {
        return Optional.ofNullable(invoices.get(id));
    }

    @Override
    public Optional<InvoiceRecord> findInvoiceByNumber(String invoiceNumber) {
        return invoices.values().stream()
                .filter(i -> i.getInvoiceNumber().equalsIgnoreCase(invoiceNumber))
                .findFirst();
    }

    @Override
    public Optional<InvoiceRecord> findInvoiceBySource(String sourceType, String sourceReference) {
        if (sourceReference == null || sourceReference.isEmpty()) return Optional.empty();
        return invoices.values().stream()
                .filter(i -> sourceType.equals(i.getSourceType())
                        && sourceReference.equalsIgnoreCase(i.getSourceReference())
                        && !i.isCancelled())
                .findFirst();
    }

    @Override
    public List<InvoiceRecord> findInvoicesForCustomer(String customerKey) {
        List<InvoiceRecord> list = new ArrayList<>();
        for (InvoiceRecord invoice : invoices.values()) {
            if (invoice.getCustomerKey().equalsIgnoreCase(customerKey)) list.add(invoice);
        }
        list.sort(Comparator.comparing(InvoiceRecord::getCreatedAt).reversed());
        return list;
    }

    @Override
    public InvoiceRecord saveInvoice(InvoiceRecord invoice) {
        invoices.put(invoice.getId(), invoice);
        return invoice;
    }

    @Override
    public boolean deleteInvoice(long id) {
        InvoiceRecord removed = invoices.remove(id);
        if (removed != null) {
            payments.entrySet().removeIf(e -> e.getValue().getInvoiceId() == id);
            return true;
        }
        return false;
    }

    @Override
    public long nextInvoiceId() { return invoiceSequence.incrementAndGet(); }

    @Override
    public long nextInvoiceItemId() { return invoiceItemSequence.incrementAndGet(); }

    @Override
    public String nextInvoiceNumber() {
        return "DV-INV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    @Override
    public List<PaymentRecord> findPaymentsForInvoice(long invoiceId) {
        List<PaymentRecord> list = new ArrayList<>();
        for (PaymentRecord payment : payments.values()) {
            if (payment.getInvoiceId() == invoiceId) list.add(payment);
        }
        list.sort(Comparator.comparing(PaymentRecord::getCreatedAt).reversed());
        return list;
    }

    @Override
    public Optional<PaymentRecord> findPayment(long id) {
        return Optional.ofNullable(payments.get(id));
    }

    @Override
    public Optional<PaymentRecord> findPaymentByReference(String reference) {
        return payments.values().stream()
                .filter(p -> p.getPaymentReference().equalsIgnoreCase(reference))
                .findFirst();
    }

    @Override
    public PaymentRecord savePayment(PaymentRecord payment) {
        payments.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public long nextPaymentId() { return paymentSequence.incrementAndGet(); }

    @Override
    public String nextPaymentReference() {
        return "DV-PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    @Override
    public List<PromotionRecord> findAllPromotions() {
        List<PromotionRecord> list = new ArrayList<>(promotions.values());
        list.sort(Comparator.comparing(PromotionRecord::getCode, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    @Override
    public Optional<PromotionRecord> findPromotion(long id) {
        return Optional.ofNullable(promotions.get(id));
    }

    @Override
    public Optional<PromotionRecord> findPromotionByCode(String code) {
        if (code == null) return Optional.empty();
        return promotions.values().stream()
                .filter(p -> p.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public PromotionRecord savePromotion(PromotionRecord promotion) {
        promotions.put(promotion.getId(), promotion);
        return promotion;
    }

    @Override
    public boolean deletePromotion(long id) {
        return promotions.remove(id) != null;
    }

    @Override
    public long nextPromotionId() { return promotionSequence.incrementAndGet(); }

    @Override
    public int countPromotionUsage(long promotionId) {
        int count = 0;
        for (PromotionUsageRecord usage : promotionUsage.values()) {
            if (usage.getPromotionId() == promotionId) count++;
        }
        return count;
    }

    @Override
    public List<PromotionUsageRecord> findUsageForPromotion(long promotionId) {
        List<PromotionUsageRecord> list = new ArrayList<>();
        for (PromotionUsageRecord usage : promotionUsage.values()) {
            if (usage.getPromotionId() == promotionId) list.add(usage);
        }
        list.sort(Comparator.comparing(PromotionUsageRecord::getUsedAt).reversed());
        return list;
    }

    @Override
    public PromotionUsageRecord savePromotionUsage(PromotionUsageRecord usage) {
        promotionUsage.put(usage.getId(), usage);
        return usage;
    }

    @Override
    public long nextPromotionUsageId() { return promotionUsageSequence.incrementAndGet(); }
}
