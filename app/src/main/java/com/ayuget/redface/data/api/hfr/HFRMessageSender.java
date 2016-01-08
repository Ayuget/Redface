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

import android.util.Log;

import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.MDMessageSender;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.ResponseCode;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.ui.UIConstants;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class HFRMessageSender implements MDMessageSender {
    private static final Pattern POST_SUCCESSFULLY_ADDED_PATTERN = Pattern.compile("(.*)(Votre réponse a été postée avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TOPIC_SUCESSFULLY_CREATED_PATTERN = Pattern.compile("(.*)(Votre message a été posté avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern POST_SUCCESSFULLY_EDITED_PATTERN = Pattern.compile("(.*)(Votre message a été édité avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern INVALID_PASSWORD_PATTERN = Pattern.compile("(.*)((Mot de passe incorrect !)|((.*)(Votre mot de passe ou nom d'utilisateur n'est pas valide)(.*)))(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern POST_FLOOD_PATTERN = Pattern.compile("(.*)(réponses consécutives)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TOPIC_FLOOD_PATTERN = Pattern.compile("(.*)(nouveaux sujets consécutifs)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FAVORITE_SUCCESFULLY_ADDED = Pattern.compile("(.*)(Favori positionné avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern MESSAGE_SUCCESSFULLY_DELETED = Pattern.compile("(.*)(Message effacé avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


    private final HTTPClientProvider httpClientProvider;

    private final MDEndpoints mdEndpoints;

    @Inject
    public HFRMessageSender(HTTPClientProvider httpClientProvider, MDEndpoints mdEndpoints) {
        this.httpClientProvider = httpClientProvider;
        this.mdEndpoints = mdEndpoints;
    }

    @Override
    public Observable<Response> replyToTopic(final User user, final Topic topic, final String message, final String hashcheck, final boolean includeSignature) {
        return Observable.create(new Observable.OnSubscribe<Response>() {
            @Override
            public void call(Subscriber<? super Response> subscriber) {
                Timber.d("Posting message for user '%s' in topic '%s'", user.getUsername(), topic.getSubject());

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                boolean isPrivateMessage = topic.getCategory().getId() == UIConstants.PRIVATE_MESSAGE_CAT_ID;

                if (isPrivateMessage) {
                    Timber.d("Replying to private message");
                }

                RequestBody formBody = new FormEncodingBuilder()
                        .add("hash_check", hashcheck)
                        .add("post", String.valueOf(topic.getId()))
                        .add("cat", isPrivateMessage ? "prive" : String.valueOf(topic.getCategory().getId()))
                        .add("verifrequet", "1100")
                        .add("MsgIcon", "20")
                        .add("page", String.valueOf(topic.getPagesCount()))
                        .add("pseudo", user.getUsername())
                        .add("sujet", topic.getSubject())
                        .add("signature", includeSignature ? "1" : "0")
                        .add("content_form", message)
                        .add("emaill", "0")
                        .build();

                Request request = new Request.Builder()
                        .url(mdEndpoints.replyUrl())
                        .post(formBody)
                        .build();

                try {
                    com.squareup.okhttp.Response response = httpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        subscriber.onNext(buildResponse(response.body().string()));
                    }
                    else {
                        Timber.d("Error HTTP Code, response is : %s", response.body().string());
                        subscriber.onNext(Response.buildFailure(ResponseCode.UNKNOWN_ERROR));
                    }

                    subscriber.onCompleted();
                }
                catch (IOException e) {
                    Timber.e(e, "Exception while posting response");
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Response> editPost(final User user, final Topic topic, final int postId, final String newContent, final String hashcheck, final boolean includeSignature) {
        return Observable.create(new Observable.OnSubscribe<Response>() {
            @Override
            public void call(Subscriber<? super Response> subscriber) {
                Timber.d("Editing message for user '%s' in topic '%s' (category id : %d)", user.getUsername(), topic.getSubject(), topic.getCategory().getId());

                StringBuilder parents = new StringBuilder();
                Matcher m = Pattern.compile("\\[quotemsg=([0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(newContent);
                while (m.find()) {
                    if (!parents.toString().equals("")) {
                        parents.append("-");
                    }
                    parents.append(m.group(1));
                }

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                boolean isPrivateMessage = topic.getCategory().getId() == UIConstants.PRIVATE_MESSAGE_CAT_ID;

                if (isPrivateMessage) {
                    Timber.d("Editing private message");
                }

                FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
                formEncodingBuilder.add("hash_check", hashcheck);
                formEncodingBuilder.add("post", String.valueOf(topic.getId()));
                formEncodingBuilder.add("cat",  isPrivateMessage ? "prive" : String.valueOf(topic.getCategory().getId()));
                formEncodingBuilder.add("verifrequet", "1100");
                formEncodingBuilder.add("pseudo", user.getUsername());
                formEncodingBuilder.add("sujet", topic.getSubject());
                formEncodingBuilder.add("signature", includeSignature ? "1" : "0");
                formEncodingBuilder.add("content_form", newContent);
                formEncodingBuilder.add("parents", parents.toString());
                formEncodingBuilder.add("post", String.valueOf(topic.getId()));
                formEncodingBuilder.add("sujet", topic.getSubject());
                formEncodingBuilder.add("numreponse", String.valueOf(postId));
                formEncodingBuilder.add("emaill", "0");
                RequestBody formBody = formEncodingBuilder.build();

                Request request = new Request.Builder()
                        .url(mdEndpoints.editUrl())
                        .post(formBody)
                        .build();

                try {
                    com.squareup.okhttp.Response response = httpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        subscriber.onNext(buildResponse(response.body().string()));
                    }
                    else {
                        Timber.d("Error HTTP Code, response is : %s", response.body().string());
                        subscriber.onNext(Response.buildFailure(ResponseCode.UNKNOWN_ERROR));
                    }

                    subscriber.onCompleted();
                }
                catch (IOException e) {
                    Timber.e(e, "Exception while posting response");
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Response> sendNewPrivateMessage(final User user, final String subject, final String recipientUsername, final String message, final String hashcheck, final boolean includeSignature) {
        return Observable.create(new Observable.OnSubscribe<Response>() {
            @Override
            public void call(Subscriber<? super Response> subscriber) {
                Timber.d("Sending new private message from user '%s' to user '%s'", user.getUsername(), recipientUsername);

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
                formEncodingBuilder.add("hash_check", hashcheck);
                formEncodingBuilder.add("cat", "prive");
                formEncodingBuilder.add("verifrequet", "1100");
                formEncodingBuilder.add("pseudo", user.getUsername());
                formEncodingBuilder.add("dest", recipientUsername);
                formEncodingBuilder.add("signature", includeSignature ? "1" : "0");
                formEncodingBuilder.add("content_form", message);
                formEncodingBuilder.add("sujet", subject);
                formEncodingBuilder.add("emaill", "0");
                formEncodingBuilder.add("MsgIcon", "20");
                RequestBody formBody = formEncodingBuilder.build();

                Request request = new Request.Builder()
                        .url(mdEndpoints.replyUrl())
                        .post(formBody)
                        .build();

                try {
                    com.squareup.okhttp.Response response = httpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        subscriber.onNext(buildResponse(response.body().string()));
                    }
                    else {
                        Timber.d("Error HTTP Code, response is : %s", response.body().string());
                        subscriber.onNext(Response.buildFailure(ResponseCode.UNKNOWN_ERROR));
                    }

                    subscriber.onCompleted();
                }
                catch (IOException e) {
                    Timber.e(e, "Exception while sending new private message");
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> markPostAsFavorite(final User user, final Topic topic, final int postId) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                Timber.d("Marking message '%d' as favorite for user '%s' in topic '%s'", postId, user.getUsername(), topic.getSubject());

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                Request request = new Request.Builder()
                        .url(mdEndpoints.favorite(topic.getCategory(), topic, postId))
                        .build();

                try {
                    com.squareup.okhttp.Response response = httpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        boolean success = matchesPattern(FAVORITE_SUCCESFULLY_ADDED, responseBody);
                        subscriber.onNext(success);
                    }
                    else {
                        Timber.d("Error HTTP Code, response is : %s", response.body().string());
                        subscriber.onNext(false);
                    }

                    subscriber.onCompleted();
                }
                catch (IOException e) {
                    Timber.e(e, "Exception while marking post as favorite");
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> deletePost(final User user, final Topic topic, final int postId, final String hashcheck) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                Timber.d("Deleting post '%d' as user '%s' in topic '%s'", postId, user.getUsername(), topic.getSubject());

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                boolean isPrivateMessage = topic.getCategory().getId() == UIConstants.PRIVATE_MESSAGE_CAT_ID;

                if (isPrivateMessage) {
                    Timber.d("Editing private message");
                }

                FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
                formEncodingBuilder.add("hash_check", hashcheck);
                formEncodingBuilder.add("cat", isPrivateMessage ? "prive" : String.valueOf(topic.getCategory().getId()));
                formEncodingBuilder.add("pseudo", user.getUsername());
                formEncodingBuilder.add("numreponse", String.valueOf(postId));
                formEncodingBuilder.add("post", String.valueOf(topic.getId()));
                formEncodingBuilder.add("delete", "1");

                RequestBody formBody = formEncodingBuilder.build();

                Request request = new Request.Builder()
                        .url(mdEndpoints.deletePost())
                        .post(formBody)
                        .build();

                try {
                    com.squareup.okhttp.Response response = httpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        boolean success = matchesPattern(MESSAGE_SUCCESSFULLY_DELETED, responseBody);
                        subscriber.onNext(success);
                    }
                    else {
                        Timber.d("Error HTTP Code, response is : %s", response.body().string());
                        subscriber.onNext(false);
                    }

                    subscriber.onCompleted();
                }
                catch (IOException e) {
                    Timber.e(e, "Exception while deleteing post");
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> reportPost(User user, Topic topic, int postId) {
        return null;
    }

    private Response buildResponse(String response) {
        Timber.d(response);

        if (matchesPattern(POST_SUCCESSFULLY_ADDED_PATTERN, response)) {
            return Response.buildSuccess(ResponseCode.POST_SUCCESSFULLY_ADDED);
        }
        else if (matchesPattern(TOPIC_SUCESSFULLY_CREATED_PATTERN, response)) {
            return Response.buildSuccess(ResponseCode.TOPIC_SUCESSFULLY_CREATED);
        }
        else if (matchesPattern(POST_SUCCESSFULLY_EDITED_PATTERN, response)) {
            return Response.buildSuccess(ResponseCode.POST_SUCCESFULLY_EDITED);
        }
        else if (matchesPattern(INVALID_PASSWORD_PATTERN, response)) {
            return Response.buildFailure(ResponseCode.INVALID_PASSWORD);
        }
        else if (matchesPattern(POST_FLOOD_PATTERN, response)) {
            return Response.buildFailure(ResponseCode.POST_FLOOD);
        }
        else if (matchesPattern(TOPIC_FLOOD_PATTERN, response)) {
            return Response.buildFailure(ResponseCode.TOPIC_FLOOD);
        }
        else {
            return Response.buildFailure(ResponseCode.UNKNOWN_ERROR);
        }
    }

    private boolean matchesPattern(Pattern p, String content) {
        return p.matcher(content).matches();
    }
}
