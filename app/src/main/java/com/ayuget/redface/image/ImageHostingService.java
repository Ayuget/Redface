package com.ayuget.redface.image;

import java.util.Map;

import okio.ByteString;
import rx.Observable;

public interface ImageHostingService {
//    Observable<HostedImage> hostFromUrl(String url);
    Observable<HostedImage> hostFromLocalImage(ByteString localImage);
    Map<ImageQuality, Integer> availableImageVariants();
}
