package com.ayuget.redface;

import android.app.Application;
import android.content.Context;
import com.ayuget.redface.account.AccountModule;
import com.ayuget.redface.network.NetworkModule;
import com.ayuget.redface.settings.RedfaceSettings;

import dagger.ObjectGraph;

public class RedfaceApp extends Application {
    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        buildObjectGraphAndInject();

        initActiveUser();
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

    public RedfaceSettings getSettings() {
        return (RedfaceSettings) getFromGraph(RedfaceSettings.class);
    }
}
