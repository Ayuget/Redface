/*
 * Copyright 2016 nbonnec
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

package com.ayuget.redface.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.LinkedHashSet;
import java.util.Set;

import timber.log.Timber;

/**
 * Management of the user blacklist.
 * Stored in SharedPreferences.
 */
public class Blacklist {
    private static final String BLACKLIST_PREFS = "RedfaceBlacklist";
    private static final String BLACKLIST_AUTHOR_KEY = "blacklist_author";

    private Set<String> blockedUser;

    /**
     * Shared preferences where the blacklist will be stored.
     */
    private SharedPreferences blacklistPrefs;

    public Blacklist(Context context) {
        this.blacklistPrefs = context.getSharedPreferences(BLACKLIST_PREFS, 0);
        /* Do not use returned instance directly, use a copy. */
        this.blockedUser =
                new LinkedHashSet<>(blacklistPrefs.getStringSet(BLACKLIST_AUTHOR_KEY, new LinkedHashSet<String>()));
    }

    /**
     * Store the name of the blocked author is Set and SharedPreferences.
     * @param author name of the author to block.
     */
    public void addBlockedAuthor(String author) {
        blockedUser.add(cleanAuthor(author));
        storeInSharedPreferences();
    }

    /**
     * Check if an author is blocked.
     * @param author name of the author to check.
     * @return true if author is blocked.
     */
    public boolean isAuthorBlocked(String author) {
        return blockedUser.contains(cleanAuthor(author));
    }

    /**
     * Unblock an author.
     * Update the Set and the SharedPreferences.
     * @param author author to unblock.
     */
    public void unblockAuthor(String author) {
        author = author.toLowerCase();
        blockedUser.remove(author);
        storeInSharedPreferences();
    }

    /**
     * List of blocked authors.
     * @return blockedUser.
     */
    public Set<String> getAll() {
        return blockedUser;
    }

    private void storeInSharedPreferences() {
        SharedPreferences.Editor editor = blacklistPrefs.edit();
        editor.putStringSet(BLACKLIST_AUTHOR_KEY, blockedUser);
        editor.apply();
    }

    /**
     * Get rid off invisible spaces and make it lower case.
     * @param name name of the author.
     * @return cleaned string.
     */
    private String cleanAuthor(String name) {
        return name.replaceAll("\\u200b", "").toLowerCase();
    }
 }
