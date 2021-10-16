package com.ayuget.redface.image.superhost;

import com.ayuget.redface.R;
import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageHostingService;
import com.ayuget.redface.image.ImageQuality;
import com.ayuget.redface.util.ImageUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
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

public class SuperHostService implements ImageHostingService {

    private static final String BASE_URL = "https://img2.super-h.fr/api/1/%s/?key=%s";
    private static final String API_KEY = "61847488c26afcf17b4a8e6294634500";
    private static final String URL_SUFFIX = "&format=json";
    private static final String SOURCE_PREFIX = "&source=";
    private static final String ACTION_UPLOAD = "upload";

    private static final int MAX_SIZE = 512000; // 500 Ko
    private static final int UPLOAD_TIMEOUT = 90; // 90 seconds

    private final OkHttpClient httpClient;
    private final SuperHostResultParser resultParser;

    public SuperHostService(OkHttpClient httpClient, SuperHostResultParser resultParser) {
        this.httpClient = httpClient;
        this.resultParser = resultParser;
    }

    @Override
    public Observable<HostedImage> hostFromUrl(String url) {
        return Observable.just(uploadFromUrl(url));
    }


    @Override
    public Observable<HostedImage> hostFromLocalImage(ByteString localImage) {return Observable
            .defer(() -> {
                try {
                    return Observable.just(ImageUtils.compressIfNeeded(localImage, MAX_SIZE));
                }
                catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .subscribeOn(Schedulers.computation())
            .map(image -> {
                try {
                    return uploadToSuperHost(image.base64(), false);
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .observeOn(Schedulers.io());
    }

    @Override
    public Map<ImageQuality, Integer> availableImageVariants() {
        Map<ImageQuality, Integer> variants = new HashMap<>();

        variants.put(ImageQuality.ORIGINAL, R.string.image_uploaded_original);
        variants.put(ImageQuality.MEDIUM, R.string.image_uploaded_medium);
        variants.put(ImageQuality.THUMBNAIL, R.string.image_uploaded_thumbnail);

        return variants;
    }

    private HostedImage uploadFromUrl(String url) {
        try {
            return uploadToSuperHost(url,true);
        }
        catch (IOException e) {
            throw  Exceptions.propagate(e);
        }
    }

    private HostedImage uploadToSuperHost(String image, boolean isUrl) throws IOException {

        String requestUrl = String.format(BASE_URL, ACTION_UPLOAD, API_KEY, image);

        String pictureName = pictureName();

        if(isUrl)
            requestUrl+= SOURCE_PREFIX + image;

        requestUrl += URL_SUFFIX;

        OkHttpClient uploadClient = httpClient.newBuilder()
                .writeTimeout(UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .build();


        Request request;

        if(!isUrl) {
            RequestBody imageRequest = RequestBody.create(MediaType.parse("image/jpeg"), image);
            RequestBody requestBody = new FormBody.Builder()
                    .add("source", image)
                    .build();

            request = new Request.Builder()
                    .header("Cache-Control", "no-transform")
                    .url(requestUrl)
                    .post(requestBody)
                    .build();
        }
        else
            request = new Request.Builder()
                    .header("Cache-Control", "no-transform")
                    .url(requestUrl)
                    .build();


        Response response = uploadClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        return resultParser.parseResult(ResponseBody.create(MediaType.parse("text/html; charset=UTF-8"),response.body().bytes()).string());
    }

    private String pictureName() {
        return "image_" + System.currentTimeMillis()/1000 + ".jpg";
    }
}
