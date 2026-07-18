<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="com.dinevista.model.TableReservationRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Edit Reservation");
    request.setAttribute("activeNav", "reservations");
    TableReservationRecord editReservation = (TableReservationRecord) request.getAttribute("editReservation");
%>
<%@ include file="fragments/header.jspf" %>
<%
    String guestName = request.getAttribute("formGuestName") == null ? editReservation.getGuestName() : request.getAttribute("formGuestName").toString();
    String email = request.getAttribute("formEmail") == null ? editReservation.getEmail() : request.getAttribute("formEmail").toString();
    String phone = request.getAttribute("formPhone") == null ? editReservation.getPhone() : request.getAttribute("formPhone").toString();
    String date = request.getAttribute("formDate") == null ? editReservation.getDateInputValue() : request.getAttribute("formDate").toString();
    String time = request.getAttribute("formTime") == null ? editReservation.getTimeInputValue() : request.getAttribute("formTime").toString();
    String party = request.getAttribute("formPartySize") == null ? String.valueOf(editReservation.getPartySize()) : request.getAttribute("formPartySize").toString();
    String area = request.getAttribute("formSeatingArea") == null ? editReservation.getSeatingPreference() : request.getAttribute("formSeatingArea").toString();
    String occasion = request.getAttribute("formOccasion") == null ? editReservation.getOccasionNotes() : request.getAttribute("formOccasion").toString();
%>
<section class="page-hero compact-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/reservations">Reservations</a><span>/</span><span>Edit</span></div>
        <span class="eyebrow">Pending reservation</span>
        <h1>Edit <%= HtmlUtil.escape(editReservation.getReference()) %>.</h1>
        <p>Customer changes are available only while the reservation remains pending.</p>
    </div>
</section>

<section class="section-sm">
    <div class="container form-layout">
        <div class="form-card">
            <% if (request.getAttribute("errors") != null) { %>
                <div class="alert alert-danger"><div><strong>Please correct the following:</strong><ul>
                    <% for (String error : (List<String>) request.getAttribute("errors")) { %><li><%= HtmlUtil.escape(error) %></li><% } %>
                </ul></div></div>
            <% } %>
            <form method="post" action="<%= ctx %>/reservations/update" novalidate>
                <input type="hidden" name="reference" value="<%= HtmlUtil.escape(editReservation.getReference()) %>">
                <div class="form-grid">
                    <div class="form-group full"><label for="guestName">Guest name</label><input class="form-control" id="guestName" name="guestName" required minlength="2" value="<%= HtmlUtil.escape(guestName) %>"></div>
                    <div class="form-group"><label for="email">Email address</label><input class="form-control" id="email" name="email" type="email" required value="<%= HtmlUtil.escape(email) %>"></div>
                    <div class="form-group"><label for="phone">Mobile number</label><input class="form-control" id="phone" name="phone" required pattern="(?:\+94|0)7[0-9]{8}" value="<%= HtmlUtil.escape(phone) %>"></div>
                    <div class="form-group"><label for="date">Reservation date</label><input class="form-control" id="date" name="date" type="date" min="<%= LocalDate.now() %>" required value="<%= HtmlUtil.escape(date) %>"></div>
                    <div class="form-group"><label for="time">Time</label><select class="form-control" id="time" name="time" required>
                        <option value="11:30" <%= HtmlUtil.selected("11:30", time) %>>11:30 AM</option>
                        <option value="12:30" <%= HtmlUtil.selected("12:30", time) %>>12:30 PM</option>
                        <option value="13:30" <%= HtmlUtil.selected("13:30", time) %>>1:30 PM</option>
                        <option value="18:30" <%= HtmlUtil.selected("18:30", time) %>>6:30 PM</option>
                        <option value="19:30" <%= HtmlUtil.selected("19:30", time) %>>7:30 PM</option>
                        <option value="20:30" <%= HtmlUtil.selected("20:30", time) %>>8:30 PM</option>
                        <option value="21:30" <%= HtmlUtil.selected("21:30", time) %>>9:30 PM</option>
                    </select></div>
                    <div class="form-group"><label for="partySize">Party size</label><input class="form-control" id="partySize" name="partySize" type="number" min="1" max="20" required value="<%= HtmlUtil.escape(party) %>"></div>
                    <div class="form-group"><label for="seatingArea">Seating preference</label><select class="form-control" id="seatingArea" name="seatingArea" required>
                        <option value="ANY" <%= HtmlUtil.selected("ANY", area) %>>Any available area</option>
                        <option value="INDOOR" <%= HtmlUtil.selected("INDOOR", area) %>>Indoor dining hall</option>
                        <option value="GARDEN" <%= HtmlUtil.selected("GARDEN", area) %>>Garden terrace</option>
                        <option value="PRIVATE_DINING" <%= HtmlUtil.selected("PRIVATE_DINING", area) %>>Private dining room</option>
                        <option value="CHEF_COUNTER" <%= HtmlUtil.selected("CHEF_COUNTER", area) %>>Chef's counter</option>
                    </select></div>
                    <div class="form-group full"><label for="occasion">Special request</label><textarea class="form-control" id="occasion" name="occasion" maxlength="500"><%= HtmlUtil.escape(occasion) %></textarea></div>
                </div>
                <div class="form-actions">
                    <button class="btn btn-primary" type="submit">Save reservation changes</button>
                    <a class="btn btn-secondary" href="<%= ctx %>/reservations/view?reference=<%= editReservation.getReference() %>">Cancel editing</a>
                </div>
            </form>
        </div>
        <aside class="info-card">
            <span class="section-kicker">Edit policy</span>
            <h3>Before confirmation</h3>
            <p class="muted">Changing the date, time, party size, or area triggers a new availability validation.</p>
            <ul class="info-list">
                <li><span><strong>Status</strong><span><%= HtmlUtil.escape(editReservation.getStatus()) %></span></span></li>
                <li><span><strong>Current table</strong><span><%= editReservation.getTableCode() == null ? "Not assigned" : HtmlUtil.escape(editReservation.getTableCode()) %></span></span></li>
                <li><span><strong>Created</strong><span><%= editReservation.getHistory().get(editReservation.getHistory().size() - 1).getChangedAtDisplay() %></span></span></li>
            </ul>
        </aside>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
