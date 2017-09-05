package com.ayuget.redface.data.api.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TopicSearchResult {
    public abstract int page();
    public abstract int postId();
    public abstract boolean noMoreResult();

    public static TopicSearchResult create(int page, int postId) {
        return new AutoValue_TopicSearchResult(page, postId, false);
    }
    public static TopicSearchResult createAsNoMoreResult() {
        return new AutoValue_TopicSearchResult(-1, -1, true);
    }
}
