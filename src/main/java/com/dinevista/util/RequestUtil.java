package com.dinevista.util;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class RequestUtil {
    private RequestUtil() {}

    public static String clean(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    public static int integer(HttpServletRequest request, String name, int fallback) {
        try {
            return Integer.parseInt(clean(request, name));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static long longValue(HttpServletRequest request, String name, long fallback) {
        try {
            return Long.parseLong(clean(request, name));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public static LocalDate date(HttpServletRequest request, String name) {
        try {
            return LocalDate.parse(clean(request, name));
        } catch (Exception ex) {
            return null;
        }
    }

    public static LocalTime time(HttpServletRequest request, String name) {
        try {
            return LocalTime.parse(clean(request, name));
        } catch (Exception ex) {
            return null;
        }
    }

    public static LocalDateTime dateTime(HttpServletRequest request, String name) {
        try {
            return LocalDateTime.parse(clean(request, name));
        } catch (Exception ex) {
            return null;
        }
    }
}
