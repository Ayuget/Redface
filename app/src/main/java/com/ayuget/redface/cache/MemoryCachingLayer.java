package com.ayuget.redface.cache;

import java.util.HashMap;

import rx.Completable;
import rx.Observable;
import timber.log.Timber;

/**
 * Specific implementation of a {@link CachingLayer}, using a plain old {@link HashMap} to store
 * key/value pairs in memory.
 */
public class MemoryCachingLayer<K, V> implements CachingLayer<K, V> {
    private final HashMap<K, CachedValue<V>> cachedValues = new HashMap<>();

    @Override
    public Observable<CachedValue<V>> get(final K key) {
        Timber.d("Request for key '%s' from memory", key);
        CachedValue<V> cachedValue = cachedValues.get(key);

        if (cachedValue == null) {
            Timber.d("Request for key '%s' from memory => no such cached value", key);
            return Observable.empty();
        } else {
            Timber.d("Request for key '%s' from memory => got value '%s'", key, cachedValue);
            return Observable.just(cachedValue);
        }
    }

    @Override
    public Completable removeAll() {
        return Completable.fromAction(cachedValues::clear);
    }

    @Override
    public Completable remove(final K key) {
        return Completable.fromAction(() -> cachedValues.remove(key));
    }

    @Override
    public Completable save(final K key, final V value) {
        Timber.d("Saving key '%s' in memory, value is '%s'", key, value);
        return Completable.fromAction(() -> {
            CachedValue<V> cachedValue = CachedValue.from(value);
            cachedValues.put(key, cachedValue);
            Timber.d("Successfully saved key '%s' in memory (value = %s)", key, value);
        });
    }
}
