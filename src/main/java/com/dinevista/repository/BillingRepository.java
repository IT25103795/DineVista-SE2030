package com.dinevista.repository;

import com.dinevista.model.InvoiceRecord;
import com.dinevista.model.PaymentRecord;
import com.dinevista.model.PromotionRecord;
import com.dinevista.model.PromotionUsageRecord;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for the Billing, Promotions & Discounts module
 * (Nawarathna N. M. I. N. / IT25103797). An in-memory implementation is
 * provided for the default demo runtime mode; {@link JdbcBillingRepository}
 * persists against the {@code invoice}, {@code invoice_item}, {@code payment},
 * {@code promotion} and {@code promotion_usage} tables in database/schema.sql
 * when MySQL mode is enabled, following the same pattern as
 * {@link JdbcInventoryRepository}.
 */
public interface BillingRepository {

    List<InvoiceRecord> findAllInvoices();
    Optional<InvoiceRecord> findInvoice(long id);
    Optional<InvoiceRecord> findInvoiceByNumber(String invoiceNumber);
    Optional<InvoiceRecord> findInvoiceBySource(String sourceType, String sourceReference);
    List<InvoiceRecord> findInvoicesForCustomer(String customerKey);
    InvoiceRecord saveInvoice(InvoiceRecord invoice);
    boolean deleteInvoice(long id);
    long nextInvoiceId();
    long nextInvoiceItemId();
    String nextInvoiceNumber();

    List<PaymentRecord> findPaymentsForInvoice(long invoiceId);
    Optional<PaymentRecord> findPayment(long id);
    Optional<PaymentRecord> findPaymentByReference(String reference);
    PaymentRecord savePayment(PaymentRecord payment);
    long nextPaymentId();
    String nextPaymentReference();

    List<PromotionRecord> findAllPromotions();
    Optional<PromotionRecord> findPromotion(long id);
    Optional<PromotionRecord> findPromotionByCode(String code);
    PromotionRecord savePromotion(PromotionRecord promotion);
    boolean deletePromotion(long id);
    long nextPromotionId();

    int countPromotionUsage(long promotionId);
    List<PromotionUsageRecord> findUsageForPromotion(long promotionId);
    PromotionUsageRecord savePromotionUsage(PromotionUsageRecord usage);
    long nextPromotionUsageId();
}
