package com.ayuget.redface.network;

import com.ayuget.redface.data.api.model.User;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import javax.inject.Inject;

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
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
