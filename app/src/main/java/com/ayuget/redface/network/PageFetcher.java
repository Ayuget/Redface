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

package com.ayuget.redface.network;

import com.ayuget.redface.data.api.model.User;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

public class PageFetcher {
    private final HTTPClientProvider httpClientProvider;

    @Inject
    public PageFetcher(HTTPClientProvider httpClientProvider) {
        this.httpClientProvider = httpClientProvider;
    }

    public Observable<String> fetchSource(final User user, final String pageUrl) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                // Obtain the HttpClient associated with the current User. Having different clients for
                // each user allows us to easily deal with cookies and to support multi-users in the app
                OkHttpClient client = httpClientProvider.getClientForUser(user);

                CacheControl cacheControl = new CacheControl.Builder().noTransform().build();

                Request request = new Request.Builder()
                        .cacheControl(cacheControl)
                        .url(pageUrl)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    subscriber.onNext(response.body().string());
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
