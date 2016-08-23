package com.ayuget.redface.cache;

import com.jakewharton.disklrucache.DiskLruCache;

import java.util.HashMap;

import rx.Completable;
import rx.Observable;
import rx.functions.Action0;
import timber.log.Timber;

/**
 * Specific implementation of a {@link CachingLayer}, using a plain old {@link HashMap} to store
 * key/value pairs in memory.
 */
public class MemoryCachingLayer<K, V extends Cacheable<K>> implements CachingLayer<K, V> {
    private final HashMap<K, Cached<V>> cachedValues = new HashMap<>();

    @Override
    public Observable<Cached<V>> get(final K key) {
        Timber.d("Request for key '%s' from memory", key);
        Cached<V> cached = cachedValues.get(key);

        if (cached == null) {
            Timber.d("Request for key '%s' from memory => no such cached value", key);
            return Observable.empty();
        } else {
            Timber.d("Request for key '%s' from memory => got value '%s'", key, cached);
            return Observable.just(cached);
        }
    }

    @Override
    public Completable removeAll() {
        return Completable.fromAction(new Action0() {
            @Override
            public void call() {
                cachedValues.clear();
            }
        });
    }

    @Override
    public Completable remove(final K key) {
        return Completable.fromAction(new Action0() {
            @Override
            public void call() {
                cachedValues.remove(key);
            }
        });
    }

    @Override
    public Completable save(final K key, final V value) {
        Timber.d("Saving key '%s' in memory, value is '%s'", key, value);
        return Completable.fromAction(new Action0() {
            @Override
            public void call() {
                Cached<V> cached = Cached.from(value);
                cachedValues.put(key, cached);
                Timber.d("Successfully saved key '%s' in memory (value = %s)", key, value);
            }
        });
    }
}
