package com.ayuget.redface.image;

import com.google.auto.value.AutoValue;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

@AutoValue
public abstract class HostedImage {
    /**
     * Image URL in full resolution
     */
    public abstract String url();
    /**
     * Available variants (in size) for this image
     */
    public abstract Map<ImageQuality, String> variants();

    public static HostedImage create(String originalUrl) {
        return create(originalUrl, new HashMap<>());
    }

    public static HostedImage create(String originalUrl, Map<ImageQuality, String> variants) {
        return new AutoValue_HostedImage(originalUrl, variants);
    }

    public String variant(ImageQuality imageQuality) {
        return variants().get(imageQuality);
    }
}
