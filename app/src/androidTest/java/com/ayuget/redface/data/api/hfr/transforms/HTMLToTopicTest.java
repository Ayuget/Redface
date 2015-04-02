package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.BaseTestCase;
import com.ayuget.redface.data.api.model.Topic;
import static org.assertj.core.api.Assertions.*;
import java.io.IOException;

public class HTMLToTopicTest extends BaseTestCase {
    public void test_parseTopicDetails() throws IOException {
        HTMLToTopic htmlToTopic = new HTMLToTopic();

        Topic topic = htmlToTopic.call(readAssetFile("hfr_topic.html"));

        assertThat(topic).isNotNull();
        assertThat(topic.getId()).isEqualTo(21748);
        assertThat(topic.getSubject()).isEqualTo("[Projet] HFR4droid 0.8.6 - 10k downloads, merci à tous");
        assertThat(topic.getPagesCount()).isEqualTo(419);
    }
}
