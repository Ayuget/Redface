package com.ayuget.redface.cache;


/**
 * Represents a "cacheable" object. Such an object must have a {@link #cacheKey()}
 * which will be used to cache the value (and which therefore should be unique).
 */
public interface Cacheable<K> {
    K cacheKey();
}
