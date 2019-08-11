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
    String homepage();

    /**
     * Category URL (page containing related topics)
     */
    String category(Category category, int page, TopicFilter topicFilter);

    /**
     * Page containing subcategories ids for a category.
     * @param id category id.
     */
    String subcategoriesIdsList(int id);

    /**
     * Subcategory URL (page containing related topics)
     */
    String subcategory(Category category, Subcategory subcategory, int page, TopicFilter topicFilter);

    /**
     * Topic URL
     */
    String topic(Topic topic, int page);
    String topic(Topic topic);
    String topic(Category category, int topicId);

    /**
     * Forum's base url
     */
    String baseurl();

    String loginUrl();

    String profile(int user_id);

    String userAvatar(int user_id);

    String smileyApiHost();

    String replyUrl();

    String editUrl();

    String quote(Category category, Topic topic, int postId);

    String post(Category category, Topic topic, int page, int postId);

    String editPost(Category category, Topic topic, int postId);

    String userForumPreferences();

    String metaPage(TopicFilter topicFilter);

    String favorite(Category category, Topic topic, int postId);

    String deletePost();

    String reportPost();

    String privateMessages();

    String privateMessages(int page);

    String smileySearch(String searchTerm);

    /**
     * URL used to unflag / unfavorite a topic
     */
    String removeFlag(Category category, Topic topic);

    String searchTopic();
}
