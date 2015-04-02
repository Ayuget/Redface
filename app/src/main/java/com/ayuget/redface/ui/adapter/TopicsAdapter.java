package com.ayuget.redface.ui.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicStatus;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.misc.UiUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {
    private static final String LOG_TAG = TopicsAdapter.class.getSimpleName();

    private List<Topic> topics = Collections.emptyList();

    private Context context;

    private OnTopicClickedListener onTopicClickedListener;

    private OnTopicLongClickListener onTopicLongClickListener;

    private final int primaryTextColor;

    private final int secondaryTextColor;

    private final ThemeManager themeManager;

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
            topicSubject = (TextView) itemView.findViewById(R.id.topic_subject);
            topicIcon = (ImageView) itemView.findViewById(R.id.topic_icon);
            topicLastPostInfos = (TextView) itemView.findViewById(R.id.topic_last_post_infos);
            topicPagesCount = (TextView) itemView.findViewById(R.id.topic_pages_count);
            unreadPagesCount = (TextView) itemView.findViewById(R.id.topic_unread_count);
        }

        public void setOnTopicClickedListener(View.OnClickListener listener) {
            parent.setOnClickListener(listener);
        }

        public void setOnLongClickListener(View.OnLongClickListener listener) {
            parent.setOnLongClickListener(listener);
        }
    }

    /**
     * Interface definition for a callback to be invoked when a topic in this list has
     * been clicked.
     */
    public interface OnTopicClickedListener {
        void onTopicClicked(Topic topic);
    }

    /**
     * Interface definition for a callback to be invoked when a long-press on a topic has been made
     */
    public interface OnTopicLongClickListener {
        void onTopicLongClick(int position);
    }

    public TopicsAdapter(Context context, ThemeManager themeManager) {
        this.context = context;
        this.primaryTextColor = UiUtils.getPrimaryTextColor(context);
        this.secondaryTextColor = UiUtils.getSecondaryTextColor(context);
        this.themeManager = themeManager;
    }

    public void setOnTopicClickedListener(OnTopicClickedListener onTopicClickedListener) {
        this.onTopicClickedListener = onTopicClickedListener;
    }

    public void setOnTopicLongClickListener(OnTopicLongClickListener onTopicLongClickListener) {
        this.onTopicLongClickListener = onTopicLongClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View parent = LayoutInflater.from(context).inflate(R.layout.list_item_topic, viewGroup, false);
        return new ViewHolder(parent);
    }

    String getPrintableUnreadPagesCount(int realCount) {
        if (realCount >= 100) {
            return "99+";
        }
        else {
            return String.valueOf(realCount);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        final Topic topic = topics.get(i);

        viewHolder.topicSubject.setText(topic.getSubject());
        viewHolder.topicLastPostInfos.setText(topic.getLastPostAuthor() + " - " + formatLastPostDate(topic));
        viewHolder.topicPagesCount.setText(String.valueOf(topic.getPagesCount()) + " p");

        GradientDrawable topicIconCircle = (GradientDrawable) viewHolder.topicIcon.getBackground();
        topicIconCircle.setColor(getTopicIconBackgroundColor(topic));

        viewHolder.topicIcon.setImageResource(getTopicIcon(topic));

        Drawable topicIconDrawable = viewHolder.topicIcon.getDrawable();
        topicIconDrawable.mutate().setColorFilter(getTopicIconTextColor(topic), PorterDuff.Mode.MULTIPLY);

        viewHolder.setOnTopicClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTopicClickedListener.onTopicClicked(topic);
            }
        });

        viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onTopicLongClickListener.onTopicLongClick(i);
                return false;
            }
        });

        if (topic.hasUnreadPosts()) {
            viewHolder.unreadPagesCount.setVisibility(View.VISIBLE);
            viewHolder.unreadPagesCount.setText(getPrintableUnreadPagesCount(topic.getUnreadPagesCount()));
            viewHolder.topicSubject.setTextColor(primaryTextColor);
        }
        else {
            viewHolder.unreadPagesCount.setVisibility(View.INVISIBLE);
            viewHolder.topicSubject.setTextColor(secondaryTextColor);
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
        Log.d(LOG_TAG, String.format("Displaying %d topics", topics.size()));
        this.topics = topics;
        notifyDataSetChanged();
    }

    public void extendWith(List<Topic> topics) {
        Log.d(LOG_TAG, String.format("Added %d topics to topic list", topics.size()));
        this.topics.addAll(topics);
        notifyDataSetChanged();
    }

    int getTopicIcon(Topic topic) {
        int topicIcon = R.drawable.ic_flag_white_24dp;

        if (topic.isSticky()) {
            topicIcon = R.drawable.ic_action_pin;
        }

        return topicIcon;
    }

    int getTopicIconBackgroundColor(Topic topic) {
        if (topic.isSticky()) {
            return context.getResources().getColor(R.color.topic_icon_sticky_bg);
        }
        else if (topic.getStatus() == TopicStatus.FAVORITE_NEW_CONTENT) {
            return context.getResources().getColor(R.color.topic_icon_favorite_bg);
        }
        else if (topic.getStatus() == TopicStatus.FLAGGED_NEW_CONTENT) {
            return context.getResources().getColor(R.color.topic_icon_flagged_bg);
        }
        else if (topic.getStatus() == TopicStatus.READ_NEW_CONTENT) {
            return UiUtils.getReadTopicIconBackgroundColor(context);
        }
        else {
            return UiUtils.getDefaultTopicIconBackgroundColor(context);
        }
    }

    int getTopicIconTextColor(Topic topic) {
        if (topic.isSticky()) {
            return context.getResources().getColor(R.color.topic_icon_sticky_color);
        }
        else if (topic.getStatus() == TopicStatus.FAVORITE_NEW_CONTENT) {
            return context.getResources().getColor(R.color.topic_icon_favorite_color);
        }
        else if (topic.getStatus() == TopicStatus.FLAGGED_NEW_CONTENT) {
            return context.getResources().getColor(R.color.topic_icon_flagged_color);
        }
        else if (topic.getStatus() == TopicStatus.READ_NEW_CONTENT) {
            return UiUtils.getReadTopicIconTextColor(context);
        }
        else {
            return UiUtils.getDefaultTopicIconTextColor(context);
        }
    }

    private CharSequence formatLastPostDate(Topic topic) {
        return DateUtils.getRelativeTimeSpanString(topic.getLastPostDate().getTime(), new Date().getTime(), 0, 0);
    }
}
