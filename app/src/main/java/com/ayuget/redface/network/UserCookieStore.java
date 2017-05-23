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
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.ayuget.redface.data.api.model.User;
import com.google.common.base.Preconditions;

import java.io.*;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * A persistent cookie store which implements the Apache HttpClient CookieStore interface.
 * Cookies are stored and will persist on the user's device between application sessions since they
 * are serialized and stored in SharedPreferences. Instances of this class are
 * designed to be used with AsyncHttpClient#setCookieStore, but can also be used with a
 * regular old apache HttpClient/HttpContext if you prefer.
 */
public class UserCookieStore implements CookieStore {
    private static final String COOKIE_PREFS = "RedfaceCookies";
    private static final String COOKIE_NAME_PREFIX = "cookie_";

    private final HashMap<String, ConcurrentHashMap<String, HttpCookie>> cookies;
    private final SharedPreferences cookiePrefs;
    private final User user;

    /**
     * Construct a persistent cookie store.
     *
     * @param context Context to attach cookie store to
     */
    public UserCookieStore(Context context, User user) {
        Preconditions.checkNotNull(user, "User cannot be null");

        cookiePrefs = context.getSharedPreferences(user.getUsername() + COOKIE_PREFS, 0);
        cookies = new HashMap<>();
        this.user = user;

        // Load any previously stored cookies into the store
        loadCookiesFromSharedPreferences();
    }

    void loadCookiesFromSharedPreferences() {
        int decodedCookiesCount = 0;
        Map<String, ?> prefsMap = cookiePrefs.getAll();
        for(Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            String cookieListValue = (String)entry.getValue();

            if (cookieListValue != null && !cookieListValue.startsWith(COOKIE_NAME_PREFIX)) {
                String[] cookieNames = TextUtils.split((String)entry.getValue(), ",");

                for (String name : cookieNames) {
                    String encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null);

                    if (encodedCookie != null) {
                        HttpCookie decodedCookie = decodeCookie(encodedCookie);
                        if (decodedCookie != null) {
                            if(!cookies.containsKey(entry.getKey())) {
                                cookies.put(entry.getKey(), new ConcurrentHashMap<String, HttpCookie>());
                            }
                            cookies.get(entry.getKey()).put(name, decodedCookie);

                            decodedCookiesCount++;
                        }
                    }
                }

            }
        }

        Timber.d("[user=%s] Successfully decoded '%d' cookies from persistence", user.getUsername(), decodedCookiesCount);
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        String name = getCookieToken(uri, cookie);
        String host = uri.getHost();

        // Save cookie into local store, or remove if expired
        if (cookie.hasExpired()) {
            if(cookies.containsKey(uri.toString())) {
                cookies.get(uri.getHost()).remove(name);
            }
        }
        else {
            if(!cookies.containsKey(uri.getHost())) {
                cookies.put(uri.getHost(), new ConcurrentHashMap<String, HttpCookie>());
            }

            // We choose deliberately not to overwrite existing cookies, because it is simply
            // not necessary. Existing (non-expired) cookies will work just fine.
            boolean cookieExists = cookies.get(uri.getHost()).containsKey(name);

            if (! cookieExists) {
                Timber.d("[user=%s] Adding cookie '%s' for URL '%s' (name='%s', host='%s')", user.getUsername(), cookie.getName(), uri.toString(), name, host);
                cookies.get(uri.getHost()).put(name, cookie);

                // Save cookie into persistent store
                SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
                prefsWriter.putString(uri.getHost(), TextUtils.join(",", Collections.list(cookies.get(uri.getHost()).keys())));
                prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableHttpCookie(cookie)));
                prefsWriter.apply();
            }
        }
    }

    protected String getCookieToken(URI uri, HttpCookie cookie) {
        return cookie.getName() + cookie.getDomain();
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
        if(cookies.containsKey(uri.getHost()))
            ret.addAll(cookies.get(uri.getHost()).values());
        return ret;
    }

    @Override
    public boolean removeAll() {
        Timber.d("[user=%s] Clearing all cookies !", user.getUsername());

        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.clear();
        prefsWriter.commit();
        cookies.clear();
        return true;
    }


    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        String name = getCookieToken(uri, cookie);

        if(cookies.containsKey(uri.getHost()) && cookies.get(uri.getHost()).containsKey(name)) {
            cookies.get(uri.getHost()).remove(name);

            SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
            if(cookiePrefs.contains(COOKIE_NAME_PREFIX + name)) {
                prefsWriter.remove(COOKIE_NAME_PREFIX + name);
            }
            prefsWriter.putString(uri.getHost(), TextUtils.join(",", cookies.get(uri.getHost()).keySet()));
            prefsWriter.commit();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<HttpCookie> getCookies() {
        ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
        for (String key : cookies.keySet())
            ret.addAll(cookies.get(key).values());

        return ret;
    }

    @Override
    public List<URI> getURIs() {
        ArrayList<URI> ret = new ArrayList<URI>();
        for (String key : cookies.keySet())
            try {
                ret.add(new URI(key));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        return ret;
    }

    /**
     * Serializes Cookie object into String
     *
     * @param cookie cookie to be encoded, can be null
     * @return cookie encoded as String
     */
    protected String encodeCookie(SerializableHttpCookie cookie) {
        if (cookie == null)
            return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (IOException e) {
            Timber.e(e, "IOException in encodeCookie");
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    /**
     * Returns cookie decoded from cookie string
     *
     * @param cookieString string of cookie as returned from http request
     * @return decoded cookie or null if exception occured
     */
    protected HttpCookie decodeCookie(String cookieString) {
        byte[] bytes = hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        HttpCookie cookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableHttpCookie) objectInputStream.readObject()).getCookie();
        } catch (IOException e) {
            Timber.e(e, "IOException in decodeCookie");
        } catch (ClassNotFoundException e) {
            Timber.e(e, "ClassNotFoundException in decodeCookie");
        }

        return cookie;
    }

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't have to rely on any
     * large Base64 libraries. Can be overridden if you like!
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    protected String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * Converts hex values from strings to byte arra
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    protected byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
