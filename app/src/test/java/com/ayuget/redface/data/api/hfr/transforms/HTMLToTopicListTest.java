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

import android.os.Build;

import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.BuildConfig;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicStatus;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class HTMLToTopicListTest extends BaseTestCase {
    @Test
    public void test_parseProgrammingCategory() throws IOException {
        HTMLToTopicList htmlToTopicList = new HTMLToTopicList(null, null, null);

        List<Topic> topics = htmlToTopicList.call(readAssetFile("hfr_topics_page.html"));

        assertThat(topics.size()).isEqualTo(54);

        Topic read = topics.get(5);
        assertThat(read.title()).isEqualTo("[Topic Unique] Samsung Galaxy S6");
        assertThat(read.pagesCount()).isEqualTo(11);
        assertThat(read.status()).isEqualTo(TopicStatus.READ_NEW_CONTENT);
        assertThat(read.lastReadPage()).isEqualTo(3);
        assertThat(read.lastReadPostId()).isEqualTo(1888628);
        assertThat(read.hasUnreadPosts()).isTrue();

        Topic favorite = topics.get(7);
        assertThat(favorite.title()).isEqualTo("TPU | Samsung Galaxy S III GT-I9300 - XXDLJ2 (4.1.1) | ROMS STOCKS /!\\");
        assertThat(favorite.pagesCount()).isEqualTo(638);
        assertThat(favorite.status()).isEqualTo(TopicStatus.FAVORITE_NEW_CONTENT);
        assertThat(favorite.lastReadPage()).isEqualTo(448);
        assertThat(favorite.lastReadPostId()).isEqualTo(1260955);
        assertThat(favorite.hasUnreadPosts()).isTrue();

        Topic flagged = topics.get(9);
        assertThat(flagged.title()).isEqualTo("[topic MWC 2015] C'est parti ! Annonces, débats, ...");
        assertThat(flagged.pagesCount()).isEqualTo(24);
        assertThat(flagged.status()).isEqualTo(TopicStatus.FLAGGED_NEW_CONTENT);
        assertThat(flagged.lastReadPage()).isEqualTo(24);
        assertThat(flagged.lastReadPostId()).isEqualTo(1891678);
        assertThat(flagged.hasUnreadPosts()).isTrue();
    }
}
