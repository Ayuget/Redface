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

public class TopicPosition implements Parcelable {
    private final int page;

    private final PagePosition pagePosition;

    public TopicPosition(int page, PagePosition pagePosition) {
        this.page = page;
        this.pagePosition = pagePosition;
    }

    public int getPage() {
        return page;
    }

    public PagePosition getPagePosition() {
        return pagePosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicPosition that = (TopicPosition) o;

        if (page != that.page) return false;
        if (!pagePosition.equals(that.pagePosition)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = page;
        result = 31 * result + pagePosition.hashCode();
        return result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.page);
        dest.writeParcelable(this.pagePosition, 0);
    }

    private TopicPosition(Parcel in) {
        this.page = in.readInt();
        this.pagePosition = in.readParcelable(PagePosition.class.getClassLoader());
    }

    public static final Parcelable.Creator<TopicPosition> CREATOR = new Parcelable.Creator<TopicPosition>() {
        public TopicPosition createFromParcel(Parcel source) {
            return new TopicPosition(source);
        }

        public TopicPosition[] newArray(int size) {
            return new TopicPosition[size];
        }
    };
}
