package com.ayuget.redface.data.api.hfr.transforms;

import android.text.Html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToBBCode implements Func1<String, String> {
    public static final Pattern POST_CONTENT_PATTERN = Pattern.compile("(?:<textarea)(?:.*)(?:name=\"content_form\")(?:[^>]*)(?:>)(.*?)(?:</textarea>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public String call(String source) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = POST_CONTENT_PATTERN.matcher(source);

        if (matcher.find()) {
            String bbCode = matcher.group(1);

            if (bbCode != null) {
                for (String line : bbCode.split("\n")) {
                    builder.append(Html.fromHtml(line));
                    builder.append("\n");
                }
            }
        }

        return builder.toString();
    }
}
