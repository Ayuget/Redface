package com.ayuget.redface.cache;

import com.ayuget.redface.util.DateUtils;
import com.google.common.base.Preconditions;
import com.jakewharton.disklrucache.DiskLruCache;
import com.squareup.moshi.JsonAdapter;
import java.io.File;
import java.io.IOException;

import rx.Completable;
import rx.Observable;
import timber.log.Timber;


/**
 * Specific implementation of a {@link CachingLayer}, using a {@link DiskLruCache} and JSON
 * serialization (with {@link com.squareup.moshi.Moshi}) behind the scenes.
 */
public class JsonDiskCachingLayer<K, V> implements CachingLayer<K, V> {
    private static final int APP_VERSION = 1;
    private static final int DATA_POSITION = 0;
    private static final int TIMESTAMP_POSITION = 1;

    private final DiskLruCache diskLruCache;
    private final JsonAdapter<V> jsonAdapter;
    final KeyResolver<K> keyResolver;

    private JsonDiskCachingLayer(DiskLruCache diskLruCache, JsonAdapter<V> jsonAdapter, KeyResolver<K> keyResolver) {
        this.diskLruCache = diskLruCache;
        this.jsonAdapter = jsonAdapter;
        this.keyResolver = keyResolver;
    }

    @Override
    public Observable<CachedValue<V>> get(final K key) {
        Timber.d("Request for key '%s' from disk", key);
        DiskLruCache.Snapshot snapshot = null;

        try {
            String cacheKey = keyResolver.resolve(key);
            snapshot = diskLruCache.get(cacheKey);

            if (snapshot == null) {
                Timber.d("Request for key '%s' from disk => no such cached value", key);
                return Observable.empty();
            }
            else {
                V decodedValue = jsonAdapter.fromJson(snapshot.getString(DATA_POSITION));

                Timber.d("Request for key '%s' from disk => got value '%s'", key, decodedValue);
                long valueTimestamp = Long.parseLong(snapshot.getString(TIMESTAMP_POSITION));
                snapshot.close();

                return Observable.just(CachedValue.from(decodedValue, valueTimestamp));
            }
        }
        catch (IOException e) {
            return Observable.error(e);
        }
        finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
    }

    @Override
    public Completable remove(final K key) {
        return Completable.create(completableSubscriber -> {
            try {
                String cacheKey = keyResolver.resolve(key);
                diskLruCache.remove(cacheKey);
                completableSubscriber.onCompleted();
            } catch (IOException e) {
                completableSubscriber.onError(e);
            }
        });
    }

    @Override
    public Completable removeAll() {
        return Completable.create(completableSubscriber -> {
            try {
                diskLruCache.flush();
                completableSubscriber.onCompleted();
            }
            catch (IOException e) {
                completableSubscriber.onError(e);
            }
        });
    }

    @Override
    public Completable save(final K key, final V value) {
        Timber.d("Saving key '%s' on disk, value is '%s'", key, value);
        return Completable.create(completableSubscriber -> {
            String jsonValue = jsonAdapter.toJson(value);
            String cacheKey = keyResolver.resolve(key);

            try {
                long nowTimestamp = DateUtils.getCurrentTimestamp();

                DiskLruCache.Editor editor = diskLruCache.edit(cacheKey);
                editor.set(DATA_POSITION, jsonValue);
                editor.set(TIMESTAMP_POSITION, String.valueOf(nowTimestamp));
                editor.commit();

                Timber.d("Successfully saved key '%s' on disk", key);
                completableSubscriber.onCompleted();
            } catch (IOException e) {
                completableSubscriber.onError(e);
            }
        });
    }

    public static class Builder<K, V> {
        private JsonAdapter<V> jsonAdapter;
        private KeyResolver<K> keyResolver;
        private File cacheDirectory;
        private long cacheMaxSizeBytes;

        public Builder<K, V> jsonAdapter(JsonAdapter<V> jsonAdapter) {
            this.jsonAdapter = jsonAdapter;
            return this;
        }

        public Builder<K, V> keyResolver(KeyResolver<K> keyResolver) {
            this.keyResolver = keyResolver;
            return this;
        }

        public Builder<K, V> cacheDirectory(File cacheDirectory) {
            this.cacheDirectory = cacheDirectory;
            return this;
        }

        public Builder<K, V> cacheMaxSizeBytes(long cacheMaxSizeBytes) {
            this.cacheMaxSizeBytes = cacheMaxSizeBytes;
            return this;
        }

        public JsonDiskCachingLayer<K, V> build() {
            Preconditions.checkNotNull(jsonAdapter, "jsonAdapter == null");
            Preconditions.checkNotNull(keyResolver, "keyResolver == null");
            Preconditions.checkNotNull(cacheDirectory, "cacheDirectory == null");
            Preconditions.checkArgument(cacheMaxSizeBytes > 0, "cacheMaxSizeBytes should be greater than zero");

            try {
                return new JsonDiskCachingLayer<>(DiskLruCache.open(cacheDirectory, APP_VERSION, 2, cacheMaxSizeBytes), jsonAdapter, keyResolver);
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to initialize DiskLruCache", e);
            }
        }
    }
}
