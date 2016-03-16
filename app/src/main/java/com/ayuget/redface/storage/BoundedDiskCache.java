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

package com.ayuget.redface.storage;

import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.util.DateUtils;
import com.jakewharton.disklrucache.DiskLruCache;
import com.squareup.moshi.JsonAdapter;

import java.io.IOException;

import rx.Observable;

/**
 * Thin wrapper around a {@link com.jakewharton.disklrucache.DiskLruCache} to store a timestamp
 * alongside the real data.
 */
public class BoundedDiskCache<K, V> {
    private static final int DATA_POSITION = 0;
    private static final int TIMESTAMP_POSITION = 1;

    private final DiskLruCache diskLruCache;

    private final JsonAdapter<V> valueAdapter;

    public BoundedDiskCache(DiskLruCache diskLruCache, JsonAdapter<V> valueAdapter) {
        this.diskLruCache = diskLruCache;
        this.valueAdapter = valueAdapter;
    }

    /**
     * Puts a new value in the cache. The value will be encoded into JSON before being persisted
     * to disk.
     */
    public void put(User user, K key, V value) throws IOException {
        String encodedKey = encodeKey(user, key);
        String jsonValue = valueAdapter.toJson(value);

        DiskLruCache.Editor editor = diskLruCache.edit(encodedKey);
        editor.set(DATA_POSITION, jsonValue);
        editor.set(TIMESTAMP_POSITION, String.valueOf(DateUtils.getCurrentTimestamp()));

        editor.commit();
    }

    private String encodeKey(User user, K key) {
        return user + "_" + key.toString();
    }

    /**
     * Gets a value from the cache
     */
    public Observable<CacheEntry<K, V>> get(User user, K key) {
        String encodedKey = encodeKey(user, key);

        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(encodedKey);

            if (snapshot == null) {
                return Observable.empty();
            }
            else {
                V decodedValue = valueAdapter.fromJson(snapshot.getString(DATA_POSITION));
                long valueTimestamp = Long.parseLong(snapshot.getString(TIMESTAMP_POSITION));
                snapshot.close();

                return Observable.just(new CacheEntry<>(key, decodedValue, valueTimestamp));
            }
        }
        catch (IOException e) {
            return Observable.error(e);
        }
    }

    public static class CacheEntry<K, V> {
        private final K key;
        private final V value;
        private final long timestamp;

        private CacheEntry(K key, V value, long timestamp) {
            this.key = key;
            this.value = value;
            this.timestamp = timestamp;
        }

        public static <K, V> CacheEntry<K, V> of(K key, V value, long timestamp) {
            return new CacheEntry<>(key, value, timestamp);
        }

        public K key() {
            return key;
        }

        public V value() {
            return value;
        }

        public long timestamp() {
            return timestamp;
        }
    }
}
