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
import java.util.Date;


public class PrivateMessage implements Parcelable {
    private final long id;

    private final String recipient;

    private final String subject;

    private final int totalMessages;

    private final boolean hasUnreadMessages;

    private final Date lastResponseDate;

    private final String lastResponseAuthor;

    private final boolean hasBeenReadByRecipient;

    private final int pagesCount;

    private PrivateMessage(long id, String recipient, String subject, int totalMessages, boolean hasUnreadMessages, Date lastResponseDate, String lastResponseAuthor, boolean hasBeenReadByRecipient, int pagesCount) {
        this.id = id;
        this.recipient = recipient;
        this.totalMessages = totalMessages;
        this.subject = subject;
        this.hasUnreadMessages = hasUnreadMessages;
        this.lastResponseDate = lastResponseDate;
        this.lastResponseAuthor = lastResponseAuthor;
        this.hasBeenReadByRecipient = hasBeenReadByRecipient;
        this.pagesCount = pagesCount;
    }

    public long getId() {
        return id;
    }

    public String getRecipient() {
        return recipient;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    public boolean hasBeenReadByRecipient() {
        return hasBeenReadByRecipient;
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

    public int getPagesCount() {
        return pagesCount;
    }

    public static class Builder {
        private long id;

        private String recipient;

        private String subject;

        private int totalMessages;

        private boolean hasUnreadMessages;

        private Date lastResponseDate;

        private String lastResponseAuthor;

        private boolean hasBeenReadByRecipient;

        private int pagesCount;

        public Builder() {
            this.hasUnreadMessages = false;
            this.hasBeenReadByRecipient = true;
        }

        public Builder forRecipient(String recipient) {
            this.recipient = recipient;
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

        public Builder withPagesCount(int pagesCount) {
            this.pagesCount = pagesCount;
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

        public Builder asReadByRecipient(boolean hasBeenReadByRecipient) {
            this.hasBeenReadByRecipient = hasBeenReadByRecipient;
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
            Preconditions.checkNotNull(recipient);
            return new PrivateMessage(id, recipient, subject, totalMessages, hasUnreadMessages, lastResponseDate, lastResponseAuthor, hasBeenReadByRecipient, pagesCount);
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
        dest.writeString(this.recipient);
        dest.writeString(this.subject);
        dest.writeInt(this.totalMessages);
        dest.writeByte(hasUnreadMessages ? (byte) 1 : (byte) 0);
        dest.writeLong(lastResponseDate != null ? lastResponseDate.getTime() : -1);
        dest.writeString(this.lastResponseAuthor);
        dest.writeByte(hasBeenReadByRecipient ? (byte) 1 : (byte) 0);
        dest.writeInt(this.pagesCount);
    }

    private PrivateMessage(Parcel in) {
        this.id = in.readLong();
        this.recipient = in.readString();
        this.subject = in.readString();
        this.totalMessages = in.readInt();
        this.hasUnreadMessages = in.readByte() != 0;
        long tmpLastResponseDate = in.readLong();
        this.lastResponseDate = tmpLastResponseDate == -1 ? null : new Date(tmpLastResponseDate);
        this.lastResponseAuthor = in.readString();
        this.hasBeenReadByRecipient = in.readByte() != 0;
        this.pagesCount = in.readInt();
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
        if (hasBeenReadByRecipient != that.hasBeenReadByRecipient) return false;
        if (!recipient.equals(that.recipient)) return false;
        if (!subject.equals(that.subject)) return false;
        if (!lastResponseDate.equals(that.lastResponseDate)) return false;
        if (pagesCount != that.pagesCount) return false;
        return lastResponseAuthor.equals(that.lastResponseAuthor);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + recipient.hashCode();
        result = 31 * result + subject.hashCode();
        result = 31 * result + totalMessages;
        result = 31 * result + (hasUnreadMessages ? 1 : 0);
        result = 31 * result + lastResponseDate.hashCode();
        result = 31 * result + lastResponseAuthor.hashCode();
        result = 31 * result + (hasBeenReadByRecipient ? 1 : 0);
        result = 31 * result + pagesCount;
        return result;
    }

    public Topic asTopic() {
        Topic topic = new Topic((int) getId());
        topic.setSubject(subject);
        topic.setPagesCount(pagesCount);

        return topic;
    }
}
