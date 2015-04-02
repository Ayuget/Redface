package com.ayuget.redface.util;

import com.ayuget.redface.data.api.model.User;
import com.google.common.base.Optional;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;

public class UserUtils {
    /**
     * Ugly method to fetch a user id from stored cookies
     */
    public static Optional<Integer> getLoggedInUserId(User user, OkHttpClient httpClient) {
        CookieManager cookieManager = (CookieManager) httpClient.getCookieHandler();
        CookieStore cookieStore = cookieManager.getCookieStore();
        for (HttpCookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("md_id")) {
                return Optional.of(Integer.valueOf(cookie.getValue()));
            }
        }

        return Optional.absent();
    }
}
