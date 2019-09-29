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

package com.ayuget.redface.ui.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicStatus;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.misc.UiUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {
    private List<Topic> topics = Collections.emptyList();

    private Context context;

    private OnTopicClickedListener onTopicClickedListener;

    private final int primaryTextColor;

    private final int secondaryTextColor;

    private final int readTextColor;

    private final ThemeManager themeManager;

    private final boolean isCompactMode;

    private final boolean isEnhancedCompactMode;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View parent;
        public ImageView topicIcon;
        public TextView topicSubject;
        public TextView topicLastPostInfos;
        public TextView topicPagesCount;
        public TextView unreadPagesCount;

        public ViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            topicSubject = itemView.findViewById(R.id.topic_subject);
            topicIcon = itemView.findViewById(R.id.topic_icon);
            topicLastPostInfos = itemView.findViewById(R.id.topic_last_post_infos);
            topicPagesCount = itemView.findViewById(R.id.topic_pages_count);
            unreadPagesCount = itemView.findViewById(R.id.topic_unread_count);
            itemView.setLongClickable(true);
        }

        public void setOnTopicClickedListener(View.OnClickListener listener) {
            parent.setOnClickListener(listener);
        }
    }

    /**
     * Interface definition for a callback to be invoked when a topic in this list has
     * been clicked.
     */
    public interface OnTopicClickedListener {
        void onTopicClicked(Topic topic);
    }

    public TopicsAdapter(Context context, ThemeManager themeManager, boolean isCompactMode, boolean isEnhancedCompactMode) {
        this.context = context;
        this.primaryTextColor = UiUtils.getPrimaryTextColor(context);
        this.secondaryTextColor = UiUtils.getSecondaryTextColor(context);
        this.readTextColor = UiUtils.getReadTextColor(context);
        this.themeManager = themeManager;
        this.isCompactMode = isCompactMode;
        this.isEnhancedCompactMode = isEnhancedCompactMode;
        Timber.d("Is enhanced compact mode ? = %b", isEnhancedCompactMode);
    }

    public void setOnTopicClickedListener(OnTopicClickedListener onTopicClickedListener) {
        this.onTopicClickedListener = onTopicClickedListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();

        int itemLayout;
        if (isCompactMode && isEnhancedCompactMode) {
            itemLayout = R.layout.list_item_topic_compact;
        } else if (isCompactMode) {
            itemLayout = R.layout.list_item_topic_compact_light;
        } else {
            itemLayout = R.layout.list_item_topic;
        }

        View parent = LayoutInflater.from(context).inflate(itemLayout, viewGroup, false);
        return new ViewHolder(parent);
    }

    private String getPrintableUnreadPagesCount(int realCount) {
        if (realCount >= 100) {
            return "99+";
        } else {
            return String.valueOf(realCount);
        }
    }

    private boolean showAdditionalDetails() {
        return !isCompactMode || (isCompactMode && isEnhancedCompactMode);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        final Topic topic = topics.get(i);

        viewHolder.topicSubject.setText(topic.title());

        if (showAdditionalDetails()) {
            viewHolder.topicLastPostInfos.setText(topic.lastPostAuthor() + " - " + formatLastPostDate(topic));
            viewHolder.topicPagesCount.setText(String.valueOf(topic.pagesCount()) + " p");
        }

        GradientDrawable topicIconCircle = (GradientDrawable) viewHolder.topicIcon.getBackground();
        topicIconCircle.setColor(getTopicIconBackgroundColor(topic));

        viewHolder.topicIcon.setImageResource(getTopicIcon(topic));

        Drawable topicIconDrawable = viewHolder.topicIcon.getDrawable();
        topicIconDrawable.mutate().setColorFilter(getTopicIconTextColor(topic), PorterDuff.Mode.MULTIPLY);

        viewHolder.setOnTopicClickedListener(v -> {
            if (onTopicClickedListener != null) {
                onTopicClickedListener.onTopicClicked(topic);
            }
        });


        if (topic.hasUnreadPosts()) {
            viewHolder.unreadPagesCount.setVisibility(View.VISIBLE);
            viewHolder.unreadPagesCount.setText(getPrintableUnreadPagesCount(topic.unreadPagesCount()));
            viewHolder.topicSubject.setTextColor(primaryTextColor);

            if (showAdditionalDetails()) {
                viewHolder.topicLastPostInfos.setTextColor(secondaryTextColor);
                viewHolder.topicPagesCount.setTextColor(secondaryTextColor);
            }
        } else {
            viewHolder.unreadPagesCount.setVisibility(View.INVISIBLE);
            viewHolder.topicSubject.setTextColor(readTextColor);

            if (showAdditionalDetails()) {
                viewHolder.topicLastPostInfos.setTextColor(readTextColor);
                viewHolder.topicPagesCount.setTextColor(readTextColor);
            }
        }

        viewHolder.unreadPagesCount.setBackground(context.getResources().getDrawable(themeManager.getTopicUnreadCountDrawable()));
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public Topic getItem(int position) {
        return this.topics.get(position);
    }

    public void replaceWith(List<Topic> topics) {
        Timber.d("Displaying %d topics", topics.size());
        this.topics = topics;
        notifyDataSetChanged();
    }

    public void extendWith(List<Topic> topics) {
        Timber.d("Added %d topics to topic list", topics.size());
        this.topics.addAll(topics);
        notifyDataSetChanged();
    }

    int getTopicIcon(Topic topic) {
        if (topic.isLocked()) {
            return R.drawable.ic_lock_white_24dp;
        } else if (topic.isSticky()) {
            return R.drawable.ic_action_pin;
        } else if (topic.status() == TopicStatus.FAVORITE_NEW_CONTENT) {
            return R.drawable.ic_action_star_10;
        } else if (topic.hasUnreadPosts()) {
            return R.drawable.ic_flag_white_24dp;
        } else {
            return R.drawable.ic_action_tick;
        }
    }

    int getTopicIconBackgroundColor(Topic topic) {
        if (topic.status() == TopicStatus.FAVORITE_NEW_CONTENT) {
            return context.getResources().getColor(R.color.topic_icon_favorite_bg);
        } else if (topic.status() == TopicStatus.FLAGGED_NEW_CONTENT) {
            return context.getResources().getColor(R.color.topic_icon_flagged_bg);
        } else if (topic.status() == TopicStatus.READ_NEW_CONTENT) {
            return UiUtils.getReadTopicIconBackgroundColor(context);
        } else if (topic.isSticky()) {
            return context.getResources().getColor(R.color.topic_icon_sticky_bg);
        } else if (topic.hasUnreadPosts()) {
            return UiUtils.getDefaultTopicIconBackgroundColor(context);
        } else {
            return UiUtils.getFullyReadTopicIconBackgroundColor(context);
        }
    }

    int getTopicIconTextColor(Topic topic) {
        if (topic.isSticky()) {
            return context.getResources().getColor(R.color.topic_icon_sticky_color);
        } else if (topic.status() == TopicStatus.FAVORITE_NEW_CONTENT) {
            return context.getResources().getColor(R.color.topic_icon_favorite_color);
        } else if (topic.status() == TopicStatus.FLAGGED_NEW_CONTENT) {
            return context.getResources().getColor(R.color.topic_icon_flagged_color);
        } else if (topic.status() == TopicStatus.READ_NEW_CONTENT) {
            return UiUtils.getReadTopicIconTextColor(context);
        } else {
            return UiUtils.getDefaultTopicIconTextColor(context);
        }
    }

    private CharSequence formatLastPostDate(Topic topic) {
        return DateUtils.getRelativeTimeSpanString(topic.lastPostDate().getTime(), new Date().getTime(), 0, 0);
    }
}
