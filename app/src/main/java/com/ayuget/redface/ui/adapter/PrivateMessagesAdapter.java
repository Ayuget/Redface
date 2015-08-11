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
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.misc.UiUtils;
import com.squareup.phrase.Phrase;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class PrivateMessagesAdapter extends RecyclerView.Adapter<PrivateMessagesAdapter.ViewHolder>{
    private static final String LOG_TAG = PrivateMessagesAdapter.class.getSimpleName();

    private static final Phrase PM_TITLE_FORMAT = Phrase.from("<strong>@{recipient} :</strong> {subject}");

    private List<PrivateMessage> privateMessages = Collections.emptyList();

    private Context context;

    private final int primaryTextColor;

    private final int secondaryTextColor;

    private final int readTextColor;

    private final ThemeManager themeManager;

    private final boolean isCompactMode;

    private OnPMClickedListener onPMClickedListener;

    private OnPMLongClickListener onPMLongClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View parent;
        public ImageView pmIcon;
        public TextView pmTitle;
        public TextView pmLastPostInfos;
        public ImageView pmUnreadByRecipientIndicator;

        public ViewHolder(View itemView) {
            super(itemView);
            parent = itemView;
            pmTitle = (TextView) itemView.findViewById(R.id.pm_title);
            pmIcon = (ImageView) itemView.findViewById(R.id.pm_icon);
            pmLastPostInfos = (TextView) itemView.findViewById(R.id.pm_last_post_infos);
            pmUnreadByRecipientIndicator = (ImageView) itemView.findViewById(R.id.pm_unread_by_recipient_indicator);
        }

        public void setOnPMClickedListener(View.OnClickListener listener) {
            parent.setOnClickListener(listener);
        }

        public void setOnLongClickListener(View.OnLongClickListener listener) {
            parent.setOnLongClickListener(listener);
        }
    }

    /**
     * Interface definition for a callback to be invoked when a private message in this list has
     * been clicked.
     */
    public interface OnPMClickedListener {
        void onPrivateMessageClicked(PrivateMessage privateMessage);
    }

    /**
     * Interface definition for a callback to be invoked when a long-press on a private message
     * has been made
     */
    public interface OnPMLongClickListener {
        void onPrivateMessageLongClick(int position);
    }

    public PrivateMessagesAdapter(Context context, ThemeManager themeManager, boolean isCompactMode) {
        this.context = context;
        this.primaryTextColor = UiUtils.getPrimaryTextColor(context);
        this.secondaryTextColor = UiUtils.getSecondaryTextColor(context);
        this.readTextColor = UiUtils.getReadTextColor(context);
        this.themeManager = themeManager;
        this.isCompactMode = isCompactMode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View parent = LayoutInflater.from(context).inflate(R.layout.list_item_private_message, viewGroup, false);
        return new ViewHolder(parent);
    }

    private CharSequence formatLastResponseDate(PrivateMessage privateMessage) {
        return DateUtils.getRelativeTimeSpanString(privateMessage.getLastResponseDate().getTime(), new Date().getTime(), 0, 0);
    }

    private int getPrivateMessageIcon(PrivateMessage privateMessage) {
        if (privateMessage.hasUnreadMessages()) {
            return R.drawable.ic_action_mail;
        }
        else {
            return R.drawable.ic_action_tick;
        }
    }

    private int getPMIconBackgroundColor(PrivateMessage privateMessage) {
        if (privateMessage.hasUnreadMessages()) {
            return context.getResources().getColor(R.color.topic_icon_flagged_bg);
        }
        else {
            return UiUtils.getFullyReadTopicIconBackgroundColor(context);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final PrivateMessage privateMessage = privateMessages.get(position);

        String title = PM_TITLE_FORMAT.put("recipient", privateMessage.getRecipient()).put("subject", privateMessage.getSubject()).format().toString();
        viewHolder.pmTitle.setText(Html.fromHtml(title));

        viewHolder.pmLastPostInfos.setText(privateMessage.getLastResponseAuthor() + " - " + formatLastResponseDate(privateMessage));
        viewHolder.pmIcon.setImageResource(getPrivateMessageIcon(privateMessage));
        GradientDrawable pmIconCircle = (GradientDrawable) viewHolder.pmIcon.getBackground();
        pmIconCircle.setColor(getPMIconBackgroundColor(privateMessage));

        if (privateMessage.hasUnreadMessages()) {
            viewHolder.pmTitle.setTextColor(primaryTextColor);
            viewHolder.pmLastPostInfos.setTextColor(secondaryTextColor);
        }
        else {
            viewHolder.pmTitle.setTextColor(secondaryTextColor);
            viewHolder.pmLastPostInfos.setTextColor(secondaryTextColor);
        }

        if (privateMessage.hasBeenReadByRecipient()) {
            Log.d(LOG_TAG, String.format("Private message [%s] has been read by recipient", privateMessage.getSubject()));
            viewHolder.pmUnreadByRecipientIndicator.setVisibility(View.INVISIBLE);
        }
        else {
            Log.d(LOG_TAG, String.format("Private message [%s] has not been read by recipient", privateMessage.getSubject()));
            viewHolder.pmUnreadByRecipientIndicator.setVisibility(View.VISIBLE);
        }

        viewHolder.pmUnreadByRecipientIndicator.setBackground(context.getResources().getDrawable(themeManager.getPrivateMessageUnreadDrawable()));
        viewHolder.pmUnreadByRecipientIndicator.setImageResource(R.drawable.ic_action_markunread_mailbox);

        viewHolder.setOnPMClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPMClickedListener.onPrivateMessageClicked(privateMessage);
            }
        });

        viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onPMLongClickListener.onPrivateMessageLongClick(position);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return privateMessages.size();
    }

    public void replaceWith(List<PrivateMessage> privateMessages) {
        Log.d(LOG_TAG, String.format("Displaying %d private messages", privateMessages.size()));
        this.privateMessages = privateMessages;
        notifyDataSetChanged();
    }

    public void extendWith(List<PrivateMessage> privateMessages) {
        Log.d(LOG_TAG, String.format("Added %d private messages to pm list", privateMessages.size()));
        this.privateMessages.addAll(privateMessages);
        notifyDataSetChanged();
    }

    public void setOnPMClickedListener(OnPMClickedListener onPMClickedListener) {
        this.onPMClickedListener = onPMClickedListener;
    }

    public void setOnPMLongClickListener(OnPMLongClickListener onPMLongClickListener) {
        this.onPMLongClickListener = onPMLongClickListener;
    }
}
