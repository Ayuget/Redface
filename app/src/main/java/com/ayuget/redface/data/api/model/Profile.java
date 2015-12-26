package com.ayuget.redface.data.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable {

    private final String avatarUrl;

    public Profile(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile profile = (Profile) o;

        return !(avatarUrl != null ? !avatarUrl.equals(profile.avatarUrl) : profile.avatarUrl != null);

    }

    @Override
    public int hashCode() {
        return avatarUrl != null ? avatarUrl.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "avatarUrl='" + avatarUrl + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avatarUrl);
    }

    private Profile(Parcel in) {
        avatarUrl = in.readString();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
