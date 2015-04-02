package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;

public class EditPostEvent {
    private final Topic topic;

    private final int postId;

    public EditPostEvent(Topic topic, int postId) {
        this.topic = topic;
        this.postId = postId;
    }

    public Topic getTopic() {
        return topic;
    }

    public int getPostId() {
        return postId;
    }
}
