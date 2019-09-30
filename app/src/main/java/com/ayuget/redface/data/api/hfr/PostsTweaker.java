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

import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.settings.Blacklist;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.util.CallbackMatcher;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * Adapts posts to the mobile app : converts internal links to be directly handled
 * by the app, handles images cache, images download settings, ...
 */
public final class PostsTweaker {
    private static final String REGULAR_LINK_REGEX = "<a\\s*href=\"(https|http)(://forum\\.hardware\\.fr.*?)\"\\s*target=\"_blank\"\\s*class=\"cLink\">";
    private static final CallbackMatcher SMILEYS_REGEX = new CallbackMatcher("<img\\s*src=\"(https://forum\\-images\\.hardware\\.fr.*?)\"\\s*alt=\"(.*?)\".*?/>");
    private static final CallbackMatcher IMAGES_REGEX = new CallbackMatcher("<img\\s*src=\"https?://[^\"]*?\"\\s*alt=\"https?://[^\"]*?\"\\s*title=\"(https?://.*?)\".*?/>", Pattern.DOTALL);
    private static final CallbackMatcher QUOTES_AND_SPOILERS = new CallbackMatcher("(?:<div class=\\\"container\\\">\\s*<table class=\\\")(oldcitation|citation|spoiler|quote)(?:[^>]+)(?:>\\s*)(?:.*?)(?:<b class=\\\")(s1|s1Topic)(?:\\\">)(?:(?:<a href=\")([^\\\"]+)(?:\")(?:[^>]+)(?:>))?(.+?:)?", Pattern.DOTALL);
    private static final CallbackMatcher END_OF_QUOTES = new CallbackMatcher("(?:\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>)", Pattern.DOTALL);
    private static final Pattern AUTHOR_NAME = Pattern.compile("(.+?) a écrit :");

    private final MDEndpoints mdEndpoints;
    private final Blacklist blacklist;
    private final RedfaceSettings appSettings;

    @Inject
    public PostsTweaker(MDEndpoints mdEndpoints, Blacklist blacklist, RedfaceSettings appSettings) {
        this.mdEndpoints = mdEndpoints;
        this.blacklist = blacklist;
        this.appSettings = appSettings;
    }

    public List<Post> tweak(List<Post> posts, boolean imagesEnabled, boolean avatarsEnabled, boolean smileysEnabled) {
        for (final Post post : posts) {
            String htmlContent = post.getHtmlContent();

            // Adds callbacks to directly handle all links (internal and external) within the app.

            // Regular links (within posts)
            htmlContent = htmlContent.replaceAll(REGULAR_LINK_REGEX, "<a onclick=\"handleUrl(event, " + post.getId() + ", '$1$2');\" class=\"cLink\">");

            // Simplify quotes HTML
            htmlContent = QUOTES_AND_SPOILERS.replaceAll(htmlContent, matchResult -> {
                Matcher author_matcher = AUTHOR_NAME.matcher(matchResult.group(4));
                boolean isOldQuote = matchResult.group(1).equals("quote");
                boolean isQuote = matchResult.group(1).equals("citation") || matchResult.group(1).equals("oldcitation");
                boolean isBlocked = isQuote && author_matcher.matches() && appSettings.isBlacklistEnabled() && blacklist.isAuthorBlocked(author_matcher.group(1));

                String onClickEvent = isQuote ? (isBlocked ? " onClick=\"showBlockedQuote(this)\"" : "") : " onClick=\"toggleSpoiler(this)\"";

                String quote = (isQuote || isOldQuote) ? "quote" : "spoiler";

                String user;
                if (isBlocked) {
                    quote += appSettings.showBlockedUser() ? " blocked" : " hidden";
                    user = author_matcher.group(1) + " a été bloqué";
                } else {
                    user = matchResult.group(4);
                }

                String output = "<div class=\"" + quote + "\"" + onClickEvent + "><b class=\"" + ((isQuote || isOldQuote) ? "s1" : "s1Topic") + "\">";

                if (isQuote) {
                    output += "<a onclick=\"handleUrl(event, " + post.getId() + ", '" + mdEndpoints.baseurl() + matchResult.group(3) + "')\">";
                }

                output += user;

                return output;
            });

            htmlContent = END_OF_QUOTES.replaceAll(htmlContent, matchResult -> "");

            // Display images on condition
            if (!imagesEnabled) {
                htmlContent = IMAGES_REGEX.replaceAll(htmlContent, matchResult ->
                        "<a href=\"" + matchResult.group(1) + "\" target=\"_blank\" class=\"cLink\">" + matchResult.group(1) + "</a>");
            }

            if (!avatarsEnabled) {
                post.setAvatarUrl("");
            }

            // Handle smileys. Smileys can be disabled in the settings, and (if not disabled of course)
            // can be cached to avoid unnecessary and expensive network requests.
            htmlContent = SMILEYS_REGEX.replaceAll(htmlContent, matchResult -> {
                if (smileysEnabled) {
                    // todo handle smileys caching
                    String smileyUrl = matchResult.group(1);
                    return matchResult.group();
                } else {
                    return matchResult.group(2);
                }
            });

            post.setHtmlContent(htmlContent);
        }

        return posts;
    }
}
