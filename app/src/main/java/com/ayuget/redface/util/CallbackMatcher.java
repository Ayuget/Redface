/*
 * Copyright 2015 Ayuget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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