package com.ayuget.redface.cache;

import rx.Observable;

public interface Fetcher<K, V> {
    Observable<V> get(K key);
}
