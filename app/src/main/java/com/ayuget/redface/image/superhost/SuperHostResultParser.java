package com.ayuget.redface.image.superhost;

import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageQuality;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class SuperHostResultParser {

    HostedImage parseResult(String result) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        SuperHostResult superHostResult = gson.fromJson(result, SuperHostResult.class);
        Map<ImageQuality, String> map = new HashMap<>();
        if (superHostResult.getImage().getMedium() != null) {
            map.put(ImageQuality.MEDIUM, superHostResult.getImage().getMedium().getUrl());
        }
        if (superHostResult.getImage().getThumb() != null) {
            map.put(ImageQuality.THUMBNAIL, superHostResult.getImage().getThumb().getUrl());
        }
        return HostedImage.create(superHostResult.getImage().url, map);
    }

}
