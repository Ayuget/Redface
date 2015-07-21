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

package com.ayuget.redface.ui.misc;

import android.os.Parcel;
import android.os.Parcelable;

public class PagePosition implements Parcelable {
    public static final int BOTTOM = -2;
    public static final int TOP = -1;

    private final long postId;

    public PagePosition(long postId) {
        this.postId = postId;
    }

    public long getPostId() {
        return postId;
    }

    public boolean isBottom() {
        return postId == BOTTOM;
    }

    public boolean isTop() {
        return postId == TOP;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.postId);
    }

    private PagePosition(Parcel in) {
        this.postId = in.readLong();
    }

    public static final Parcelable.Creator<PagePosition> CREATOR = new Parcelable.Creator<PagePosition>() {
        public PagePosition createFromParcel(Parcel source) {
            return new PagePosition(source);
        }

        public PagePosition[] newArray(int size) {
            return new PagePosition[size];
        }
    };

    @Override
    public String toString() {
        return "PagePosition{" + "postId=" + postId + '}';
    }

    public static PagePosition top() {
        return new PagePosition(PagePosition.TOP);
    }

    public static PagePosition bottom() {
        return new PagePosition(PagePosition.BOTTOM);
    }

    public static PagePosition at(long postId) {
        return new PagePosition(postId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PagePosition that = (PagePosition) o;

        return postId == that.postId;

    }

    @Override
    public int hashCode() {
        return (int) (postId ^ (postId >>> 32));
    }
}
