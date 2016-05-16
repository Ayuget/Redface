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
import android.util.Pair;

import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.misc.PostAction;
import com.squareup.phrase.Phrase;

import java.util.HashMap;
import java.util.Map;

/**
 * Template used to render individual actions on a post
 */
public class PostActionsTemplate extends HTMLTemplate<Post> {
    private static final String POST_ACTIONS_TEMPLATE = "post_actions.html";

    private UserManager userManager;

    /**
     * Mapping from post actions to Javascript function & icon
     */
    private static final Map<PostAction, Pair<String, String>> POST_ACTIONS_MAPPING;
    static {
        POST_ACTIONS_MAPPING = new HashMap<>(6);
        POST_ACTIONS_MAPPING.put(PostAction.EDIT, Pair.create("editPost", "edit"));
        POST_ACTIONS_MAPPING.put(PostAction.DELETE, Pair.create("deletePost", "trash"));
        POST_ACTIONS_MAPPING.put(PostAction.FAVORITE, Pair.create("markPostAsFavorite", "star"));

        // SOON :O
        // POST_ACTIONS_MAPPING.put(PostAction.REPORT, Pair.create("reportPost", "exclamation-triangle"));
        POST_ACTIONS_MAPPING.put(PostAction.WRITE_PRIVATE_MESSAGE, Pair.create("writePrivateMessage", "envelope"));
        POST_ACTIONS_MAPPING.put(PostAction.COPY_LINK_TO_POST, Pair.create("copyLinkToPost", "link"));
    }

    /**
     * Action HTML code
     */
    private static final String POST_ACTION_HTML = "<li><a material onclick=\"Android.%s(%d)\"><i class=\"fa fa-%s\"></i></a></li>";

    public PostActionsTemplate(Context context, UserManager userManager) {
        super(context, POST_ACTIONS_TEMPLATE);
        this.userManager = userManager;
    }

    private void renderAction(PostAction action, long postId, StringBuilder stream) {
        Pair<String, String> details = POST_ACTIONS_MAPPING.get(action);
        stream.append(String.format(POST_ACTION_HTML, details.first, postId, details.second));
    }

    @Override
    protected void render(Post post, Phrase templateContent, StringBuilder stream) {
        if (userManager.isActiveUser(post.getAuthor())) {
            renderAction(PostAction.EDIT, post.getId(), stream);
            renderAction(PostAction.DELETE, post.getId(), stream);
        }

        renderAction(PostAction.FAVORITE, post.getId(), stream);
        renderAction(PostAction.WRITE_PRIVATE_MESSAGE, post.getId(), stream);
        renderAction(PostAction.COPY_LINK_TO_POST, post.getId(), stream);
    }
}
