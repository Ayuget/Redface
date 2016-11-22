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
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.data.api.model.User;

import java.util.List;

import rx.Observable;

public interface MDService {
    /**
     * Lists all categories for a given user (some categories are
     * hidden / not available for some users, for example moderators forum, ...)
     */
    Observable<List<Category>> listCategories(User user);

    /**
     * List all subcategories, focusing on ids.
     */
    Observable<List<Subcategory>> listSubCategories(User user);

    /**
     * Lists all topics for a given category
     * @param category category
     * @param filter filter to apply
     * @return topics list
     */
    Observable<List<Topic>> listTopics(User user, final Category category, final Subcategory subcategory, int page, final TopicFilter filter);

    /**
     * Lists all topics for the meta page
     * @param filter filter to apply
     * @param sortByDate sort topics by date (desc) or group them by categories
     * @return topics list
     */
    Observable<List<Topic>> listMetaPageTopics(User user, final TopicFilter filter, boolean sortByDate);

    /**
     * Lists private messages for a given user
     */
    Observable<List<PrivateMessage>> listPrivateMessages(User user, int page);

    /**
     * Returns all private messages with unread messages
     */
    Observable<List<PrivateMessage>> getNewPrivateMessages(User user);

    /**
     * Returns a specific topic page
     */
    Observable<List<Post>> listPosts(User user, Topic topic, int page);

    /**
     * Returns basic informations (subject and pages count) about a topic
     */
    Observable<Topic> getTopic(User user, Category category, int topicId);

    /**
     * Returns quote BBCode for a given post
     */
    Observable<String> getQuote(User user, Topic topic, int postId);

    /**
     * Returns post BBCode
     */
    Observable<String> getPostContent(User user, Topic topic, int postId);

    /**
     * Returns a list of the smileys the most recently used by the user
     */
    Observable<List<Smiley>> getRecentlyUsedSmileys(User user);

    /**
     * Returns a list of popular smileys
     */
    Observable<List<Smiley>> getPopularSmileys();

    /**
     * Search for smileys
     * @param searchExpression search criteria
     * @return list of smileys matching the expression
     */
    Observable<List<Smiley>> searchSmileys(User user, String searchExpression);

    /**
     * Reply to a topic
     */
    Observable<Response> replyToTopic(User user, Topic topic, String message, boolean includeSignature);

    /**
     * Edit a post
     */
    Observable<Response> editPost(User user, Topic topic, int postId, String newMessage, boolean includeSignature);

    /**
     * New private message
     */
    Observable<Response> sendNewPrivateMessage(User user, String subject, String recipientUsername, String message, boolean includeSignature);

    /**
     * Marks a certain post in a topic as favorite (will set the topic as favorite in the process)
     */
    Observable<Boolean> markPostAsFavorite(User user, Topic topic, int postId);

    /**
     * Deletes a given post
     */
    Observable<Boolean> deletePost(User user, Topic topic, int postId);

    /**
     * Reports a given post to moderators
     */
    Observable<Boolean> reportPost(User user, Topic topic, int postId);

    /**
     * Get a user profile
     */
    Observable<Profile> getProfile(User user, int user_id);

    /**
     * Removes a flag (or a favorite) on a given {@link Topic}
     */
    Observable<Boolean> unflagTopic(User user, Topic topic);
}
