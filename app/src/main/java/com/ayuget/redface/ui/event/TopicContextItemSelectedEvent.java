package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;

public class TopicContextItemSelectedEvent {
    private final Topic topic;

    private final int itemId;

    public TopicContextItemSelectedEvent(Topic topic, int itemId) {
        this.itemId = itemId;
        this.topic = topic;
    }

    public int getItemId() {
        return itemId;
    }

    public Topic getTopic() {
        return topic;
    }
}
