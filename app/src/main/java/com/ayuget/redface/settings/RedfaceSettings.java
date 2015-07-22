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

package com.ayuget.redface.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.ui.RedfaceTheme;
import com.ayuget.redface.ui.misc.MetaPageOrdering;

public class RedfaceSettings {
    private final Context context;
    private final SharedPreferences sharedPreferences;

    public RedfaceSettings(Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }

    public String getActiveUsername() {
        return sharedPreferences.getString(SettingsConstants.KEY_ACTIVE_USERNAME, null);
    }

    public void updateActiveUsername(String activeUsername) {
        sharedPreferences.edit().putString(SettingsConstants.KEY_ACTIVE_USERNAME, activeUsername).apply();
    }

    public boolean isProxyEnabled() {
        return sharedPreferences.getBoolean(SettingsConstants.KEY_ENABLE_PROXY, false);
    }

    public String getProxyHost() {
        return sharedPreferences.getString(SettingsConstants.KEY_PROXY_HOST, null);
    }

    public RedfaceTheme getTheme() {
        String themeValue = sharedPreferences.getString(SettingsConstants.KEY_THEME, context.getString(R.string.pref_theme_default));
        return RedfaceTheme.valueOf(themeValue);
    }

    public int getProxyPort() {
        return Integer.valueOf(sharedPreferences.getString(SettingsConstants.KEY_PROXY_PORT, "0"));
    }

    public TopicFilter getDefaultTopicFilter() {
        String topicFilterValue = sharedPreferences.getString(SettingsConstants.KEY_DEFAULT_TOPIC_FILTER, context.getResources().getString(R.string.pref_default_topic_filter_default));
        return TopicFilter.valueOf(topicFilterValue);
    }

    public MetaPageOrdering getDefaultMetaPageOrdering() {
        String metaPageOrderingValue = sharedPreferences.getString(SettingsConstants.KEY_META_PAGE_ORDERING, context.getResources().getString(R.string.pref_default_meta_ordering_default));
        return MetaPageOrdering.valueOf(metaPageOrderingValue);
    }

    public boolean showFullyReadTopics() {
        return sharedPreferences.getBoolean(SettingsConstants.KEY_SHOW_FULLY_READ_TOPICS, true);
    }

    public boolean showPreviousPageLastPost() {
        return sharedPreferences.getBoolean(SettingsConstants.KEY_SHOW_PREVIOUS_PAGE_LAST_POST, true);
    }

    public int getDefaultCategoryId() {
        return Integer.valueOf(sharedPreferences.getString(SettingsConstants.KEY_DEFAULT_CATEGORY, context.getResources().getString(R.string.pref_default_category_default)));
    }

    public int getNotLoggedInDefaultCategoryId() {
        return 13; // fixme : ugly
    }

    public boolean isCompactModeEnabled() {
        return sharedPreferences.getBoolean(SettingsConstants.KEY_ENABLE_COMPACT_MODE, false);
    }
}
