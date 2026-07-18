package com.dinevista.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

public final class FlashUtil {
    private static final String SUCCESS = "flashSuccess";
    private static final String ERRORS = "flashErrors";

    private FlashUtil() {}

    public static void success(HttpServletRequest request, String message) {
        request.getSession().setAttribute(SUCCESS, message);
    }

    public static void errors(HttpServletRequest request, List<String> errors) {
        request.getSession().setAttribute(ERRORS, errors);
    }

    @SuppressWarnings("unchecked")
    public static void expose(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object success = session.getAttribute(SUCCESS);
        Object errors = session.getAttribute(ERRORS);
        if (success != null) request.setAttribute("successMessage", success);
        if (errors != null) request.setAttribute("errors", (List<String>) errors);
        session.removeAttribute(SUCCESS);
        session.removeAttribute(ERRORS);
    }
}
