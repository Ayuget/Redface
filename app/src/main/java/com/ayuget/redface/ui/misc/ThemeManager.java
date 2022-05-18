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

package com.ayuget.redface.ui.misc;

import android.content.res.Configuration;

import com.ayuget.redface.R;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.theme.RedfaceTheme;

public class ThemeManager {
    private static final String LIGHT_THEME_CSS_CLASS = "redface-light";
    private static final String DARK_THEME_CSS_CLASS = "redface-dark";
    private static final String NIGHT_THEME_CSS_CLASS = "redface-night";
    private static final String MODERN_QUOTE_STYLE_CLASS = "modern-quotes";
    private static final String OLD_QUOTE_STYLE_CLASS = "old-quotes";

    private final RedfaceSettings settings;

    private boolean refreshNeeded = false;

    public ThemeManager(RedfaceSettings settings) {
        this.settings = settings;
    }

    private boolean isOsLightModeEnabled() {
        return (this.settings.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;
    }

    public int getActiveThemeStyle() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT || (activeTheme == RedfaceTheme.AUTO && isOsLightModeEnabled())) {
            return R.style.Theme_Redface_Light;
        }
        else if (activeTheme == RedfaceTheme.DARK){
            return R.style.Theme_Redface_Dark;
        }
        else {
            return R.style.Theme_Redface_Night;
        }
    }

    public int getReplyWindowStyle() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT || (activeTheme == RedfaceTheme.AUTO && isOsLightModeEnabled())) {
            return R.style.Theme_Redface_Transparent_Light;
        }
        else {
            return R.style.Theme_Redface_Transparent_Dark;
        }
    }

    public String getActiveThemeCssClass() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT || (activeTheme == RedfaceTheme.AUTO && isOsLightModeEnabled())) {
            return LIGHT_THEME_CSS_CLASS;
        }
        else if (activeTheme == RedfaceTheme.DARK){
            return DARK_THEME_CSS_CLASS;
        }
        else {
            return NIGHT_THEME_CSS_CLASS;
        }
    }

    public String getFontSizeCssClass() {
        return "font-" + settings.getFontSize().toString().toLowerCase();
    }

    public int getListDividerDrawable() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT || (activeTheme == RedfaceTheme.AUTO && isOsLightModeEnabled())) {
            return R.drawable.list_divider_light;
        }
        else {
            return R.drawable.list_divider_dark;
        }
    }

    public int getTopicUnreadCountDrawable() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT || (activeTheme == RedfaceTheme.AUTO && isOsLightModeEnabled())) {
            return R.drawable.topic_unread_count_light;
        }
        else {
            return R.drawable.topic_unread_count_dark;
        }
    }

    public int getPrivateMessageUnreadDrawable() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT || (activeTheme == RedfaceTheme.AUTO && isOsLightModeEnabled())) {
            return R.drawable.private_message_unread_light;
        }
        else {
            return R.drawable.private_message_unread_dark;
        }
    }

    public String getQuoteStyleExtraClass() {
        return settings.isUseModernQuoteStyleEnabled() ? MODERN_QUOTE_STYLE_CLASS : OLD_QUOTE_STYLE_CLASS;
    }

    public int getProfileAvatarBackgroundDrawable() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT || (activeTheme == RedfaceTheme.AUTO && isOsLightModeEnabled())) {
            return R.drawable.profile_avatar_background_light;
        }
        else {
            return R.drawable.profile_avatar_background_dark;
        }
    }

    public boolean isRefreshNeeded() {
        return refreshNeeded;
    }

    public void setRefreshNeeded(boolean refreshNeeded) {
        this.refreshNeeded = refreshNeeded;
    }
}
