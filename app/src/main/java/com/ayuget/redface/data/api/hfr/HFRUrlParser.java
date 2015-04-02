package com.ayuget.redface.data.api.hfr;

import android.util.Log;

import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.MDLink;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.UrlParser;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.ui.misc.PagePosition;
import com.squareup.otto.Bus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class HFRUrlParser implements UrlParser {
    private static final String LOG_TAG = HFRUrlParser.class.getSimpleName();

    private static final String REWRITTEN_TOPIC_LIST_REGEX = "([^\\/]+)(?:\\/)(?:([^\\/]+)(?:\\/))?(?:liste_sujet-)(\\d+)(?:.*)";

    private static final String REWRITTEN_TOPIC_REGEX = "([^\\/]+)(?:\\/)(?:([^\\/]+)(?:\\/))?(?:[^\\_]+)(?:_)(\\d+)(?:_)(\\d+)(?:\\.htm)";

    private static final String STANDARD_TOPIC_REGEX = "(?:\\/forum2\\.php\\?)(.*)";

    private final Pattern rewrittenTopicListPattern;

    private final Pattern rewrittenTopicPattern;

    private final Pattern standardTopicPattern;

    private final MDEndpoints mdEndpoints;

    private final MDService mdService;

    private final Bus bus;

    private final CategoriesStore categoriesStore;

    @Inject
    public HFRUrlParser(MDEndpoints mdEndpoints, MDService mdService, Bus bus, CategoriesStore categoriesStore) {
        this.mdEndpoints = mdEndpoints;
        this.mdService = mdService;
        this.bus = bus;
        this.categoriesStore = categoriesStore;

        String baseRewrittenRegex = "(?:" + Pattern.quote(mdEndpoints.baseurl() + "/hfr/") + ")";
        String baseStandardRegex = "(?:" + Pattern.quote(mdEndpoints.baseurl()) + ")";

        rewrittenTopicListPattern = Pattern.compile(baseRewrittenRegex + REWRITTEN_TOPIC_LIST_REGEX);
        rewrittenTopicPattern = Pattern.compile(baseRewrittenRegex + REWRITTEN_TOPIC_REGEX);
        standardTopicPattern = Pattern.compile(baseStandardRegex + STANDARD_TOPIC_REGEX);
    }

    @Override
    public MDLink parseUrl(String url) {
        // Split url and anchor
        String[] urlParts = url.split("#");
        url = urlParts[0];
        String anchor = urlParts.length >= 2 ? urlParts[1] : null;

        Matcher rewrittenTopicListMatcher = rewrittenTopicListPattern.matcher(url);
        if (rewrittenTopicListMatcher.matches()) {
            Log.d(LOG_TAG, String.format("Rewritten topic list URL : %s", url));
            return MDLink.invalid();
        }

        Matcher rewrittenTopicMatcher = rewrittenTopicPattern.matcher(url);
        if (rewrittenTopicMatcher.matches()) {
            boolean hasSubCat = rewrittenTopicMatcher.groupCount() == 4;
            int subcatOffset = hasSubCat ? 1 : 0;

            String categorySlug = rewrittenTopicMatcher.group(1);
            String subcategorySlug = hasSubCat ? rewrittenTopicMatcher.group(2) : null;
            int topicId = Integer.parseInt(rewrittenTopicMatcher.group(subcatOffset + 2));
            int pageNumber = Integer.parseInt(rewrittenTopicMatcher.group(subcatOffset + 3));

            Log.d(LOG_TAG, String.format("Rewritten topic URL : %s, category : %s, subcategory : %s, topicId : %d, pageNumber : %d", url, categorySlug, subcategorySlug, topicId, pageNumber));

            Category topicCategory = categoriesStore.getCategoryBySlug(categorySlug);

            if (topicCategory == null) {
                Log.e(LOG_TAG, String.format("Category '%s' is unknown", categorySlug));
                return MDLink.invalid();
            }
            else {
                Log.d(LOG_TAG, String.format("Link is for category '%s'", topicCategory.getName()));
                return MDLink.forTopic(topicCategory, topicId)
                        .atPage(pageNumber)
                        .atPost(parseAnchor(anchor))
                        .build();
            }
        }

        Matcher standardTopicMatcher = standardTopicPattern.matcher(url);
        if (standardTopicMatcher.matches()) {
            Log.d(LOG_TAG, String.format("Standard topic URL : %s", url));
        }

        return MDLink.invalid();
    }

    public PagePosition parseAnchor(String anchor) {
        if (anchor == null || ! (anchor.length() > 1 && anchor.charAt(0) == 't')) {
            Log.e(LOG_TAG, String.format("Anchor '%s' is invalid", anchor));
            return new PagePosition(PagePosition.TOP);
        }
        else {
            return new PagePosition(Integer.valueOf(anchor.substring(1)));
        }
    }
}
