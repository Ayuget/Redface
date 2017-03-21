package com.ayuget.redface.cache;

public interface KeyResolver<V> {
    String resolve(V value);
}

