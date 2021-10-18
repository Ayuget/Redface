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

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.work.Configuration;

import com.ayuget.redface.settings.RedfaceSettings;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import rx_activity_result.RxActivityResult;
import timber.log.Timber;

public class RedfaceApp extends DaggerApplication implements Configuration.Provider {
    @Override
    public void onCreate() {
        super.onCreate();

        // Setup logging
        // Error logs are sent to the cloud with Crashlytics
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        RxActivityResult.register(this);

        RedfaceNotifications.setupNotifications(this);
    }

    public static RedfaceApp get(Context context) {
        return (RedfaceApp) context.getApplicationContext();
    }

    public RedfaceSettings getSettings() {
        return ((RedfaceComponent) applicationInjector()).redfaceSettings();
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerRedfaceComponent.factory()
                .create(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        DaggerWorkerFactory workerFactory = ((RedfaceComponent) applicationInjector()).daggerWorkerFactory();

        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}
