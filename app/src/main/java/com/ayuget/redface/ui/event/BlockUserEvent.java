/*
 * Copyright 2016 nbonnec
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

public class BlockUserEvent {
    private final String author;
    private final Topic topic;

    public BlockUserEvent(String author, Topic topic) {
        this.author = author;
        this.topic = topic;
    }

    public String getAuthor() {
        return author;
    }

    public Topic getTopic() {
        return topic;
    }
}
