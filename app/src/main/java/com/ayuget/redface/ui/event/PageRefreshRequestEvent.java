package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;

public class PageRefreshRequestEvent {
    private final Topic topic;

    public PageRefreshRequestEvent(Topic topic) {
        this.topic = topic;
    }

    public Topic getTopic() {
        return topic;
    }
}
