package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;

public class TopicPageCountUpdatedEvent {
    private final Topic topic;

    private final int newPageCount;

    public TopicPageCountUpdatedEvent(Topic topic, int newPageCount) {
        this.topic = topic;
        this.newPageCount = newPageCount;
    }

    public Topic getTopic() {
        return topic;
    }

    public int getNewPageCount() {
        return newPageCount;
    }
}
