<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Page Not Found"); request.setAttribute("activeNav", ""); %>
<%@ include file="../fragments/header.jspf" %>
<section class="error-page"><div><div class="error-code">404</div><h1>That page is not on the menu.</h1><p>The address may be incorrect or the page may have moved. Let us guide you back to the DineVista experience.</p><a class="btn btn-primary" href="<%= ctx %>/">Return home</a></div></section>
<%@ include file="../fragments/footer.jspf" %>
