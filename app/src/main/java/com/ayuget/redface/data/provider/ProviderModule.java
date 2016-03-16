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

import android.content.Context;

import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.network.PageFetcher;
import com.ayuget.redface.storage.BoundedDiskCache;
import com.ayuget.redface.storage.DiskLruCacheFactory;
import com.squareup.moshi.Moshi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public class ProviderModule {
    @Provides @Singleton Moshi provideMoshi() {
        return new Moshi.Builder().build();
    }

    @Provides @Singleton ProfileProvider provideProfileProvider(DiskLruCacheFactory diskLruCacheFactory, PageFetcher pageFetcher, MDEndpoints mdEndpoints, Moshi moshi) {
        BoundedDiskCache<Integer, Profile> profileCache = new BoundedDiskCache<>(diskLruCacheFactory.create("profile"), moshi.adapter(Profile.class));
        return new ProfileProvider(profileCache, pageFetcher, mdEndpoints);
    }
}
