package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.ui.misc.PagePosition;

public class GoToTopicEvent {
    private final Category category;

    private final  int topicId;

    private final  int topicPage;

    private final PagePosition pagePosition;

    public GoToTopicEvent(Category category, int topicId, int topicPage, PagePosition pagePosition) {
        this.category = category;
        this.topicId = topicId;
        this.topicPage = topicPage;
        this.pagePosition = pagePosition;
    }

    public Category getCategory() {
        return category;
    }

    public int getTopicId() {
        return topicId;
    }

    public int getTopicPage() {
        return topicPage;
    }

    public PagePosition getPagePosition() {
        return pagePosition;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GoToTopicEvent{");
        sb.append("category=").append(category);
        sb.append(", topicId=").append(topicId);
        sb.append(", topicPage=").append(topicPage);
        sb.append(", pagePosition=").append(pagePosition);
        sb.append('}');
        return sb.toString();
    }
}
