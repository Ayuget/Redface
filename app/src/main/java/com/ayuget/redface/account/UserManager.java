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
            User foundUser =  accountManager.getAccountByName(activeUsername);

            if (foundUser == null) {
                Log.e(LOG_TAG, String.format("User '%s' was not found in accounts", activeUsername));
                return guestUser;
            }
            else {
                return foundUser;
            }
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
