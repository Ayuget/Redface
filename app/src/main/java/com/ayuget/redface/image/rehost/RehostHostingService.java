package com.ayuget.redface.image.rehost;

import com.ayuget.redface.R;
import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageHostingService;
import com.ayuget.redface.image.ImageQuality;
import com.ayuget.redface.util.ImageUtils;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.ByteString;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

public class RehostHostingService implements ImageHostingService {
    private static final String REHOST_BASE_URL = "https://reho.st/";
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
    public Observable<HostedImage> hostFromUrl(String url) {
        return Observable.just(HostedImage.create(REHOST_BASE_URL + url));
    }

    @Override
    public Observable<HostedImage> hostFromLocalImage(final ByteString localImage) {
        return Observable
                .defer(() -> {
                    try {
                        return Observable.just(ImageUtils.compressIfNeeded(localImage, REHOST_UPLOADED_FILES_MAX_SIZE));
                    }
                    catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .map(image -> {
                    try {
                        return uploadToRehost(image);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .observeOn(Schedulers.io());
    }

    @Override
    public Map<ImageQuality, Integer> availableImageVariants() {
        return ImmutableMap.<ImageQuality, Integer>builder()
                .put(ImageQuality.ORIGINAL, R.string.image_uploaded_original)
                .put(ImageQuality.MEDIUM, R.string.image_uploaded_medium)
                .put(ImageQuality.PREVIEW, R.string.image_uploaded_preview)
                .put(ImageQuality.THUMBNAIL, R.string.image_uploaded_thumbnail)
                .build();
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
        return resultParser.parseResultPage(ResponseBody.create(MediaType.parse("text/html; charset=UTF-8"),response.body().bytes()).string());
    }
}
