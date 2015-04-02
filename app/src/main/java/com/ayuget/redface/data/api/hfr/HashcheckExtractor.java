package com.ayuget.redface.data.api.hfr;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashcheckExtractor {
    private static final String LOG_TAG = HashcheckExtractor.class.getSimpleName();

    private static final Pattern HASHCHECK_PATTERN = Pattern.compile("<input\\s*type=\"hidden\"\\s*name=\"hash_check\"\\s*value=\"(.+?)\" />", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static String extract(String htmlContent) {
        Matcher m = HASHCHECK_PATTERN.matcher(htmlContent);

        if (m.find()) {
            String hashCheck = m.group(1) != null ? m.group(1) : m.group(2);
            Log.d(LOG_TAG, String.format("Hashcheck = %s", hashCheck));
            return hashCheck;
        }
        else {
            return null;
        }
    }
}
