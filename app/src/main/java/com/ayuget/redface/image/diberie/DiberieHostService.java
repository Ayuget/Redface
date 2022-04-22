package com.ayuget.redface.image.diberie;

import com.ayuget.redface.R;
import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageHostingService;
import com.ayuget.redface.image.ImageQuality;
import com.ayuget.redface.util.ImageUtils;

import java.io.IOException;
import java.util.HashMap;
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

public class DiberieHostService implements ImageHostingService {

    private static final String DIBERIE_URL = "https://rehost.diberie.com/Host/UploadFiles?PrivateMode=false&SendMail=false&Comment=";
    private static final int MAX_SIZE = 10240000; // 10 Mo
    private static final int UPLOAD_TIMEOUT = 90; // 90 seconds

    private final OkHttpClient httpClient;
    private final DiberieHostResultParser resultParser;

    public DiberieHostService(OkHttpClient httpClient, DiberieHostResultParser resultParser) {
        this.httpClient = httpClient;
        this.resultParser = resultParser;
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
                    return uploadToDiberie(image);
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

    private HostedImage uploadToDiberie(ByteString image) throws IOException {

        OkHttpClient uploadClient = httpClient.newBuilder()
                .writeTimeout(UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(UPLOAD_TIMEOUT, TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("foo.png", "foo.png",
                        RequestBody.create(MediaType.parse("image/png"), image))
                .build();

        Request request = new Request.Builder()
                .url(DIBERIE_URL)
                .post(formBody)
                .build();

        Response response = uploadClient.newCall(request).execute();

        String respBodyStr = "";
        if (response.body() != null) {
            respBodyStr = ResponseBody.create(MediaType.parse("text/html; charset=UTF-8"),response.body().bytes()).string();
        }
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response + " body=" + respBodyStr);
        return resultParser.parseResult(respBodyStr);
    }

}