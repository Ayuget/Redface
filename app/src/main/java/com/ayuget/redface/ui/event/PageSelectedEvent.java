package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;

public class PageSelectedEvent {
    private final Topic topic;

    private final int page;

    public PageSelectedEvent(Topic topic, int page) {
        this.topic = topic;
        this.page = page;
    }

    public Topic getTopic() {
        return topic;
    }

    public int getPage() {
        return page;
    }
}
