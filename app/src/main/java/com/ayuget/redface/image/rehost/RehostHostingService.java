package com.ayuget.redface.image.rehost;

import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageHostingService;
import com.ayuget.redface.util.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.ByteString;
import rx.Single;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RehostHostingService implements ImageHostingService {
    private static final String REHOST_BASE_URL = "http://reho.st/";
    private static final String REHOST_UPLOAD_URL = REHOST_BASE_URL + "upload";
    private static final long REHOST_UPLOADED_FILES_MAX_SIZE = 2097152; // 2 MB
    private static final int REHOST_UPLOAD_TIMEOUT = 90; // 90 seconds

    private final OkHttpClient httpClient;
    private final RehostResultParser resultParser;

    public RehostHostingService(OkHttpClient httpClient, RehostResultParser resultParser) {
        this.httpClient = httpClient;
        this.resultParser = resultParser;
    }

    @Override
    public Single<HostedImage> hostFromUrl(String url) {
        return Single.just(HostedImage.create(REHOST_BASE_URL + url));
    }

    @Override
    public Single<HostedImage> hostFromLocalImage(final ByteString localImage) {
        return Single
                .defer(() -> Single.just(ImageUtils.compressIfNeeded(localImage, REHOST_UPLOADED_FILES_MAX_SIZE)))
                .observeOn(Schedulers.computation())
                .map(image -> {
                    try {
                        return uploadToRehost(image);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .observeOn(Schedulers.io());
    }

    private HostedImage uploadToRehost(ByteString localImage) throws IOException {
        RequestBody imageRequest = RequestBody.create(MediaType.parse("image/jpeg"), localImage);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fichier", "redface", imageRequest)
                .build();

        OkHttpClient uploadClient = httpClient.newBuilder()
                .writeTimeout(REHOST_UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REHOST_UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(REHOST_UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .header("Cache-Control", "no-transform")
                .url(REHOST_UPLOAD_URL)
                .post(requestBody)
                .build();

        Response response = uploadClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        return resultParser.parseResultPage(response.body().string());
    }
}
