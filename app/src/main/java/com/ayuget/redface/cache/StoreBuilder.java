package com.ayuget.redface.cache;

import com.squareup.moshi.JsonAdapter;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class StoreBuilder<K, V> {
    private final Class<V> cachedType;
    private JsonAdapter<V> jsonAdapter;
    private File appCacheDirectory;
    private long maxCacheSize;
    private long expirationDelay;
    private Fetcher<K, V> networkObservable;
    private boolean enableDiskCaching = true;

    private StoreBuilder(Class<V> cachedType) {
        this.cachedType = cachedType;
    }

    public static <K, V> StoreBuilder<K, V> cache(Class<V> cachedType) {
        return new StoreBuilder<>(cachedType);
    }

    public StoreBuilder<K, V> fetcher(Fetcher<K, V> networkObservable) {
        this.networkObservable = networkObservable;
        return this;
    }

    public StoreBuilder<K, V> jsonAdapter(JsonAdapter<V> jsonAdapter) {
        this.jsonAdapter = jsonAdapter;
        return this;
    }

    public StoreBuilder<K, V> cacheDirectory(File cacheDirectory) {
        this.appCacheDirectory = cacheDirectory;
        return this;
    }

    public StoreBuilder<K, V> maxCacheSizeInBytes(long maxCacheSizeInBytes) {
        this.maxCacheSize = maxCacheSizeInBytes;
        return this;
    }

    public StoreBuilder<K, V> expireDataAfter(long expirationDelaySeconds) {
        this.expirationDelay = expirationDelaySeconds;
        return this;
    }

    public StoreBuilder<K, V> disableDiskCaching() {
        this.enableDiskCaching = false;
        return this;
    }

    private File getTypeCacheDirectory() {
        return new File(appCacheDirectory, cachedType.getSimpleName());
    }

    private JsonDiskCachingLayer<K, V> buildDiskCachingLayer() {
        return new JsonDiskCachingLayer.Builder<K, V>()
                .jsonAdapter(jsonAdapter)
                .cacheDirectory(getTypeCacheDirectory())
                .cacheMaxSizeBytes(maxCacheSize)
                .build();
    }

    private MemoryCachingLayer<K, V> buildMemoryCachingLayer() {
        return new MemoryCachingLayer<>();
    }

    public Store<K, V> build() {
        Store.Builder<K, V> builder = new Store.Builder<>(networkObservable);

        if (enableDiskCaching) {
            builder.addCachingLayer(buildDiskCachingLayer());
        }

        builder.addCachingLayer(buildMemoryCachingLayer())
                .expireValuesAfter(expirationDelay, TimeUnit.SECONDS);

        return builder.build();
    }
}
