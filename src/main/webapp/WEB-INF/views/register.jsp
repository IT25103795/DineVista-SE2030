<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Create Account"); request.setAttribute("activeNav", ""); %>
<%@ include file="fragments/header.jspf" %>
<section class="auth-page">
    <div class="container">
        <div class="auth-shell">
            <aside class="auth-visual">
                <a class="brand" href="<%= ctx %>/"><img src="<%= ctx %>/assets/images/logo.svg" alt=""><span>Dine<em>Vista</em></span></a>
                <h2>Your DineVista experience starts with one account.</h2>
                <p>A future database-backed account will connect reservations, orders, events, invoices, promotions, and notifications.</p>
                <ul class="check-list"><li>Faster restaurant bookings</li><li>Saved food order history</li><li>Event booking and payment tracking</li></ul>
            </aside>
            <div class="auth-content">
                <span class="section-kicker">Customer registration</span>
                <h1>Create account</h1>
                <p>This polished frontend form is ready to be connected to the user-management CRUD module.</p>
                <form onsubmit="event.preventDefault(); DineVista.toast('Account form validated. Connect this to the user module next.'); this.reset();">
                    <div class="form-grid">
                        <div class="form-group"><label for="firstName">First name</label><input class="form-control" id="firstName" required autocomplete="given-name"></div>
                        <div class="form-group"><label for="lastName">Last name</label><input class="form-control" id="lastName" required autocomplete="family-name"></div>
                        <div class="form-group full"><label for="registerEmail">Email address</label><input class="form-control" id="registerEmail" type="email" required autocomplete="email"></div>
                        <div class="form-group full"><label for="registerPhone">Mobile number</label><input class="form-control" id="registerPhone" required pattern="(?:\\+94|0)7[0-9]{8}" placeholder="0771234567"></div>
                        <div class="form-group"><label for="registerPassword">Password</label><input class="form-control" id="registerPassword" type="password" required minlength="8" autocomplete="new-password"></div>
                        <div class="form-group"><label for="confirmPassword">Confirm password</label><input class="form-control" id="confirmPassword" type="password" required minlength="8" autocomplete="new-password"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary btn-block" type="submit">Create demo account</button></div>
                </form>
                <p class="auth-footer">Already registered? <a href="<%= ctx %>/login">Sign in</a></p>
            </div>
        </div>
    </div>
</section>
<%@ include file="fragments/footer.jspf" %>
