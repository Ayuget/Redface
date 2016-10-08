package com.ayuget.redface.image;

import java.io.File;

import okio.ByteString;
import rx.Single;

public interface ImageHostingService {
    Single<HostedImage> hostFromUrl(String url);
    Single<HostedImage> hostFromLocalImage(ByteString localImage);
}
