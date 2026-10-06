<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dinevista.model.FoodOrderRecord" %>
<%@ page import="com.dinevista.model.EventBookingRecord" %>
<%@ page import="com.dinevista.model.OrderItemRecord" %>
<%@ page import="com.dinevista.model.InvoiceRecord" %>
<%@ page import="com.dinevista.model.PromotionRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map" %>
<%
    request.setAttribute("pageTitle", "Generate Invoice");
    request.setAttribute("activeNav", "staffBilling");
    FoodOrderRecord prefillOrder = (FoodOrderRecord) request.getAttribute("prefillOrder");
    EventBookingRecord selectedEvent = (EventBookingRecord) request.getAttribute("selectedEventBooking");
    List<EventBookingRecord> completedEvents = (List<EventBookingRecord>) request.getAttribute("completedEventBookings");
    Set<String> invoicedEvents = (Set<String>) request.getAttribute("invoicedEventReferences");
    InvoiceRecord existingInvoice = (InvoiceRecord) request.getAttribute("existingInvoice");
    List<PromotionRecord> promotions = (List<PromotionRecord>) request.getAttribute("promotions");
    Map<Long, Integer> promotionUsageCounts = (Map<Long, Integer>) request.getAttribute("promotionUsageCounts");
    String sourceType = request.getParameter("sourceType");
    if (prefillOrder != null) sourceType = "FOOD_ORDER";
    if (sourceType == null || sourceType.isEmpty()) sourceType = "FOOD_ORDER";
    boolean eventMode = "EVENT_BOOKING".equals(sourceType);
    String sourceReference = request.getParameter("sourceReference");
    String[] postedDescriptions = "POST".equals(request.getMethod()) ? request.getParameterValues("description") : null;
    String[] postedQuantities = "POST".equals(request.getMethod()) ? request.getParameterValues("quantity") : null;
    String[] postedPrices = "POST".equals(request.getMethod()) ? request.getParameterValues("unitPrice") : null;
%>
<%@ include file="fragments/header.jspf" %>
<section class="operations-hero compact">
    <div class="container">
        <div>
            <div class="breadcrumbs dark"><a href="<%= ctx %>/staff/billing">Billing</a><span>/</span><span>Generate invoice</span></div>
            <span class="eyebrow">Billing, Promotions &amp; Discounts</span>
            <h1>Generate an invoice.</h1>
            <p>After the customer accepts the final event quote, its agreed amount starts the invoice. Add disclosed extras and an eligible discount; the customer can then review and simulate payment.</p>
        </div>
        <a class="btn btn-secondary" href="<%= ctx %>/staff/billing">Back to billing</a>
    </div>
</section>

<section class="section-sm">
    <div class="container form-layout">
        <div class="form-card">
            <% if (request.getAttribute("errors") != null) { %>
                <div class="alert alert-danger"><div><strong>Unable to generate that invoice:</strong><ul>
                    <% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %>
                </ul></div></div>
            <% } %>

            <% if (existingInvoice != null) { %>
                <div class="alert alert-danger">
                    An active invoice <strong><%= HtmlUtil.escape(existingInvoice.getInvoiceNumber()) %></strong>
                    already exists for this source.
                    <a href="<%= ctx %>/staff/billing/view?id=<%= existingInvoice.getId() %>">Open it instead</a>.
                </div>
            <% } %>

            <form method="post" action="<%= ctx %>/staff/billing/generate" id="invoiceForm">
                <div class="form-grid">
                    <div class="form-group">
                        <label for="sourceType">Source type</label>
                        <select class="form-control" id="sourceType" name="sourceType">
                            <option value="FOOD_ORDER" <%= "FOOD_ORDER".equals(sourceType) ? "selected" : "" %>>Food order</option>
                            <option value="EVENT_BOOKING" <%= eventMode ? "selected" : "" %>>Event booking</option>
                            <option value="OTHER" <%= "OTHER".equals(sourceType) ? "selected" : "" %>>Other / manual</option>
                        </select>
                    </div>
                    <div class="form-group" id="manualReferenceGroup">
                        <label for="manualReference">Source reference</label>
                        <input class="form-control" id="manualReference" name="sourceReference" type="text" maxlength="30"
                               placeholder="e.g. DV-O-1A2B3C4D"
                               value="<%= prefillOrder != null ? HtmlUtil.escape(prefillOrder.getReference()) : !eventMode && sourceReference != null ? HtmlUtil.escape(sourceReference) : "" %>">
                    </div>
                    <div class="form-group" id="eventReferenceGroup" hidden>
                        <label for="eventReference">Source reference</label>
                        <select class="form-control" id="eventReference" name="sourceReference" disabled>
                            <option value="">Select an event with an accepted quote</option>
                            <% if (completedEvents != null) for (EventBookingRecord booking : completedEvents) { boolean invoiced = invoicedEvents != null && invoicedEvents.contains(booking.getReference()); %>
                            <option value="<%= HtmlUtil.escape(booking.getReference()) %>"
                                    data-name="<%= HtmlUtil.escape(booking.getCustomerName()) %>"
                                    data-email="<%= HtmlUtil.escape(booking.getEmail()) %>"
                                    data-package="<%= HtmlUtil.escape(booking.getPackageName()) %>"
                                    data-amount="<%= booking.getTotalAmount().toPlainString() %>"
                                    data-date="<%= HtmlUtil.escape(booking.getEventDate()) %>"
                                    data-invoiced="<%= invoiced %>"
                                    <%= selectedEvent != null && selectedEvent.getReference().equals(booking.getReference()) ? "selected" : "" %>><%= HtmlUtil.escape(booking.getReference()) %><%= invoiced ? " - invoice already issued" : "" %></option>
                            <% } %>
                        </select>
                        <small class="muted">Bookings with an accepted quote are listed, including completed events. An existing invoice cannot be generated twice.</small>
                    </div>
                    <div class="form-group">
                        <label for="customerName">Customer name</label>
                        <input class="form-control" id="customerName" name="customerName" type="text" maxlength="160"
                               value="<%= selectedEvent != null ? HtmlUtil.escape(selectedEvent.getCustomerName()) : prefillOrder != null ? HtmlUtil.escape(prefillOrder.getCustomerName()) : request.getParameter("customerName") != null ? HtmlUtil.escape(request.getParameter("customerName")) : "" %>" placeholder="Walk-in customer">
                    </div>
                    <div class="form-group">
                        <label for="customerEmail">Customer email</label>
                        <input class="form-control" id="customerEmail" name="customerEmail" type="email" maxlength="160"
                               value="<%= selectedEvent != null ? HtmlUtil.escape(selectedEvent.getEmail()) : prefillOrder != null ? HtmlUtil.escape(prefillOrder.getEmail()) : request.getParameter("customerEmail") != null ? HtmlUtil.escape(request.getParameter("customerEmail")) : "" %>">
                    </div>
                    <input id="customerKey" type="hidden" name="customerKey" value="<%= selectedEvent != null ? HtmlUtil.escape(selectedEvent.getEmail().toLowerCase()) : prefillOrder != null ? HtmlUtil.escape(prefillOrder.getCustomerKey()) : "" %>">
                    <div class="form-group">
                        <label for="promotionCode">Promotion / discount code (optional)</label>
                        <input class="form-control" id="promotionCode" name="promotionCode" type="text" maxlength="40" list="availablePromotions" placeholder="e.g. WELCOME10" value="<%= request.getParameter("promotionCode") != null ? HtmlUtil.escape(request.getParameter("promotionCode")) : selectedEvent != null ? HtmlUtil.escape(selectedEvent.getPromotionCode()) : "" %>">
                    </div>
                </div>

                <div id="eventBaseCharge" class="detail-card" style="margin-top:20px" hidden>
                    <strong>Agreed event booking amount</strong>
                    <p class="muted small" id="eventBaseDescription">Choose a booking with an accepted quote to load its amount and customer.</p>
                    <strong id="eventBaseAmount">LKR 0.00</strong>
                    <p class="muted small" style="margin-top:8px">This amount comes from the saved booking and cannot be changed on this form.</p>
                </div>

                <h3 style="margin-top:24px">Billable lines</h3>
                <p class="muted small" id="linesHelp">For events, add only charges beyond the agreed booking amount. The final amount includes 2.5% service tax and any eligible discount.</p>
                <div id="lineRows">
                <% boolean anyPrefill = false;
                   if (postedDescriptions != null) {
                       for (int i = 0; i < postedDescriptions.length; i++) {
                           anyPrefill = true; %>
                    <div class="form-grid line-row">
                        <div class="form-group full"><label>Description</label>
                            <input class="form-control" name="description" type="text" maxlength="255" value="<%= HtmlUtil.escape(postedDescriptions[i] == null ? "" : postedDescriptions[i]) %>" placeholder="e.g. Extra decoration"></div>
                        <div class="form-group"><label>Quantity</label>
                            <input class="form-control" name="quantity" type="number" step="0.01" min="0.01" value="<%= postedQuantities != null && i < postedQuantities.length ? HtmlUtil.escape(postedQuantities[i]) : "1" %>"></div>
                        <div class="form-group"><label>Unit price (LKR)</label>
                            <input class="form-control" name="unitPrice" type="number" step="0.01" min="0" value="<%= postedPrices != null && i < postedPrices.length ? HtmlUtil.escape(postedPrices[i]) : "0" %>"></div>
                    </div>
                    <% }
                   } else if (prefillOrder != null && !eventMode) {
                       for (OrderItemRecord item : prefillOrder.getItems()) {
                           anyPrefill = true; %>
                    <div class="form-grid line-row">
                        <div class="form-group full"><label>Description</label>
                            <input class="form-control" name="description" type="text" maxlength="255" value="<%= HtmlUtil.escape(item.getItemName()) %>"></div>
                        <div class="form-group"><label>Quantity</label>
                            <input class="form-control" name="quantity" type="number" step="0.01" min="0.01" value="<%= item.getQuantity() %>"></div>
                        <div class="form-group"><label>Unit price (LKR)</label>
                            <input class="form-control" name="unitPrice" type="number" step="0.01" min="0" value="<%= item.getUnitPrice().toPlainString() %>"></div>
                    </div>
                    <% }
                       if (prefillOrder.getServiceCharge() != null && prefillOrder.getServiceCharge().signum() > 0) { %>
                    <div class="form-grid line-row">
                        <div class="form-group full"><label>Description</label>
                            <input class="form-control" name="description" type="text" maxlength="255" value="Service charge"></div>
                        <div class="form-group"><label>Quantity</label>
                            <input class="form-control" name="quantity" type="number" step="0.01" min="0.01" value="1"></div>
                        <div class="form-group"><label>Unit price (LKR)</label>
                            <input class="form-control" name="unitPrice" type="number" step="0.01" min="0" value="<%= prefillOrder.getServiceCharge().toPlainString() %>"></div>
                    </div>
                    <% }
                   }
                   if (!anyPrefill) { %>
                    <div class="form-grid line-row">
                        <div class="form-group full"><label>Description</label>
                            <input class="form-control" name="description" type="text" maxlength="255" placeholder="e.g. Table service charges"></div>
                        <div class="form-group"><label>Quantity</label>
                            <input class="form-control" name="quantity" type="number" step="0.01" min="0.01" value="1"></div>
                        <div class="form-group"><label>Unit price (LKR)</label>
                            <input class="form-control" name="unitPrice" type="number" step="0.01" min="0" value="0"></div>
                    </div>
                <% } %>
                </div>
                <button type="button" class="btn btn-secondary btn-sm" id="addLineBtn" style="margin-top:8px">Add another line</button>

                <article class="detail-card" style="margin-top:20px" aria-live="polite">
                    <h3>Final amount for the customer to review</h3>
                    <div class="detail-fact-grid" style="margin-top:12px">
                        <div><span>Subtotal</span><strong id="previewSubtotal">LKR 0.00</strong></div>
                        <div><span>Service tax (2.5%)</span><strong id="previewTax">LKR 0.00</strong></div>
                        <div><span>Discount</span><strong id="previewDiscount">- LKR 0.00</strong></div>
                        <div><span>Final total</span><strong id="previewTotal">LKR 0.00</strong></div>
                    </div>
                    <p class="muted small" id="promotionFeedback" style="margin-top:10px">Enter a promotion code to preview an eligible discount.</p>
                    <p class="muted small">This is a preview. The saved invoice recalculates every amount before payment is recorded.</p>
                </article>

                <div class="form-actions">
                    <button class="btn btn-primary" id="generateInvoiceBtn" type="submit">Generate invoice</button>
                    <a class="btn btn-secondary" href="<%= ctx %>/staff/billing">Cancel</a>
                </div>
            </form>
        </div>

        <aside class="detail-sidebar">
            <article class="panel">
                <span class="section-kicker">Active promotions</span>
                <ul class="check-list operational-checks">
                <% if (promotions == null || promotions.isEmpty()) { %>
                    <li>No promotions configured yet.</li>
                <% } else { for (PromotionRecord promotion : promotions) { if (promotion.isCurrentlyValid() && (promotion.getUsageLimit() == null || promotionUsageCounts.getOrDefault(promotion.getId(), 0) < promotion.getUsageLimit())) { %>
                    <li><strong><%= HtmlUtil.escape(promotion.getCode()) %></strong> — <%= promotion.getDiscountValueDisplay() %> off, min. <%= HtmlUtil.escape(promotion.getMinimumSpendDisplay()) %></li>
                <% }}} %>
                </ul>
                <a class="btn btn-secondary btn-sm" style="margin-top:12px;display:inline-flex" href="<%= ctx %>/staff/billing/promotions">Manage promotions</a>
            </article>
            <article class="panel">
                <span class="section-kicker">Things to know</span>
                <ul class="check-list operational-checks">
                    <li>For events, the agreed quote amount and customer details come from the saved booking.</li>
                    <li>Additional lines and an eligible promotion change the amount to collect.</li>
                    <li>Only after the final invoice is issued can the customer choose the simulated payment.</li>
                    <li>Only one active invoice is allowed per source reference.</li>
                </ul>
            </article>
        </aside>
    </div>
</section>
<datalist id="availablePromotions">
    <% if (promotions != null) for (PromotionRecord promotion : promotions) {
           if (promotion.isCurrentlyValid() && (promotion.getUsageLimit() == null || promotionUsageCounts.getOrDefault(promotion.getId(), 0) < promotion.getUsageLimit())) { %>
    <option value="<%= HtmlUtil.escape(promotion.getCode()) %>" data-type="<%= HtmlUtil.escape(promotion.getDiscountType()) %>"
            data-value="<%= promotion.getDiscountValue().toPlainString() %>" data-minimum="<%= promotion.getMinimumSpend().toPlainString() %>"></option>
    <% }} %>
</datalist>
<script>
(function () {
    var sourceType = document.getElementById('sourceType');
    var eventReference = document.getElementById('eventReference');
    var manualReference = document.getElementById('manualReference');
    var customerName = document.getElementById('customerName');
    var customerEmail = document.getElementById('customerEmail');
    var customerKey = document.getElementById('customerKey');
    var lineRows = document.getElementById('lineRows');
    var promotionCode = document.getElementById('promotionCode');
    var generateButton = document.getElementById('generateInvoiceBtn');
    var lastSourceType = sourceType.value;
    var money = function (value) { return 'LKR ' + value.toLocaleString('en-LK', {minimumFractionDigits: 2, maximumFractionDigits: 2}); };
    var round = function (value) { return Math.round((value + Number.EPSILON) * 100) / 100; };

    function updateTotals() {
        var eventMode = sourceType.value === 'EVENT_BOOKING';
        var selected = eventReference.options[eventReference.selectedIndex];
        var subtotal = eventMode && selected && selected.dataset.amount ? Number(selected.dataset.amount) : 0;
        lineRows.querySelectorAll('.line-row').forEach(function (row) {
            var description = row.querySelector('[name="description"]').value.trim();
            if (!description) return;
            var quantity = Number(row.querySelector('[name="quantity"]').value);
            var price = Number(row.querySelector('[name="unitPrice"]').value);
            if (Number.isFinite(quantity) && Number.isFinite(price) && quantity > 0 && price >= 0) subtotal += quantity * price;
        });
        subtotal = round(subtotal);
        var tax = round(subtotal * 0.025);
        var discount = 0;
        var code = promotionCode.value.trim().toUpperCase();
        var feedback = document.getElementById('promotionFeedback');
        if (code) {
            var option = Array.from(document.querySelectorAll('#availablePromotions option')).find(function (item) { return item.value.toUpperCase() === code; });
            if (!option) feedback.textContent = 'This code is unavailable. No discount is included in the preview.';
            else if (subtotal < Number(option.dataset.minimum)) feedback.textContent = 'This code needs a higher subtotal before the discount applies.';
            else {
                discount = option.dataset.type === 'PERCENTAGE' ? round(subtotal * Number(option.dataset.value) / 100) : round(Number(option.dataset.value));
                discount = Math.min(subtotal, discount);
                feedback.textContent = code + ' discount is included in this preview.';
            }
        } else feedback.textContent = 'Enter a promotion code to preview an eligible discount.';
        document.getElementById('previewSubtotal').textContent = money(subtotal);
        document.getElementById('previewTax').textContent = money(tax);
        document.getElementById('previewDiscount').textContent = '- ' + money(discount);
        document.getElementById('previewTotal').textContent = money(Math.max(0, round(subtotal + tax - discount)));
    }

    function updateEventSelection() {
        var eventMode = sourceType.value === 'EVENT_BOOKING';
        var selected = eventReference.options[eventReference.selectedIndex];
        document.getElementById('eventBaseCharge').hidden = !eventMode;
        if (eventMode && selected && selected.value) {
            customerName.value = selected.dataset.name || '';
            customerEmail.value = selected.dataset.email || '';
            customerKey.value = (selected.dataset.email || '').toLowerCase();
            document.getElementById('eventBaseDescription').textContent = (selected.dataset.package || 'Event') + ' · ' + (selected.dataset.date || '');
            document.getElementById('eventBaseAmount').textContent = money(Number(selected.dataset.amount || 0));
            generateButton.disabled = selected.dataset.invoiced === 'true';
        } else if (eventMode) {
            customerName.value = '';
            customerEmail.value = '';
            customerKey.value = '';
            document.getElementById('eventBaseDescription').textContent = 'Choose a booking with an accepted quote to load its amount and customer.';
            document.getElementById('eventBaseAmount').textContent = money(0);
            generateButton.disabled = true;
        } else generateButton.disabled = false;
        updateTotals();
    }

    function updateSourceMode() {
        var eventMode = sourceType.value === 'EVENT_BOOKING';
        if (sourceType.value !== lastSourceType) {
            var firstRow = lineRows.querySelector('.line-row');
            lineRows.innerHTML = '';
            if (firstRow) {
                firstRow.querySelectorAll('input').forEach(function (input) {
                    input.value = input.name === 'quantity' ? '1' : input.name === 'unitPrice' ? '0' : '';
                });
                lineRows.appendChild(firstRow);
            }
            customerName.value = '';
            customerEmail.value = '';
            customerKey.value = '';
            manualReference.value = '';
            if (!eventMode) eventReference.value = '';
            lastSourceType = sourceType.value;
        }
        document.getElementById('manualReferenceGroup').hidden = eventMode;
        document.getElementById('eventReferenceGroup').hidden = !eventMode;
        manualReference.disabled = eventMode;
        eventReference.disabled = !eventMode;
        eventReference.required = eventMode;
        customerName.readOnly = eventMode;
        customerEmail.readOnly = eventMode;
        document.getElementById('linesHelp').textContent = eventMode
            ? 'Add only charges beyond the agreed event amount. The saved booking amount cannot be edited here.'
            : 'Add every confirmed charge. Subtotal, service tax, discount and final total update below.';
        updateEventSelection();
    }

    document.getElementById('addLineBtn').addEventListener('click', function () {
        var row = lineRows.querySelector('.line-row').cloneNode(true);
        row.querySelectorAll('input').forEach(function (input) {
            input.value = input.name === 'quantity' ? '1' : input.name === 'unitPrice' ? '0' : '';
        });
        lineRows.appendChild(row);
        updateTotals();
    });
    sourceType.addEventListener('change', updateSourceMode);
    eventReference.addEventListener('change', updateEventSelection);
    lineRows.addEventListener('input', updateTotals);
    promotionCode.addEventListener('input', updateTotals);
    updateSourceMode();
})();
</script>
<%@ include file="fragments/footer.jspf" %>
