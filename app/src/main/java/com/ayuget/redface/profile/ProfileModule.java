package com.ayuget.redface.profile;

import com.ayuget.redface.data.api.MDService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
	complete = false,
	library = true
)
public class ProfileModule {
	@Singleton
	@Provides
	ProfileManager provideProfileManager(MDService mdService) {
		return new ProfileManager(mdService);
	}
}
