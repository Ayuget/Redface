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

package com.ayuget.redface.data.api.hfr;

import android.os.Handler;
import android.os.Looper;

import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.MDMessageSender;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.hfr.transforms.HTMLToBBCode;
import com.ayuget.redface.data.api.hfr.transforms.HTMLToCategoryList;
import com.ayuget.redface.data.api.hfr.transforms.HTMLToPostList;
import com.ayuget.redface.data.api.hfr.transforms.HTMLToPrivateMessageList;
import com.ayuget.redface.data.api.hfr.transforms.HTMLToProfile;
import com.ayuget.redface.data.api.hfr.transforms.HTMLToSmileyList;
import com.ayuget.redface.data.api.hfr.transforms.HTMLToTopic;
import com.ayuget.redface.data.api.hfr.transforms.HTMLToTopicList;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.data.api.model.TopicSearchResult;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.network.PageFetcher;
import com.ayuget.redface.settings.Blacklist;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.TopicPageCountUpdatedEvent;
import com.ayuget.redface.ui.misc.PostReportStatus;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Dedicated service to parse pages from HFR's forum
 */
@SuppressWarnings({"WeakerAccess", "Guava"})
public class HFRForumService implements MDService {
    private static final Pattern POST_REPORT_IN_PROGRESS_PATTERN = Pattern.compile("(.*)(Votre demande de modération sur ce message n'est pas encore traitée)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern POST_JOIN_REPORT_ALERT = Pattern.compile("(.*)(Une demande de modération a déjà été envoyée sur ce message)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern POST_REPORT_TREATED = Pattern.compile("(.*)(Votre demande de modération sur ce message a été traitée)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern POST_JOIN_REPORT_IN_PROGRESS = Pattern.compile("(.*)(La demande de modération sur ce message à laquelle vous vous êtes joint n'est pas encore traitée)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Inject PageFetcher pageFetcher;

    @Inject PostsTweaker postsTweaker;

    @Inject MDEndpoints mdEndpoints;

    @Inject HTTPClientProvider httpClientProvider;

    @Inject Bus bus;

    @Inject CategoriesStore categoriesStore;

    @Inject MDMessageSender mdMessageSender;

    @Inject RedfaceSettings appSettings;

    @Inject Blacklist blacklist;

    private String currentHashcheck;

    @Override
    public Observable<List<Category>> listCategories(final User user) {
        Timber.d("Retrieving categories for user '%s'", user.getUsername());

        List<Category> cachedCategories = categoriesStore.getCategories(user);

        if (cachedCategories == null) {
            return pageFetcher.fetchSource(user, mdEndpoints.homepage())
                    .map(new HTMLToCategoryList())
                    .map(categories -> {
                        Timber.d("Successfully retrieved '%d' categories from network for user '%s', caching them", categories.size(), user.getUsername());
                        categoriesStore.storeCategories(user, categories);
                        return categories;
                    });
        }
        else {
            Timber.d("Successfully retrieved '%d' categories from cache for user '%s'", cachedCategories.size(), user.getUsername());
            return Observable.just(cachedCategories);
        }
    }

    private String getTopicListEndpoint(final Category category, final Subcategory subcategory, int page, final TopicFilter filter) {
        if (subcategory == null) {
            return mdEndpoints.category(category, page, filter);
        }
        else {
            return mdEndpoints.subcategory(category, subcategory, page, filter);
        }
    }

    @Override
    public Observable<List<Topic>> listTopics(User user, final Category category, final Subcategory subcategory, int page, final TopicFilter filter) {
        return pageFetcher.fetchSource(user, getTopicListEndpoint(category, subcategory, page, filter))
                .map(new HTMLToTopicList(categoriesStore, this, user))
                .flatMap(Observable::from)
                .filter(topic -> (topic.hasUnreadPosts() != null && topic.hasUnreadPosts() == Boolean.TRUE) || appSettings.showFullyReadTopics())
                .map(topic -> topic.withCategory(category))
                .toList();
    }

    @Override
    public Observable<List<Topic>> listMetaPageTopics(User user, TopicFilter filter, boolean sortByDate) {
        Observable<Topic> metaPageTopics = pageFetcher.fetchSource(user, mdEndpoints.metaPage(filter))
                .map(new HTMLToTopicList(categoriesStore, this, user))
                .flatMap(Observable::from)
                .filter(topic -> (topic.hasUnreadPosts() != null && topic.hasUnreadPosts() == Boolean.TRUE) || appSettings.showFullyReadTopics());

        if (sortByDate) {
            return metaPageTopics.toSortedList((topic, topic2) -> topic2.lastPostDate().compareTo(topic.lastPostDate()));
        }
        else {
            return metaPageTopics.toList();
        }
    }

    public Observable<List<Category>> refreshCategories(User user) {
        return Observable.defer(() -> {
            categoriesStore.clearCategories(user);
            return listCategories(user);
        });
    }

    @Override
    public Observable<List<Post>> listPosts(User user, final Topic topic, final int page,
                                            final boolean imagesEnabled,
                                            final boolean avatarsEnabled,
                                            final boolean smileysEnabled) {
        return pageFetcher.fetchSource(user, mdEndpoints.topic(topic, page))
                .map(htmlSource -> {
                    // Hashcheck is needed by the server to post new content
                    currentHashcheck = HashcheckExtractor.extract(htmlSource);
                    return htmlSource;
                })
                .map(new HTMLToPostList()) // Convert HTML source to objects
                .map(posts -> {
                    // Last post of previous page is automatically put in first position of
                    // next page. This can be annoying...
                    if (!appSettings.showPreviousPageLastPost() && page > 1 && posts.size() > 1) {
                        posts.remove(0);
                        return posts;
                    }
                    else {
                        return posts;
                    }
                })
                .map(posts -> {
                    if (posts.size() > 0) {
                        int newTopicPagesCount = posts.get(0).getTopicPagesCount();

                        // If the topic pages count is known and different from the one we have,
                        // it usually means new pages have been added since. The event emitted
                        // below can be catched by the UI to update itself.
                        if (newTopicPagesCount != UIConstants.UNKNOWN_PAGES_COUNT && newTopicPagesCount != topic.pagesCount()) {
                            new Handler(Looper.getMainLooper()).post(() -> bus.post(new TopicPageCountUpdatedEvent(topic, posts.get(0).getTopicPagesCount())));
                        }
                    }
                    return posts;
                })
                .map(posts -> postsTweaker.tweak(posts, imagesEnabled, avatarsEnabled, smileysEnabled));
    }

    @Override
    public Observable<String> getQuote(User user, Topic topic, int postId) {
        return pageFetcher.fetchSource(user, mdEndpoints.quote(topic.category(), topic, postId))
                .map(new HTMLToBBCode());
    }

    @Override
    public Observable<String> getPostContent(User user, Topic topic, int postId) {
        return pageFetcher.fetchSource(user, mdEndpoints.editPost(topic.category(), topic, postId))
                .map(new HTMLToBBCode());
    }

    @Override
    public Observable<Topic> getTopic(User user, Category category, int topicId) {
        return pageFetcher.fetchSource(user, mdEndpoints.topic(category, topicId))
                .map(new HTMLToTopic());
    }

    @Override
    public Observable<List<Smiley>> searchSmileys(User user, String searchExpression) {
        return pageFetcher.fetchSource(user, mdEndpoints.smileySearch(searchExpression))
                .map(new HTMLToSmileyList());
    }

    @Override
    public Observable<Response> replyToTopic(User user, Topic topic, String message, boolean includeSignature) {
        return mdMessageSender.replyToTopic(user, topic, message, currentHashcheck, includeSignature);
    }

    @Override
    public Observable<Response> editPost(User user, Topic topic, int postId, String newMessage, boolean includeSignature) {
        return mdMessageSender.editPost(user, topic, postId, newMessage, currentHashcheck, includeSignature);
    }

    @Override
    public Observable<Response> sendNewPrivateMessage(User user, String subject, String recipientUsername, String message, boolean includeSignature) {
        return mdMessageSender.sendNewPrivateMessage(user, subject, recipientUsername, message, currentHashcheck, includeSignature);
    }

    @Override
    public Observable<Boolean> markPostAsFavorite(User user, Topic topic, int postId) {
        return mdMessageSender.markPostAsFavorite(user, topic, postId);
    }

    @Override
    public Observable<List<PrivateMessage>> listPrivateMessages(User user, int page) {
        return pageFetcher.fetchSource(user, mdEndpoints.privateMessages(page))
                .map(htmlSource -> {
                    // Hashcheck is needed by the server to post new content
                    currentHashcheck = HashcheckExtractor.extract(htmlSource);
                    return htmlSource;
                })
                .map(new HTMLToPrivateMessageList());
    }

    @Override
    public Observable<List<PrivateMessage>> getNewPrivateMessages(User user) {
        return listPrivateMessages(user, 1)
                .flatMap(Observable::from)
                .filter(PrivateMessage::hasUnreadMessages)
                .toList();
    }

    @Override
    public Observable<Boolean> reportPost(User user, Topic topic, int postId, String reason, boolean joinReport) {
        return mdMessageSender.reportPost(user, topic, postId, reason, joinReport, currentHashcheck);
    }

    @Override
    public Observable<PostReportStatus> checkPostReportStatus(User user, Topic topic, int postId) {
        return pageFetcher.fetchSource(user, mdEndpoints.reportPost(topic.category(), topic, postId))
                .map(pageSource -> {
                    if (matchesPattern(POST_JOIN_REPORT_ALERT, pageSource)) {
                        return PostReportStatus.JOIN_REPORT;
                    }
                    else if (matchesPattern(POST_REPORT_IN_PROGRESS_PATTERN, pageSource) || matchesPattern(POST_JOIN_REPORT_IN_PROGRESS, pageSource)) {
                        return PostReportStatus.REPORT_IN_PROGRESS;
                    }
                    else if (matchesPattern(POST_REPORT_TREATED, pageSource)) {
                        return PostReportStatus.REPORT_TREATED;
                    }
                    else {
                        return PostReportStatus.NO_EXISTING_REPORT;
                    }
                });
    }

    @Override
    public Observable<Boolean> deletePost(User user, Topic topic, int postId) {
        return mdMessageSender.deletePost(user, topic, postId, currentHashcheck);
    }

    @Override
    public Observable<Profile> getProfile(User user, int user_id) {
        Timber.d("Retrieving profile for user id '%d'", user_id);

        return pageFetcher.fetchSource(user, mdEndpoints.profile(user_id))
                .map(new HTMLToProfile());
    }

    @Override
    public Observable<Boolean> unflagTopic(User user, Topic topic) {
        return mdMessageSender.unflagTopic(user, topic);
    }

    @Override
    public Observable<TopicSearchResult> searchInTopic(User user, Topic topic, long startFromPostId, String word, String author, boolean firstSearch) {
        return mdMessageSender.searchInTopic(user, topic, startFromPostId, word, author, firstSearch, currentHashcheck);
    }

    private boolean matchesPattern(Pattern p, String content) {
        return p.matcher(content).matches();
    }
}
