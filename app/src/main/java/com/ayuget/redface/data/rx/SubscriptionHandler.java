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

package com.ayuget.redface.data.rx;

import android.util.LruCache;

import java.util.LinkedHashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class SubscriptionHandler<K, T> {
    private final int maxCacheSize;
    private final LruCache<K, T> keysCache;
    private final Map<K, PublishSubject<T>> requests = new LinkedHashMap<>();

    public SubscriptionHandler() {
        this.maxCacheSize = 5;
        this.keysCache = new LruCache<>(maxCacheSize);
    }

    public SubscriptionHandler(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        this.keysCache = new LruCache<>(maxCacheSize);
    }

    public Subscription loadAndCache(final K key, Observable<T> observable, Observer<T> observer) {
        T cachedValues = keysCache.get(key);

        if (cachedValues != null) {
            // If there is some data cached, return it directly
            PublishSubject<T> request = PublishSubject.create();
            Subscription subscription = request.subscribe(observer);

            Observable<T> observableValues = Observable.just(cachedValues);
            observableValues.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(request);

            return subscription;
        }
        else {
            return load(key, observable, observer);
        }
    }

    public void clearAll() {
        keysCache.evictAll();
    }

    public void clearKey(K key) {
        keysCache.remove(key);
    }

    public Subscription load(final K key, Observable<T> observable, Observer<T> observer) {
        PublishSubject<T> request = PublishSubject.create();
        requests.put(key, request);

        Subscription subscription = request.subscribe(observer);

        request.subscribe(new EndObserver<T>() {
            @Override
            public void onEnd() {
                requests.remove(key);
            }

            @Override
            public void onNext(T values) {
                keysCache.put(key, values);
            }
        });

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(request);

        return subscription;
    }
}
