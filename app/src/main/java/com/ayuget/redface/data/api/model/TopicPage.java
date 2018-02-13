package com.ayuget.redface.data.api.model;

import com.ayuget.redface.ui.misc.PagePosition;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TopicPage {
    public abstract Topic topic();
    public abstract int page();
    public abstract List<Post> posts();
    public abstract PagePosition pageInitialPosition();
    public abstract boolean positionAfterPageLoad();

    public static TopicPage create(Topic topic, int page, List<Post> posts, PagePosition pageInitialPosition, boolean positionAfterPageLoad) {
        return new AutoValue_TopicPage(topic, page, posts, pageInitialPosition, positionAfterPageLoad);
    }
}
