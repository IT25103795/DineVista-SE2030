<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<% request.setAttribute("pageTitle", "Sign In"); request.setAttribute("activeNav", ""); %>
<%@ include file="fragments/header.jspf" %>
<section class="auth-page">
    <div class="container">
        <div class="auth-shell">
            <aside class="auth-visual">
                <a class="brand" href="<%= ctx %>/"><img src="<%= ctx %>/assets/images/logo.svg" alt=""><span>Dine<em>Vista</em></span></a>
                <h2>Welcome back to better dining and event management.</h2>
                <p>Use the demo role selector to preview the customer dashboard or the restaurant operations dashboard.</p>
                <ul class="check-list"><li>Customer reservations and event requests</li><li>Manager operations and performance overview</li><li>Secure role-based architecture ready for JDBC</li></ul>
            </aside>
            <div class="auth-content">
                <span class="section-kicker">Account access</span>
                <h1>Sign in</h1>
                <p>Choose a demo role, then enter any non-empty email and password.</p>
                <% if (request.getAttribute("loginNotice") != null) { %><div class="alert alert-success"><%= HtmlUtil.escape(request.getAttribute("loginNotice")) %></div><% } %>
                <% if (request.getAttribute("loginError") != null) { %><div class="alert alert-danger"><%= HtmlUtil.escape(request.getAttribute("loginError")) %></div><% } %>
                <form method="post" action="<%= ctx %>/login">
                    <div class="role-selector">
                        <div class="role-option"><input id="customerRole" type="radio" name="role" value="customer" checked><label for="customerRole">Customer</label></div>
                        <div class="role-option"><input id="managerRole" type="radio" name="role" value="manager"><label for="managerRole">Manager</label></div>
                    </div>
                    <div class="form-grid">
                        <div class="form-group full"><label for="loginEmail">Email address</label><input class="form-control" id="loginEmail" name="email" type="email" required autocomplete="email" value="<%= HtmlUtil.escape(request.getAttribute("formEmail")) %>" placeholder="name@example.com"></div>
                        <div class="form-group full"><label for="loginPassword">Password</label><input class="form-control" id="loginPassword" name="password" type="password" required minlength="4" autocomplete="current-password" placeholder="Enter your password"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary btn-block" type="submit">Open dashboard</button></div>
                </form>
                <p class="auth-footer">New to DineVista? <a href="<%= ctx %>/register">Create an account</a></p>
            </div>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
