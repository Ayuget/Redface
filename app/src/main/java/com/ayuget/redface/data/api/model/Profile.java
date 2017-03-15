package com.ayuget.redface.data.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class Profile implements Parcelable {
    public abstract String username();
    @Nullable public abstract String email();

    // Stored as String because some birthdates are invalid (sometimes stored as
    // "00/00/0000", it seems there is no validation from the forum).
    @Nullable public abstract String birthDate();

    @Nullable public abstract String avatarUrl();

    public static Builder builder() {
        return new AutoValue_Profile.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder username(String username);
        public abstract Builder email(@Nullable String email);
        public abstract Builder birthDate(@Nullable String birthDate);
        public abstract Builder avatarUrl(@Nullable String avatarUrl);
        public abstract Profile build();
    }

}
