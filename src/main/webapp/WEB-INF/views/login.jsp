<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<% request.setAttribute("pageTitle", "Customer Sign In"); request.setAttribute("activeNav", ""); %>
<%@ include file="fragments/header.jspf" %>
<section class="auth-page">
    <div class="container">
        <div class="auth-shell">
            <aside class="auth-visual">
                <a class="brand" href="<%= ctx %>/"><img src="<%= ctx %>/assets/images/logo.svg" alt=""><span>Dine<em>Vista</em></span></a>
                <h2>Welcome back to your DineVista experience.</h2>
                <p>This customer portal keeps reservations, food orders, events, and notifications connected to your account.</p>
                <ul class="check-list"><li>Database-backed customer account</li><li>Private reservation and order history</li><li>Separate access from restaurant management</li></ul>
            </aside>
            <div class="auth-content">
                <span class="section-kicker">Customer access</span>
                <h1>Customer sign in</h1>
                <p>Enter the email and password used for your customer account.</p>
                <% if (request.getAttribute("loginNotice") != null) { %><div class="alert alert-success"><%= HtmlUtil.escape(request.getAttribute("loginNotice")) %></div><% } %>
                <% List<String> authErrors = (List<String>) request.getAttribute("authErrors");
                   if (authErrors != null) { for (String error : authErrors) { %>
                    <div class="alert alert-danger"><%= HtmlUtil.escape(error) %></div>
                <% }} %>
                <form method="post" action="<%= ctx %>/login">
                    <div class="form-grid">
                        <div class="form-group full"><label for="loginEmail">Email address</label><input class="form-control" id="loginEmail" name="email" type="email" required maxlength="160" autocomplete="email" value="<%= HtmlUtil.escape(request.getAttribute("formEmail")) %>" placeholder="name@example.com"></div>
                        <div class="form-group full"><label for="loginPassword">Password</label><input class="form-control" id="loginPassword" name="password" type="password" required maxlength="128" autocomplete="current-password" placeholder="Enter your password"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary btn-block" type="submit">Open customer dashboard</button></div>
                </form>
                <p class="auth-footer">New to DineVista? <a href="<%= ctx %>/register">Create a customer account</a></p>
                <p class="auth-footer">Restaurant manager? <a href="<%= ctx %>/manager/login">Use the manager portal</a></p>
            </div>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
