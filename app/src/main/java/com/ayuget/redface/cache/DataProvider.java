package com.ayuget.redface.cache;

import rx.Observable;

public interface DataProvider<K, V> {
    Observable<V> get(K key);
}
