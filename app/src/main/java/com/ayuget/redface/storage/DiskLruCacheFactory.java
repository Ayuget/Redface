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

package com.ayuget.redface.storage;

import android.content.Context;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

public class DiskLruCacheFactory {
    private static final String CACHING_DIRECTORY = "cache";

    private static final int CACHING_APP_VERSION = 0;

    private static final int CACHE_VALUE_COUNT = 2;

    private static final long DEFAULT_CACHE_MAX_SIZE = 1024 * 1024; // 1MB

    private final File cacheDirectory;

    public DiskLruCacheFactory(Context context) {
        cacheDirectory = context.getDir(CACHING_DIRECTORY, Context.MODE_PRIVATE);
    }

    private File getResourceCacheDir(String resourceName) {
        File resourceCacheDir = new File(cacheDirectory, resourceName);

        if (! resourceCacheDir.exists()) {
            boolean creationSucceeded = resourceCacheDir.mkdir();

            if (! creationSucceeded) {
                throw new RuntimeException("Unable to create cache directory");
            }
        }

        return resourceCacheDir;
    }

    public DiskLruCache create(String resourceName) {
        return create(resourceName, DEFAULT_CACHE_MAX_SIZE);
    }

    public DiskLruCache create(String resourceName, long maxCacheSize) {
        File resourceCacheDir = getResourceCacheDir(resourceName);

        try {
            return DiskLruCache.open(resourceCacheDir, CACHING_APP_VERSION, CACHE_VALUE_COUNT, maxCacheSize);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to create disk LRU cache", e);
        }
    }
}
