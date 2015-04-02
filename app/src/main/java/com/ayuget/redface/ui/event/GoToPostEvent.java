package com.ayuget.redface.ui.event;

import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.view.TopicPageView;

public class GoToPostEvent {
    private final TopicPageView topicPageView;

    private final int page;

    private final PagePosition pagePosition;

    public GoToPostEvent(int page, PagePosition pagePosition, TopicPageView topicPageView) {
        this.topicPageView = topicPageView;
        this.pagePosition = pagePosition;
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public PagePosition getPagePosition() {
        return pagePosition;
    }

    public TopicPageView getTopicPageView() {
        return topicPageView;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GoToPostEvent{");
        sb.append("page=").append(page);
        sb.append(", pagePosition=").append(pagePosition);
        sb.append('}');
        return sb.toString();
    }
}
