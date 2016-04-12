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

package com.ayuget.redface.data.api.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.Date;

import javax.annotation.Nullable;

@AutoValue
public abstract class Topic implements Parcelable {
    /**
     * Topic identifier
     */
    public abstract int id();

    /**
     * Topic's title
     */
    public abstract String title();

    /**
     * Total number of pages in the topic
     */
    public abstract int pagesCount();

    /**
     * The "slug" is the url identifier associated with the topic
     */
    @Nullable
    public abstract String slug();


    @Nullable
    public abstract String author();

    /**
     * Total number of posts in the topic
     */
    @Nullable
    public abstract Integer postsCount();

    @Nullable
    public abstract Date lastPostDate();

    @Nullable
    public abstract String lastPostAuthor();

    @Nullable
    public abstract TopicStatus status();

    /**
     * A sticky topic will appear at the top of the category
     */
    @Nullable
    public abstract Boolean isSticky();

    @Nullable
    public abstract Boolean isLocked();

    @Nullable
    public abstract Category category();

    @Nullable
    public abstract Integer lastReadPage();

    @Nullable
    public abstract Long lastReadPostId();

    @Nullable
    public abstract Boolean hasUnreadPosts();

    public int unreadPagesCount() {
        return pagesCount() - lastReadPage();
    }

    public abstract Topic withCategory(Category category);
    public abstract Topic withPagesCount(int pagesCount);

    public static Builder builder() {
        return new AutoValue_Topic.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(int id);
        public abstract Builder title(String title);
        public abstract Builder pagesCount(int pageCount);
        public abstract Builder slug(String slug);
        public abstract Builder author(String author);
        public abstract Builder postsCount(Integer postCount);
        public abstract Builder lastPostDate(Date lastPostDate);
        public abstract Builder status(TopicStatus status);
        public abstract Builder isSticky(Boolean isSticky);
        public abstract Builder isLocked(Boolean isSticky);
        public abstract Builder category(Category category);
        public abstract Builder lastReadPage(Integer lastReadPage);
        public abstract Builder lastReadPostId(Long lastReadPostId);
        public abstract Builder hasUnreadPosts(Boolean hasUnreadPosts);
        public abstract Builder lastPostAuthor(String lastPostAuthor);
        public abstract Topic build();
    }
}
