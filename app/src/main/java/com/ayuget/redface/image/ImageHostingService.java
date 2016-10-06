package com.ayuget.redface.image;

import java.io.File;
import rx.Single;

public interface ImageHostingService {
    Single<HostedImage> hostFromUrl(String url);
    Single<HostedImage> hostFromLocalImage(File localImage);
}
