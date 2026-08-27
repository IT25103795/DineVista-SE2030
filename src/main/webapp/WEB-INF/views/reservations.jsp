<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="com.dinevista.model.AvailabilityAlternativeRecord" %>
<%@ page import="com.dinevista.model.RestaurantTableRecord" %>
<%@ page import="com.dinevista.model.TableReservationRecord" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%
    request.setAttribute("pageTitle", "Table Reservations");
    request.setAttribute("activeNav", "reservations");
%>
<%@ include file="fragments/header.jspf" %>
<%
    List<TableReservationRecord> customerReservations =
            (List<TableReservationRecord>) request.getAttribute("customerReservations");
    List<RestaurantTableRecord> availableTables =
            (List<RestaurantTableRecord>) request.getAttribute("availableTables");
    List<AvailabilityAlternativeRecord> availabilityAlternatives =
            (List<AvailabilityAlternativeRecord>) request.getAttribute("availabilityAlternatives");
    boolean availabilitySearched = Boolean.TRUE.equals(request.getAttribute("availabilitySearched"));

    String formGuestName = request.getAttribute("formGuestName") == null
            ? (session.getAttribute("displayName") == null ? "" : session.getAttribute("displayName").toString())
            : request.getAttribute("formGuestName").toString();
    String formEmail = request.getAttribute("formEmail") == null
            ? (session.getAttribute("demoEmail") == null ? "" : session.getAttribute("demoEmail").toString())
            : request.getAttribute("formEmail").toString();
    String formPhone = request.getAttribute("formPhone") == null ? "" : request.getAttribute("formPhone").toString();
    String formDate = request.getAttribute("formDate") == null ? "" : request.getAttribute("formDate").toString();
    String formTime = request.getAttribute("formTime") == null ? "" : request.getAttribute("formTime").toString();
    String formParty = request.getAttribute("formPartySize") == null ? "2" : request.getAttribute("formPartySize").toString();
    String formArea = request.getAttribute("formSeatingArea") == null ? "ANY" : request.getAttribute("formSeatingArea").toString();
    String formOccasion = request.getAttribute("formOccasion") == null ? "" : request.getAttribute("formOccasion").toString();
%>

<section class="page-hero reservation-hero">
    <div class="container">
        <div class="breadcrumbs"><a href="<%= ctx %>/">Home</a><span>/</span><span>Table Reservations</span></div>
        <span class="eyebrow">Complete reservation management</span>
        <h1>Find a table, reserve your time, and manage every visit.</h1>
        <p>Check real-time availability, create a reservation request, update pending details, track confirmation, or cancel an eligible booking.</p>
        <div class="hero-actions">
            <a class="btn btn-primary" href="#create-reservation">Create reservation</a>
            <a class="btn btn-secondary" href="#my-reservations">My reservations</a>
        </div>
    </div>
</section>

<section class="section-sm section-soft">
    <div class="container">
        <% if (request.getAttribute("successMessage") != null && request.getParameter("created") != null) { %>
            <div class="success-celebration reservation-created-card" role="status" aria-live="polite">
                <span class="success-celebration-icon" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="m5 12 4 4L19 6"/></svg></span>
                <div>
                    <span class="success-label">Reservation requested</span>
                    <h2>Your table request is in.</h2>
                    <p><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></p>
                    <div class="success-actions">
                        <code><%= HtmlUtil.escape(request.getParameter("created")) %></code>
                        <button class="btn btn-secondary btn-sm" type="button" data-copy-reference="<%= HtmlUtil.escape(request.getParameter("created")) %>">Copy reference</button>
                        <a class="btn btn-primary btn-sm" href="<%= ctx %>/reservations/view?reference=<%= HtmlUtil.escape(request.getParameter("created")) %>">Track reservation</a>
                    </div>
                </div>
            </div>
        <% } else if (request.getAttribute("successMessage") != null) { %>
            <div class="alert alert-success"><strong><%= HtmlUtil.escape(request.getAttribute("successMessage")) %></strong></div>
        <% } %>
        <% if (request.getAttribute("errors") != null) { %>
            <div class="alert alert-danger">
                <div><strong>Please correct the following:</strong>
                    <ul>
                        <% for (String error : (List<String>) request.getAttribute("errors")) { %>
                            <li><%= HtmlUtil.escape(error) %></li>
                        <% } %>
                    </ul>
                </div>
            </div>
        <% } %>

        <div class="module-heading">
            <div>
                <span class="section-kicker">Availability checker</span>
                <h2>Choose the best available table.</h2>
                <p>Each reservation uses a 90-minute dining slot. Overlapping confirmed table allocations are automatically blocked.</p>
            </div>
            <span class="module-badge">Live table availability</span>
        </div>

        <form class="availability-form" method="get" action="<%= ctx %>/reservations">
            <div class="form-group">
                <label for="availabilityDate">Date</label>
                <input class="form-control" id="availabilityDate" name="availabilityDate" type="date"
                       min="<%= LocalDate.now() %>" required
                       value="<%= HtmlUtil.escape(request.getParameter("availabilityDate")) %>">
            </div>
            <div class="form-group">
                <label for="availabilityTime">Time</label>
                <select class="form-control" id="availabilityTime" name="availabilityTime" required>
                    <option value="">Select time</option>
                    <% String availabilityTime = request.getParameter("availabilityTime") == null ? "" : request.getParameter("availabilityTime"); %>
                    <option value="11:30" <%= HtmlUtil.selected("11:30", availabilityTime) %>>11:30 AM</option>
                    <option value="12:30" <%= HtmlUtil.selected("12:30", availabilityTime) %>>12:30 PM</option>
                    <option value="13:30" <%= HtmlUtil.selected("13:30", availabilityTime) %>>1:30 PM</option>
                    <option value="18:30" <%= HtmlUtil.selected("18:30", availabilityTime) %>>6:30 PM</option>
                    <option value="19:30" <%= HtmlUtil.selected("19:30", availabilityTime) %>>7:30 PM</option>
                    <option value="20:30" <%= HtmlUtil.selected("20:30", availabilityTime) %>>8:30 PM</option>
                    <option value="21:30" <%= HtmlUtil.selected("21:30", availabilityTime) %>>9:30 PM</option>
                </select>
            </div>
            <div class="form-group">
                <label for="availabilityPartySize">Guests</label>
                <input class="form-control" id="availabilityPartySize" name="availabilityPartySize"
                       type="number" min="1" max="20" required
                       value="<%= HtmlUtil.escape(request.getParameter("availabilityPartySize") == null ? "2" : request.getParameter("availabilityPartySize")) %>">
            </div>
            <div class="form-group">
                <label for="availabilityArea">Area</label>
                <% String availabilityArea = request.getParameter("availabilityArea") == null ? "ANY" : request.getParameter("availabilityArea"); %>
                <select class="form-control" id="availabilityArea" name="availabilityArea">
                    <option value="ANY" <%= HtmlUtil.selected("ANY", availabilityArea) %>>Any area</option>
                    <option value="INDOOR" <%= HtmlUtil.selected("INDOOR", availabilityArea) %>>Indoor dining</option>
                    <option value="GARDEN" <%= HtmlUtil.selected("GARDEN", availabilityArea) %>>Garden terrace</option>
                    <option value="PRIVATE_DINING" <%= HtmlUtil.selected("PRIVATE_DINING", availabilityArea) %>>Private dining</option>
                    <option value="CHEF_COUNTER" <%= HtmlUtil.selected("CHEF_COUNTER", availabilityArea) %>>Chef's counter</option>
                </select>
            </div>
            <button class="btn btn-dark" type="submit">Check availability</button>
        </form>

        <% if (availabilitySearched) { %>
            <div class="availability-results">
                <div class="result-heading">
                    <h3><%= availableTables == null ? 0 : availableTables.size() %> suitable table<%= availableTables != null && availableTables.size() == 1 ? "" : "s" %> available</h3>
                    <span>Best-fit tables are shown first.</span>
                </div>
                <% if (availableTables == null || availableTables.isEmpty()) { %>
                    <div class="empty-module-state">
                        <strong>No suitable table is available for this slot.</strong>
                        <p>Try one of the nearest available times below, another seating area, or a smaller party size.</p>
                    </div>
                    <% if (availabilityAlternatives != null && !availabilityAlternatives.isEmpty()) { %>
                        <div class="alternative-slots" aria-labelledby="alternative-slots-title">
                            <div class="alternative-slots-heading">
                                <div>
                                    <span class="section-kicker">Nearest availability</span>
                                    <h3 id="alternative-slots-title">Other times that fit your party</h3>
                                </div>
                                <span>Up to four closest options</span>
                            </div>
                            <div class="alternative-slot-grid">
                                <% for (AvailabilityAlternativeRecord alternative : availabilityAlternatives) {
                                    RestaurantTableRecord suggestedTable = alternative.getTables().get(0);
                                %>
                                    <article class="alternative-slot-card">
                                        <div>
                                            <strong><%= HtmlUtil.escape(alternative.getDateDisplay()) %></strong>
                                            <span><%= HtmlUtil.escape(alternative.getTimeDisplay()) %></span>
                                            <small><%= alternative.getAvailableTableCount() %> suitable table<%= alternative.getAvailableTableCount() == 1 ? "" : "s" %></small>
                                        </div>
                                        <a class="btn btn-secondary btn-sm" href="#create-reservation"
                                           data-prefill-reservation
                                           data-date="<%= HtmlUtil.escape(alternative.getDateInputValue()) %>"
                                           data-time="<%= HtmlUtil.escape(alternative.getTimeInputValue()) %>"
                                           data-party="<%= HtmlUtil.escape(request.getParameter("availabilityPartySize")) %>"
                                           data-area="<%= HtmlUtil.escape(suggestedTable.getSeatingArea()) %>">Choose time</a>
                                    </article>
                                <% } %>
                            </div>
                        </div>
                    <% } %>
                <% } else { %>
                    <div class="floor-plan-legend" aria-label="Table availability legend">
                        <span><i class="legend-dot available"></i>Available now</span>
                        <span><i class="legend-dot capacity"></i>Number shows seats</span>
                        <span><i class="legend-dot staff"></i>Final table confirmed by staff</span>
                    </div>
                    <div class="table-card-grid visual-table-map" data-table-map>
                        <% for (RestaurantTableRecord table : availableTables) { %>
                            <article class="table-card table-map-spot" data-table-spot>
                                <div class="table-visual">
                                    <span><%= table.getCapacity() %></span>
                                    <small>seats</small>
                                </div>
                                <div>
                                    <span class="tag"><%= HtmlUtil.escape(table.getSeatingAreaDisplay()) %></span>
                                    <h3>Table <%= HtmlUtil.escape(table.getCode()) %></h3>
                                    <p>Suitable for up to <%= table.getCapacity() %> guests.</p>
                                </div>
                                <a class="btn btn-secondary btn-sm" href="#create-reservation"
                                   data-prefill-reservation
                                   data-date="<%= HtmlUtil.escape(request.getParameter("availabilityDate")) %>"
                                   data-time="<%= HtmlUtil.escape(request.getParameter("availabilityTime")) %>"
                                   data-party="<%= HtmlUtil.escape(request.getParameter("availabilityPartySize")) %>"
                                   data-area="<%= HtmlUtil.escape(table.getSeatingArea()) %>">Use this slot</a>
                            </article>
                        <% } %>
                    </div>
                <% } %>
            </div>
        <% } %>
    </div>
</section>

<section class="section-sm" id="create-reservation">
    <div class="container form-layout">
        <div class="form-card">
            <span class="section-kicker">Create reservation</span>
            <h2>Plan your DineVista visit.</h2>
            <p>Restaurant staff will assign the best available table and confirm your request.</p>

            <form method="post" action="<%= ctx %>/reservations/create" novalidate data-reservation-form data-reservation-stepper>
                <ol class="reservation-stepper-nav" aria-label="Reservation progress">
                    <li><button type="button" data-step-target="0"><span>1</span><small>Visit</small></button></li>
                    <li><button type="button" data-step-target="1"><span>2</span><small>Guest</small></button></li>
                    <li><button type="button" data-step-target="2"><span>3</span><small>Review</small></button></li>
                </ol>

                <div class="reservation-step-panel" data-step-panel="0">
                    <div class="step-panel-heading"><span>Step 1 of 3</span><h3>When would you like to visit?</h3></div>
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="date">Reservation date</label>
                            <input class="form-control" id="date" name="date" type="date"
                                   min="<%= LocalDate.now() %>" required data-reservation-field
                                   value="<%= HtmlUtil.escape(formDate) %>">
                        </div>
                        <div class="form-group">
                            <label for="time">Preferred time</label>
                            <select class="form-control" id="time" name="time" required data-reservation-field>
                                <option value="">Select time</option>
                                <option value="11:30" <%= HtmlUtil.selected("11:30", formTime) %>>11:30 AM</option>
                                <option value="12:30" <%= HtmlUtil.selected("12:30", formTime) %>>12:30 PM</option>
                                <option value="13:30" <%= HtmlUtil.selected("13:30", formTime) %>>1:30 PM</option>
                                <option value="18:30" <%= HtmlUtil.selected("18:30", formTime) %>>6:30 PM</option>
                                <option value="19:30" <%= HtmlUtil.selected("19:30", formTime) %>>7:30 PM</option>
                                <option value="20:30" <%= HtmlUtil.selected("20:30", formTime) %>>8:30 PM</option>
                                <option value="21:30" <%= HtmlUtil.selected("21:30", formTime) %>>9:30 PM</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="partySize">Party size</label>
                            <input class="form-control" id="partySize" name="partySize" type="number"
                                   min="1" max="20" required data-reservation-field
                                   value="<%= HtmlUtil.escape(formParty) %>">
                        </div>
                        <div class="form-group">
                            <label for="seatingArea">Seating preference</label>
                            <select class="form-control" id="seatingArea" name="seatingArea"
                                    required data-reservation-field>
                                <option value="ANY" <%= HtmlUtil.selected("ANY", formArea) %>>Any available area</option>
                                <option value="INDOOR" <%= HtmlUtil.selected("INDOOR", formArea) %>>Indoor dining hall</option>
                                <option value="GARDEN" <%= HtmlUtil.selected("GARDEN", formArea) %>>Garden terrace</option>
                                <option value="PRIVATE_DINING" <%= HtmlUtil.selected("PRIVATE_DINING", formArea) %>>Private dining room</option>
                                <option value="CHEF_COUNTER" <%= HtmlUtil.selected("CHEF_COUNTER", formArea) %>>Chef's counter</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-actions step-actions"><button class="btn btn-primary" type="button" data-step-next>Continue to guest details</button></div>
                </div>

                <div class="reservation-step-panel" data-step-panel="1">
                    <div class="step-panel-heading"><span>Step 2 of 3</span><h3>Who should we welcome?</h3></div>
                    <div class="form-grid">
                        <div class="form-group full">
                            <label for="guestName">Guest name</label>
                            <input class="form-control" id="guestName" name="guestName" required minlength="2" maxlength="160"
                                   autocomplete="name" value="<%= HtmlUtil.escape(formGuestName) %>"
                                   placeholder="Enter the booking name">
                        </div>
                        <div class="form-group">
                            <label for="email">Email address</label>
                            <input class="form-control" id="email" name="email" type="email" required maxlength="160"
                                   autocomplete="email" value="<%= HtmlUtil.escape(formEmail) %>"
                                   placeholder="name@example.com">
                        </div>
                        <div class="form-group">
                            <label for="phone">Mobile number</label>
                            <input class="form-control" id="phone" name="phone" required
                                   pattern="(?:\+94|0)7[0-9]{8}" value="<%= HtmlUtil.escape(formPhone) %>"
                                   placeholder="0771234567">
                            <span class="form-note">Use 07XXXXXXXX or +947XXXXXXXX.</span>
                        </div>
                        <div class="form-group full">
                            <label for="occasion">Occasion or special request</label>
                            <textarea class="form-control" id="occasion" name="occasion"
                                      maxlength="500" placeholder="Birthday, anniversary, dietary note, accessibility request, or other details"><%= HtmlUtil.escape(formOccasion) %></textarea>
                        </div>
                    </div>
                    <div class="form-actions step-actions">
                        <button class="btn btn-secondary" type="button" data-step-back>Back</button>
                        <button class="btn btn-primary" type="button" data-step-next>Review reservation</button>
                    </div>
                </div>

                <div class="reservation-step-panel" data-step-panel="2">
                    <div class="step-panel-heading"><span>Step 3 of 3</span><h3>Review and send your request.</h3></div>
                    <div class="step-review-card" data-step-review aria-live="polite"></div>
                    <p class="form-note">Your request remains pending until restaurant staff assign a suitable table and confirm it.</p>
                    <div class="form-actions step-actions">
                        <button class="btn btn-secondary" type="button" data-step-back>Back</button>
                        <button class="btn btn-primary" type="submit">Submit reservation request</button>
                        <a class="btn btn-ghost" href="<%= ctx %>/menu">View menu first</a>
                    </div>
                </div>
            </form>
        </div>

        <aside class="info-card">
            <span class="section-kicker">Your selection</span>
            <h3>Reservation summary</h3>
            <div class="cart-summary" data-reservation-summary></div>
            <ul class="info-list">
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg><span><strong>Opening hours</strong><span>Daily from 11:00 AM to 11:00 PM.</span></span></li>
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21s7-5.2 7-12a7 7 0 1 0-14 0c0 6.8 7 12 7 12z"/><circle cx="12" cy="9" r="2"/></svg><span><strong>Location</strong><span>Malabe, Sri Lanka.</span></span></li>
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16v16H4zM8 9h8M8 13h5"/></svg><span><strong>Business rule</strong><span>Reservations must be made at least 30 minutes in advance.</span></span></li>
                <li><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 12h18M12 3v18"/></svg><span><strong>Table assignment</strong><span>Staff assign a suitable non-overlapping table before confirmation.</span></span></li>
            </ul>
        </aside>
    </div>
</section>

<section class="section-sm section-soft" id="my-reservations">
    <div class="container">
        <div class="module-heading">
            <div>
                <span class="section-kicker">Reservation records</span>
                <h2>My current and previous reservations.</h2>
                <p>Track confirmation, table assignment, seating, completion, or cancellation status.</p>
            </div>
        </div>

        <% if (customerReservations == null || customerReservations.isEmpty()) { %>
            <div class="empty-module-state">
                <strong>No reservations yet.</strong>
                <p>Your submitted reservation requests will appear here.</p>
                <a class="btn btn-primary btn-sm" href="#create-reservation">Create your first reservation</a>
            </div>
        <% } else { %>
            <div class="record-grid">
                <% for (TableReservationRecord reservation : customerReservations) { %>
                    <article class="record-card">
                        <div class="record-card-top">
                            <div>
                                <span class="record-reference"><%= HtmlUtil.escape(reservation.getReference()) %></span>
                                <h3><%= HtmlUtil.escape(reservation.getGuestName()) %></h3>
                            </div>
                            <span class="status <%= reservation.getStatusCss() %>"><%= HtmlUtil.escape(reservation.getStatus().replace('_', ' ')) %></span>
                        </div>
                        <div class="record-facts">
                            <span><strong><%= reservation.getDateDisplay() %></strong>Date</span>
                            <span><strong><%= reservation.getTimeDisplay() %></strong>Time</span>
                            <span><strong><%= reservation.getPartySize() %></strong>Guests</span>
                            <span><strong><%= reservation.getTableCode() == null ? "Pending" : HtmlUtil.escape(reservation.getTableCode()) %></strong>Table</span>
                        </div>
                        <p class="muted small"><%= HtmlUtil.escape(reservation.getSeatingPreferenceDisplay()) %></p>
                        <div class="record-actions">
                            <a class="btn btn-secondary btn-sm" href="<%= ctx %>/reservations/view?reference=<%= reservation.getReference() %>">View details</a>
                            <% if ("PENDING".equals(reservation.getStatus())) { %>
                                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/reservations/edit?reference=<%= reservation.getReference() %>">Edit</a>
                            <% } %>
                        </div>
                    </article>
                <% } %>
            </div>
        <% } %>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
