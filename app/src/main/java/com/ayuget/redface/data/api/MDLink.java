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
import com.ayuget.redface.ui.misc.PagePosition;
import com.google.common.base.Preconditions;

/**
 * Represents an internal link, that can be in two forms :
 *
 * <ul>
 *     <li>Rewritten URLs : new kind of links, SEO optimized</li>
 *     <li>Classic links</li>
 * </ul>
 */
public class MDLink {
    /**
     * Type of links handled in the application
     */
    private enum LinkType {
        TOPIC,
        CATEGORY,
        INVALID
    }

    public interface IfIsTopicLink {
        void call(Category category, int topicId, int topicPage, PagePosition pagePosition);
    }

    public interface IfIsCategoryLink {
        void call(Category category);
    }

    public interface IfIsInvalidLink {
        void call();
    }

    private final LinkType linkType;

    private final Category category;

    private final int topicId;

    private final int topicPage;

    private final PagePosition pagePosition;

    public MDLink(Builder builder) {
        this.linkType = builder.linkType;
        this.category = builder.category;
        this.topicId = builder.topicId;
        this.topicPage = builder.topicPage;
        this.pagePosition = builder.pagePosition;
    }

    public static class Builder {
        private final LinkType linkType;

        private Category category;

        private int topicId;

        private int topicPage;

        private PagePosition pagePosition;

        private Builder(LinkType linkType) {
            this.linkType = linkType;
            this.topicPage = 1;
            this.pagePosition = new PagePosition(PagePosition.TOP);
        }

        public Builder withCategory(Category category) {
            this.category = category;
            return this;
        }

        public Builder withTopicId(int topicId) {
            this.topicId = topicId;
            return this;
        }

        public Builder atPage(int topicPage) {
            this.topicPage = topicPage;
            return this;
        }

        public Builder atPost(PagePosition pagePosition) {
            this.pagePosition = pagePosition;
            return this;
        }

        public MDLink build() {
            if (linkType != LinkType.INVALID) {
                Preconditions.checkNotNull(this.category, "Category cannot be null");
            }
            return new MDLink(this);
        }
    }

    public static Builder forCategory(Category category) {
        return new Builder(LinkType.CATEGORY).withCategory(category);
    }

    public static Builder forTopic(Category category, int topicId) {
        return new Builder(LinkType.TOPIC).withCategory(category).withTopicId(topicId);
    }

    public static MDLink invalid() {
        return new Builder(LinkType.INVALID).build();
    }

    public MDLink ifCategoryLink(IfIsCategoryLink callback) {
        if (linkType == LinkType.CATEGORY) {
            callback.call(category);
        }

        return this;
    }

    public MDLink ifTopicLink(IfIsTopicLink callback) {
        if (linkType == LinkType.TOPIC) {
            callback.call(category, topicId, topicPage, pagePosition);
        }

        return this;
    }

    public MDLink ifInvalid(IfIsInvalidLink callback) {
        if (linkType == LinkType.INVALID) {
            callback.call();
        }

        return this;
    }

    public boolean isCategory() {
        return linkType == LinkType.CATEGORY;
    }

    public boolean isTopic() {
        return linkType == LinkType.TOPIC;
    }

    public boolean isInvalid() {
        return linkType == LinkType.INVALID;
    }

    public Category getCategory() {
        return category;
    }

    public int getTopicId() {
        return topicId;
    }

    public int getTopicPage() {
        return topicPage;
    }

    public PagePosition getPagePosition() {
        return pagePosition;
    }
}
