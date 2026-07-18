package com.dinevista.util;

public final class HtmlUtil {
    private HtmlUtil() {}

    public static String escape(Object value) {
        if (value == null) return "";
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String selected(String expected, String actual) {
        return expected != null && expected.equals(actual) ? "selected" : "";
    }
}
