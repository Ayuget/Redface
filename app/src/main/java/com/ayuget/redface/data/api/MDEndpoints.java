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

package com.ayuget.redface.data.api;

import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicFilter;

public interface MDEndpoints {
    /**
     * Homepage URL (with the list of categories)
     */
    public String homepage();

    /**
     * Category URL (page containing related topics)
     */
    public String category(Category category, int page, TopicFilter topicFilter);

    /**
     * Subcategory URL (page containing related topics)
     */
    public String subcategory(Category category, Subcategory subcategory, int page, TopicFilter topicFilter);

    /**
     * Topic URL
     */
    public String topic(Topic topic, int page);
    public String topic(Topic topic);
    public String topic(Category category, int topicId);

    /**
     * Forum's base url
     */
    public String baseurl();

    public String loginUrl();

    public String userAvatar(int user_id);

    public String smileyApiHost();

    public String replyUrl();

    public String editUrl();

    public String quote(Category category, Topic topic, int postId);

    public String editPost(Category category, Topic topic, int postId);

    public String userForumPreferences();

    public String metaPage(TopicFilter topicFilter);

    public String favorite(Category category, Topic topic, int postId);

    public String privateMessages();

    public String privateMessages(int page);
}
