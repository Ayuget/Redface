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
