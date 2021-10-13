package com.ayuget.redface.image;

import com.ayuget.redface.image.rehost.RehostHostingService;
import com.ayuget.redface.image.rehost.RehostResultParser;
import com.ayuget.redface.network.SecureHttpClientFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ImageModule {
	@Provides
	@Singleton
	ImageHostingService provideImageHostingService() {
		return new RehostHostingService(SecureHttpClientFactory.newBuilder().build(), new RehostResultParser());
	}
}
