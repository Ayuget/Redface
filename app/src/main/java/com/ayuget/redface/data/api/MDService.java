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
    public Observable<List<Category>> listCategories(User user);

    /**
     * Returns details about a particular category
     */
    public Observable<Category> getCategoryById(User user, int categoryId);

    /**
     * Lists all topics for a given category
     * @param category category
     * @param filter filter to apply
     * @return topics list
     */
    public Observable<List<Topic>> listTopics(User user, final Category category, final Subcategory subcategory, int page, final TopicFilter filter);

    /**
     * Returns a specific topic page
     */
    public Observable<List<Post>> listPosts(User user, Topic topic, int page);

    /**
     * Returns basic informations (subject and pages count) about a topic
     */
    public Observable<Topic> getTopic(User user, Category category, int topicId);

    /**
     * Logs in the given user
     */
    public Observable<Boolean> login(User user);

    /**
     * Returns quote BBCode for a given post
     */
    public Observable<String> getQuote(User user, Topic topic, int postId);

    /**
     * Returns post BBCode
     */
    public Observable<String> getPostContent(User user, Topic topic, int postId);

    /**
     * Returns a list of the smileys the most recently used by the user
     */
    public Observable<List<Smiley>> getRecentlyUsedSmileys(User user);

    /**
     * Returns a list of popular smileys
     */
    public Observable<List<Smiley>> getPopularSmileys();

    /**
     * Search for smileys
     * @param searchExpression search criteria
     * @return list of smileys matching the expression
     */
    public Observable<List<Smiley>> searchSmileys(String searchExpression);

    /**
     * Reply to a topic
     */
    public Observable<Response> replyToTopic(User user, Topic topic, String message, boolean includeSignature);

    /**
     * Edit a post
     */
    public Observable<Response> editPost(User user, Topic topic, int postId, String newMessage, boolean includeSignature);

    /**
     * Returns current hashcheck, needed for certain actions (like reply to a topic, ...)
     */
    public String getHashcheck();
}
