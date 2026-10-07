package com.dinevista.controller;

import com.dinevista.util.FlashUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** A saved manager detail URL can outlive a record deleted from another session. */
final class MissingManagerRecord {
    private MissingManagerRecord() {}

    static void returnToDashboard(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        FlashUtil.error(request, "This record is no longer available. You are back on the dashboard.");
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
