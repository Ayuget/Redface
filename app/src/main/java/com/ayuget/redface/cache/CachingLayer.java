package com.ayuget.redface.cache;

import rx.Completable;
import rx.Observable;

public interface CachingLayer<K, V extends Cacheable<K>> {
    Observable<Cached<V>> get(final K key);

    Completable remove(final K key);

    Completable removeAll();

    Completable save(final K key, final V value);
}
