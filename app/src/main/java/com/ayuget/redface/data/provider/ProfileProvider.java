/*
 * Copyright 2015 Ayuget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ayuget.redface.data.provider;

import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.hfr.transforms.HTMLToProfile;
import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.network.PageFetcher;
import com.ayuget.redface.storage.BoundedDiskCache;

import rx.Observable;

public class ProfileProvider extends AbstractDataProvider<Integer, Profile> {
    private static final long PROFILE_STALE_THRESHOLD = 7*24*3600;

    private final PageFetcher pageFetcher;

    private final MDEndpoints mdEndpoints;

    public ProfileProvider(BoundedDiskCache<Integer, Profile> boundedDiskCache, PageFetcher pageFetcher, MDEndpoints mdEndpoints) {
        super(boundedDiskCache, PROFILE_STALE_THRESHOLD);
        this.pageFetcher = pageFetcher;
        this.mdEndpoints = mdEndpoints;
    }

    @Override
    protected Observable<Profile> fromNetwork(User user, Integer key) {
        return pageFetcher.fetchSource(user, mdEndpoints.profile(key))
                .map(new HTMLToProfile());
    }
}
