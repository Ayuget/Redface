package com.ayuget.redface.data.api.model;

import android.content.Context;

public class User {
    private final String username;

    private final String password;

    private boolean hasAvatar = true;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayUsername(Context context) {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isGuest() {
        return false;
    }

    public boolean hasAvatar() {
        return hasAvatar;
    }

    public void setHasAvatar(boolean hasAvatar) {
        this.hasAvatar = hasAvatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (hasAvatar != user.hasAvatar) return false;
        if (!password.equals(user.password)) return false;
        if (!username.equals(user.username)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + (hasAvatar ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("User{");
        sb.append("username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
