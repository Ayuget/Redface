package com.ayuget.redface.data.api;

import com.ayuget.redface.data.api.model.User;

import rx.Observable;

public interface MDAuthenticator {
    public Observable<Boolean> login(final User user);
}
