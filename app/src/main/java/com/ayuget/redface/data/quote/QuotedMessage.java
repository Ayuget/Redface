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

class QuotedMessage implements Parcelable {
    private final String postBBCode;

    private final int page;

    private final long postId;

    private QuotedMessage(long postId, int page, String postBBCode) {
        this.postId = postId;
        this.page = page;
        this.postBBCode = postBBCode;
    }

    public static QuotedMessage of(long postId, int page, String postBBCode) {
        return new QuotedMessage(postId, page, postBBCode);
    }

    public long postId() {
        return postId;
    }

    public int page() {
        return page;
    }

    public String bbCode() {
        return postBBCode;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("QuotedMessage{");
        sb.append("postId=").append(postId);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuotedMessage that = (QuotedMessage) o;

        return postId == that.postId;

    }

    @Override
    public int hashCode() {
        return (int) (postId ^ (postId >>> 32));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.postBBCode);
        dest.writeInt(this.page);
        dest.writeLong(this.postId);
    }

    protected QuotedMessage(Parcel in) {
        this.postBBCode = in.readString();
        this.page = in.readInt();
        this.postId = in.readLong();
    }

    public static final Creator<QuotedMessage> CREATOR = new Creator<QuotedMessage>() {
        @Override
        public QuotedMessage createFromParcel(Parcel source) {
            return new QuotedMessage(source);
        }

        @Override
        public QuotedMessage[] newArray(int size) {
            return new QuotedMessage[size];
        }
    };
}
