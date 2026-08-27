<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<% request.setAttribute("pageTitle", "Create Customer Account"); request.setAttribute("activeNav", ""); %>
<%@ include file="fragments/header.jspf" %>
<section class="auth-page">
    <div class="container">
        <div class="auth-shell">
            <aside class="auth-visual">
                <a class="brand" href="<%= ctx %>/"><img src="<%= ctx %>/assets/images/logo.svg" alt=""><span>Dine<em>Vista</em></span></a>
                <h2>Your DineVista experience starts with one customer account.</h2>
                <p>Your profile is saved securely and connects reservations, food orders, events, and notifications.</p>
                <ul class="check-list"><li>Faster restaurant bookings</li><li>Saved food order history</li><li>Customer-only dashboard access</li></ul>
            </aside>
            <div class="auth-content">
                <span class="section-kicker">Customer registration</span>
                <h1>Create customer account</h1>
                <p>Register here for normal dining and event services.</p>
                <% List<String> authErrors = (List<String>) request.getAttribute("authErrors");
                   if (authErrors != null) { for (String error : authErrors) { %>
                    <div class="alert alert-danger"><%= HtmlUtil.escape(error) %></div>
                <% }} %>
                <form method="post" action="<%= ctx %>/register">
                    <div class="form-grid">
                        <div class="form-group"><label for="firstName">First name</label><input class="form-control" id="firstName" name="firstName" required minlength="2" maxlength="80" autocomplete="given-name" value="<%= HtmlUtil.escape(request.getAttribute("formFirstName")) %>"></div>
                        <div class="form-group"><label for="lastName">Last name</label><input class="form-control" id="lastName" name="lastName" required minlength="2" maxlength="80" autocomplete="family-name" value="<%= HtmlUtil.escape(request.getAttribute("formLastName")) %>"></div>
                        <div class="form-group full"><label for="registerEmail">Email address</label><input class="form-control" id="registerEmail" name="email" type="email" required maxlength="160" autocomplete="email" value="<%= HtmlUtil.escape(request.getAttribute("formEmail")) %>"></div>
                        <div class="form-group full"><label for="registerPhone">Mobile number</label><input class="form-control" id="registerPhone" name="phone" required maxlength="20" pattern="(?:\+94|0)7[0-9]{8}" autocomplete="tel" value="<%= HtmlUtil.escape(request.getAttribute("formPhone")) %>" placeholder="0771234567"></div>
                        <div class="form-group"><label for="registerPassword">Password</label><input class="form-control" id="registerPassword" name="password" type="password" required minlength="8" maxlength="128" autocomplete="new-password"></div>
                        <div class="form-group"><label for="confirmPassword">Confirm password</label><input class="form-control" id="confirmPassword" name="confirmPassword" type="password" required minlength="8" maxlength="128" autocomplete="new-password"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary btn-block" type="submit">Create customer account</button></div>
                </form>
                <p class="auth-footer">Already registered? <a href="<%= ctx %>/login">Customer sign in</a></p>
                <p class="auth-footer">Creating a management account? <a href="<%= ctx %>/manager/register">Open manager registration</a></p>
            </div>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
