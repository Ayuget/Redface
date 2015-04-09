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

import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.User;

import rx.Observable;

public interface MDMessageSender {
    public Observable<Response> replyToTopic(User user, Topic topic, String message, String hashcheck, boolean includeSignature);

    public Observable<Response> editPost(User user, Topic topic, int postId, String newContent, String hashcheck, boolean includeSignature);
}
