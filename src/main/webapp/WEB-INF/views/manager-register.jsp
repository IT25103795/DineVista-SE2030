<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.dinevista.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<% request.setAttribute("pageTitle", "Create Manager Account"); request.setAttribute("activeNav", ""); %>
<%@ include file="fragments/header.jspf" %>
<section class="auth-page">
    <div class="container">
        <div class="auth-shell">
            <aside class="auth-visual">
                <a class="brand" href="<%= ctx %>/"><img src="<%= ctx %>/assets/images/logo.svg" alt=""><span>Dine<em>Vista</em></span></a>
                <h2>Protected manager account registration.</h2>
                <p>A valid manager token is required before an account receives access to restaurant operations.</p>
                <ul class="check-list"><li>Manager token verification</li><li>Secure password hashing</li><li>Database-enforced manager role</li></ul>
            </aside>
            <div class="auth-content">
                <span class="section-kicker">Restricted registration</span>
                <h1>Create manager account</h1>
                <p>Complete all fields and enter the manager token issued by DineVista.</p>
                <% List<String> authErrors = (List<String>) request.getAttribute("authErrors");
                   if (authErrors != null) { for (String error : authErrors) { %>
                    <div class="alert alert-danger"><%= HtmlUtil.escape(error) %></div>
                <% }} %>
                <form method="post" action="<%= ctx %>/manager/register">
                    <div class="form-grid">
                        <div class="form-group"><label for="managerFirstName">First name</label><input class="form-control" id="managerFirstName" name="firstName" required minlength="2" maxlength="80" autocomplete="given-name" value="<%= HtmlUtil.escape(request.getAttribute("formFirstName")) %>"></div>
                        <div class="form-group"><label for="managerLastName">Last name</label><input class="form-control" id="managerLastName" name="lastName" required minlength="2" maxlength="80" autocomplete="family-name" value="<%= HtmlUtil.escape(request.getAttribute("formLastName")) %>"></div>
                        <div class="form-group full"><label for="managerRegisterEmail">Manager email</label><input class="form-control" id="managerRegisterEmail" name="email" type="email" required maxlength="160" autocomplete="email" value="<%= HtmlUtil.escape(request.getAttribute("formEmail")) %>"></div>
                        <div class="form-group full"><label for="managerPhone">Mobile number</label><input class="form-control" id="managerPhone" name="phone" required maxlength="20" pattern="(?:\+94|0)7[0-9]{8}" autocomplete="tel" value="<%= HtmlUtil.escape(request.getAttribute("formPhone")) %>" placeholder="0771234567"></div>
                        <div class="form-group"><label for="managerNewPassword">Password</label><input class="form-control" id="managerNewPassword" name="password" type="password" required minlength="8" maxlength="128" autocomplete="new-password"></div>
                        <div class="form-group"><label for="managerConfirmPassword">Confirm password</label><input class="form-control" id="managerConfirmPassword" name="confirmPassword" type="password" required minlength="8" maxlength="128" autocomplete="new-password"></div>
                        <div class="form-group full"><label for="managerToken">Manager token</label><input class="form-control" id="managerToken" name="managerToken" type="password" required maxlength="100" autocomplete="off" placeholder="Enter your issued manager token"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary btn-block" type="submit">Create manager account</button></div>
                </form>
                <p class="auth-footer">Already verified? <a href="<%= ctx %>/manager/login">Manager sign in</a></p>
                <p class="auth-footer">Need a customer account? <a href="<%= ctx %>/register">Customer registration</a></p>
            </div>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
