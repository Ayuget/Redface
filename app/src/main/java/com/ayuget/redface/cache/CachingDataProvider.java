package com.ayuget.redface.cache;

import com.ayuget.redface.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Completable;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import timber.log.Timber;


@SuppressWarnings("Convert2MethodRef")
public class CachingDataProvider<K, V extends Cacheable<K>> implements DataProvider<K, V> {
    private final DataProvider<K, V> dataProvider;

    private final List<CachingLayer<K, V>> cachingLayers;

    private final long cacheExpirationDelay;

    private final Scheduler scheduler;

    private CachingDataProvider(DataProvider<K, V> dataProvider, List<CachingLayer<K, V>> cachingLayers, long cacheExpirationDelay, Scheduler scheduler) {
        this.dataProvider = dataProvider;
        this.cachingLayers = cachingLayers;
        this.cacheExpirationDelay = cacheExpirationDelay;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<V> get(K key) {
        Timber.d("Requesting value for key '%s'", key);
        List<Observable<V>> sources = new ArrayList<>(cachingLayers.size() + 1);

        for (int i = cachingLayers.size() - 1; i >= 0; i--) {
            sources.add(wrapLayer(i, key));
        }

        sources.add(wrapProvider(key));

        return Observable.concat(Observable.from(sources)).first();
    }

    public void clear(final K key) {
        Timber.d("Clearing caches for key '%s'", key);

        List<Completable> clearingCompletables = new ArrayList<>();

        for (CachingLayer<K, V> cachingLayer : cachingLayers) {
            clearingCompletables.add(cachingLayer.remove(key).subscribeOn(scheduler));
        }

        Completable.merge(clearingCompletables).await();
    }

    public void clearAll() {
        List<Completable> clearingCompletables = new ArrayList<>();

        for (CachingLayer<K, V> cachingLayer : cachingLayers) {
            clearingCompletables.add(cachingLayer.removeAll().subscribeOn(scheduler));
        }

        Completable.merge(clearingCompletables).await();
    }

    private Observable<V> wrapProvider(final K key) {
        return Observable.defer(() -> dataProvider.get(key)
                .doOnNext(v -> cacheValueInUpperLayers(-1, key, v)));
    }

    private Observable<V> wrapLayer(final int layerId, final K key) {
        return Observable.defer(() -> {
            CachingLayer<K, V> cachingLayer = cachingLayers.get(layerId);

            return cachingLayer.get(key)
                    // Ignores expired values
                    .filter(cached -> {
                        long cachedSince = DateUtils.getCurrentTimestamp() - cached.timestamp();
                        boolean hasExpired = cachedSince >= cacheExpirationDelay;

                        if (hasExpired) {
                            Timber.d("Value has expired (layer %d) !", layerId);
                        }

                        return !hasExpired;
                    })
                    .map(cached -> cached.value())
                    // Caches returned value in upper caching layers
                    .doOnNext(value -> cacheValueInUpperLayers(layerId, key, value));
        });
    }

    private void cacheValueInUpperLayers(int sourceLayerId, K key, V value) {
        List<Completable> completables = new ArrayList<>(cachingLayers.size());

        for (int layerId = sourceLayerId + 1; layerId <= cachingLayers.size() - 1; layerId++) {
            CachingLayer<K, V> cachingLayer = cachingLayers.get(layerId);
            completables.add(cachingLayer.save(key, value));
        }

        Completable.merge(completables).await();
    }

    public static <K, V extends Cacheable<K>> Builder<K, V> provideWith(DataProvider<K, V> dataProvider) {
        return new Builder<>(dataProvider);
    }

    public static class Builder<K, V extends Cacheable<K>> {
        private final DataProvider<K, V> dataProvider;
        private List<CachingLayer<K ,V>> cachingLayers = new ArrayList<>();
        private long cacheExpirationDelay;
        private Scheduler scheduler = Schedulers.io();

        public Builder(DataProvider<K, V> dataProvider) {
            this.dataProvider = dataProvider;
        }

        /** Sets cache expiration delay */
        public Builder<K, V> expireValuesAfter(long cacheExpirationDelay, TimeUnit timeUnit) {
            this.cacheExpirationDelay = TimeUnit.SECONDS.convert(cacheExpirationDelay, timeUnit);
            return this;
        }

        public Builder<K, V> addCachingLayer(CachingLayer<K, V> cachingLayer) {
            cachingLayers.add(cachingLayer);
            return this;
        }

        public Builder<K, V> scheduleOn(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public CachingDataProvider<K, V> build() {
            return new CachingDataProvider<>(dataProvider, cachingLayers, cacheExpirationDelay, scheduler);
        }
    }
}
