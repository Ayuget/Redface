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

import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.model.Post;

public class EditIconTemplate extends HTMLTemplate<Post> {
    private static final String EDIT_ICON_TEMPLATE = "edit_icon.html";

    private UserManager userManager;

    public EditIconTemplate(Context context, UserManager userManager) {
        super(context, EDIT_ICON_TEMPLATE);
        this.userManager = userManager;
    }

    @Override
    protected void render(Post post, String templateContent, StringBuilder stream) {
        if (post.getAuthor().equals(userManager.getActiveUser().getUsername())) {
            stream.append(TextUtils.replace(
                    templateContent,
                    new String[]{"{post_id}"},
                    new String[]{String.valueOf(post.getId())}
            ));
        }
        else {
            stream.append("");
        }
    }
}
