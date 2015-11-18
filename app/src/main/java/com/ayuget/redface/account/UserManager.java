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

package com.ayuget.redface.account;

import android.util.Log;

import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.data.api.model.Guest;
import com.ayuget.redface.data.api.model.User;

import java.util.List;

import javax.inject.Inject;

public class UserManager {
    private static final String LOG_TAG = UserManager.class.getSimpleName();

    /**
     * Application settings
     */
    RedfaceSettings settings;

    /**
     * Real accounts manager (guest is not considered as an account). Accounts
     * will appear in the appropriate Android preference screen.
     */
    RedfaceAccountManager accountManager;

    /**
     * Guest user, not logged in
     */
    User guestUser;

    /**
     * Real user used to preload pages
     */
    User preloadingUser;

    /**
     * Active user
     */
    User activeUser = null;

    @Inject
    public UserManager(RedfaceSettings settings, RedfaceAccountManager accountManager) {
        this.settings = settings;
        this.accountManager = accountManager;
        this.guestUser = new Guest();
    }

    public User getActiveUser() {
        String activeUsername = settings.getActiveUsername();

        if (activeUsername == null || activeUsername.equals(guestUser.getUsername())) {
            return guestUser;
        }
        else {
            User foundUser;

            if (activeUser != null && activeUser.getUsername().equals(activeUsername)) {
                foundUser = activeUser;
            } else {
                foundUser = activeUser = accountManager.getAccountByName(activeUsername);
            }

            if (foundUser == null) {
                Log.e(LOG_TAG, String.format("User '%s' was not found in accounts", activeUsername));
                return guestUser;
            }
            else {
                return foundUser;
            }
        }
    }

    public boolean isActiveUser(String username) {
        // Forum automatically adds some zero width spaces to usernames
        username = username.replace("\u200b", "");

        if (getActiveUser() == null) {
            return false;
        }
        else {
            return username.toLowerCase().equals(getActiveUser().getUsername().toLowerCase());
        }
    }

    public void setActiveUser(User user) {
        Log.d(LOG_TAG, String.format("Updating active user to '%s'", user.getUsername()));
        settings.updateActiveUsername(user.getUsername());
    }

    public boolean activeUserIsLoggedIn() {
        return ! getActiveUser().isGuest();
    }

    /**
     * Returns a list of all available users, including guest.
     */
    public List<User> getAllUsers() {
        List<User> users = accountManager.getAccounts();
        users.add(0, guestUser);
        return users;
    }

    /**
     * Returns a list of all "real" users, meaning actual forum and app users.
     * Excludes guest, preloading user, ...
     */
    public List<User> getRealUsers() {
        return accountManager.getAccounts();
    }

    /**
     * Returns guest user. Can be used to browse the forum anonymously, or to
     * load certain pieces of information without messing up with flags...
     */
    public User getGuestUser() {
        return this.guestUser;
    }

    /**
     * Returns user to be used to preload pages.
     */
    public User getPreloadingUser() {
        return this.preloadingUser;
    }
}
