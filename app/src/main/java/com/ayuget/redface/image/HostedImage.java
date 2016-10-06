package com.ayuget.redface.image;

import com.google.auto.value.AutoValue;

import java.util.Map;

import javax.annotation.Nullable;

@AutoValue
public abstract class HostedImage {
    /**
     * Image URL, at it's original size
     */
    abstract String url();

    /**
     * Available variants (in size) for this image
     */
    @Nullable abstract Map<ImageQuality, String> variants();

    public static HostedImage create(String url, Map<ImageQuality, String> variants) {
        return null;
    }
}
