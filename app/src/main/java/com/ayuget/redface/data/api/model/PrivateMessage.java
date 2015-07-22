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

package com.ayuget.redface.data.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PrivateMessage implements Parcelable {
    private final long id;

    private final List<String> recipients;

    private final String subject;

    private final int totalMessages;

    private final boolean hasUnreadMessages;

    private final Date lastResponseDate;

    private final String lastResponseAuthor;

    private PrivateMessage(long id, List<String> recipients, String subject, int totalMessages, boolean hasUnreadMessages, Date lastResponseDate, String lastResponseAuthor) {
        this.id = id;
        this.recipients = recipients;
        this.totalMessages = totalMessages;
        this.subject = subject;
        this.hasUnreadMessages = hasUnreadMessages;
        this.lastResponseDate = lastResponseDate;
        this.lastResponseAuthor = lastResponseAuthor;
    }

    public long getId() {
        return id;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    public Date getLastResponseDate() {
        return lastResponseDate;
    }

    public String getLastResponseAuthor() {
        return lastResponseAuthor;
    }

    public String getSubject() {
        return subject;
    }

    public static class Builder {
        private long id;

        private final List<String> recipients;

        private String subject;

        private int totalMessages;

        private boolean hasUnreadMessages;

        private Date lastResponseDate;

        private String lastResponseAuthor;

        public Builder() {
            this.recipients = new ArrayList<>();
            this.hasUnreadMessages = false;
        }

        public Builder addRecipient(String recipient) {
            this.recipients.add(recipient);
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withLastResponse(String author, Date responseDate) {
            this.lastResponseDate = responseDate;
            this.lastResponseAuthor = author;
            return this;
        }

        public Builder withUnreadMessages(boolean hasUnreadMessages) {
            this.hasUnreadMessages = hasUnreadMessages;
            return this;
        }

        public Builder withTotalMessages(int totalMessages) {
            this.totalMessages = totalMessages;
            return this;
        }

        public PrivateMessage build() {
            Preconditions.checkNotNull(id);
            Preconditions.checkNotNull(totalMessages);
            Preconditions.checkNotNull(lastResponseAuthor);
            Preconditions.checkNotNull(lastResponseDate);
            Preconditions.checkNotNull(subject);
            return new PrivateMessage(id, recipients, subject, totalMessages, hasUnreadMessages, lastResponseDate, lastResponseAuthor);
        }
    }

    /**
     * /!\ Boilerplate code below (parcelable and equals) /!\
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeStringList(this.recipients);
        dest.writeString(this.subject);
        dest.writeInt(this.totalMessages);
        dest.writeByte(hasUnreadMessages ? (byte) 1 : (byte) 0);
        dest.writeLong(lastResponseDate != null ? lastResponseDate.getTime() : -1);
        dest.writeString(this.lastResponseAuthor);
    }

    private PrivateMessage(Parcel in) {
        this.id = in.readLong();
        this.recipients = new ArrayList<>();
        in.readStringList(this.recipients);
        this.subject = in.readString();
        this.totalMessages = in.readInt();
        this.hasUnreadMessages = in.readByte() != 0;
        long tmpLastResponseDate = in.readLong();
        this.lastResponseDate = tmpLastResponseDate == -1 ? null : new Date(tmpLastResponseDate);
        this.lastResponseAuthor = in.readString();
    }

    public static final Parcelable.Creator<PrivateMessage> CREATOR = new Parcelable.Creator<PrivateMessage>() {
        public PrivateMessage createFromParcel(Parcel source) {
            return new PrivateMessage(source);
        }

        public PrivateMessage[] newArray(int size) {
            return new PrivateMessage[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrivateMessage that = (PrivateMessage) o;

        if (id != that.id) return false;
        if (totalMessages != that.totalMessages) return false;
        if (hasUnreadMessages != that.hasUnreadMessages) return false;
        if (!recipients.equals(that.recipients)) return false;
        if (!subject.equals(that.subject)) return false;
        if (!lastResponseDate.equals(that.lastResponseDate)) return false;
        return lastResponseAuthor.equals(that.lastResponseAuthor);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + recipients.hashCode();
        result = 31 * result + subject.hashCode();
        result = 31 * result + totalMessages;
        result = 31 * result + (hasUnreadMessages ? 1 : 0);
        result = 31 * result + lastResponseDate.hashCode();
        result = 31 * result + lastResponseAuthor.hashCode();
        return result;
    }
}
