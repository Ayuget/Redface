package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.view.TopicPageView;

public class PageLoadedEvent {
    private final Topic topic;

    private final int page;

    private final TopicPageView topicPageView;

    public PageLoadedEvent(Topic topic, int page, TopicPageView topicPageView) {
        this.topic = topic;
        this.page = page;
        this.topicPageView = topicPageView;
    }

    public Topic getTopic() {
        return topic;
    }

    public int getPage() {
        return page;
    }

    public TopicPageView getTopicPageView() {
        return topicPageView;
    }
}
