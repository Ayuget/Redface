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

package com.ayuget.redface.data.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class ResponseStore {
    private static final String RESPONSE_PREFS = "RedfaceResponses";
    private static final String RESPONSE_NAME_PREFIX = "response_";

    /**
     * Shared preferences where the responses will be persisted
     */
    private SharedPreferences responsesPrefs;

    /**
     * Responses cache, key is the topic
     */
    private Map<CacheKey, String> responsesCache;

    private static class CacheKey {
        String username;
        int topicId;

        private CacheKey(String username, int topicId) {
            this.username = username;
            this.topicId = topicId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (topicId != cacheKey.topicId) return false;
            if (!username.equals(cacheKey.username)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = username.hashCode();
            result = 31 * result + topicId;
            return result;
        }
    }

    public ResponseStore(Context context) {
        this.responsesCache = new HashMap<>();

        this.responsesPrefs = context.getSharedPreferences(RESPONSE_PREFS, 0);
        loadFromSharedPreferences();
    }

    public String getResponse(User user, Topic topic) {
        CacheKey key = new CacheKey(user.getUsername(), topic.id());
        if (responsesCache.containsKey(key)) {
            return responsesCache.get(key);
        }
        else {
            return null;
        }
    }

    private String getPrefsKey(User user, Topic topic) {
        return RESPONSE_NAME_PREFIX + user.getUsername() + "_" + topic.id();
    }

    public void storeResponse(User user, Topic topic, String message) {
        String prefsKey = getPrefsKey(user,  topic);
        CacheKey cacheKey = new CacheKey(user.getUsername(), topic.id());

        responsesCache.put(cacheKey, message);

        SharedPreferences.Editor editor = responsesPrefs.edit();
        editor.putString(prefsKey, message);
        editor.apply();
    }

    public void removeResponse(User user, Topic topic) {
        String prefsKey = getPrefsKey(user,  topic);
        CacheKey cacheKey = new CacheKey(user.getUsername(), topic.id());

        responsesCache.remove(cacheKey);

        SharedPreferences.Editor editor = responsesPrefs.edit();
        editor.remove(prefsKey);
        editor.apply();
    }

    protected void loadFromSharedPreferences() {
        Map<String, ?> prefsMap = responsesPrefs.getAll();

        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            String entryKey = entry.getKey();
            String entryValue = (String) entry.getValue();

            if (entryKey.startsWith(RESPONSE_NAME_PREFIX)) {
                List<String> tokens = Arrays.asList(TextUtils.split(entryKey, "_"));

                if (tokens.size() == 3) {
                    String username = tokens.get(1);
                    int topicId = Integer.valueOf(tokens.get(2));

                    CacheKey key = new CacheKey(username, topicId);
                    responsesCache.put(key, entryValue);
                }
                else {
                    Timber.w("Unable to decode property '%s' from sharedPreferences, invalid name", entryKey);
                }
            }
        }
    }
}
