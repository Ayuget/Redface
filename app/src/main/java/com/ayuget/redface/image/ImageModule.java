package com.ayuget.redface.image;

import com.ayuget.redface.image.rehost.RehostHostingService;
import com.ayuget.redface.image.rehost.RehostResultParser;
import com.ayuget.redface.image.superhost.SuperHostResultParser;
import com.ayuget.redface.image.superhost.SuperHostService;
import com.ayuget.redface.settings.RedfaceSettings;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class ImageModule {
    @Provides
    @Singleton
    ImageHostingService provideImageHostingService(RedfaceSettings settings) {
        if(settings.getDefaultHostingProvider().equals(ImageHostingProvider.SUPERH))
            return new SuperHostService(new OkHttpClient(), new SuperHostResultParser());
        else
            return new RehostHostingService(new OkHttpClient(), new RehostResultParser());
    }
}
