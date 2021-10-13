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

import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.settings.ProxySettingsChangedEvent;
import com.ayuget.redface.settings.RedfaceSettings;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.CookieJar;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class HTTPClientProvider {
	private final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36";

	private final Context context;
	private RedfaceSettings settings;
	private final Map<User, UserCookieStore> cookieStores;
	private final Map<User, OkHttpClient> httpClients;

	public HTTPClientProvider(Context context, RedfaceSettings settings, Bus bus) {
		this.context = context;
		this.settings = settings;
		this.cookieStores = new HashMap<>();
		this.httpClients = new HashMap<>();
		bus.register(this);
	}

	private OkHttpClient buildHttpClient(User user) {
		return SecureHttpClientFactory.newBuilder()
				.addInterceptor(new UserAgentInterceptor(USER_AGENT))
				.connectTimeout(10, TimeUnit.SECONDS)
				.readTimeout(10, TimeUnit.SECONDS)
				.writeTimeout(10, TimeUnit.SECONDS)
				.cookieJar(provideUserCookieJar(user)) // Persistent cookies
				.proxy(provideProxy()) // User-controlled proxy settings
				.build();
	}

	/**
	 * Provides current proxy settings.
	 * <p>
	 * Proxy details can be changed in the app settings.
	 */
	private Proxy provideProxy() {
		boolean proxySettingsAreCorrect = settings.getProxyHost() != null && settings.getProxyPort() > 0;

		if (settings.isProxyEnabled() && proxySettingsAreCorrect) {
			Timber.e("Enabling HTTP Proxy for all requests, host='%s', port='%d'", settings.getProxyHost(), settings.getProxyPort());
			return new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(settings.getProxyHost(), settings.getProxyPort()));
		} else {
			return Proxy.NO_PROXY;
		}
	}

	/**
	 * Provides user personal {@link CookieJar}.
	 * <p>
	 * This is implemented a persistent CookieStore (otherwhise it would of coursebe quite annoying
	 * for the user to enter its credentials every time the app restarts...)
	 */
	private CookieJar provideUserCookieJar(User user) {
		return new JavaNetCookieJar(new CookieManager(getUserCookieStore(user), CookiePolicy.ACCEPT_ALL));
	}

	/**
	 * Returns HTTP client associated with a given user. Separate instances are used because we
	 * need different CookieStores for each user.
	 */
	public synchronized OkHttpClient getClientForUser(User user) {
		OkHttpClient userHttpClient = httpClients.get(user);
		if (userHttpClient == null) {
			userHttpClient = buildHttpClient(user);
			httpClients.put(user, userHttpClient);
		}

		return userHttpClient;
	}

	/**
	 * Clears all cookies associated with a given {@link User}
	 */
	public void clearUserCookies(User user) {
		UserCookieStore cookieStore = cookieStores.get(user);

		if (cookieStore != null) {
			cookieStore.removeAll();
		}
	}

	public CookieStore getUserCookieStore(User user) {
		UserCookieStore cookieStore = cookieStores.get(user);

		if (cookieStore == null) {
			cookieStore = new UserCookieStore(context, user);
			cookieStores.put(user, cookieStore);
		}
		return cookieStore;
	}

	private void clearCachedClients() {
		httpClients.clear();
	}

	@Subscribe
	public void proxySettingsChanged(ProxySettingsChangedEvent event) {
		clearCachedClients();
	}
}
