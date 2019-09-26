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

package com.ayuget.redface.data;

import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observer;
import rx.Subscription;
import timber.log.Timber;

@Singleton
public class DataService {
    private final MDService mdService;

    private SubscriptionHandler<User, Profile> profileSubscriptionHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<User, List<Category>> categoriesSubscriptionHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<CategoryPage, List<Topic>> topicsSubscriptionHandler = new SubscriptionHandler<>(5);
    private SubscriptionHandler<MetaPageOptions, List<Topic>> metaPageSubscriptionHandler = new SubscriptionHandler<>(5);
    private SubscriptionHandler<TopicPage, List<Post>> postsSubscriptionHandler = new SubscriptionHandler<>(3);
    private SubscriptionHandler<User, List<Smiley>> recentSmileysHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<String, List<Smiley>> smileysSearchHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<String, List<Smiley>> popularSmileysHandler = new SubscriptionHandler<>();

    @Inject
    public DataService(MDService mdService) {
        this.mdService = mdService;
    }

    @AutoValue
    public static abstract class TopicPage {
        public abstract Topic topic();

        public abstract int page();

        public static TopicPage create(Topic topic, int page) {
            return new AutoValue_DataService_TopicPage(topic, page);
        }
    }

    @AutoValue
    public static abstract class MetaPageOptions {
        public abstract User user();

        @Nullable
        public abstract TopicFilter topicFilter();

        public abstract boolean sortByDate();

        public static MetaPageOptions create(User user, @Nullable TopicFilter topicFilter, boolean sortByDate) {
            return new AutoValue_DataService_MetaPageOptions(user, topicFilter, sortByDate);
        }
    }

    @AutoValue
    public static abstract class CategoryPage {
        public abstract User user();

        public abstract Category category();

        @Nullable
        public abstract Subcategory subcategory();

        @Nullable
        public abstract TopicFilter topicFilter();

        public abstract int page();

        public static CategoryPage create(User user, Category category, @Nullable Subcategory subcategory, @Nullable TopicFilter topicFilter, int page) {
            return new AutoValue_DataService_CategoryPage(user, category, subcategory, topicFilter, page);
        }
    }

    public Subscription loadProfile(final User user, int user_id, Observer<Profile> observer) {
        Timber.d("Loading profile for user id '%d'", user_id);
        return profileSubscriptionHandler.loadAndCache(user, mdService.getProfile(user, user_id), observer);
    }

    public Subscription loadCategories(final User user, Observer<List<Category>> observer) {
        Timber.d("Loading categories for user '%s'", user.getUsername());
        return categoriesSubscriptionHandler.loadAndCache(user, mdService.listCategories(user), observer);
    }

    public Subscription loadTopics(final User user, final Category category, final Subcategory subcategory, int page, final TopicFilter topicFilter, Observer<List<Topic>> observer) {
        return topicsSubscriptionHandler.loadAndCache(CategoryPage.create(user, category, subcategory, topicFilter, page), mdService.listTopics(user, category, subcategory, page, topicFilter), observer);
    }

    public Subscription loadMetaPageTopics(final User user, final TopicFilter topicFilter, boolean sortByDate, Observer<List<Topic>> observer) {
        return metaPageSubscriptionHandler.load(MetaPageOptions.create(user, topicFilter, sortByDate), mdService.listMetaPageTopics(user, topicFilter, sortByDate), observer);
    }

    public Subscription loadPosts(final User user, final Topic topic, int page, boolean imagesEnabled, boolean avatarsEnabled, boolean smileysEnabled, Observer<List<Post>> observer) {
        return postsSubscriptionHandler.loadAndCache(TopicPage.create(topic, page), mdService.listPosts(user, topic, page, imagesEnabled, avatarsEnabled, smileysEnabled), observer);
    }

    public Subscription searchForSmileys(final User user, final String searchExpression, Observer<List<Smiley>> observer) {
        return smileysSearchHandler.loadAndCache(searchExpression, mdService.searchSmileys(user, searchExpression), observer);
    }

    public void clearPostsCache(final Topic topic, int page) {
        postsSubscriptionHandler.clearKey(TopicPage.create(topic, page));
    }

    public void clearTopicListCache() {
        topicsSubscriptionHandler.clearAll();
    }
}
