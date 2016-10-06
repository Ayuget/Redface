package com.ayuget.redface.image.rehost;

import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageHostingService;
import com.ayuget.redface.util.ImageUtils;

import java.io.File;
import java.util.concurrent.Callable;

import okhttp3.OkHttpClient;
import rx.Single;

public class RehostService implements ImageHostingService {
    private static final String REHOST_UPLOAD_URL = "http://reho.st/upload";
    private static final long REHOST_UPLOADED_FILES_MAX_SIZE = 2097152; // 2 MB

    private final OkHttpClient httpClient;

    public RehostService(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Single<HostedImage> hostFromUrl(String url) {
        return null;
    }

    @Override
    public Single<HostedImage> hostFromLocalImage(final File localImage) {
        return Single.defer(new Callable<Single<HostedImage>>() {
            @Override
            public Single<HostedImage> call() throws Exception {
                File imageToUpload = ImageUtils.compressIfNeeded(localImage, REHOST_UPLOADED_FILES_MAX_SIZE);
                return Single.just(uploadToRehost(imageToUpload));
            }
        });
    }

    private HostedImage uploadToRehost(File localImage) {


        return null;
    }
}
