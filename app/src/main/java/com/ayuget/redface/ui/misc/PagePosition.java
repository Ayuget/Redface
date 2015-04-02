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
}
