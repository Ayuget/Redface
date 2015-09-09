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

import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.model.Post;
import com.squareup.phrase.Phrase;


public class PostTemplate extends HTMLTemplate<Post> {
    private static final String LOG_TAG = PostTemplate.class.getSimpleName();

    private static final String POST_TEMPLATE = "post.html";

    private AvatarTemplate avatarTemplate;

    private PostExtraDetailsTemplate extraDetailsTemplate;

    private PostActionsTemplate postActionsTemplate;

    private QuickActionsTemplate quickActionsTemplate;

    private UserManager userManager;

    public PostTemplate(Context context, UserManager userManager, AvatarTemplate avatarTemplate, PostExtraDetailsTemplate extraDetailsTemplate, PostActionsTemplate postActionsTemplate, QuickActionsTemplate quickActionsTemplate) {
        super(context, POST_TEMPLATE);
        this.userManager = userManager;
        this.avatarTemplate = avatarTemplate;
        this.extraDetailsTemplate = extraDetailsTemplate;
        this.postActionsTemplate = postActionsTemplate;
        this.quickActionsTemplate = quickActionsTemplate;
    }

    @Override
    protected void render(Post post, Phrase templateContent, StringBuilder stream) {
        String postId = String.valueOf(post.getId());

        stream.append(
                templateContent
                        .put("author", post.getAuthor())
                        .put("content", post.getHtmlContent())
                        .put("avatar", avatarTemplate.render(post))
                        .put("posted_on", formatDate(post.getPostDate()))
                        .put("author_id", post.getAuthor())
                        .put("post_id", postId)
                        .put("post_quick_actions", quickActionsTemplate.render(post))
                        .put("extra_details", extraDetailsTemplate.render(post))
                        .put("post_actions", userManager.activeUserIsLoggedIn() ? postActionsTemplate.render(post) : "")
                        .format()
                        .toString()
        );
    }
}
