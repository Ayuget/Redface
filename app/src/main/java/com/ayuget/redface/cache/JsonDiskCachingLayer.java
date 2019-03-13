package com.ayuget.redface.cache;

import com.ayuget.redface.util.DateUtils;
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
public class JsonDiskCachingLayer<T extends Cacheable<String>> implements CachingLayer<String, T> {
    private static final int APP_VERSION = 1;
    private static final int DATA_POSITION = 0;
    private static final int TIMESTAMP_POSITION = 1;

    private final DiskLruCache diskLruCache;

    private final JsonAdapter<T> jsonAdapter;

    private JsonDiskCachingLayer(DiskLruCache diskLruCache, JsonAdapter<T> jsonAdapter) {
        this.diskLruCache = diskLruCache;
        this.jsonAdapter = jsonAdapter;
    }

    @Override
    public Observable<Cached<T>> get(final String key) {
        Timber.d("Request for key '%s' from disk", key);

        try (DiskLruCache.Snapshot snapshot = diskLruCache.get(key)) {

            if (snapshot == null) {
                Timber.d("Request for key '%s' from disk => no such cached value", key);
                return Observable.empty();
            } else {
                T decodedValue = jsonAdapter.fromJson(snapshot.getString(DATA_POSITION));

                Timber.d("Request for key '%s' from disk => got value '%s'", key, decodedValue);
                long valueTimestamp = Long.parseLong(snapshot.getString(TIMESTAMP_POSITION));
                snapshot.close();

                return Observable.just(Cached.from(decodedValue, valueTimestamp));
            }
        } catch (IOException e) {
            return Observable.error(e);
        }
    }

    @Override
    public Completable remove(final String key) {
        return Completable.create(completableSubscriber -> {
            try {
                diskLruCache.remove(key);
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
    public Completable save(final String key, final T value) {
        Timber.d("Saving key '%s' on disk, value is '%s'", key, value);
        return Completable.create(completableSubscriber -> {
            String jsonValue = jsonAdapter.toJson(value);

            try {
                long nowTimestamp = DateUtils.getCurrentTimestamp();

                DiskLruCache.Editor editor = diskLruCache.edit(key);
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

    public static class Builder<T extends Cacheable<String>> {
        private JsonAdapter<T> jsonAdapter;
        private File cacheDirectory;
        private long cacheMaxSize;

        Builder<T> jsonAdapter(JsonAdapter<T> jsonAdapter) {
            this.jsonAdapter = jsonAdapter;
            return this;
        }

        Builder<T> cacheDirectory(File cacheDirectory) {
            this.cacheDirectory = cacheDirectory;
            return this;
        }

        Builder<T> cacheMaxSize(long cacheMaxSize) {
            this.cacheMaxSize = cacheMaxSize;
            return this;
        }

        public JsonDiskCachingLayer<T> build() {
            try {
                return new JsonDiskCachingLayer<>(DiskLruCache.open(cacheDirectory, APP_VERSION, 2, cacheMaxSize), jsonAdapter);
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to initialize DiskLruCache", e);
            }
        }
    }
}
