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

import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.view.TopicPageView;

public class GoToPostEvent {
    private final TopicPageView topicPageView;
    private final int page;
    private final PagePosition targetPost;

    public GoToPostEvent(int page, PagePosition targetPost, TopicPageView topicPageView) {
        this.topicPageView = topicPageView;
        this.targetPost = targetPost;
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public PagePosition getTargetPost() {
        return targetPost;
    }

    public TopicPageView getTopicPageView() {
        return topicPageView;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GoToPostEvent{");
        sb.append("page=").append(page);
        sb.append(", targetPost=").append(targetPost);
        sb.append('}');
        return sb.toString();
    }
}
