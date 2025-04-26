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

package com.ayuget.redface.data.api.hfr.transforms;


import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.data.api.model.Post;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HTMLToPostListTest extends BaseTestCase {
    @Test
    public void test_parsePosts() throws IOException {
        HTMLToPostList htmlToPostList = new HTMLToPostList();

        List<Post> posts = htmlToPostList.call(readAssetFile("hfr_posts_page.html"));

        // 40 posts per page
        assertThat(posts.size()).isEqualTo(40);
    }

    @Test
    public void test_parsePrivateMessages() throws IOException {
        HTMLToPostList htmlToPostList = new HTMLToPostList();

        List<Post> posts = htmlToPostList.call(readAssetFile("hfr_private_message_page.html"));

        assertThat(posts.size()).isEqualTo(6);
    }

}
