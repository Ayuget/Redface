package com.ayuget.redface.cache;

import com.squareup.moshi.JsonAdapter;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class RedfaceCache<V extends Cacheable<String>> {
    private final Class<V> cachedType;

    private JsonAdapter<V> jsonAdapter;

    private File appCacheDirectory;

    private long maxCacheSize;

    private long expirationDelay;

    private DataProvider<String, V> networkObservable;

    private RedfaceCache(Class<V> cachedType) {
        this.cachedType = cachedType;
    }

    public static <V extends Cacheable<String>> RedfaceCache<V> cache(Class<V> cachedType) {
        return new RedfaceCache<>(cachedType);
    }

    public RedfaceCache<V> provideDataWith(DataProvider<String, V> networkObservable) {
        this.networkObservable = networkObservable;
        return this;
    }

    public RedfaceCache<V> withJsonAdapter(JsonAdapter<V> jsonAdapter) {
        this.jsonAdapter = jsonAdapter;
        return this;
    }

    public RedfaceCache<V> withCacheDirectory(File cacheDirectory) {
        this.appCacheDirectory = cacheDirectory;
        return this;
    }

    public RedfaceCache<V> withMaxCacheSize(long maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        return this;
    }

    public RedfaceCache<V> expireDataAfter(long expirationDelay) {
        this.expirationDelay = expirationDelay;
        return this;
    }

    private File getTypeCacheDirectory() {
        return new File(appCacheDirectory, cachedType.getSimpleName());
    }

    private JsonDiskCachingLayer<V> buildDiskCachingLayer() {
        return new JsonDiskCachingLayer.Builder<V>()
                .jsonAdapter(jsonAdapter)
                .cacheDirectory(getTypeCacheDirectory())
                .cacheMaxSize(maxCacheSize)
                .build();
    }

    private MemoryCachingLayer<String, V> buildMemoryCachingLayer() {
        return new MemoryCachingLayer<>();
    }

    public CachingDataProvider<String, V> build() {
        return new CachingDataProvider.Builder<>(networkObservable)
                .addCachingLayer(buildDiskCachingLayer())
                .addCachingLayer(buildMemoryCachingLayer())
                .expireValuesAfter(expirationDelay, TimeUnit.SECONDS)
                .build();
    }
}
