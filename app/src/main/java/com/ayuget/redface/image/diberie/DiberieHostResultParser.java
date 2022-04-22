package com.ayuget.redface.image.diberie;

import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageQuality;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class DiberieHostResultParser {

    HostedImage parseResult(String result) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        DiberieHostResult diberieHostResult = gson.fromJson(result, DiberieHostResult.class);
        Map<ImageQuality, String> map = new HashMap<>();
        if (diberieHostResult.getResizedURL() != null) {
            map.put(ImageQuality.MEDIUM, diberieHostResult.getResizedURL());
        }
        if (diberieHostResult.getThumbURL() != null) {
            map.put(ImageQuality.THUMBNAIL, diberieHostResult.getThumbURL());
        }
        return HostedImage.create(diberieHostResult.getPicURL(), map);
    }

}
