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

import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToPostList implements Func1<String, List<Post>> {
    /**
     * Default number of posts per topic page. Used to initialize default capacity
     * for the list of posts (small performance improvement ?)
     */
    private static final int DEFAULT_POSTS_COUNT = 40;

    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile(
            "(?:<meta name=\"Description\" content=\")(?:.*)(?:Pages : )(\\d+)(?:[^\"])"
    );

    private static final Pattern POST_PATTERN = Pattern.compile(
            "(<table\\s*cellspacing.*?class=\"([a-z]+)\">.*?" +
            "<tr.*?class=\"message.*?" +
            "<a.*?href=\"#t([0-9]+)\".*?" +
            "<b.*?class=\"s2\">(?:<a.*?>)?(.*?)(?:</a>)?</b>.*?" +
            "(?:(?:<div\\s*class=\"avatar_center\".*?><img src=\"(.*?)\"\\s*alt=\".*?\"\\s*/></div>)|</td>).*?" +
            "<div.*?class=\"left\">Posté le ([0-9]+)-([0-9]+)-([0-9]+).*?([0-9]+):([0-9]+):([0-9]+).*?" +
            "<div.*?id=\"para[0-9]+\">(.*?)<div style=\"clear: both;\">\\s*</div></p>" +
            "(?:<div\\s*class=\"edited\">)?(?:<a.*?>Message cité ([0-9]+) fois</a>)?(?:<br\\s*/>Message édité par .*? le ([0-9]+)-([0-9]+)-([0-9]+).*?([0-9]+):([0-9]+):([0-9]+)</div>)?.*?" +
            "</div></td></tr></table>)"
            , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public List<Post> call(String source) {
        List<Post> posts = new ArrayList<>(DEFAULT_POSTS_COUNT);

        // Description tag parsing to find the total number of pages. If
        int topicPagesCount = UIConstants.UNKNOWN_PAGES_COUNT;
        Matcher pagesMatcher = DESCRIPTION_PATTERN.matcher(source);

        if (pagesMatcher.find()) {
            topicPagesCount = Integer.valueOf(pagesMatcher.group(1));
        }

        Matcher m = POST_PATTERN.matcher(source);

        while (m.find()) {
            long postId = Long.parseLong(m.group(3));
            String postHTMLContent = m.group(12);
            Date postDate = DateUtils.fromHTMLDate(m.group(8), m.group(7), m.group(6), m.group(9), m.group(10), m.group(11));
            Date lastEditDate = null;
            int quoteCount = 0;
            String author = m.group(4);
            String avatarUrl = m.group(5);
            boolean wasEdited = m.group(14) != null;
            boolean wasQuoted = m.group(13) != null;

            if (wasEdited) {
                lastEditDate = DateUtils.fromHTMLDate(m.group(16), m.group(15), m.group(14), m.group(17), m.group(18), m.group(19));
            }

            if (wasQuoted) {
                quoteCount = Integer.parseInt(m.group(13));
            }

            Post post = new Post(postId);
            post.setHtmlContent(postHTMLContent);
            post.setAuthor(author);
            post.setAvatarUrl(avatarUrl);
            post.setLastEditionDate(lastEditDate);
            post.setPostDate(postDate);
            post.setQuoteCount(quoteCount);

            if (topicPagesCount != UIConstants.UNKNOWN_PAGES_COUNT) {
                post.setTopicPagesCount(topicPagesCount);
            }

            posts.add(post);
        }

        return posts;
    }
}
