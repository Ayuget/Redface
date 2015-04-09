/*
 * Copyright 2015 Ayuget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ayuget.redface.ui.event;

import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.view.TopicPageView;

public class PageLoadedEvent {
    private final Topic topic;

    private final int page;

    private final TopicPageView topicPageView;

    public PageLoadedEvent(Topic topic, int page, TopicPageView topicPageView) {
        this.topic = topic;
        this.page = page;
        this.topicPageView = topicPageView;
    }

    public Topic getTopic() {
        return topic;
    }

    public int getPage() {
        return page;
    }

    public TopicPageView getTopicPageView() {
        return topicPageView;
    }
}
