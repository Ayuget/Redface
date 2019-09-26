package com.ayuget.redface.profile;

import android.util.LruCache;

import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.User;

import java.util.Optional;

import javax.inject.Inject;

import rx.Observable;

public class ProfileManager {
	private MDService mdService;

	private LruCache<Integer, Profile> profileCache = new LruCache<>(30);

	@Inject
	public ProfileManager(MDService mdService) {
		this.mdService = mdService;
	}

	public Profile getProfile(int profileId) {
		return profileCache.get(profileId);
	}

	public Observable<Profile> loadProfile(User user, int profileId) {
		return loadProfile(user, profileId, false);
	}

	public Observable<Profile> loadProfile(User user, int profileId, boolean disableCache) {
		Profile cachedProfile = profileCache.get(profileId);

		if (cachedProfile == null || disableCache) {
			return mdService.getProfile(user, profileId)
				.doOnNext(profile -> profileCache.put(profileId, profile));
		}
		else {
			return Observable.just(cachedProfile);
		}
	}
}
