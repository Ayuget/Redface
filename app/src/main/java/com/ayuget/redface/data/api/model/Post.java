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

import com.ayuget.redface.ui.UIConstants;

import java.util.Date;

public class Post implements Parcelable {
    private final long id;

    private String author;

    private String avatarUrl;

    private Date postDate;

    private Date lastEditionDate;

    private boolean isFromModerators;

    private boolean isDeleted;

    private int quoteCount;

    private int topicPagesCount = UIConstants.UNKNOWN_PAGES_COUNT;

    /**
     * Content of the post, straight in HTML.
     */
    private String htmlContent;

    public Post(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public Date getLastEditionDate() {
        return lastEditionDate;
    }

    public void setLastEditionDate(Date lastEditionDate) {
        this.lastEditionDate = lastEditionDate;
    }

    public boolean isFromModerators() {
        return isFromModerators;
    }

    public void setFromModerators(boolean isFromModerators) {
        this.isFromModerators = isFromModerators;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public int getQuoteCount() {
        return quoteCount;
    }

    public void setQuoteCount(int quoteCount) {
        this.quoteCount = quoteCount;
    }

    public int getTopicPagesCount() {
        return topicPagesCount;
    }

    public void setTopicPagesCount(int topicPagesCount) {
        this.topicPagesCount = topicPagesCount;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Post{");
        sb.append("id=").append(id);
        sb.append(", author='").append(author).append('\'');
        sb.append(", postDate=").append(postDate);
        sb.append('}');
        return sb.toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.author);
        dest.writeString(this.avatarUrl);
        dest.writeLong(postDate != null ? postDate.getTime() : -1);
        dest.writeLong(lastEditionDate != null ? lastEditionDate.getTime() : -1);
        dest.writeByte(isFromModerators ? (byte) 1 : (byte) 0);
        dest.writeByte(isDeleted ? (byte) 1 : (byte) 0);
        dest.writeInt(this.quoteCount);
        dest.writeString(this.htmlContent);
        dest.writeInt(this.topicPagesCount);
    }

    private Post(Parcel in) {
        this.id = in.readLong();
        this.author = in.readString();
        this.avatarUrl = in.readString();
        long tmpPostDate = in.readLong();
        this.postDate = tmpPostDate == -1 ? null : new Date(tmpPostDate);
        long tmpLastEditionDate = in.readLong();
        this.lastEditionDate = tmpLastEditionDate == -1 ? null : new Date(tmpLastEditionDate);
        this.isFromModerators = in.readByte() != 0;
        this.isDeleted = in.readByte() != 0;
        this.quoteCount = in.readInt();
        this.htmlContent = in.readString();
        this.topicPagesCount = in.readInt();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
}
