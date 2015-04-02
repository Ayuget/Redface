package com.ayuget.redface.util;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallbackMatcher {
    public static interface Callback {
        public String foundMatch(final MatchResult matchResult);
    }

    private final Pattern pattern;

    public CallbackMatcher(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public CallbackMatcher(String regex, int flags) {
        this.pattern = Pattern.compile(regex, flags);
    }

    public String replaceAll(String input, Callback callback) {
        final Matcher matcher = this.pattern.matcher(input);
        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            final MatchResult matchResult = matcher.toMatchResult();
            final String replacement = callback.foundMatch(matchResult);
            matcher.appendReplacement(output, replacement);
        }

        matcher.appendTail(output);

        return output.toString();
    }
}