package com.ayuget.redface.account;

import android.accounts.AccountManager;

import com.ayuget.redface.ContextModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = ContextModule.class,
        injects = {
                UserManager.class
        },
        library = true
)
public class AccountModule {
    @Provides @Singleton
    RedfaceAccountManager provideAccountManager(AccountManager androidAccountManager) {
        return new RedfaceAccountManager(androidAccountManager);
    }
}
