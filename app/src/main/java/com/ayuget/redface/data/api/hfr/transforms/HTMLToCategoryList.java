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

import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.util.HTMLUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

/**
 * Parse HFR's forum categories from homepage HTML source.
 *
 * Regex are usually a bad idea to parse HTML, but in this case this is clearly the best option
 * in terms of performance. Jsoup was my first choice, but parsing the source and constructing
 * the DOM tree takes a huge amount of time on a phone (400 ms on my recent Android phone, ...)
 */
public final class HTMLToCategoryList implements Func1<String, List<Category>> {
    private static final Pattern categoryPattern = Pattern.compile(
            "<tr.*?id=\"cat([0-9]+)\".*?" +
            "<td.*?class=\"catCase1\".*?<b><a\\s*href=\"/hfr/([a-zA-Z0-9-]+)/.*?\"\\s*class=\"cCatTopic\">(.+?)</a></b>(.*?)" +
            "</tr>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern subcategoryPattern = Pattern.compile(
            "<a\\s*href=\"/hfr/[a-zA-Z0-9-]+/([a-zA-Z0-9-]+)/.*?\"\\s*class=\"Tableau\">(.+?)</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public List<Category> call(String source) {
        List<Category> categories = new ArrayList<>();

        Matcher m = categoryPattern.matcher(source);

        while (m.find()) {
            int categoryId = Integer.parseInt(m.group(1));
            String categoryName = HTMLUtils.escapeHTML(m.group(3).trim());
            String categorySlug = m.group(2);

            Category category = Category.builder()
                .id(categoryId)
                .name(categoryName)
                .slug(categorySlug)
                .subcategories(Collections.emptyList())
                .build();

            categories.add(category);
        }

        return categories;
    }
}
