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

package com.ayuget.redface.data.api.hfr.transforms;

import android.text.Html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToBBCode implements Func1<String, String> {
    public static final Pattern POST_CONTENT_PATTERN = Pattern.compile("<textarea.*name=\"content_form\"[^>]*>(.*?)</textarea>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public String call(String source) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = POST_CONTENT_PATTERN.matcher(source);

        if (matcher.find()) {
            String bbCode = matcher.group(1);

            if (bbCode != null) {
                for (String line : bbCode.split("\\R")) {
                    // fromHtml collapses white spaces, as intended (https://www.w3.org/TR/html401/struct/text.html#whitespace).
                    // HFR does not, it renders every spaces. It could be important as some users could use whitespaces for formatting.
                    var temp = Html.fromHtml(line.replace(" ", "&nbsp;"));
                    var tempString = temp.toString().replace(" ", " ");
                    builder.append(tempString);
                    builder.append("\n");
                }
            }
        }

        return builder.toString();
    }
}
