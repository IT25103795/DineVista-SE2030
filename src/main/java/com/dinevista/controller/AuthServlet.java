package com.dinevista.controller;

import com.dinevista.model.UserAccountRecord;
import com.dinevista.service.AccountService;
import com.dinevista.service.OperationResult;
import com.dinevista.util.AccountContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {"/login", "/manager/login", "/register", "/manager/register"})
public class AuthServlet extends HttpServlet {
    private AccountService accountService;

    @Override
    public void init() {
        accountService = AccountContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/manager/login".equals(path) && "manager".equalsIgnoreCase(request.getParameter("required"))) {
            request.setAttribute("loginNotice", "Sign in as Manager to access restaurant operations.");
        }
        if (request.getParameter("registered") != null) {
            request.setAttribute("loginNotice", "Account created successfully. You can sign in now.");
        }
        request.getRequestDispatcher(viewFor(path)).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            if (path.endsWith("/register")) {
                register(request, response, path);
            } else {
                login(request, response, path);
            }
        } catch (RuntimeException ex) {
            getServletContext().log("Authentication operation failed", ex);
            request.setAttribute("authErrors", java.util.Collections.singletonList(
                    "Account service is temporarily unavailable. Please try again."));
            retainSafeFields(request);
            request.getRequestDispatcher(viewFor(path)).forward(request, response);
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response, String path)
            throws ServletException, IOException {
        String role = path.startsWith("/manager/") ? AccountService.MANAGER : AccountService.CUSTOMER;
        String email = value(request.getParameter("email"));
        OperationResult<UserAccountRecord> result = accountService.authenticate(
                email, request.getParameter("password"), role);
        if (!result.isSuccess()) {
            request.setAttribute("authErrors", result.getErrors());
            request.setAttribute("formEmail", email);
            request.getRequestDispatcher(viewFor(path)).forward(request, response);
            return;
        }

        HttpSession previous = request.getSession(false);
        if (previous != null) previous.invalidate();
        UserAccountRecord account = result.getValue();
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", account.getUserId());
        session.setAttribute("demoRole", account.getRole().toLowerCase(java.util.Locale.ROOT));
        session.setAttribute("demoEmail", account.getEmail());
        session.setAttribute("displayName", account.getDisplayName());
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private void register(HttpServletRequest request, HttpServletResponse response, String path)
            throws ServletException, IOException {
        String role = path.startsWith("/manager/") ? AccountService.MANAGER : AccountService.CUSTOMER;
        OperationResult<UserAccountRecord> result = accountService.register(
                role,
                request.getParameter("firstName"), request.getParameter("lastName"),
                request.getParameter("email"), request.getParameter("phone"),
                request.getParameter("password"), request.getParameter("confirmPassword"),
                request.getParameter("managerToken"));
        if (!result.isSuccess()) {
            request.setAttribute("authErrors", result.getErrors());
            retainSafeFields(request);
            request.getRequestDispatcher(viewFor(path)).forward(request, response);
            return;
        }
        String loginPath = AccountService.MANAGER.equals(role) ? "/manager/login" : "/login";
        response.sendRedirect(request.getContextPath() + loginPath + "?registered=1");
    }

    private void retainSafeFields(HttpServletRequest request) {
        request.setAttribute("formFirstName", value(request.getParameter("firstName")));
        request.setAttribute("formLastName", value(request.getParameter("lastName")));
        request.setAttribute("formEmail", value(request.getParameter("email")));
        request.setAttribute("formPhone", value(request.getParameter("phone")));
    }

    private String viewFor(String path) {
        switch (path) {
            case "/manager/login": return "/WEB-INF/views/manager-login.jsp";
            case "/manager/register": return "/WEB-INF/views/manager-register.jsp";
            case "/register": return "/WEB-INF/views/register.jsp";
            default: return "/WEB-INF/views/login.jsp";
        }
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
