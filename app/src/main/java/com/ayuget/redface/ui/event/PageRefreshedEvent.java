package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.misc.PagePosition;

public class PageRefreshedEvent {
    private final Topic topic;

    private final PagePosition targetPagePosition;

    public PageRefreshedEvent(Topic topic, PagePosition targetPagePosition) {
        this.topic = topic;
        this.targetPagePosition = targetPagePosition;
    }

    public Topic getTopic() {
        return topic;
    }

    public PagePosition getTargetPagePosition() {
        return targetPagePosition;
    }
}
