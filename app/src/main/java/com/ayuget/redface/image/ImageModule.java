package com.ayuget.redface.image;

import com.ayuget.redface.image.superhost.SuperHostResultParser;
import com.ayuget.redface.image.superhost.SuperHostService;
import com.ayuget.redface.network.SecureHttpClientFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ImageModule {
	@Provides
	@Singleton
	ImageHostingService provideImageHostingService() {
		return new SuperHostService(SecureHttpClientFactory.newBuilder().build(), new SuperHostResultParser());
	}
}
