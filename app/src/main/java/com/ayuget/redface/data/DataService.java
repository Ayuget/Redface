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

import android.util.Log;

import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.SubscriptionHandler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observer;
import rx.Subscription;

@Singleton
public class DataService {
    private static final String LOG_TAG = DataService.class.getSimpleName();

    @Inject MDService mdService;

    private SubscriptionHandler<User, List<Category>> categoriesSubscriptionHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<CategoryPageKey, List<Topic>> topicsSubscriptionHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<User, List<Topic>> metaPageSubscriptionHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<Topic, List<Post>> postsSubscriptionHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<User, List<Smiley>> recentSmileysHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<String, List<Smiley>> smileysSearchHandler = new SubscriptionHandler<>();
    private SubscriptionHandler<String, List<Smiley>> popularSmileysHandler = new SubscriptionHandler<>();

    public static class CategoryPageKey {
        private final User user;
        private final Category category;
        private final Subcategory subcategory;
        private final int page;

        public CategoryPageKey(User user, Category category, Subcategory subcategory, int page) {
            this.user = user;
            this.category = category;
            this.subcategory = subcategory;
            this.page = page;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CategoryPageKey that = (CategoryPageKey) o;

            if (page != that.page) return false;
            if (!user.equals(that.user)) return false;
            if (!category.equals(that.category)) return false;
            return !(subcategory != null ? !subcategory.equals(that.subcategory) : that.subcategory != null);

        }

        @Override
        public int hashCode() {
            int result = user.hashCode();
            result = 31 * result + category.hashCode();
            result = 31 * result + (subcategory != null ? subcategory.hashCode() : 0);
            result = 31 * result + page;
            return result;
        }
    }

    public Subscription loadCategories(final User user, Observer<List<Category>> observer) {
        Log.d(LOG_TAG, String.format("Loading categories for user '%s'", user.getUsername()));
        return categoriesSubscriptionHandler.loadAndCache(user, mdService.listCategories(user), observer);
    }

    public Subscription loadTopics(final User user, final Category category, final Subcategory subcategory, int page, final TopicFilter topicFilter, Observer<List<Topic>> observer) {
        return topicsSubscriptionHandler.load(new CategoryPageKey(user, category, subcategory, page), mdService.listTopics(user, category, subcategory, page, topicFilter), observer);
    }

    public Subscription loadMetaPageTopics(final User user, final TopicFilter topicFilter, boolean sortByDate, Observer<List<Topic>> observer) {
        return metaPageSubscriptionHandler.load(user, mdService.listMetaPageTopics(user, topicFilter, sortByDate), observer);
    }

    public Subscription loadPosts(final User user, final Topic topic, int page, Observer<List<Post>> observer) {
        return postsSubscriptionHandler.load(topic, mdService.listPosts(user, topic, page), observer);
    }

    public Subscription getRecentlyUsedSmileys(final User user, Observer<List<Smiley>> observer) {
        return recentSmileysHandler.loadAndCache(user, mdService.getRecentlyUsedSmileys(user), observer);
    }

    public Subscription searchForSmileys(final String searchExpression, Observer<List<Smiley>> observer) {
        return smileysSearchHandler.loadAndCache(searchExpression, mdService.searchSmileys(searchExpression), observer);
    }

    public Subscription getPopularSmileys(Observer<List<Smiley>> observer) {
        return popularSmileysHandler.loadAndCache(null, mdService.getPopularSmileys(), observer);
    }
}
