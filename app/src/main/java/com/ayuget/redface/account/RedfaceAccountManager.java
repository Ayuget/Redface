package com.ayuget.redface.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

import com.ayuget.redface.data.api.model.User;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;


public class RedfaceAccountManager {
    private static final String LOG_TAG = RedfaceAccountManager.class.getSimpleName();

    private static final String REDFACE_ACCOUNT_TYPE = "com.ayuget.redface.account";

    private static final String REDFACE_AUTHTOKEN_TYPE = "password";

    private final AccountManager accountManager;

    public RedfaceAccountManager(AccountManager accountManager) {
        this.accountManager = Preconditions.checkNotNull(accountManager, "accountManager cannot be null");
    }

    public List<User> getAccounts() {
        final Account[] accounts = accountManager.getAccountsByType(REDFACE_ACCOUNT_TYPE);
        ArrayList<User> Users = new ArrayList<>(accounts.length);

        for (final Account account : accounts) {
            String password = accountManager.getPassword(account);
            Users.add(new User(account.name, password));
        }

        return Users;
    }

    public User getAccountByName(String username) {
        for (User user : getAccounts()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }

    public void addAccount(User user) {
        Log.d(LOG_TAG, String.format("Adding account for user '%s'", user.getUsername()));
        Account account = new Account(user.getUsername(), REDFACE_ACCOUNT_TYPE);

        accountManager.addAccountExplicitly(account, user.getPassword(), null);
        accountManager.setAuthToken(account, user.getPassword(), REDFACE_AUTHTOKEN_TYPE);
    }

    public void removeAccount(User user) {
        Account account = new Account(user.getUsername(), REDFACE_ACCOUNT_TYPE);
        accountManager.removeAccount(account, null, null);
    }

    public void updatePassword(User user) {
        final Account[] accounts = accountManager.getAccountsByType(REDFACE_ACCOUNT_TYPE);
        for (final Account account : accounts) {
            if (Objects.equal(account.name, user.getUsername())) {
                accountManager.setPassword(account, user.getPassword());
                return;
            }
        }
    }

}
