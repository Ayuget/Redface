package com.ayuget.redface;

import android.accounts.AccountManager;
import android.content.Context;
import android.preference.PreferenceManager;

import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.data.state.ResponseStore;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.settings.RedfaceSettings;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        library = true
)
public class ContextModule {
    private final Context applicationContext;

    public ContextModule(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Provides @Singleton AccountManager provideAccountManager() {
        return AccountManager.get(applicationContext);
    }


    @Provides @Singleton
    RedfaceSettings provideRedfaceSettings() {
        return new RedfaceSettings(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext));
    }

    @Provides @Singleton HTTPClientProvider provideHTTPClientProvider(RedfaceSettings settings, Bus bus) {
        return new HTTPClientProvider(applicationContext, settings, bus);
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }


    @Provides @Singleton CategoriesStore provideCategoriesStore() {
        return new CategoriesStore(applicationContext);
    }

    @Provides @Singleton ResponseStore provideResponseStore() {
        return new ResponseStore(applicationContext);
    }
}
