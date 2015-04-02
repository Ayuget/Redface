package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.misc.PagePosition;

public class InternalLinkClickedEvent {
    private final Topic topic;

    private final int page;

    private final PagePosition pagePosition;

    public InternalLinkClickedEvent(Topic topic, int page, PagePosition pagePosition) {
        this.pagePosition = pagePosition;
        this.page = page;
        this.topic = topic;
    }

    public Topic getTopic() {
        return topic;
    }

    public int getPage() {
        return page;
    }

    public PagePosition getPagePosition() {
        return pagePosition;
    }
}
