package com.ayuget.redface.data.api.model;

import android.content.Context;

import com.ayuget.redface.R;

public class Guest extends User {
    public Guest() {
        super("Guest", "guest");
    }

    @Override
    public boolean isGuest() {
        return true;
    }

    @Override
    public String getDisplayUsername(Context context) {
        return context.getString(R.string.guest_username);
    }
}
