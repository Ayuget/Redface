package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.util.HTMLUtils;

import java.util.ArrayList;
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

            // Parse subcategories directly, and save an HTTP request !
            List<Subcategory> subcategories = new ArrayList<>();
            String subcatsHTML = m.group(4);

            Matcher submatchs = subcategoryPattern.matcher(subcatsHTML);
            while (submatchs.find()) {
                Subcategory subcategory = Subcategory.create(HTMLUtils.escapeHTML(submatchs.group(2)), submatchs.group(1));
                subcategories.add(subcategory);
            }

            Category category = Category.create(categoryId, categoryName, categorySlug, subcategories);
            categories.add(category);
        }

        return categories;
    }
}
