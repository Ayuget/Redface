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

import com.ayuget.redface.data.api.model.User;

import java.util.List;

public interface UserManager {

    /**
     * Returns currently active user.
     */
    User getActiveUser();

    /**
     * Checks if a given user is the active one.
     *
     * Can be used to trigger some additional actions
     * in the app, like post edition buttons, ...
     */
    boolean isActiveUser(String username);

    /**
     * Sets application currently active user.
     */
    void setActiveUser(User user);

    /**
     * Returns true if the currently active user is logged in.
     * In guest mode, basically all forum actions are disabled
     */
    boolean isActiveUserLoggedIn();

    /**
     * Returns all users, including guest user
     */
    List<User> getAllUsers();

    /**
     * Returns only "real" users, meaning all users EXCEPT
     * the guest user.
     */
    List<User> getRealUsers();

    /**
     * Returns guest user. That user is "virtual", meaning that it
     * has no real forum account.
     */
    User getGuestUser();
}
