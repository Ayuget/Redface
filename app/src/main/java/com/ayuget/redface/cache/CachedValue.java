package com.ayuget.redface.cache;

import android.support.annotation.NonNull;
import com.ayuget.redface.util.DateUtils;

/**
 * Represents a cached value.
 *
 * A timestamp is stored alongside the value, which represents the time when the value has been
 * cached.
 *
 * @param <T> value type
 */
public class CachedValue<T> {
    private final T value;
    private final long timestamp;

    private CachedValue(@NonNull T value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public T value() {
        return value;
    }

    public long timestamp() {
        return timestamp;
    }

    public static <T> CachedValue<T> from(T value) {
        return new CachedValue<>(value, DateUtils.getCurrentTimestamp());
    }

    public static <T> CachedValue<T> from(T value, long timestamp) {
        return new CachedValue<>(value, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedValue<?> cachedValue = (CachedValue<?>) o;

        return value.equals(cachedValue.value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
