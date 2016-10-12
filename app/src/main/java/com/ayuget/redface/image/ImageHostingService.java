package com.ayuget.redface.image;

import java.io.File;

import okio.ByteString;
import rx.Observable;
import rx.Single;

public interface ImageHostingService {
    Observable<HostedImage> hostFromUrl(String url);
    Observable<HostedImage> hostFromLocalImage(ByteString localImage);
}
