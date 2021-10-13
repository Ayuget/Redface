package com.ayuget.redface.image;

import com.ayuget.redface.image.superhost.SuperHostResultParser;
import com.ayuget.redface.image.superhost.SuperHostService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class ImageModule {
    @Provides
    @Singleton
    ImageHostingService provideImageHostingService() {
        return new SuperHostService(new OkHttpClient(), new SuperHostResultParser());
    }
}
