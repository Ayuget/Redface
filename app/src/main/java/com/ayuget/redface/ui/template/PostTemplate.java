package com.ayuget.redface.ui.template;

import android.content.Context;
import android.text.TextUtils;

import com.ayuget.redface.data.api.model.Post;


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
    protected void render(Post post, String templateContent, StringBuilder stream) {
        String[] variables = {"{author}", "{content}", "{avatar}", "{postedOn}", "{author_id}", "{post_id}", "{post_id_quote}", "{edit_icon}", "{extra_details}"};
        String postId = String.valueOf(post.getId());
        String[] values = {post.getAuthor(), post.getHtmlContent(), avatarTemplate.render(post), formatDate(post.getPostDate()), post.getAuthor(), postId, postId, editIconTemplate.render(post), extraDetailsTemplate.render(post)};

        stream.append(TextUtils.replace(templateContent, variables, values));
    }
}
