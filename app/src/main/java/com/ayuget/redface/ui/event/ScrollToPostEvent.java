package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.misc.PagePosition;

public class ScrollToPostEvent {
    private final Topic topic;

    private final int page;

    private final PagePosition pagePosition;

    public ScrollToPostEvent(Topic topic, int page, PagePosition pagePosition) {
        this.topic = topic;
        this.page = page;
        this.pagePosition = pagePosition;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScrollToPostEvent that = (ScrollToPostEvent) o;

        if (page != that.page) return false;
        if (pagePosition != null ? !pagePosition.equals(that.pagePosition) : that.pagePosition != null)
            return false;
        if (topic != null ? !topic.equals(that.topic) : that.topic != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = topic != null ? topic.hashCode() : 0;
        result = 31 * result + page;
        result = 31 * result + (pagePosition != null ? pagePosition.hashCode() : 0);
        return result;
    }
}
