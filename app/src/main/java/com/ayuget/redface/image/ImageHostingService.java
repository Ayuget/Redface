package com.ayuget.redface.image;

<<<<<<< HEAD
=======
import java.io.File;
>>>>>>> upstream/develop
import java.util.Map;

import okio.ByteString;
import rx.Observable;
<<<<<<< HEAD
=======
import rx.Single;
>>>>>>> upstream/develop

public interface ImageHostingService {
    Observable<HostedImage> hostFromUrl(String url);
    Observable<HostedImage> hostFromLocalImage(ByteString localImage);
    Map<ImageQuality, Integer> availableImageVariants();
}
