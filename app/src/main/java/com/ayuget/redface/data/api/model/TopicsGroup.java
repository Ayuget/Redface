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

package com.ayuget.redface.data.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Topics grouped by category, used in meta pages
 */
public class TopicsGroup {
    private final Category category;

    private final List<Topic> topics;

    public TopicsGroup(Category category) {
        this.category = category;
        this.topics = new ArrayList<>();
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    public Category getCategory() {
        return category;
    }

    public List<Topic> getTopics() {
        return topics;
    }
}
