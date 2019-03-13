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

package com.ayuget.redface;

import android.accounts.AccountManager;
import android.content.Context;
import android.preference.PreferenceManager;

import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.data.state.ResponseStore;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.settings.Blacklist;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.storage.DiskLruCacheFactory;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        library = true
)
public class ContextModule {
    private final Context applicationContext;

    ContextModule(Context applicationContext) {
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

    @Provides @Singleton Blacklist provideBlacklist() {
        return new Blacklist(applicationContext);
    }

    @Provides @Singleton
    DiskLruCacheFactory provideDiskLRUCacheFactory() {
        return new DiskLruCacheFactory(applicationContext);
    }
}
