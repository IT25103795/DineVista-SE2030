<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<% request.setAttribute("pageTitle", "Manager Sign In"); request.setAttribute("activeNav", ""); %>
<%@ include file="fragments/header.jspf" %>
<section class="auth-page">
    <div class="container">
        <div class="auth-shell">
            <aside class="auth-visual">
                <a class="brand" href="<%= ctx %>/"><img src="<%= ctx %>/assets/images/logo.svg" alt=""><span>Dine<em>Vista</em></span></a>
                <h2>Restaurant operations manager portal.</h2>
                <p>Only verified manager accounts can access reservations, kitchen orders, inventory, and management reporting.</p>
                <ul class="check-list"><li>Role checked against the database</li><li>Manager-only operational access</li><li>Customer accounts are rejected here</li></ul>
            </aside>
            <div class="auth-content">
                <span class="section-kicker">Restricted access</span>
                <h1>Manager sign in</h1>
                <p>Use your verified manager email and password.</p>
                <% if (request.getAttribute("loginNotice") != null) { %><div class="alert alert-success"><%= HtmlUtil.escape(request.getAttribute("loginNotice")) %></div><% } %>
                <% List<String> authErrors = (List<String>) request.getAttribute("authErrors");
                   if (authErrors != null) { for (String error : authErrors) { %>
                    <div class="alert alert-danger"><%= HtmlUtil.escape(error) %></div>
                <% }} %>
                <form method="post" action="<%= ctx %>/manager/login">
                    <div class="form-grid">
                        <div class="form-group full"><label for="managerEmail">Manager email</label><input class="form-control" id="managerEmail" name="email" type="email" required maxlength="160" autocomplete="email" value="<%= HtmlUtil.escape(request.getAttribute("formEmail")) %>" placeholder="manager@example.com"></div>
                        <div class="form-group full"><label for="managerPassword">Password</label><input class="form-control" id="managerPassword" name="password" type="password" required maxlength="128" autocomplete="current-password" placeholder="Enter your password"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary btn-block" type="submit">Open management dashboard</button></div>
                </form>
                <p class="auth-footer">Need a manager account? <a href="<%= ctx %>/manager/register">Register with a manager token</a></p>
                <p class="auth-footer">Dining customer? <a href="<%= ctx %>/login">Use customer sign in</a></p>
            </div>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
