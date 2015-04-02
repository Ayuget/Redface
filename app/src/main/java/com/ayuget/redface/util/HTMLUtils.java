package com.ayuget.redface.util;

public class HTMLUtils {
    public static String escapeHTML(String input) {
        return input
                .replaceAll("&amp;", "&")
                .replaceAll("&#034;", "\"")
                .replaceAll("&gt;", ">")
                .replaceAll("&lt;", ">")
                .replaceAll("&euro;", "€");
    }
}
