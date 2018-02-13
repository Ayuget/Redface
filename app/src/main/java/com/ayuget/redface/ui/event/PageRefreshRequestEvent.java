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

public class PageRefreshRequestEvent {
    private final Topic topic;
    private final int page;

    public PageRefreshRequestEvent(Topic topic) {
        this(topic, -1);
    }

    public PageRefreshRequestEvent(Topic topic, int page) {
        this.topic = topic;
        this.page = page;
    }

    public Topic getTopic() {
        return topic;
    }

    public int getPage() {
        return page;
    }

    public boolean isPageDefined() {
        return page != -1;
    }
}
