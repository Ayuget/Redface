package com.ayuget.redface.data.rx;

import java.util.LinkedHashMap;
import java.util.Map;
import rx.Observable;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class SubscriptionHandler<K, T> {
    private final Map<K, T> keysCache = new LinkedHashMap<>();
    private final Map<K, PublishSubject<T>> requests = new LinkedHashMap<>();

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
