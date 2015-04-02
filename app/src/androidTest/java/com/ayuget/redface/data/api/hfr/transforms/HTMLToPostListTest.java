package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.data.api.model.Post;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HTMLToPostListTest extends BaseTestCase {
    public void test_parsePosts() throws IOException {
        HTMLToPostList htmlToPostList = new HTMLToPostList();

        List<Post> posts = htmlToPostList.call(readAssetFile("hfr_posts_page.html"));

        // 40 posts per page
        assertThat(posts.size()).isEqualTo(40);
    }
}
