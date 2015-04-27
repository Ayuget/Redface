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
