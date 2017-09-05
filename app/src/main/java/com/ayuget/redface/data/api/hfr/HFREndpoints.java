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

import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.ui.UIConstants;
import com.google.common.base.Optional;
import com.squareup.phrase.Phrase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import timber.log.Timber;

public class HFREndpoints implements MDEndpoints {
    private static final String FORUM_BASE_URL = "http://forum.hardware.fr";

    private static final String CATEGORY_URL = "{base_url}/hfr/{category_slug}/liste_sujet-{page}.htm";

    private static final String SUBCATEGORY_URL = "{base_url}/hfr/{category_slug}/{subcategory_slug}/liste_sujet-{page}.htm";

    private static final String TOPIC_URL = "{base_url}/forum2.php?config=hfr.inc&cat={category_id}&post={topic_id}&page={page}";

    private static final String PRIVATE_MESSAGES_URL = "{base_url}/forum1.php?config=hfr.inc&cat=prive&page={page}&subcat=&sondage=0&owntopic=0&trash=0&trash_post=0&moderation=0&new=0&nojs=0&subcatgroup=0";

    private static final String AUTH_FORM_URL = "{base_url}/login_validation.php?config=hfr.inc";

    private static final String FILTERED_URL = "{base_url}/forum1.php?config=hfr.inc&cat={category_id}&page={page}&subcat={subcategory_id}&sondage=0&owntopic={filter_id}&trash=0&trash_post=0&moderation=0&new=0&nojs=0&subcatgroup=0";

    private static final String USER_PROFILE_URL = "{base_url}/hfr/profil-{user_id}.htm";

    private static final String USER_AVATAR_URL = "http://forum-images.hardware.fr/images/mesdiscussions-{user_id}.png";

    private static final String SMILEY_API_HOST = "http://stickersapi.feeligo.com";

    private static final String REPLY_URL = "{base_url}/bddpost.php?config=hfr.inc";

    private static final String EDIT_FORM_URL = "{base_url}/bdd.php?config=hfr.inc";

    private static final String QUOTE_URL = "{base_url}/message.php?config=hfr.inc&cat={category_id}&post={topic_id}&numrep={post_id}";

    private static final String EDIT_URL = "{base_url}/message.php?config=hfr.inc&cat={category_id}&post={topic_id}&numreponse={post_id}";

    private static final String USER_FORUM_PREFERENCES_URL = "{base_url}/user/editprofil.php?config=hfr.inc&page=3";

    private static final String META_PAGE_URL = "{base_url}/forum1f.php?config=hfr.inc&owntopic={filter_id}&new=0&nojs=0";

    private static final String FAVORITE_URL = "{base_url}/user/addflag.php?config=hfr.inc&cat={category_id}&post={topic_id}&numreponse={post_id}";

    public static final String PRIVATE_MESSAGE_REAL_CAT_ID = "prive";

    public static final String SMILEY_SEARCH_URL = "{base_url}/message-smi-mp-aj.php?config=hfr.inc&findsmilies={search_term}";

    private static final String REMOVE_FLAG_URL = "{base_url}/user/delflag.php?config=hfr.inc&cat={category_id}&post={topic_id}&p=1&sondage=0&owntopic=1&new=0";

    private static final String POST_URL = "{base_url}/forum2.php?config=hfr.inc&cat={category_id}&post={topic_id}&page={page}&p=1&sondage=0&owntopic=1&trash=0&trash_post=0&print=0&numreponse=0&quote_only=0&new=0&nojs=0#t{post_id}";

    private static final String SEARCH_TOPIC_URL = "{base_url}/transsearch.php";

    /**
     * Homepage URL (with the list of categories)
     */
    @Override
    public String homepage() {
        return FORUM_BASE_URL;
    }

    private Optional<Integer> getFilterId(TopicFilter topicFilter) {
        Optional<Integer> filterId = Optional.absent();
        if (topicFilter == TopicFilter.PARTICIPATED) {
            filterId = Optional.of(1);
        }
        else if (topicFilter == TopicFilter.FAVORITE) {
            filterId = Optional.of(3);
        }
        else if (topicFilter == TopicFilter.READ) {
            filterId = Optional.of(2);
        }

        return filterId;
    }

    /**
     * Category URL (page containing related topics)
     */
    @Override
    public String category(Category category, int page, TopicFilter topicFilter) {
        Optional<Integer> filterId = getFilterId(topicFilter);

        if (topicFilter == TopicFilter.NONE || !filterId.isPresent()) {
            return Phrase.from(CATEGORY_URL)
                    .put("base_url", FORUM_BASE_URL)
                    .put("category_slug", category.slug())
                    .put("page", page)
                    .format().toString();
        }
        else {
            return Phrase.from(FILTERED_URL)
                    .put("base_url", FORUM_BASE_URL)
                    .put("category_id", category.id())
                    .put("subcategory_id", 0)
                    .put("page", page)
                    .put("filter_id", filterId.get())
                    .format().toString();
        }
    }

    /**
     * Subcategory URL (page containing related topics)
     */
    @Override
    public String subcategory(Category category, Subcategory subcategory, int page, TopicFilter topicFilter) {
        Optional<Integer> filterId = getFilterId(topicFilter);
        if (topicFilter == TopicFilter.NONE || !filterId.isPresent()) {
            return Phrase.from(SUBCATEGORY_URL)
                    .put("base_url", FORUM_BASE_URL)
                    .put("category_slug", category.slug())
                    .put("subcategory_slug", subcategory.slug())
                    .put("page", page)
                    .format().toString();
        }
        else {
            return Phrase.from(FILTERED_URL)
                    .put("base_url", FORUM_BASE_URL)
                    .put("category_id", category.id())
                    .put("subcategory_id", 0) // FIXME fetch subcategory id properly
                    .put("page", page)
                    .put("filter_id", filterId.get())
                    .format().toString();
        }
    }

    private String getTopicRealCategoryId(Topic topic) {
        boolean isPrivateMessage = topic.category().id() == UIConstants.PRIVATE_MESSAGE_CAT_ID;
        return isPrivateMessage ? PRIVATE_MESSAGE_REAL_CAT_ID : String.valueOf(topic.category().id());
    }

    /**
     * Topic URL
     */
    @Override
    public String topic(Topic topic, int page) {
        return Phrase.from(TOPIC_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("category_id", getTopicRealCategoryId(topic))
                .put("topic_id", topic.id())
                .put("page", page)
                .format().toString();
    }

    @Override
    public String topic(Topic topic) {
        return topic(topic, 1);
    }

    @Override
    public String topic(Category category, int topicId) {
        return Phrase.from(TOPIC_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("category_id", category.id())
                .put("topic_id", topicId)
                .put("page", 1)
                .format().toString();
    }

    @Override
    public String baseurl() {
        return FORUM_BASE_URL;
    }

    @Override
    public String loginUrl() {
        return Phrase.from(AUTH_FORM_URL)
                .put("base_url", FORUM_BASE_URL)
                .format()
                .toString();
    }

    @Override
    public String profile(int user_id) {
        return Phrase.from(USER_PROFILE_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("user_id", user_id)
                .format()
                .toString();
    }

    @Override
    public String userAvatar(int user_id) {
        return Phrase.from(USER_AVATAR_URL).put("user_id", user_id).format().toString();
    }

    @Override
    public String smileyApiHost() {
        return SMILEY_API_HOST;
    }

    @Override
    public String replyUrl() {
        return Phrase.from(REPLY_URL)
                .put("base_url", FORUM_BASE_URL)
                .format()
                .toString();
    }

    @Override
    public String editUrl() {
        return Phrase.from(EDIT_FORM_URL)
                .put("base_url", FORUM_BASE_URL)
                .format()
                .toString();
    }

    @Override
    public String quote(Category category, Topic topic, int postId) {
        return Phrase.from(QUOTE_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("category_id", getTopicRealCategoryId(topic))
                .put("topic_id", topic.id())
                .put("post_id", postId)
                .format().toString();
    }

    @Override
    public String post(Category category, Topic topic, int page, int postId) {
        return Phrase.from(POST_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("category_id", getTopicRealCategoryId(topic))
                .put("topic_id", topic.id())
                .put("page", page)
                .put("post_id", postId)
                .format().toString();
    }

    @Override
    public String editPost(Category category, Topic topic, int postId) {
        return Phrase.from(EDIT_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("category_id", getTopicRealCategoryId(topic))
                .put("topic_id", topic.id())
                .put("post_id", postId)
                .format().toString();
    }

    @Override
    public String userForumPreferences() {
        return Phrase.from(USER_FORUM_PREFERENCES_URL)
                .put("base_url", USER_FORUM_PREFERENCES_URL)
                .format()
                .toString();
    }

    @Override
    public String metaPage(TopicFilter topicFilter) {
        if (topicFilter == TopicFilter.NONE) {
            topicFilter = TopicFilter.PARTICIPATED;
        }

        Optional<Integer> filterId = getFilterId(topicFilter);

        if (filterId.isPresent()) {
            return Phrase.from(META_PAGE_URL)
                    .put("base_url", FORUM_BASE_URL)
                    .put("filter_id", filterId.get())
                    .format().toString();
        }
        else {
            throw new IllegalStateException("Invalid topic filter for meta page");
        }
    }

    @Override
    public String favorite(Category category, Topic topic, int postId) {
        return Phrase.from(FAVORITE_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("category_id", category.id())
                .put("topic_id", topic.id())
                .put("post_id", postId)
                .format().toString();
    }

    @Override
    public String deletePost() {
        return Phrase.from(EDIT_FORM_URL)
                .put("base_url", FORUM_BASE_URL)
                .format()
                .toString();
    }

    @Override
    public String reportPost() {
        return null;
    }

    @Override
    public String privateMessages() {
        return privateMessages(1);
    }

    @Override
    public String privateMessages(int page) {
        return Phrase.from(PRIVATE_MESSAGES_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("page", page)
                .format().toString();
    }

    @Override
    public String smileySearch(String searchTerm) {
        String encodedTerm;
        try {
            encodedTerm = URLEncoder.encode(searchTerm, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Timber.e(e, "Error while encoding smiley search term");
            encodedTerm = searchTerm;
        }

        return Phrase.from(SMILEY_SEARCH_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("search_term", encodedTerm)
                .format().toString();
    }

    @Override
    public String removeFlag(Category category, Topic topic) {
        return Phrase.from(REMOVE_FLAG_URL)
                .put("base_url", FORUM_BASE_URL)
                .put("category_id", category.id())
                .put("topic_id", topic.id())
                .format()
                .toString();
    }

    @Override
    public String searchTopic() {
        return Phrase.from(SEARCH_TOPIC_URL)
                .put("base_url", FORUM_BASE_URL)
                .format()
                .toString();
    }
}
