package com.ayuget.redface.data.api.model;

import android.content.Context;

import com.ayuget.redface.R;

public enum TopicFilter {
    NONE,
    FAVORITE,
    PARTICIPATED,
    READ;

    public String resolve(Context context) {
        if (this == NONE) {
            return context.getResources().getString(R.string.action_topics_filter_all);
        }
        else if (this == FAVORITE) {
            return context.getResources().getString(R.string.action_topics_filter_favorites);
        }
        else if (this == PARTICIPATED) {
            return context.getResources().getString(R.string.action_topics_filter_participated);
        }
        else {
            return context.getResources().getString(R.string.action_topics_filter_read);
        }
    }
}
