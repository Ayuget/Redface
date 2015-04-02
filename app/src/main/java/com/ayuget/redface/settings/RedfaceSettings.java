package com.ayuget.redface.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.ui.RedfaceTheme;

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

    /**
     * @todo ugly... Make it configurable
     */
    public int getDefaultCategoryId() {
        return 13; // Discussions
    }
}
