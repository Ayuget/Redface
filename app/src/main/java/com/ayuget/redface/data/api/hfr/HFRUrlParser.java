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

package com.ayuget.redface.data.api.hfr;

import android.net.Uri;

import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.MDLink;
import com.ayuget.redface.data.api.UrlParser;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.network.HTTPRedirection;
import com.ayuget.redface.ui.misc.PagePosition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Analyzes an internal URL (i.e. a forum URL) and extracts useful information from it (topic id,
 * page number, ...)
 */
public class HFRUrlParser implements UrlParser {
    /**
     * Pattern used to detect URLs that are, in the end, redirected. We need to resolve the
     * redirection, otherwise the user will be send to a wrong location (those URLs usually only
     * have a post id, and a page number set to 1)
     */
    private static final Pattern REDIRECTED_URL_PATTERN = Pattern.compile("(?:.*)(?:numreponse=)(\\d+)(?:.*)");

    /**
     * Regex to match a rewritten URL.
     */
    private static final String REWRITTEN_TOPIC_REGEX = "([^\\/]+)(?:\\/)(?:([^\\/]+)(?:\\/))?(?:[^\\_]+)(?:_)(\\d+)(?:_)(\\d+)(?:\\.htm)";

    private final Pattern rewrittenTopicPattern;

    private final String baseRewrittenUrlRegex;

    private final String baseStandardUrlRegex;

    private final MDEndpoints mdEndpoints;

    private final CategoriesStore categoriesStore;

    @Inject
    public HFRUrlParser(MDEndpoints mdEndpoints, CategoriesStore categoriesStore) {
        this.mdEndpoints = mdEndpoints;
        this.categoriesStore = categoriesStore;

        String baseRewrittenRegex = "(?:" + Pattern.quote(mdEndpoints.baseurl() + "/hfr/") + ")";
        baseRewrittenUrlRegex = Pattern.quote(mdEndpoints.baseurl()) + "/hfr/.*";

        baseStandardUrlRegex = Pattern.quote(mdEndpoints.baseurl()) + "/forum.*";
        rewrittenTopicPattern = Pattern.compile(baseRewrittenRegex + REWRITTEN_TOPIC_REGEX);
    }

    @Override
    public Observable<MDLink> parseUrl(String url) {
        if (url.matches(baseRewrittenUrlRegex)) {
            return Observable.just(parseRewrittenUrl(url));
        }
        else if (url.matches(baseStandardUrlRegex)){
            Matcher redirectedMatcher = REDIRECTED_URL_PATTERN.matcher(url);

            if (redirectedMatcher.matches() && !redirectedMatcher.group(1).equals("0")) {
                return HTTPRedirection.resolve(url)
                        .map(new Func1<String, MDLink>() {
                            @Override
                            public MDLink call(String targetUrl) {
                                return parseRewrittenUrl(targetUrl);
                            }
                        });
            }
            else {
                return Observable.just(parseStandardUrl(url));
            }
        }
        else {
            return Observable.just(MDLink.invalid());
        }
    }

    public MDLink parseRewrittenUrl(String url) {
        Timber.d("Parsing rewritten topic URL : %s", url);

        // Split url and anchor
        String[] urlParts = url.split("#");
        url = urlParts[0];
        String anchor = urlParts.length >= 2 ? urlParts[1] : null;

        Matcher rewrittenTopicMatcher = rewrittenTopicPattern.matcher(url);

        if (rewrittenTopicMatcher.matches()) {
            boolean hasSubCat = rewrittenTopicMatcher.groupCount() == 4;
            int subcatOffset = hasSubCat ? 1 : 0;

            String categorySlug = rewrittenTopicMatcher.group(1);
            String subcategorySlug = hasSubCat ? rewrittenTopicMatcher.group(2) : null;
            int topicId = Integer.parseInt(rewrittenTopicMatcher.group(subcatOffset + 2));
            int pageNumber = Integer.parseInt(rewrittenTopicMatcher.group(subcatOffset + 3));

            Timber.d("Rewritten topic URL : %s, category : %s, subcategory : %s, topicId : %d, pageNumber : %d", url, categorySlug, subcategorySlug, topicId, pageNumber);

            Category topicCategory = categoriesStore.getCategoryBySlug(categorySlug);

            if (topicCategory == null) {
                Timber.e("Category '%s' is unknown", categorySlug);
                return MDLink.invalid();
            }
            else {
                Timber.d("Link is for category '%s'", topicCategory.name());
                return MDLink.forTopic(topicCategory, topicId)
                        .atPage(pageNumber)
                        .atPost(parseAnchor(anchor))
                        .build();
            }
        }
        else {
            return MDLink.invalid();
        }
    }

    public MDLink parseStandardUrl(String url) {
        Timber.d("Parsing standard topic URL : %s", url);

        Uri parsedUri = Uri.parse(url);

        String categoryId = parsedUri.getQueryParameter("cat");
        String pageNumber = parsedUri.getQueryParameter("page");
        String topicId = parsedUri.getQueryParameter("post");
        String anchor = parsedUri.getFragment();

        // Set default page number as 1 (first page)
        if (pageNumber == null) { pageNumber = "1"; }

        if (categoryId == null || topicId == null) {
            Timber.e("URL '%s' is invalid, category, or topic id not found", url);
            return MDLink.invalid();
        }

        try {
            Category topicCategory = categoriesStore.getCategoryById(Integer.parseInt(categoryId));
            if (topicCategory == null) {
                Timber.e("Category with id '%s' is unknown", categoryId);
                return MDLink.invalid();
            } else {
                Timber.d("Link is for category '%s'", topicCategory.name());
                return MDLink.forTopic(topicCategory, Integer.parseInt(topicId))
                        .atPage(Integer.parseInt(pageNumber))
                        .atPost(parseAnchor(anchor))
                        .build();
            }
        }
        catch (NumberFormatException e) {
            Timber.e(e, "Error while parsing standard URL");
            return MDLink.invalid();
        }
    }

    public PagePosition parseAnchor(String anchor) {
        if (anchor == null || ! (anchor.length() > 1 && anchor.charAt(0) == 't')) {
            Timber.w("Anchor '%s' is invalid", anchor);
            return new PagePosition(PagePosition.TOP);
        }
        else {
            return new PagePosition(Integer.valueOf(anchor.substring(1)));
        }
    }
}
