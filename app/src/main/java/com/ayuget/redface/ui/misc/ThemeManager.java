package com.ayuget.redface.ui.misc;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.ayuget.redface.R;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.RedfaceTheme;

public class ThemeManager {
    private static final String LIGHT_THEME_CSS_CLASS = "redface-light";
    private static final String DARK_THEME_CSS_CLASS = "redface-dark";

    private final RedfaceSettings settings;

    private boolean refreshNeeded = false;

    public ThemeManager(RedfaceSettings settings) {
        this.settings = settings;
    }

    public int getActiveThemeStyle() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT) {
            return R.style.Theme_Redface_Light;
        }
        else {
            return R.style.Theme_Redface_Dark;
        }
    }

    public int getReplyWindowStyle() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT) {
            return R.style.Theme_Redface_Transparent_Light;
        }
        else {
            return R.style.Theme_Redface_Transparent_Dark;
        }
    }

    public String getActiveThemeCssClass() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT) {
            return LIGHT_THEME_CSS_CLASS;
        }
        else {
            return DARK_THEME_CSS_CLASS;
        }
    }

    public Theme getMaterialDialogTheme() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT) {
            return Theme.LIGHT;
        }
        else {
            return Theme.DARK;
        }
    }

    public int getListDividerDrawable() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT) {
            return R.drawable.list_divider_light;
        }
        else {
            return R.drawable.list_divider_dark;
        }
    }

    public int getTopicUnreadCountDrawable() {
        RedfaceTheme activeTheme = settings.getTheme();

        if (activeTheme == RedfaceTheme.LIGHT) {
            return R.drawable.topic_unread_count_light;
        }
        else {
            return R.drawable.topic_unread_count_dark;
        }
    }

    public boolean isRefreshNeeded() {
        return refreshNeeded;
    }

    public void setRefreshNeeded(boolean refreshNeeded) {
        this.refreshNeeded = refreshNeeded;
    }
}
