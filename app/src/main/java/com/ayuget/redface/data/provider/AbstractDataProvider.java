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

package com.ayuget.redface.data.provider;

import android.util.Pair;

import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.storage.BoundedDiskCache;
import com.ayuget.redface.util.DateUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

public abstract class AbstractDataProvider<K, V> {
    private final long staleThreshold;

    private final Map<User, UserMemoryCache<K, V>> userMemoryCaches;

    private final BoundedDiskCache<K, V> boundedDiskCache;

    public AbstractDataProvider(BoundedDiskCache<K, V> boundedDiskCache, long staleThreshold) {
        this.userMemoryCaches = new HashMap<>();
        this.boundedDiskCache = boundedDiskCache;
        this.staleThreshold = staleThreshold;
    }

    private UserMemoryCache<K, V> getUserCache(User user) {
        if (! userMemoryCaches.containsKey(user)) {
            UserMemoryCache<K, V> userMemoryCache = new UserMemoryCache<>();
            userMemoryCaches.put(user, userMemoryCache);
        }

        return userMemoryCaches.get(user);
    }

    private Observable<V> fromMemory(User user, K key) {
        UserMemoryCache<K, V> userMemoryCache = userMemoryCaches.get(user);

        if (!userMemoryCache.containsKey(key)) {
            return Observable.empty();
        }
        else {
            Pair<Long, V> cachedValue = userMemoryCache.get(key);
            long cachedSince = DateUtils.getCurrentTimestamp() - cachedValue.first;

            if (cachedSince < staleThreshold) {
                return Observable.just(userMemoryCache.get(key).second);
            }
            else {
                // Value has expired
                userMemoryCache.remove(key);
                return Observable.empty();
            }
        }
    }

    private Observable<V> fromDisk(User user, K key) {
        return boundedDiskCache.get(user, key).filter(new Func1<BoundedDiskCache.CacheEntry<K, V>, Boolean>() {
            @Override
            public Boolean call(BoundedDiskCache.CacheEntry<K, V> kvCacheEntry) {
                long cachedSince = DateUtils.getCurrentTimestamp() - kvCacheEntry.timestamp();
                return cachedSince < staleThreshold;
            }
        }).map(new Func1<BoundedDiskCache.CacheEntry<K, V>, V>() {
            @Override
            public V call(BoundedDiskCache.CacheEntry<K, V> kvCacheEntry) {
                return kvCacheEntry.value();
            }
        });
    }

    private void saveToMemory(User user, K key, V value) {
        long currentTimestamp = DateUtils.getCurrentTimestamp();
        getUserCache(user).put(key, value, currentTimestamp);
    }

    private void saveToDisk(User user, K key, V value) {
        try {
            boundedDiskCache.put(user, key, value);
        }
        catch (IOException e) {
            // fixme internal lint error when exception is passed as first argument. Probably a
            // platform bug, or a bug in the custom Timber Lint rule
        }
    }

    private Observable<V> fromDiskWithCaching(final User user, final K key) {
        return fromDisk(user, key).doOnNext(new Action1<V>() {
            @Override
            public void call(V value) {
                saveToMemory(user, key, value);
            }
        });
    }

    private Observable<V> fromNetworkWithCaching(final User user, final K key) {
        return fromNetwork(user, key).doOnNext(new Action1<V>() {
            @Override
            public void call(V value) {
                saveToDisk(user, key, value);
                saveToMemory(user, key, value);
            }
        });
    }

    protected abstract Observable<V> fromNetwork(User user, K key);

    public Observable<V> get(User user, K key) {
        return Observable
                .concat(fromMemory(user, key), fromDiskWithCaching(user, key), fromNetworkWithCaching(user, key))
                .first();
    }

    
}
