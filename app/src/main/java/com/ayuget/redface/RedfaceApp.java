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

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ayuget.redface.account.AccountModule;
import com.ayuget.redface.job.JobUtils;
import com.ayuget.redface.network.NetworkModule;
import com.ayuget.redface.settings.RedfaceSettings;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.ypresto.timbertreeutils.CrashlyticsLogTree;

import dagger.ObjectGraph;
import timber.log.Timber;

public class RedfaceApp extends Application {
    private ObjectGraph objectGraph;

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup logging
        // Error logs are sent to the cloud with Crashlytics
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        else {
            Timber.plant(new CrashlyticsLogTree(Log.ERROR));
        }

        // Setup dependency injection
        buildObjectGraphAndInject();

        initActiveUser();

        refWatcher = LeakCanary.install(this);

        JobUtils.runNotificationService(this);
    }

    private void initActiveUser() {

    }

    public void buildObjectGraphAndInject() {
        objectGraph = ObjectGraph.create(
                new ContextModule(this.getApplicationContext()),
                new AccountModule(),
                new NetworkModule(),
                new RedfaceModule(this)
        );
        objectGraph.inject(this);
    }

    public void inject(Object o) {
        objectGraph.inject(o);
    }

    public Object getFromGraph(Class c) {
        return objectGraph.get(c);
    }

    public static RedfaceApp get(Context context) {
        return (RedfaceApp) context.getApplicationContext();
    }

    public static RefWatcher getRefWatcher(Context context) {
        RedfaceApp application = (RedfaceApp) context.getApplicationContext();
        return application.refWatcher;
    }

    public RedfaceSettings getSettings() {
        return (RedfaceSettings) getFromGraph(RedfaceSettings.class);
    }
}
