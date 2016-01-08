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

import timber.log.Timber;

public class RedfaceUserManager implements UserManager {
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
    * Active user
    */
    User activeUser = null;

    @Inject
    public RedfaceUserManager(RedfaceSettings settings, RedfaceAccountManager accountManager) {
        this.settings = settings;
        this.accountManager = accountManager;
        this.guestUser = new Guest();
    }

    @Override
    public User getActiveUser() {
        String activeUsername = settings.getActiveUsername();

        if (activeUsername == null || activeUsername.equals(guestUser.getUsername())) {
            return guestUser;
        }
        else {
            User foundUser;

            if (activeUser != null && activeUser.getUsername().equals(activeUsername)) {
                foundUser = activeUser;
            }
            else {
                foundUser = activeUser = accountManager.getAccountByName(activeUsername);
            }

            if (foundUser == null) {
                Timber.e("User '%s' was not found in accounts", activeUsername);
                return guestUser;
            }
            else {
                return foundUser;
            }
        }
    }

    @Override
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

    @Override
    public void setActiveUser(User user) {
        Timber.d("Updating active user to '%s'", user.getUsername());
        settings.updateActiveUsername(user.getUsername());
    }

    @Override
    public boolean isActiveUserLoggedIn() {
        return ! getActiveUser().isGuest();
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = accountManager.getAccounts();
        users.add(0, guestUser);
        return users;
    }

    @Override
    public List<User> getRealUsers() {
        return accountManager.getAccounts();
    }

    @Override
    public User getGuestUser() {
        return this.guestUser;
    }
}
