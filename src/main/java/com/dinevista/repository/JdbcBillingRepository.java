package com.dinevista.repository;

import com.dinevista.model.InvoiceItemRecord;
import com.dinevista.model.InvoiceRecord;
import com.dinevista.model.PaymentRecord;
import com.dinevista.model.PromotionRecord;
import com.dinevista.model.PromotionUsageRecord;
import com.dinevista.model.StatusHistoryRecord;
import com.dinevista.util.DatabaseConfig;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MySQL-backed {@link BillingRepository} for the Billing, Promotions &amp;
 * Discounts module (Nawarathna N. M. I. N. / IT25103797). Persists against
 * the {@code invoice}, {@code invoice_item}, {@code payment}, {@code promotion}
 * and {@code promotion_usage} tables in database/schema.sql, following the
 * same connection and error-handling pattern as {@link JdbcInventoryRepository}.
 *
 * The base schema models {@code invoice.customer_id} as a foreign key into
 * {@code customer_profile}, but the demo login system has no numeric user id
 * backing a session. So — exactly like {@code stock_transaction.performed_by_name}
 * — a small set of extra, demo-friendly columns are added automatically on
 * startup if the database was created from an older copy of schema.sql:
 * a text customer key/name/email on {@code invoice}, an audit note and
 * verifier name on {@code payment}, and an invoice/customer-key link on
 * {@code promotion_usage}.
 *
 * Invoice status changes are persisted in invoice_status_history so reloads
 * retain the finance audit trail.
 */
public class JdbcBillingRepository implements BillingRepository {
    private final DatabaseConfig config;

    public JdbcBillingRepository(DatabaseConfig config) throws SQLException {
        this.config = config;
        try (Connection ignored = config.openConnection()) {
            // Fail early so the application can safely fall back to memory mode.
        }
        ensureColumn("invoice", "source_reference", "VARCHAR(30)");
        ensureColumn("invoice", "customer_key", "VARCHAR(190)");
        ensureColumn("invoice", "customer_name", "VARCHAR(160)");
        ensureColumn("invoice", "customer_email", "VARCHAR(160)");
        ensureColumn("invoice", "promotion_code", "VARCHAR(40)");
        ensureColumn("invoice", "notes", "VARCHAR(500)");
        ensureColumn("invoice", "amount_paid", "DECIMAL(14,2) NOT NULL DEFAULT 0");
        ensureColumn("invoice", "created_by_name", "VARCHAR(160)");
        ensureColumn("invoice", "created_at", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        ensureColumn("invoice", "updated_at", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        ensureColumn("payment", "note", "VARCHAR(255)");
        ensureColumn("payment", "verified_by_name", "VARCHAR(160)");
        ensureColumn("payment", "created_at", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        ensureColumn("promotion_usage", "invoice_id", "BIGINT");
        ensureColumn("promotion_usage", "promotion_code", "VARCHAR(40)");
        ensureColumn("promotion_usage", "customer_key", "VARCHAR(190)");
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS invoice_status_history ("
                             + "history_id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                             + "invoice_id BIGINT NOT NULL,status VARCHAR(40) NOT NULL,"
                             + "note VARCHAR(500),changed_by VARCHAR(160),"
                             + "changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                             + "CONSTRAINT fk_invoice_history_invoice FOREIGN KEY (invoice_id) "
                             + "REFERENCES invoice(invoice_id) ON DELETE CASCADE) ENGINE=InnoDB")) {
            statement.executeUpdate();
        }
    }

    private void ensureColumn(String table, String column, String ddlType) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(checkSql)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next() && rows.getInt(1) > 0) return;
            }
        }
        String alterSql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + ddlType;
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(alterSql)) {
            statement.executeUpdate();
        }
    }

    // ---------------------------------------------------------------- invoices

    @Override
    public List<InvoiceRecord> findAllInvoices() {
        return loadInvoices("ORDER BY i.created_at DESC");
    }

    @Override
    public Optional<InvoiceRecord> findInvoice(long id) {
        List<InvoiceRecord> list = loadInvoices("WHERE i.invoice_id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<InvoiceRecord> findInvoiceByNumber(String invoiceNumber) {
        List<InvoiceRecord> list = loadInvoices("WHERE i.invoice_number = ?", invoiceNumber);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<InvoiceRecord> findInvoiceBySource(String sourceType, String sourceReference) {
        List<InvoiceRecord> list = loadInvoices(
                "WHERE i.invoice_type = ? AND i.source_reference = ? AND i.invoice_status <> 'CANCELLED'",
                sourceType, sourceReference);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<InvoiceRecord> findInvoicesForCustomer(String customerKey) {
        return loadInvoices("WHERE i.customer_key = ? ORDER BY i.created_at DESC", customerKey);
    }

    private List<InvoiceRecord> loadInvoices(String whereAndOrder, Object... params) {
        String sql = "SELECT i.invoice_id, i.invoice_number, i.invoice_type, i.source_reference, "
                + "i.customer_key, i.customer_name, i.customer_email, i.issue_date, i.due_date, "
                + "i.subtotal, i.tax_amount, i.discount_amount, i.total_amount, i.amount_paid, "
                + "i.invoice_status, i.promotion_code, i.notes, i.created_by_name, i.created_at, i.updated_at "
                + "FROM invoice i " + whereAndOrder;
        List<InvoiceRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapInvoice(connection, rows));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load invoices.", ex);
        }
        return result;
    }

    private InvoiceRecord mapInvoice(Connection connection, ResultSet rows) throws SQLException {
        long invoiceId = rows.getLong("invoice_id");
        List<InvoiceItemRecord> items = loadItems(connection, invoiceId);
        Date issue = rows.getDate("issue_date");
        Date due = rows.getDate("due_date");
        Timestamp created = rows.getTimestamp("created_at");
        Timestamp updated = rows.getTimestamp("updated_at");
        return new InvoiceRecord(
                invoiceId,
                rows.getString("invoice_number"),
                rows.getString("invoice_type"),
                rows.getString("source_reference"),
                rows.getString("customer_key"),
                rows.getString("customer_name"),
                rows.getString("customer_email"),
                issue == null ? LocalDate.now() : issue.toLocalDate(),
                due == null ? null : due.toLocalDate(),
                items,
                rows.getBigDecimal("subtotal"),
                rows.getBigDecimal("tax_amount"),
                rows.getBigDecimal("discount_amount"),
                rows.getBigDecimal("total_amount"),
                rows.getBigDecimal("amount_paid"),
                rows.getString("invoice_status"),
                rows.getString("promotion_code"),
                rows.getString("notes"),
                rows.getString("created_by_name"),
                created == null ? LocalDateTime.now() : created.toLocalDateTime(),
                updated == null ? null : updated.toLocalDateTime(),
                loadHistory(connection, invoiceId));
    }

    private List<StatusHistoryRecord> loadHistory(Connection connection, long invoiceId) throws SQLException {
        List<StatusHistoryRecord> history = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT status,note,changed_by,changed_at FROM invoice_status_history "
                        + "WHERE invoice_id=? ORDER BY history_id DESC")) {
            statement.setLong(1, invoiceId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) history.add(new StatusHistoryRecord(rows.getString("status"),
                        rows.getString("note"), rows.getString("changed_by"),
                        rows.getTimestamp("changed_at").toLocalDateTime()));
            }
        }
        return history;
    }

    private List<InvoiceItemRecord> loadItems(Connection connection, long invoiceId) throws SQLException {
        String sql = "SELECT invoice_item_id, description, quantity, unit_price "
                + "FROM invoice_item WHERE invoice_id = ? ORDER BY invoice_item_id";
        List<InvoiceItemRecord> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, invoiceId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    items.add(new InvoiceItemRecord(
                            rows.getLong("invoice_item_id"),
                            rows.getString("description"),
                            rows.getBigDecimal("quantity"),
                            rows.getBigDecimal("unit_price")));
                }
            }
        }
        return items;
    }

    @Override
    public InvoiceRecord saveInvoice(InvoiceRecord invoice) {
        String existsSql = "SELECT invoice_id FROM invoice WHERE invoice_id = ?";
        try (Connection connection = config.openConnection()) {
            connection.setAutoCommit(false);
            try {
            boolean exists;
            try (PreparedStatement statement = connection.prepareStatement(existsSql)) {
                statement.setLong(1, invoice.getId());
                try (ResultSet rows = statement.executeQuery()) {
                    exists = rows.next();
                }
            }
            if (exists) {
                updateInvoice(connection, invoice);
            } else {
                insertInvoice(connection, invoice);
            }
            replaceItems(connection, invoice);
            saveNewHistory(connection, invoice);
            connection.commit();
            return invoice;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to save invoice.", ex);
        }
    }

    private void saveNewHistory(Connection connection, InvoiceRecord invoice) throws SQLException {
        int persisted;
        try (PreparedStatement count = connection.prepareStatement(
                "SELECT COUNT(*) FROM invoice_status_history WHERE invoice_id=?")) {
            count.setLong(1, invoice.getId());
            try (ResultSet rows = count.executeQuery()) { rows.next(); persisted = rows.getInt(1); }
        }
        List<StatusHistoryRecord> history = invoice.getHistory();
        int newCount = history.size() - persisted;
        if (newCount <= 0) return;
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO invoice_status_history(invoice_id,status,note,changed_by,changed_at) "
                        + "VALUES (?,?,?,?,?)")) {
            for (int i = newCount - 1; i >= 0; i--) {
                StatusHistoryRecord entry = history.get(i);
                insert.setLong(1, invoice.getId());
                insert.setString(2, entry.getStatus());
                insert.setString(3, entry.getNote());
                insert.setString(4, entry.getChangedBy());
                insert.setTimestamp(5, Timestamp.valueOf(entry.getChangedAt()));
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    @Override
    public boolean deleteInvoice(long id) {
        try (Connection connection = config.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM invoice_item WHERE invoice_id = ?")) {
                    statement.setLong(1, id);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM payment WHERE invoice_id = ?")) {
                    statement.setLong(1, id);
                    statement.executeUpdate();
                }

                int rowsDeleted;
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM invoice WHERE invoice_id = ?")) {
                    statement.setLong(1, id);
                    rowsDeleted = statement.executeUpdate();
                }

                connection.commit();
                return rowsDeleted > 0;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to delete invoice.", ex);
        }
    }

    private void insertInvoice(Connection connection, InvoiceRecord invoice) throws SQLException {
        String sql = "INSERT INTO invoice (invoice_id, invoice_number, invoice_type, source_reference, "
                + "customer_key, customer_name, customer_email, issue_date, due_date, subtotal, tax_amount, "
                + "discount_amount, total_amount, amount_paid, invoice_status, promotion_code, notes, "
                + "created_by_name, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, invoice.getId());
            bindInvoiceFields(statement, invoice, 2);
            statement.executeUpdate();
        }
    }

    private void updateInvoice(Connection connection, InvoiceRecord invoice) throws SQLException {
        String sql = "UPDATE invoice SET invoice_number=?, invoice_type=?, source_reference=?, customer_key=?, "
                + "customer_name=?, customer_email=?, issue_date=?, due_date=?, subtotal=?, tax_amount=?, "
                + "discount_amount=?, total_amount=?, amount_paid=?, invoice_status=?, promotion_code=?, notes=?, "
                + "created_by_name=?, created_at=?, updated_at=? WHERE invoice_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = bindInvoiceFields(statement, invoice, 1);
            statement.setLong(index, invoice.getId());
            statement.executeUpdate();
        }
    }

    private int bindInvoiceFields(PreparedStatement statement, InvoiceRecord invoice, int start) throws SQLException {
        int i = start;
        statement.setString(i++, invoice.getInvoiceNumber());
        statement.setString(i++, invoice.getSourceType());
        statement.setString(i++, invoice.getSourceReference());
        statement.setString(i++, invoice.getCustomerKey());
        statement.setString(i++, invoice.getCustomerName());
        statement.setString(i++, invoice.getCustomerEmail());
        statement.setDate(i++, Date.valueOf(invoice.getIssueDate()));
        statement.setDate(i++, invoice.getDueDate() == null ? null : Date.valueOf(invoice.getDueDate()));
        statement.setBigDecimal(i++, invoice.getSubtotal());
        statement.setBigDecimal(i++, invoice.getTaxAmount());
        statement.setBigDecimal(i++, invoice.getDiscountAmount());
        statement.setBigDecimal(i++, invoice.getTotalAmount());
        statement.setBigDecimal(i++, invoice.getAmountPaid());
        statement.setString(i++, invoice.getStatus());
        statement.setString(i++, invoice.getPromotionCode());
        statement.setString(i++, invoice.getNotes());
        statement.setString(i++, invoice.getCreatedBy());
        statement.setTimestamp(i++, Timestamp.valueOf(invoice.getCreatedAt()));
        statement.setTimestamp(i++, Timestamp.valueOf(invoice.getUpdatedAt()));
        return i;
    }

    private void replaceItems(Connection connection, InvoiceRecord invoice) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM invoice_item WHERE invoice_id = ?")) {
            delete.setLong(1, invoice.getId());
            delete.executeUpdate();
        }
        String insertSql = "INSERT INTO invoice_item (invoice_item_id, invoice_id, description, quantity, "
                + "unit_price, line_total) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
            for (InvoiceItemRecord item : invoice.getItems()) {
                long itemId = item.getId() > 0 ? item.getId() : nextInvoiceItemId();
                insert.setLong(1, itemId);
                insert.setLong(2, invoice.getId());
                insert.setString(3, item.getDescription());
                insert.setBigDecimal(4, item.getQuantity());
                insert.setBigDecimal(5, item.getUnitPrice());
                insert.setBigDecimal(6, item.getLineTotal());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    @Override
    public long nextInvoiceId() { return nextId("invoice", "invoice_id"); }

    @Override
    public long nextInvoiceItemId() { return nextId("invoice_item", "invoice_item_id"); }

    @Override
    public String nextInvoiceNumber() {
        return "DV-INV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    // ----------------------------------------------------------------- payments

    @Override
    public List<PaymentRecord> findPaymentsForInvoice(long invoiceId) {
        return loadPayments("WHERE invoice_id = ? ORDER BY created_at DESC", invoiceId);
    }

    @Override
    public Optional<PaymentRecord> findPayment(long id) {
        List<PaymentRecord> list = loadPayments("WHERE payment_id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<PaymentRecord> findPaymentByReference(String reference) {
        List<PaymentRecord> list = loadPayments("WHERE payment_reference = ?", reference);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private List<PaymentRecord> loadPayments(String whereAndOrder, Object... params) {
        String sql = "SELECT payment_id, invoice_id, payment_reference, payment_method, amount, "
                + "payment_status, paid_at, verified_by_name, note, created_at FROM payment " + whereAndOrder;
        List<PaymentRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapPayment(rows));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load payments.", ex);
        }
        return result;
    }

    private PaymentRecord mapPayment(ResultSet rows) throws SQLException {
        Timestamp paidAt = rows.getTimestamp("paid_at");
        Timestamp created = rows.getTimestamp("created_at");
        return new PaymentRecord(
                rows.getLong("payment_id"),
                rows.getLong("invoice_id"),
                rows.getString("payment_reference"),
                rows.getString("payment_method"),
                rows.getBigDecimal("amount"),
                rows.getString("payment_status"),
                paidAt == null ? null : paidAt.toLocalDateTime(),
                rows.getString("verified_by_name"),
                rows.getString("note"),
                created == null ? LocalDateTime.now() : created.toLocalDateTime());
    }

    @Override
    public PaymentRecord savePayment(PaymentRecord payment) {
        String existsSql = "SELECT payment_id FROM payment WHERE payment_id = ?";
        try (Connection connection = config.openConnection()) {
            boolean exists;
            try (PreparedStatement statement = connection.prepareStatement(existsSql)) {
                statement.setLong(1, payment.getId());
                try (ResultSet rows = statement.executeQuery()) {
                    exists = rows.next();
                }
            }
            if (exists) {
                String sql = "UPDATE payment SET invoice_id=?, payment_reference=?, payment_method=?, amount=?, "
                        + "payment_status=?, paid_at=?, verified_by_name=?, note=? WHERE payment_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    bindPayment(statement, payment, 1);
                    statement.setLong(9, payment.getId());
                    statement.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO payment (payment_id, invoice_id, payment_reference, payment_method, "
                        + "amount, payment_status, paid_at, verified_by_name, note, created_at) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, payment.getId());
                    bindPayment(statement, payment, 2);
                    statement.setTimestamp(10, Timestamp.valueOf(payment.getCreatedAt()));
                    statement.executeUpdate();
                }
            }
            return payment;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to save payment.", ex);
        }
    }

    private void bindPayment(PreparedStatement statement, PaymentRecord payment, int start) throws SQLException {
        int i = start;
        statement.setLong(i++, payment.getInvoiceId());
        statement.setString(i++, payment.getPaymentReference());
        statement.setString(i++, payment.getPaymentMethod());
        statement.setBigDecimal(i++, payment.getAmount());
        statement.setString(i++, payment.getStatus());
        statement.setTimestamp(i++, payment.getPaidAt() == null ? null : Timestamp.valueOf(payment.getPaidAt()));
        statement.setString(i++, payment.getVerifiedBy());
        statement.setString(i, payment.getNote());
    }

    @Override
    public long nextPaymentId() { return nextId("payment", "payment_id"); }

    @Override
    public String nextPaymentReference() {
        return "DV-PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    // --------------------------------------------------------------- promotions

    @Override
    public List<PromotionRecord> findAllPromotions() {
        return loadPromotions("ORDER BY promotion_code");
    }

    @Override
    public Optional<PromotionRecord> findPromotion(long id) {
        List<PromotionRecord> list = loadPromotions("WHERE promotion_id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<PromotionRecord> findPromotionByCode(String code) {
        List<PromotionRecord> list = loadPromotions("WHERE promotion_code = ?", code);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private List<PromotionRecord> loadPromotions(String whereAndOrder, Object... params) {
        String sql = "SELECT promotion_id, promotion_code, promotion_name, discount_type, discount_value, "
                + "minimum_spend, start_date, end_date, usage_limit, is_active FROM promotion " + whereAndOrder;
        List<PromotionRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, params);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(mapPromotion(rows));
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load promotions.", ex);
        }
        return result;
    }

    private PromotionRecord mapPromotion(ResultSet rows) throws SQLException {
        Date start = rows.getDate("start_date");
        Date end = rows.getDate("end_date");
        int usageLimit = rows.getInt("usage_limit");
        return new PromotionRecord(
                rows.getLong("promotion_id"),
                rows.getString("promotion_code"),
                rows.getString("promotion_name"),
                rows.getString("discount_type"),
                rows.getBigDecimal("discount_value"),
                rows.getBigDecimal("minimum_spend"),
                start == null ? null : start.toLocalDate(),
                end == null ? null : end.toLocalDate(),
                rows.wasNull() ? null : usageLimit,
                rows.getBoolean("is_active"));
    }

    @Override
    public PromotionRecord savePromotion(PromotionRecord promotion) {
        String existsSql = "SELECT promotion_id FROM promotion WHERE promotion_id = ?";
        try (Connection connection = config.openConnection()) {
            boolean exists;
            try (PreparedStatement statement = connection.prepareStatement(existsSql)) {
                statement.setLong(1, promotion.getId());
                try (ResultSet rows = statement.executeQuery()) {
                    exists = rows.next();
                }
            }
            if (exists) {
                String sql = "UPDATE promotion SET promotion_code=?, promotion_name=?, discount_type=?, "
                        + "discount_value=?, minimum_spend=?, start_date=?, end_date=?, usage_limit=?, "
                        + "is_active=? WHERE promotion_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    bindPromotion(statement, promotion);
                    statement.setLong(10, promotion.getId());
                    statement.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO promotion (promotion_id, promotion_code, promotion_name, discount_type, "
                        + "discount_value, minimum_spend, start_date, end_date, usage_limit, is_active) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, promotion.getId());
                    bindPromotion(statement, promotion, 2);
                    statement.executeUpdate();
                }
            }
            return promotion;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to save promotion.", ex);
        }
    }

    private void bindPromotion(PreparedStatement statement, PromotionRecord promotion) throws SQLException {
        bindPromotion(statement, promotion, 1);
    }

    private void bindPromotion(PreparedStatement statement, PromotionRecord promotion, int start) throws SQLException {
        int i = start;
        statement.setString(i++, promotion.getCode());
        statement.setString(i++, promotion.getName());
        statement.setString(i++, promotion.getDiscountType());
        statement.setBigDecimal(i++, promotion.getDiscountValue());
        statement.setBigDecimal(i++, promotion.getMinimumSpend());
        statement.setDate(i++, promotion.getStartDate() == null ? null : Date.valueOf(promotion.getStartDate()));
        statement.setDate(i++, promotion.getEndDate() == null ? null : Date.valueOf(promotion.getEndDate()));
        if (promotion.getUsageLimit() == null) statement.setNull(i++, java.sql.Types.INTEGER);
        else statement.setInt(i++, promotion.getUsageLimit());
        statement.setBoolean(i, promotion.isActive());
    }

    @Override
    public boolean deletePromotion(long id) {
        String sql = "DELETE FROM promotion WHERE promotion_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to delete promotion.", ex);
        }
    }

    @Override
    public long nextPromotionId() { return nextId("promotion", "promotion_id"); }

    @Override
    public int countPromotionUsage(long promotionId) {
        String sql = "SELECT COUNT(*) FROM promotion_usage WHERE promotion_id = ?";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, promotionId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to count promotion usage.", ex);
        }
    }

    @Override
    public List<PromotionUsageRecord> findUsageForPromotion(long promotionId) {
        String sql = "SELECT promotion_usage_id, promotion_id, promotion_code, invoice_id, customer_key, "
                + "discount_applied, used_at FROM promotion_usage WHERE promotion_id = ? ORDER BY used_at DESC";
        List<PromotionUsageRecord> result = new ArrayList<>();
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, promotionId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    Timestamp usedAt = rows.getTimestamp("used_at");
                    result.add(new PromotionUsageRecord(
                            rows.getLong("promotion_usage_id"),
                            rows.getLong("promotion_id"),
                            rows.getString("promotion_code"),
                            rows.getLong("invoice_id"),
                            rows.getString("customer_key"),
                            rows.getBigDecimal("discount_applied"),
                            usedAt == null ? LocalDateTime.now() : usedAt.toLocalDateTime()));
                }
            }
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to load promotion usage.", ex);
        }
        return result;
    }

    @Override
    public PromotionUsageRecord savePromotionUsage(PromotionUsageRecord usage) {
        String sql = "INSERT INTO promotion_usage (promotion_usage_id, promotion_id, promotion_code, "
                + "invoice_id, customer_key, discount_applied, used_at) VALUES (?,?,?,?,?,?,?)";
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, usage.getId());
            statement.setLong(2, usage.getPromotionId());
            statement.setString(3, usage.getPromotionCode());
            statement.setLong(4, usage.getInvoiceId());
            statement.setString(5, usage.getCustomerKey());
            statement.setBigDecimal(6, usage.getDiscountApplied());
            statement.setTimestamp(7, Timestamp.valueOf(usage.getUsedAt()));
            statement.executeUpdate();
            return usage;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to record promotion usage.", ex);
        }
    }

    @Override
    public long nextPromotionUsageId() { return nextId("promotion_usage", "promotion_usage_id"); }

    // ------------------------------------------------------------------- shared

    private long nextId(String table, String column) {
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) + 1 FROM " + table;
        try (Connection connection = config.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            return rows.next() ? rows.getLong(1) : 1;
        } catch (SQLException ex) {
            throw repositoryFailure("Unable to allocate a database identifier.", ex);
        }
    }

    private void bind(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    private static IllegalStateException repositoryFailure(String message, SQLException ex) {
        return new IllegalStateException(message + " " + ex.getMessage(), ex);
    }
}
