package com.ayuget.redface.cache;

import androidx.annotation.NonNull;

import com.ayuget.redface.util.DateUtils;

/**
 * Represents a cached value.
 *
 * A timestamp is stored alongside the value, which represents the
 * time when the value has been cached.
 *
 * @param <T> value type
 */
public class Cached<T> {
    private final T value;
    private final long timestamp;

    private Cached(@NonNull T value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public T value() {
        return value;
    }

    public long timestamp() {
        return timestamp;
    }

    public static <T> Cached<T> from(T value) {
        return new Cached<>(value, DateUtils.getCurrentTimestamp());
    }

    public static <T> Cached<T> from(T value, long timestamp) {
        return new Cached<>(value, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cached<?> cached = (Cached<?>) o;

        return value.equals(cached.value);

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
