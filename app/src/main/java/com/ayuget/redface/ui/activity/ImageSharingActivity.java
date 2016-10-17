package com.ayuget.redface.ui.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.ayuget.redface.R;
import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageHostingService;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.util.ImageUtils;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class ImageSharingActivity extends BaseActivity {
    @Inject
    ImageHostingService imageHostingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri imageUri = parseUriFromIntent();
        if (imageUri == null) {
            finishWithoutAnimation();
        }
        else {
            shareImage(imageUri);
        }
    }

    private Uri parseUriFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getAction() !=null && intent.getAction().equals(Intent.ACTION_SEND)) {
            Bundle extras = intent.getExtras();
            return (Uri) extras.get(Intent.EXTRA_STREAM);
        }
        else {
            return null;
        }
    }

    private void shareImage(@NonNull Uri imageUri) {
        Timber.d("Sharing image with URI = %s", imageUri);

        Observable<HostedImage> hostedImageObservable;

        if (uriStartsWithHTTPProtocol(imageUri)) {
            // Image is already hosted somewhere...
            hostedImageObservable = imageHostingService.hostFromUrl(imageUri.toString());
        } else {
            // Local image, needs to be uploaded
            hostedImageObservable = Observable.fromCallable(() -> getContentResolver().openInputStream(imageUri))
                    .map(ImageUtils::readStreamFully)
                    .flatMap(b -> imageHostingService.hostFromLocalImage(b));
        }

        hostedImageObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hostedImage -> {
                    Timber.d("Successfully shared image ! -> %s", hostedImage);
                    UiUtils.copyToClipboard(this, hostedImage.url(), false);
                    Toast.makeText(ImageSharingActivity.this, R.string.image_shared_success, Toast.LENGTH_LONG).show();
                    finishWithoutAnimation();
                }, error -> {
                    Toast.makeText(ImageSharingActivity.this, R.string.image_shared_failure, Toast.LENGTH_LONG).show();
                    finishWithoutAnimation();
                });
    }

    private void finishWithoutAnimation() {
        finish();
        overridePendingTransition(0, 0);
    }
    private boolean uriStartsWithHTTPProtocol(Uri uri) {
        return uri.toString().startsWith("http://") || uri.toString().startsWith("https://");
    }
}
