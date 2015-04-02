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

public class HFRMessageSender implements MDMessageSender {
    private static final String LOG_TAG = HFRMessageSender.class.getSimpleName();

    private static final Pattern POST_SUCCESSFULLY_ADDED_PATTERN = Pattern.compile("(.*)(Votre réponse a été postée avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TOPIC_SUCESSFULLY_CREATED_PATTERN = Pattern.compile("(.*)(Votre message a été posté avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern POST_SUCCESSFULLY_EDITED_PATTERN = Pattern.compile("(.*)(Votre message a été édité avec succès)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern INVALID_PASSWORD_PATTERN = Pattern.compile("(.*)((Mot de passe incorrect !)|((.*)(Votre mot de passe ou nom d'utilisateur n'est pas valide)(.*)))(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern POST_FLOOD_PATTERN = Pattern.compile("(.*)(réponses consécutives)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TOPIC_FLOOD_PATTERN = Pattern.compile("(.*)(nouveaux sujets consécutifs)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


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
                Log.d(LOG_TAG, String.format("Posting message for user '%s' in topic '%s'", user.getUsername(), topic.getSubject()));

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                RequestBody formBody = new FormEncodingBuilder()
                        .add("hash_check", hashcheck)
                        .add("post", String.valueOf(topic.getId()))
                        .add("cat", String.valueOf(topic.getCategory().getId()))
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
                        Log.d(LOG_TAG, String.format("Error HTTP Code, response is : %s", response.body().string()));
                        subscriber.onNext(Response.buildFailure(ResponseCode.UNKNOWN_ERROR));
                    }
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Exception while posting response", e);
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
                Log.d(LOG_TAG, String.format("Posting message for user '%s' in topic '%s'", user.getUsername(), topic.getSubject()));

                StringBuilder parents = new StringBuilder();
                Matcher m = Pattern.compile("\\[quotemsg=([0-9]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(newContent);
                while (m.find()) {
                    if (!parents.toString().equals("")) {
                        parents.append("-");
                    }
                    parents.append(m.group(1));
                }

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
                formEncodingBuilder.add("hash_check", hashcheck);
                formEncodingBuilder.add("post", String.valueOf(topic.getId()));
                formEncodingBuilder.add("cat", String.valueOf(topic.getCategory().getId()));
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
                        Log.d(LOG_TAG, String.format("Error HTTP Code, response is : %s", response.body().string()));
                        subscriber.onNext(Response.buildFailure(ResponseCode.UNKNOWN_ERROR));
                    }
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Exception while posting response", e);
                    subscriber.onError(e);
                }
            }
        });
    }

    private Response buildResponse(String response) {
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
