package com.ayuget.redface;

import com.ayuget.redface.account.AccountModule;
import com.ayuget.redface.data.DataModule;
import com.ayuget.redface.network.NetworkModule;
import com.ayuget.redface.privatemessages.PrivateMessagesModule;
import com.ayuget.redface.profile.ProfileModule;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.UIModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        UIModule.class,
        ProfileModule.class,
        PrivateMessagesModule.class,
        NetworkModule.class,
        DataModule.class,
        AccountModule.class
})
public interface RedfaceComponent extends AndroidInjector<RedfaceApp> {
    DaggerWorkerFactory daggerWorkerFactory();

    RedfaceSettings redfaceSettings();

    @Component.Factory
    interface Factory {
        RedfaceComponent create(@BindsInstance RedfaceApp redfaceApp);
    }
}
