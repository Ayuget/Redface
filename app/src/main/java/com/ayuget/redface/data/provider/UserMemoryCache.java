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

import java.util.HashMap;
import java.util.Map;

public class UserMemoryCache<K, V> {
    private final Map<K, Pair<Long, V>> valueCache = new HashMap<>();

    public void put(K key, V value, long timestamp) {
        valueCache.put(key, Pair.create(timestamp, value));
    }

    public Pair<Long, V> get(K key) {
        return valueCache.get(key);
    }

    public boolean containsKey(K key) {
        return valueCache.containsKey(key);
    }

    public void remove(K key) {
        valueCache.remove(key);
    }
}
