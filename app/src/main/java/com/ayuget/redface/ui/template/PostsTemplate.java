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

package com.ayuget.redface.ui.template;

import android.content.Context;

import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.TopicPage;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.squareup.phrase.Phrase;

import java.util.Locale;

public class PostsTemplate extends HTMLTemplate<TopicPage> {
    private static final String POSTS_TEMPLATE = "posts.html";

    private PostTemplate postTemplate;

    private ThemeManager themeManager;

    public PostsTemplate(Context context, PostTemplate postTemplate, ThemeManager themeManager) {
        super(context, POSTS_TEMPLATE);
        this.postTemplate = postTemplate;
        this.themeManager = themeManager;
    }

    @Override
    public Phrase compile(String templateContent) {
        return Phrase.from(templateContent)
                .put("css", readAssetFile("styles.css"))
                .put("js", readAssetFile("hfr.js"));
    }

    @Override
    protected void render(TopicPage topicPage, Phrase templateContent, StringBuilder stream) {
        StringBuilder postsBuffer = new StringBuilder();
        for(Post post : topicPage.posts()) {
            postTemplate.render(post, postsBuffer);
        }

        stream.append(
                templateContent
                        .put("posts", postsBuffer.toString())
                        .put("theme_class", themeManager.getActiveThemeCssClass() + " " + themeManager.getFontSizeCssClass() + " " + themeManager.getQuoteStyleExtraClass())
                        .put("target_anchor", getTargetAnchor(topicPage.pageInitialPosition()))
                        .format()
                        .toString()
        );
    }

    private String getTargetAnchor(PagePosition pagePosition) {
        if(pagePosition.isTop()) {
            return "top";
        }
        else if (pagePosition.isBottom()) {
            return "bottom";
        }
        else {
            return String.format(Locale.getDefault(), "post%d", pagePosition.getPostId());
        }
    }
}
