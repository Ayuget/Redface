/*
 * Copyright 2015 Ayuget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ayuget.redface.ui.misc;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.ShareCompat;

import com.ayuget.redface.R;
import com.ayuget.redface.network.SecureHttpClientFactory;
import com.ayuget.redface.storage.StorageHelper;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.activity.BaseActivity;
import com.ayuget.redface.ui.activity.ExifDetailsActivity;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.ayuget.redface.util.ImageUtils.isScopedStorageEnabled;

public class ImageMenuHandler {
    private final Activity activity;

    private final String imageUrl;

    public interface ImageSavedCallback {
        void onImageSaved(Uri savedImage, String mimeType);
    }

    public ImageMenuHandler(Activity activity, String imageUrl) {
        this.activity = activity;
        this.imageUrl = imageUrl;
    }

    public void saveImage() {
        saveImage(true, false, null);
    }

    public void saveTemporaryImage(final ImageSavedCallback imageSavedCallback) {
        saveImage(false, true, imageSavedCallback);
    }

    private void saveImage(final boolean notifyUser, final boolean isTemporary, final ImageSavedCallback imageSavedCallback) {
        if (isScopedStorageEnabled()) {
            Timber.d("Scoped storage is enabled, no need to ask any permission");
            saveImageFromNetwork(notifyUser, isTemporary, imageSavedCallback);
        } else {
            RxPermissions.getInstance(activity)
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(isPermissionGranted -> {
                        if (!isPermissionGranted) {
                            Timber.w("WRITE_EXTERNAL_STORAGE denied, unable to save image");
                            SnackbarHelper.makeError(activity, R.string.error_saving_image_permission_denied).show();
                            return;
                        }

                        saveImageFromNetwork(notifyUser, isTemporary, imageSavedCallback);
                    });
        }
    }

    private void saveImageFromNetwork(final boolean notifyUser, final boolean isTemporary, final ImageSavedCallback imageSavedCallback) {
        OkHttpClient okHttpClient = SecureHttpClientFactory.newBuilder().build();

        final Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        String imageFilename = StorageHelper.getFilenameFromUrl(imageUrl);

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SnackbarHelper.makeError(activity, R.string.error_saving_image).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final byte[] imageBytes = response.body().bytes();
                final String imageMimeType = response.header("Content-Type");

                Timber.d("Image successfully downloaded (size: %d bytes)", imageBytes.length);

                try {
                    Uri savedImageUri;

                    if (isTemporary) {
                        savedImageUri = saveTemporaryImage(imageFilename, imageMimeType, imageBytes);
                    } else if (isScopedStorageEnabled()) {
                        savedImageUri = saveImagePostAndroidQ(imageFilename, imageMimeType, imageBytes);
                    } else {
                        savedImageUri = saveImagePreAndroidQ(imageFilename, imageMimeType, imageBytes);
                    }

                    if (notifyUser) {
                        SnackbarHelper.showWithAction(activity, R.string.image_saved_successfully, R.string.action_snackbar_open_image, view -> {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(savedImageUri, "image/*");
                            activity.startActivity(intent);
                        });
                    }

                    notifyImageWasSaved(imageSavedCallback, savedImageUri, imageMimeType);
                } catch (IOException e) {
                    Timber.e(e, "Unable to save image to external storage");
                    SnackbarHelper.makeError(activity, R.string.error_saving_image).show();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private Uri saveImagePostAndroidQ(final String filename, final String mimeType, final byte[] rawData) throws IOException {
        final ContentValues savedImageDetails = new ContentValues();
        savedImageDetails.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        savedImageDetails.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        savedImageDetails.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        ContentResolver contentResolver = activity.getApplicationContext().getContentResolver();
        Uri imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        Uri imageUri = contentResolver.insert(imageCollection, savedImageDetails);

        try (final OutputStream outputStream = contentResolver.openOutputStream(imageUri)) {
            outputStream.write(rawData);
        }

        return imageUri;
    }

    private Uri saveImagePreAndroidQ(final String filename, final String mimeType, final byte[] rawData) throws IOException {
        File mediaFile = StorageHelper.getMediaFile(filename);
        StorageHelper.storeImageToFile(rawData, mediaFile);

        // Notifies the system an image has been saved so it keeps gallery is up-to-date
        StorageHelper.broadcastImageWasSaved(activity, mediaFile, mimeType);

        return Uri.fromFile(mediaFile);
    }

    private Uri saveTemporaryImage(final String filename, final String mimeType, final byte[] rawData) throws IOException {
        File temporaryImageFile = File.createTempFile(filename, null, activity.getCacheDir());
        StorageHelper.storeImageToFile(rawData, temporaryImageFile);
        return Uri.fromFile(temporaryImageFile);
    }

    private void notifyImageWasSaved(final ImageSavedCallback imageSavedCallback, final Uri savedImage, String savedImageMimeType) {
        if (imageSavedCallback != null) {
            Handler mainHandler = new Handler(activity.getMainLooper());
            mainHandler.post(() -> imageSavedCallback.onImageSaved(savedImage, savedImageMimeType));
        }
    }

    public void openImage() {
        Timber.d("Opening '%s' in browser (or custom tab if supported)", imageUrl);
        ((BaseActivity) activity).openLink(imageUrl);
    }

    public void openExifData() {
        launchExifActivity();
    }

    private void launchExifActivity() {
        Intent intent = new Intent(activity, ExifDetailsActivity.class);
        intent.putExtra(UIConstants.ARG_EXIF_IMAGE, imageUrl);
        activity.startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_up, R.anim.slide_down).toBundle());
    }

    public void shareImage() {
        saveTemporaryImage((savedImage, mimeType) -> {
            String imageFilename = StorageHelper.getFilenameFromUrl(imageUrl);
            Timber.d("Sharing image : '%s'", savedImage);
            ShareCompat.IntentBuilder.from(activity)
                    .setText(activity.getText(R.string.action_share_image))
                    .setType(mimeType)
                    .setSubject(imageFilename)
                    .setStream(savedImage)
                    .startChooser();
        });
    }
}
