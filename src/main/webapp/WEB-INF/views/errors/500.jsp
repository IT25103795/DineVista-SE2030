<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Server Error"); request.setAttribute("activeNav", ""); %>
<%@ include file="../fragments/header.jspf" %>
<section class="error-page"><div><div class="error-code">500</div><h1>The kitchen hit an unexpected problem.</h1><p>Please try again. During development, review the Tomcat log and the related servlet or JSP for the exact error.</p><a class="btn btn-primary" href="<%= ctx %>/">Return home</a></div></section>
<%@ include file="../fragments/footer.jspf" %>
