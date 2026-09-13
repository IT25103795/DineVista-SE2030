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

@WebServlet(urlPatterns = {"/profile/edit", "/profile/password", "/profile/delete"})
public class ProfileServlet extends HttpServlet {
    private AccountService accountService;

    @Override
    public void init() {
        accountService = AccountContext.service(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        long userId = (Long) session.getAttribute("userId");
        String path = request.getServletPath();

        if ("/profile/password".equals(path)) {
            handlePasswordChange(request, response, session, userId);
        } else if ("/profile/delete".equals(path)) {
            handleDeleteAccount(request, response, session, userId);
        } else {
            handleProfileEdit(request, response, session, userId);
        }
    }

    private void handleProfileEdit(HttpServletRequest request, HttpServletResponse response,
                                   HttpSession session, long userId)
            throws IOException {
        String firstName = value(request.getParameter("firstName"));
        String lastName  = value(request.getParameter("lastName"));
        String phone     = value(request.getParameter("phone"));

        OperationResult<UserAccountRecord> result = accountService.updateProfile(userId, firstName, lastName, phone);
        if (result.isSuccess()) {
            session.setAttribute("displayName", result.getValue().getDisplayName());
            redirect(request, response, "profileSuccess", "Your profile has been updated successfully.");
        } else {
            redirect(request, response, "profileError", String.join(" ", result.getErrors()));
        }
    }

    private void handlePasswordChange(HttpServletRequest request, HttpServletResponse response,
                                      HttpSession session, long userId)
            throws IOException {
        String current  = request.getParameter("currentPassword");
        String newPwd   = request.getParameter("newPassword");
        String confirm  = request.getParameter("confirmPassword");

        OperationResult<Void> result = accountService.changePassword(userId, current, newPwd, confirm);
        if (result.isSuccess()) {
            redirect(request, response, "passwordSuccess", "Your password has been changed successfully.");
        } else {
            redirect(request, response, "passwordError", String.join(" ", result.getErrors()));
        }
    }

    private void handleDeleteAccount(HttpServletRequest request, HttpServletResponse response,
                                     HttpSession session, long userId) throws IOException {
        try {
            accountService.deleteAccount(userId);
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?deleted=1");
        } catch (RuntimeException ex) {
            redirect(request, response, "profileError", "Could not delete account: " + ex.getMessage());
        }
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String flashKey, String message) throws IOException {
        response.sendRedirect(request.getContextPath() + "/dashboard?" + flashKey + "=" +
                java.net.URLEncoder.encode(message, "UTF-8"));
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
