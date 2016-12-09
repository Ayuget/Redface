package com.ayuget.redface.image;

import com.ayuget.redface.image.rehost.RehostHostingService;
import com.ayuget.redface.image.rehost.RehostResultParser;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

<<<<<<<HEAD
        =======
        >>>>>>>upstream/develop

@Module(library = true)
public class ImageModule {
    @Provides @Singleton
    ImageHostingService provideImageHostingService() {
        return new RehostHostingService(new OkHttpClient(), new RehostResultParser());
    }
}
