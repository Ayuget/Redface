package com.ayuget.redface.ui.template;

import android.content.Context;
import android.text.TextUtils;

import com.ayuget.redface.data.api.model.Post;

public class PostExtraDetailsTemplate extends HTMLTemplate<Post> {
    private static final String EXTRA_DETAILS_TEMPLATE = "extra_details.html";

    public PostExtraDetailsTemplate(Context context) {
        super(context, EXTRA_DETAILS_TEMPLATE);
    }

    @Override
    protected void render(Post post, String templateContent, StringBuilder stream) {
        if (post.getQuoteCount() > 0) {
            stream.append(TextUtils.replace(
                    templateContent,
                    new String[]{"{quoteCount}"},
                    new String[]{String.valueOf(post.getQuoteCount())}
            ));
        }
        else {
            stream.append("");
        }
    }
}
