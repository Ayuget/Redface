package com.ayuget.redface.ui.template;

import android.content.Context;
import android.text.TextUtils;

import com.ayuget.redface.data.api.model.Post;


public class AvatarTemplate extends HTMLTemplate<Post> {
    private static final String LOG_TAG = AvatarTemplate.class.getSimpleName();

    private static final String AVATAR_TEMPLATE = "avatar.html";

    public AvatarTemplate(Context context) {
        super(context, AVATAR_TEMPLATE);
    }

    @Override
    protected void render(Post post, String templateContent, StringBuilder stream) {
        if (post.getAvatarUrl() == null) {
            stream.append("");
        }
        else {
            stream.append(TextUtils.replace(
                    templateContent,
                    new String[]{"{avatarUrl}"},
                    new String[]{post.getAvatarUrl()}
            ));
        }
    }
}
