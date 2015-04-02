package com.ayuget.redface.ui.template;

import android.content.Context;
import android.text.TextUtils;

import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.ui.misc.ThemeManager;

import java.util.List;

public class PostsTemplate extends HTMLTemplate<List<Post>> {
    private static final String LOG_TAG = PostsTemplate.class.getSimpleName();

    private static final String POSTS_TEMPLATE = "posts.html";

    private PostTemplate postTemplate;

    private ThemeManager themeManager;

    public PostsTemplate(Context context, PostTemplate postTemplate, ThemeManager themeManager) {
        super(context, POSTS_TEMPLATE);
        this.postTemplate = postTemplate;
        this.themeManager = themeManager;
    }

    @Override
    public String compile(String templateContent) {
        return TextUtils.replace(
                templateContent,
                new String[]{"{css}", "{js}"},
                new String[]{readAssetFile("styles.css"), readAssetFile("hfr.js")}
        ).toString();
    }

    @Override
    protected void render(List<Post> content, String templateContent, StringBuilder stream) {
        StringBuilder postsBuffer = new StringBuilder();
        for(Post post : content) {
            postTemplate.render(post, postsBuffer);
        }

        stream.append(TextUtils.replace(
                templateContent,
                new String[]{"{posts}", "{theme_class}"},
                new String[]{postsBuffer.toString(), themeManager.getActiveThemeCssClass()}
        ));
    }
}
