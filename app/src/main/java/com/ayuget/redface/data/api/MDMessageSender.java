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

import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicSearchResult;
import com.ayuget.redface.data.api.model.User;

import rx.Observable;

/**
 * Interface describing all possible interactions with a {@code MesDiscussions} forum.
 */
public interface MDMessageSender {
    /**
     * Posts a reply to the given {@link Topic}. User's signature can be included, using the
     * {@code includeSignature} argument.
     *
     * {@code hashcheck} argument is purely a technical one, used as "security" by the forum.
     */
    Observable<Response> replyToTopic(User user, Topic topic, String message, String hashcheck, boolean includeSignature);

    /**
     * Edits a post.
     */
    Observable<Response> editPost(User user, Topic topic, int postId, String newContent, String hashcheck, boolean includeSignature);

    /**
     * Sends a new private message to {@code recipientUsername}.
     *
     * While the forum technically allows a private message to be sent to multiple recipients, that
     * features seems to be disabled and therefore not implemented here.
     */
    Observable<Response> sendNewPrivateMessage(User user, String subject, String recipientUsername, String message, String hashcheck, boolean includeSignature);

    /**
     * Marks a post as favorite.
     */
    Observable<Boolean> markPostAsFavorite(User user, Topic topic, int postId);

    /**
     * Deletes a post. One can only delete its own posts, excepts for moderators who have
     * additional rights.
     */
    Observable<Boolean> deletePost(User user, Topic topic, int postId, final String hashcheck);

    /**
     * Reports a post to the moderators.
     */
    Observable<Boolean> reportPost(User user, Topic topic, int postId);

    /**
     * Removes a flag (or a favorite) on a given {@link Topic}
     */
    Observable<Boolean> unflagTopic(User user, Topic topic);

    /**
     * Searches a particular word and/or author in a topic, starting at a given post id.
     */
    Observable<TopicSearchResult> searchInTopic(User user, Topic topic, long startFromPostId, String word, String author, boolean firstSearch, String hashcheck);
}
