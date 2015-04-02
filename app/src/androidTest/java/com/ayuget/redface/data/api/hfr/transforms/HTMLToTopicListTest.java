package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicStatus;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.List;

public class HTMLToTopicListTest extends BaseTestCase {
    public void test_parseProgrammingCategory() throws IOException {
        HTMLToTopicList htmlToTopicList = new HTMLToTopicList();

        List<Topic> topics = htmlToTopicList.call(readAssetFile("hfr_topics_page.html"));

        assertThat(topics.size()).isEqualTo(54);

        Topic read = topics.get(5);
        assertThat(read.getSubject()).isEqualTo("[Topic Unique] Samsung Galaxy S6");
        assertThat(read.getPagesCount()).isEqualTo(11);
        assertThat(read.getStatus()).isEqualTo(TopicStatus.READ_NEW_CONTENT);
        assertThat(read.getLastReadPostPage()).isEqualTo(3);
        assertThat(read.getLastReadPostId()).isEqualTo(1888628);

        Topic favorite = topics.get(7);
        assertThat(favorite.getSubject()).isEqualTo("TPU | Samsung Galaxy S III GT-I9300 - XXDLJ2 (4.1.1) | ROMS STOCKS /!\\");
        assertThat(favorite.getPagesCount()).isEqualTo(638);
        assertThat(favorite.getStatus()).isEqualTo(TopicStatus.FAVORITE_NEW_CONTENT);
        assertThat(favorite.getLastReadPostPage()).isEqualTo(448);
        assertThat(favorite.getLastReadPostId()).isEqualTo(1260955);

        Topic flagged = topics.get(9);
        assertThat(flagged.getSubject()).isEqualTo("[topic MWC 2015] C'est parti ! Annonces, débats, ...");
        assertThat(flagged.getPagesCount()).isEqualTo(24);
        assertThat(flagged.getStatus()).isEqualTo(TopicStatus.FLAGGED_NEW_CONTENT);
        assertThat(flagged.getLastReadPostPage()).isEqualTo(24);
        assertThat(flagged.getLastReadPostId()).isEqualTo(1891678);
    }
}
