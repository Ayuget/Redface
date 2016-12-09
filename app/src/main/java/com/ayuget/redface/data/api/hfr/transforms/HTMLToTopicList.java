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

package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.data.api.hfr.HFRForumService;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicStatus;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.util.DateUtils;
import com.ayuget.redface.util.HTMLUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import rx.functions.Action0;
import rx.functions.Func1;

public class HTMLToTopicList extends TopicTransform implements Func1<String, List<Topic>> {
    /**
     * Default number of topics per page. Used to initialize default capacity
     * for the list of topics (small performance improvement ?)
     */
    private static final int DEFAULT_TOPICS_COUNT = 50;

    private final CategoriesStore categoriesStore;

    private final HFRForumService hfrForumService;

    private final User user;

    public HTMLToTopicList(CategoriesStore categoriesStore, HFRForumService hfrForumService, User user) {
        this.categoriesStore = categoriesStore;
        this.hfrForumService = hfrForumService;
        this.user = user;
    }

    private TopicStatus extractTopicStatusFromImageName(String imageName) {
        if (imageName == null) {
            return TopicStatus.NONE;
        }
        else if (imageName.equals("flag1")) {
            return TopicStatus.FLAGGED_NEW_CONTENT;
        }
        else if (imageName.equals("flag0")) {
            return TopicStatus.READ_NEW_CONTENT;
        }
        else if (imageName.equals("favoris")) {
            return TopicStatus.FAVORITE_NEW_CONTENT;
        }
        else if (imageName.equals("closed")) {
            return TopicStatus.NO_NEW_CONTENT;
        }
        else {
            return TopicStatus.NONE;
        }
    }

    @Override
    public List<Topic> call(String source) {
        List<Topic> topics = new ArrayList<>(DEFAULT_TOPICS_COUNT);

        Matcher m = TOPIC_PATTERN.matcher(source);
        Category currentCategory = null;

        while (m.find()) {
            if (m.group(1) != null) {
                int categoryId = Integer.parseInt(m.group(1));

                if (categoriesStore != null) {
                    currentCategory = categoriesStore.getCategoryById(categoryId);

                    // fixme super ugly : force a refresh of the categories cache if no category is
                    // found (can happen when new categories are created)
                    if (currentCategory == null) {
                        hfrForumService.refreshCategories(user).toBlocking().first();
                        currentCategory = categoriesStore.getCategoryById(categoryId);
                    }
                }
            }
            else {
                int topicId = Integer.parseInt(m.group(7));
                String subject = HTMLUtils.escapeHTML(m.group(8));
                String author = m.group(13);
                int pagesCount = m.group(9) != null ? Integer.parseInt(m.group(9)) : 1;
                String lastPostAuthor = m.group(20);
                Date lastPostDate = DateUtils.fromHTMLDate(m.group(17), m.group(16), m.group(15), m.group(18), m.group(19));
                boolean isSticky = m.group(4) != null;
                boolean isLocked = isTopicLocked(m.group(3));
                TopicStatus status = extractTopicStatusFromImageName(m.group(11) != null ? m.group(11) : m.group(5));
                int lastReadPage = m.group(12) != null ? Integer.parseInt(m.group(12)) : -1;
                long lastReadPostId = m.group(10) != null ? Long.parseLong(m.group(10)) : -1;
                boolean unreadPosts = hasUnreadPosts(m.group(3));

                Topic topic = Topic.builder()
                        .id(topicId)
                        .title(subject)
                        .pagesCount(pagesCount)
                        .author(author)
                        .status(status)
                        .lastPostAuthor(lastPostAuthor)
                        .lastPostDate(lastPostDate)
                        .isSticky(isSticky)
                        .isLocked(isLocked)
                        .lastReadPage(lastReadPage)
                        .lastReadPostId(lastReadPostId)
                        .hasUnreadPosts(unreadPosts)
                        .category(currentCategory)
                        .isPrivateMessage(false)
                        .build();

                topics.add(topic);
            }
        }


        return topics;
    }
}
