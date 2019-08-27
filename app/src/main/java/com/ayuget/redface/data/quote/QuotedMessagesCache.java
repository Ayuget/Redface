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

package com.ayuget.redface.data.quote;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * A simple in-memory cache for currently quoted messages (multi-quote feature).
 *
 * The lifecycle of this object is tied to the {@link com.ayuget.redface.ui.fragment.TopicFragment}
 * which kinda makes sense...
 */
public class QuotedMessagesCache implements Parcelable {
    private List<QuotedMessage> quotedMessages = new ArrayList<>();

    /**
     * Adds a message to the list of quoted messages for the given topic.
     *
     * If that message was already quoted, the message is moved at the end of the list.
     */
    public void add(long postId, int page, String postBBCode) {
        Timber.d("Adding quote for page='%d' and post='%d'", page, postId);

        QuotedMessage quotedMessage = QuotedMessage.of(postId, page, postBBCode);

        if (quotedMessages.contains(quotedMessage)) {
            quotedMessages.remove(quotedMessage); // Remove if existing
        }

        quotedMessages.add(quotedMessage);

        Timber.d("Cache size is = '%d'", quotedMessages.size());
    }

    /**
     * Removes a message from the quoted messages list.
     */
    public void remove(long postId) {
        Timber.d("Removing post '%d' from quoted messages", postId);
        quotedMessages.remove(QuotedMessage.of(postId, 0, null));
        Timber.d("Cache size is = '%d'", quotedMessages.size());
    }

    /**
     * Returns the identifiers of the quoted posts for a given page
     */
    public List<Long> getPageQuotedMessages(int page) {
        List<Long> pageQuotedMessages = new ArrayList<>();

        for (QuotedMessage quotedMessage : quotedMessages) {
            if (quotedMessage.page() == page) {
                pageQuotedMessages.add(quotedMessage.postId());
            }
        }

        return pageQuotedMessages;
    }

    /**
     * Joins all quoted messages BBCodes
     */
    public String join() {
        Timber.d("Joining all '%d' messages for quoting", quotedMessages.size());

        List<String> bbCodes = new ArrayList<>();

        for (QuotedMessage quotedMessage : quotedMessages) {
            bbCodes.add(quotedMessage.bbCode());
        }

        return TextUtils.join("\n", bbCodes);
    }

    /**
     * Clears cache content
     */
    public void clear() {
        Timber.d("About to clear '%d' messages", quotedMessages.size());
        quotedMessages.clear();
    }

    /**
     * Returns the number of quoted messages in the cache
     */
    public int size() {
        return quotedMessages.size();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(quotedMessages);
    }

    public QuotedMessagesCache() {
    }

    protected QuotedMessagesCache(Parcel in) {
        this.quotedMessages = in.createTypedArrayList(QuotedMessage.CREATOR);
    }

    public static final Parcelable.Creator<QuotedMessagesCache> CREATOR = new Parcelable.Creator<QuotedMessagesCache>() {
        @Override
        public QuotedMessagesCache createFromParcel(Parcel source) {
            return new QuotedMessagesCache(source);
        }

        @Override
        public QuotedMessagesCache[] newArray(int size) {
            return new QuotedMessagesCache[size];
        }
    };
}
