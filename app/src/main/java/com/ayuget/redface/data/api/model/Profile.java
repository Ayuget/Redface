package com.ayuget.redface.data.api.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class Profile implements Parcelable {
    public abstract String username();

    // General information
    @Nullable
    public abstract String avatarUrl();

    @Nullable
    public abstract String emailAddress();

    @Nullable
    public abstract String birthday();

    @Nullable
    public abstract String sexGenre();

    @Nullable
    public abstract String city();

    @Nullable
    public abstract String employment();

    @Nullable
    public abstract String hobbies();

    // Forum related details
    public abstract String status();
    public abstract String arrivalDate();
    public abstract long messageCount();

    @Nullable
    public abstract String lastMessageDate();

    @Nullable
    public abstract String personalQuote();

    @Nullable
    public abstract String messageSignature();

    public abstract List<Smiley> personalSmilies();

    public static Profile.Builder builder() {
        return new AutoValue_Profile.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder username(String username);
        public abstract Builder avatarUrl(String avatarUrl);
        public abstract Builder emailAddress(String emailAddress);
        public abstract Builder birthday(String birthday);
        public abstract Builder sexGenre(String sexGenre);
        public abstract Builder city(String city);
        public abstract Builder employment(String employment);
        public abstract Builder hobbies(String hobbies);
        public abstract Builder status(String status);
        public abstract Builder arrivalDate(String emailAddress);
        public abstract Builder messageCount(long messageCount);
        public abstract Builder lastMessageDate(String lastMessageDate);
        public abstract Builder personalQuote(String personalQuote);
        public abstract Builder messageSignature(String messageSignature);
        public abstract Builder personalSmilies(List<Smiley> personalSmilies);
        public abstract Profile build();
    }
}
