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
import com.ayuget.redface.data.api.MDMessageSender;
import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.ResponseCode;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicSearchResult;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.ui.UIConstants;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class HFRMessageSender implements MDMessageSender {
    private static final Pattern POST_SUCCESSFULLY_ADDED_PATTERN = Pattern.compile("(.*)(Votre réponse a été postée avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TOPIC_SUCESSFULLY_CREATED_PATTERN = Pattern.compile("(.*)(Votre message a été posté avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern POST_SUCCESSFULLY_EDITED_PATTERN = Pattern.compile("(.*)(Votre message a été édité avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern INVALID_PASSWORD_PATTERN = Pattern.compile("(.*)((Mot de passe incorrect !)|((.*)(Votre mot de passe ou nom d'utilisateur n'est pas valide)(.*)))(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern POST_FLOOD_PATTERN = Pattern.compile("(.*)(réponses consécutives)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern MESSAGE_ALERT_SUCCESSFULLY_JOINED = Pattern.compile("(.*)(Vous êtes désormais)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TOPIC_FLOOD_PATTERN = Pattern.compile("(.*)(nouveaux sujets consécutifs)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FAVORITE_SUCCESSFULLY_ADDED = Pattern.compile("(.*)(Favori positionné avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern MESSAGE_SUCCESSFULLY_DELETED = Pattern.compile("(.*)(Message effacé avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern MESSAGE_SUCCESSFULLY_REPORTED = Pattern.compile("(.*)(Un message a été envoyé avec succès aux modérateurs)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FLAG_SUCCESSFULLY_REMOVED = Pattern.compile("(.*)(Drapeau effacé avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EXTRACT_SEARCHED_POST_LOCATION_PATTERN = Pattern.compile("(?:page=)(\\d+)(?:.*?)(?:currentnum=)(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
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
                Timber.d("Posting message for user '%s' in topic '%s'", user.getUsername(), topic.title());

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                boolean isPrivateMessage = topic.category().id() == UIConstants.PRIVATE_MESSAGE_CAT_ID;

                if (isPrivateMessage) {
                    Timber.d("Replying to private message");
                }

                RequestBody formBody = new FormBody.Builder()
                        .add("hash_check", hashcheck)
                        .add("post", String.valueOf(topic.id()))
                        .add("cat", isPrivateMessage ? "prive" : String.valueOf(topic.category().id()))
                        .add("verifrequet", "1100")
                        .add("MsgIcon", "20")
                        .add("page", String.valueOf(topic.pagesCount()))
                        .add("pseudo", user.getUsername())
                        .add("sujet", topic.title())
                        .add("signature", includeSignature ? "1" : "0")
                        .add("content_form", message)
                        .add("emaill", "0")
                        .build();

                Request request = new Request.Builder()
                        .url(mdEndpoints.replyUrl())
                        .post(formBody)
                        .build();

                try {
                    okhttp3.Response response = httpClient.newCall(request).execute();

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
                Timber.d("Editing message for user '%s' in topic '%s' (category id : %d)", user.getUsername(), topic.title(), topic.category().id());

                StringBuilder parents = new StringBuilder();
                Matcher m = Pattern.compile("\\[quotemsg=([0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(newContent);
                while (m.find()) {
                    if (!parents.toString().equals("")) {
                        parents.append("-");
                    }
                    parents.append(m.group(1));
                }

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                boolean isPrivateMessage = topic.category().id() == UIConstants.PRIVATE_MESSAGE_CAT_ID;

                if (isPrivateMessage) {
                    Timber.d("Editing private message");
                }

                RequestBody formBody = new FormBody.Builder()
                        .add("hash_check", hashcheck)
                        .add("post", String.valueOf(topic.id()))
                        .add("cat",  isPrivateMessage ? "prive" : String.valueOf(topic.category().id()))
                        .add("verifrequet", "1100")
                        .add("pseudo", user.getUsername())
                        .add("sujet", topic.title())
                        .add("signature", includeSignature ? "1" : "0")
                        .add("content_form", newContent)
                        .add("parents", parents.toString())
                        .add("post", String.valueOf(topic.id()))
                        .add("sujet", topic.title())
                        .add("numreponse", String.valueOf(postId))
                        .add("emaill", "0")
                        .build();

                Request request = new Request.Builder()
                        .url(mdEndpoints.editUrl())
                        .post(formBody)
                        .build();

                try {
                    okhttp3.Response response = httpClient.newCall(request).execute();

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

                RequestBody formBody = new FormBody.Builder()
                        .add("hash_check", hashcheck)
                        .add("cat", "prive")
                        .add("verifrequet", "1100")
                        .add("pseudo", user.getUsername())
                        .add("dest", recipientUsername)
                        .add("signature", includeSignature ? "1" : "0")
                        .add("content_form", message)
                        .add("sujet", subject)
                        .add("emaill", "0")
                        .add("MsgIcon", "20")
                        .build();

                Request request = new Request.Builder()
                        .url(mdEndpoints.replyUrl())
                        .post(formBody)
                        .build();

                try {
                    okhttp3.Response response = httpClient.newCall(request).execute();

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
                Timber.d("Marking message '%d' as favorite for user '%s' in topic '%s'", postId, user.getUsername(), topic.title());

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                Request request = new Request.Builder()
                        .url(mdEndpoints.favorite(topic.category(), topic, postId))
                        .build();

                try {
                    okhttp3.Response response = httpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        boolean success = matchesPattern(FAVORITE_SUCCESSFULLY_ADDED, responseBody);
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
                Timber.d("Deleting post '%d' as user '%s' in topic '%s'", postId, user.getUsername(), topic.title());

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                boolean isPrivateMessage = topic.category().id() == UIConstants.PRIVATE_MESSAGE_CAT_ID;

                if (isPrivateMessage) {
                    Timber.d("Editing private message");
                }

                RequestBody formBody = new FormBody.Builder()
                        .add("hash_check", hashcheck)
                        .add("cat", isPrivateMessage ? "prive" : String.valueOf(topic.category().id()))
                        .add("pseudo", user.getUsername())
                        .add("numreponse", String.valueOf(postId))
                        .add("post", String.valueOf(topic.id()))
                        .add("delete", "1")
                        .build();

                Request request = new Request.Builder()
                        .url(mdEndpoints.deletePost())
                        .post(formBody)
                        .build();

                try {
                    okhttp3.Response response = httpClient.newCall(request).execute();

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
    public Observable<Boolean> reportPost(User user, Topic topic, int postId, String reason, boolean joinReport, final String hashcheck) {
        return Observable.create(subscriber -> {
            Timber.d("Reporting post '%d' as user '%s' in topic '%s'", postId, user.getUsername(), topic.title());

            OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

            RequestBody formBody;

            if (joinReport) {
                formBody = new FormBody.Builder()
                        .add("hash_check", hashcheck)
                        .add("referer_page", mdEndpoints.topic(topic))
                        .add("cfmodoalert", "1")
                        .build();
            }
            else {
                formBody = new FormBody.Builder()
                        .add("hash_check", hashcheck)
                        .add("referer_page", mdEndpoints.topic(topic))
                        .add("raison", reason)
                        .build();
            }

            Request request = new Request.Builder()
                    .url(mdEndpoints.reportPost(topic.category(), topic, postId))
                    .post(formBody)
                    .build();

            try {
                okhttp3.Response response = httpClient.newCall(request).execute();

                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();

                    boolean success;

                    if (joinReport) {
                        success = matchesPattern(MESSAGE_ALERT_SUCCESSFULLY_JOINED, responseBody);
                    }
                    else {
                        success = matchesPattern(MESSAGE_SUCCESSFULLY_REPORTED, responseBody);
                    }

                    subscriber.onNext(success);
                }
                else {
                    Timber.d("Error HTTP Code, response is : %s", response.body().string());
                    subscriber.onNext(false);
                }

                subscriber.onCompleted();
            }
            catch (IOException e) {
                Timber.e(e, "Exception while reporting post");
                subscriber.onError(e);
            }
        });
    }

    @Override
    public Observable<Boolean> unflagTopic(final User user, final Topic topic) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                Timber.d("Removing flag (or favorite) on topic '%d' for user '%s'", topic.id(), user.getUsername());

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                Request request = new Request.Builder()
                        .url(mdEndpoints.removeFlag(topic.category(), topic))
                        .build();

                try {
                    okhttp3.Response response = httpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        final String responseBody = response.body().string();
                        boolean success = matchesPattern(FLAG_SUCCESSFULLY_REMOVED, responseBody);
                        subscriber.onNext(success);
                    }
                    else {
                        Timber.d("Error HTTP Code, response is : %s", response.body().string());
                        subscriber.onNext(false);
                    }

                    subscriber.onCompleted();
                }
                catch (IOException e) {
                    Timber.e(e, "Exception while removing flag on topic");
                    subscriber.onError(e);
                }
            }
        });
    }

    @Override
    public Observable<TopicSearchResult> searchInTopic(User user, Topic topic, long startFromPostId, String word, String author, boolean firstSearch, String hashcheck) {
        return Observable.create(subscriber -> {
            Timber.d("Searching topic '%d' for word '%s', starting at post '%d'", topic.id(), word, startFromPostId);

            boolean isPrivateMessage = topic.category().id() == UIConstants.PRIVATE_MESSAGE_CAT_ID;

            OkHttpClient httpClient = httpClientProvider.getClientForUser(user)
                    .newBuilder()
                    .followRedirects(false)
                    .build();

            RequestBody formBody = new FormBody.Builder()
                    .add("hash_check", hashcheck)
                    .add("post", String.valueOf(topic.id()))
                    .add("cat", isPrivateMessage ? "prive" : String.valueOf(topic.category().id()))
                    .add("p", "1")
                    .add("word", word == null ? "" : word)
                    .add("spseudo", author == null ? "" : author)
                    .add("dep", "0")
                    .add(firstSearch ? "firstnum": "currentnum", String.valueOf(startFromPostId))
                    .build();

            Request request = new Request.Builder()
                    .url(mdEndpoints.searchTopic())
                    .post(formBody)
                    .build();

            try {
                okhttp3.Response response = httpClient.newCall(request).execute();

                if (response.isRedirect()) {
                    String locationHeader = response.header("Location");
                    subscriber.onNext(parseFoundPostLocation(locationHeader));
                }
                else {
                    subscriber.onNext(TopicSearchResult.createAsNoMoreResult());
                }
            }
            catch (IOException e) {
                Timber.e(e, "Exception while removing flag on topic");
                subscriber.onError(e);
            }
        });
    }

    private TopicSearchResult parseFoundPostLocation(String locationHeader) {
        Matcher matcher = EXTRACT_SEARCHED_POST_LOCATION_PATTERN.matcher(locationHeader);
        if (matcher.find()) {
            return TopicSearchResult.create(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        }
        else {
            return TopicSearchResult.createAsNoMoreResult();
        }
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
