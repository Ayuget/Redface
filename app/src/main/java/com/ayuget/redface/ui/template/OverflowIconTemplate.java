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

public class OverflowIconTemplate extends HTMLTemplate<Post> {
    private static final String OVERFLOW_TEMPLATE = "overflow_icon.html";

    private UserManager userManager;

    public OverflowIconTemplate(Context context, UserManager userManager) {
        super(context, OVERFLOW_TEMPLATE);
        this.userManager = userManager;
    }

    @Override
    protected void render(Post post, Phrase templateContent, StringBuilder stream) {
        stream.append(templateContent.put("post_id", String.valueOf(post.getId())).format().toString());
    }
}
