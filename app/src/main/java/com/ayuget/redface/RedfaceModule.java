package com.ayuget.redface;

import com.ayuget.redface.data.DataModule;
import com.ayuget.redface.ui.UIModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    includes = {
        DataModule.class,
        UIModule.class
    },
    injects = {
        RedfaceApp.class
    }
)
public class RedfaceModule {
    private final RedfaceApp app;

    public RedfaceModule(RedfaceApp app) {
        this.app = app;
    }

    @Provides @Singleton
    RedfaceApp provideApp() {
        return app;
    }
}
