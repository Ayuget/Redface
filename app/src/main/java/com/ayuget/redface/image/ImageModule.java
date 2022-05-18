package com.ayuget.redface.image;

import com.ayuget.redface.image.diberie.DiberieHostResultParser;
import com.ayuget.redface.image.diberie.DiberieHostService;
import com.ayuget.redface.network.SecureHttpClientFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ImageModule {
	@Provides
	@Singleton
	ImageHostingService provideImageHostingService() {
		return new DiberieHostService(SecureHttpClientFactory.newBuilder().build(), new DiberieHostResultParser());
	}
}