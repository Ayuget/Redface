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
import android.text.TextUtils;

import com.ayuget.redface.data.api.model.Post;
import com.squareup.phrase.Phrase;


public class PostTemplate extends HTMLTemplate<Post> {
    private static final String LOG_TAG = PostTemplate.class.getSimpleName();

    private static final String POST_TEMPLATE = "post.html";

    private AvatarTemplate avatarTemplate;

    private EditIconTemplate editIconTemplate;

    private PostExtraDetailsTemplate extraDetailsTemplate;

    public PostTemplate(Context context, AvatarTemplate avatarTemplate, EditIconTemplate editIconTemplate, PostExtraDetailsTemplate extraDetailsTemplate) {
        super(context, POST_TEMPLATE);
        this.avatarTemplate = avatarTemplate;
        this.editIconTemplate = editIconTemplate;
        this.extraDetailsTemplate = extraDetailsTemplate;
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
                        .put("post_id_quote", postId)
                        .put("edit_icon", editIconTemplate.render(post))
                        .put("extra_details", extraDetailsTemplate.render(post))
                        .format()
                        .toString()
        );
    }
}
