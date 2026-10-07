package com.dinevista.controller;

import com.dinevista.util.FlashUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

final class MissingCustomerRecord {
    private MissingCustomerRecord() {}

    static void returnToDashboard(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        FlashUtil.error(request, "This record is no longer available. You are back on your dashboard.");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
