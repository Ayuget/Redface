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
