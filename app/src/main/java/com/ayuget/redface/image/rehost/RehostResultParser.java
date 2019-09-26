package com.ayuget.redface.image.rehost;

import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageQuality;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RehostResultParser {
    private static final Pattern REHOST_RESULTS_PATTERN = Pattern.compile("(?:<h4>)([^<]+)(?:<\\/h4>)(?s:.*?)(?:<code>)(?s:.*?)(?:\\[url=)([^\\]]+)(?:\\])(?s:.*?)(?:\\[img\\])([^\\]]+)(?:\\[\\/img\\])(?:[^<]+)(?:<\\/code>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Map<String, ImageQuality> QUALITY_MAPPINGS;

    static {
        QUALITY_MAPPINGS = new HashMap<>();
        QUALITY_MAPPINGS.put("Taille reelle", ImageQuality.ORIGINAL);
        QUALITY_MAPPINGS.put("Medium (800)", ImageQuality.MEDIUM);
        QUALITY_MAPPINGS.put("Preview (600)", ImageQuality.PREVIEW);
        QUALITY_MAPPINGS.put("Miniature", ImageQuality.THUMBNAIL);
    }

    HostedImage parseResultPage(String resultPageSource) throws IOException {
        Matcher m = REHOST_RESULTS_PATTERN.matcher(resultPageSource);

        Map<ImageQuality, String> variants = new HashMap<>();

        while (m.find()) {
            ImageQuality imageQuality = QUALITY_MAPPINGS.get(m.group(1));
            if (imageQuality == null) {
                imageQuality = ImageQuality.ORIGINAL;
            }

            String variantUrl = m.group(3);
            variants.put(imageQuality, variantUrl);
        }

        String originalImageUrl = variants.get(ImageQuality.ORIGINAL);
        if (originalImageUrl == null) {
            throw new IOException("Couldn't parse original image URL from result page");
        }

        return HostedImage.create(originalImageUrl, variants);
    }
}
