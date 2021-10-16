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

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Resolves an URL which is causing a redirection (HTTP code {@code 301}) to its destination URL
 */
public class HTTPRedirection {
	/**
	 * HTTP status code for "redirected"
	 */
	private static final int REDIRECTED_STATUS_CODE = 301;

	/**
	 * HTTP location header indicating the target URL when redirected
	 */
	private static final String LOCATION_HEADER = "Location";

	private HTTPRedirection() {
		// Utility class, prevent instantiation
	}

	/**
	 * Resolves a redirected URL {@code originalUrl} to its final location.
	 * <p>
	 * If the URL is not really redirected, the original URL is returned.
	 */
	public static Observable<String> resolve(final String originalUrl) {
		return Observable.create(new Observable.OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				OkHttpClient httpClient = SecureHttpClientFactory.newBuilder()
						.followRedirects(false)
						.build();

				Request request = new Request.Builder()
						.url(originalUrl)
						.build();

				try {
					Response response = httpClient.newCall(request).execute();

					if (response.code() == REDIRECTED_STATUS_CODE) {
						String locationHeader = response.header(LOCATION_HEADER);
						String targetUrl = locationHeader == null ? originalUrl : "http://" + new URL(originalUrl).getHost() + locationHeader;
						Timber.d("URL '%s' is redirected to '%s'", originalUrl, targetUrl);
						subscriber.onNext(targetUrl);
					} else {
						Timber.w("URL '%s' is not redirected", originalUrl);
						subscriber.onNext(originalUrl);
					}
				} catch (IOException e) {
					subscriber.onError(e);
				}
			}
		});
	}
}
