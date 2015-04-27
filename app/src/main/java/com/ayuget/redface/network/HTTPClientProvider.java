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

package com.ayuget.redface.network;

import android.content.Context;
import android.util.Log;

import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.settings.ProxySettingsChangedEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HTTPClientProvider {
    private static final String LOG_TAG = HTTPClientProvider.class.getSimpleName();

    private final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36";

    private final Context context;

    private RedfaceSettings settings;

    private OkHttpClient httpClient;

    private final Map<User, UserCookieStore> cookieStores;

    public HTTPClientProvider(Context context, RedfaceSettings settings, Bus bus) {
        this.context = context;
        this.settings = settings;
        this.cookieStores = new HashMap<>();
        bus.register(this);
        initHttpClient();
    }

    private void initHttpClient() {
        httpClient = new OkHttpClient();

        if (settings.isProxyEnabled()) {
            enableHTTPProxy();
        }

        httpClient.networkInterceptors().add(new UserAgentInterceptor(USER_AGENT));

        httpClient.setConnectTimeout(10, TimeUnit.SECONDS);
        httpClient.setReadTimeout(10, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(10, TimeUnit.SECONDS);
    }

    private void enableHTTPProxy() {
        if (settings.getProxyHost() != null && settings.getProxyPort() > 0) {
            Log.d(LOG_TAG, String.format("Enabling HTTP Proxy for all requests, host='%s', port='%d'", settings.getProxyHost(), settings.getProxyPort()));
            httpClient.setProxy(new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(settings.getProxyHost(), settings.getProxyPort())));
        }
    }

    private void disableHTTPProxy() {
        httpClient.setProxy(null);
    }

    /**
     * Returns HTTP client associated with a given user. Separate
     * instances are used because we want different CookieStores
     * for each user.
     */
    public synchronized OkHttpClient getClientForUser(User user) {
        UserCookieStore cookieStore = cookieStores.get(user);

        if (cookieStore == null) {
            cookieStore = new UserCookieStore(context, user);
            cookieStores.put(user, cookieStore);
        }

        httpClient.setCookieHandler(new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL));

        return httpClient;
    }

    @Subscribe public void proxySettingsChanged(ProxySettingsChangedEvent event) {
        if (settings.isProxyEnabled()) {
            enableHTTPProxy();
        }
        else {
            disableHTTPProxy();
        }
    }
}
