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

import com.ayuget.redface.BuildConfig;
import com.ayuget.redface.data.api.MDAuthenticator;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.network.HTTPClientProvider;

import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class HFRAuthenticator implements MDAuthenticator {
    public static final Pattern AUTHENTICATION_ERROR_PATTERN = Pattern.compile("(.*)(Votre mot de passe ou nom d\'utilisateur n\'est pas valide)(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final HTTPClientProvider httpClientProvider;

    private final MDEndpoints mdEndpoints;

    @Inject
    public HFRAuthenticator(HTTPClientProvider httpClientProvider, MDEndpoints mdEndpoints) {
        this.httpClientProvider = httpClientProvider;
        this.mdEndpoints = mdEndpoints;
    }

    @Override
    public Observable<Boolean> login(final User user) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                Timber.d("Logging in user '%s'", user.getUsername());

                OkHttpClient httpClient = httpClientProvider.getClientForUser(user);

                RequestBody formBody = new FormBody.Builder()
                        .add("pseudo", user.getUsername())
                        .add("password", user.getPassword())
                        .build();

                Request request = new Request.Builder()
                        .url(mdEndpoints.loginUrl())
                        .post(formBody)
                        .build();

                try {
                    Response response = httpClient.newCall(request).execute();

                    Matcher matcher = AUTHENTICATION_ERROR_PATTERN.matcher(response.body().string());
                    boolean loginFailed = matcher.find();

                    if (response.isSuccessful() && !loginFailed) {
                        // Successful login
                        Timber.d("User '%s' was successfully logged in", user.getUsername());

                        if (BuildConfig.DEBUG) {
                            printReceivedCookies(user);
                        }

                        subscriber.onNext(true);
                    }
                    else {
                        Timber.e("Failed to log in user '%s'", user.getUsername());
                        subscriber.onNext(false);
                    }

                    subscriber.onCompleted();

                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private void printReceivedCookies(User user) {
        CookieStore cookieStore = httpClientProvider.getUserCookieStore(user);
        for (HttpCookie cookie : cookieStore.getCookies()) {
            Timber.d("Received cookie '%s'", cookie.getName());
        }
    }
}
