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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.view.View;

import com.ayuget.redface.R;
import com.ayuget.redface.storage.StorageHelper;
import com.ayuget.redface.ui.activity.BaseActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;

import rx.functions.Action1;
import timber.log.Timber;

public class ImageMenuHandler {
    private final Activity activity;

    private final String imageUrl;

    public interface ImageSavedCallback {
        void onImageSaved(File savedImage, Bitmap.CompressFormat format);
    }

    public ImageMenuHandler(Activity activity, String imageUrl) {
        this.activity = activity;
        this.imageUrl = imageUrl;
    }

    public void saveImage(boolean compressAsPng) {
        saveImage(compressAsPng, true, null);
    }
    public void saveImage(boolean compressAsPng, final boolean notifyUser, final ImageSavedCallback imageSavedCallback) {
        final String imageName = StorageHelper.getFilenameFromUrl(imageUrl);
        final Bitmap.CompressFormat targetFormat = compressAsPng ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;

        Picasso.with(activity).load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                Timber.d("Image successfully decoded, requesting WRITE_EXTERNAL_STORAGE permission to save image");

                RxPermissions.getInstance(activity)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean granted) {
                                if (granted) {
                                    Timber.d("WRITE_EXTERNAL_STORAGE granted, saving image to disk");

                                    try {
                                        final File mediaFile = StorageHelper.getMediaFile(imageName);
                                        Timber.d("Saving image to %s", mediaFile.getAbsolutePath());

                                        StorageHelper.storeImageToFile(bitmap, mediaFile, targetFormat);

                                        // First, notify the system that a new image has been saved
                                        // to external storage. This is important for user experience
                                        // because it makes the image visible in the system gallery
                                        // app.
                                        StorageHelper.broadcastImageWasSaved(activity, mediaFile, targetFormat);

                                        if (notifyUser) {
                                            // Then, notify the user with an enhanced snackbar, allowing
                                            // him (or her) to open the image in his favorite app.
                                            Snackbar snackbar = SnackbarHelper.makeWithAction(activity, R.string.image_saved_successfully, R.string.action_snackbar_open_image, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Intent.ACTION_VIEW);
                                                    intent.setDataAndType(Uri.parse("file://" + mediaFile.getAbsolutePath()), "image/*");
                                                    activity.startActivity(intent);
                                                }
                                            });
                                            snackbar.show();
                                        }

                                        if (imageSavedCallback != null) {
                                            imageSavedCallback.onImageSaved(mediaFile, targetFormat);
                                        }
                                    }
                                    catch (IOException e) {
                                        Timber.e(e, "Unable to save image to external storage");
                                        SnackbarHelper.makeError(activity, R.string.error_saving_image).show();
                                    }
                                }
                                else {
                                    Timber.w("WRITE_EXTERNAL_STORAGE denied, unable to save image");
                                    SnackbarHelper.makeError(activity, R.string.error_saving_image_permission_denied).show();
                                }
                            }
                        });
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                SnackbarHelper.makeError(activity, R.string.error_saving_image).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    public void openImage() {
        Timber.d("Opening '%s' in browser (or custom tab if supported)", imageUrl);
        ((BaseActivity) activity).openLink(imageUrl);
    }

    public void shareImage() {
        saveImage(false, false, new ImageSavedCallback() {
            @Override
            public void onImageSaved(File savedImage, Bitmap.CompressFormat format) {
                Timber.d("Sharing image : '%s'", savedImage);
                ShareCompat.IntentBuilder.from(activity)
                        .setText(activity.getText(R.string.action_share_image))
                        .setType(StorageHelper.getImageMimeType(format))
                        .setSubject(savedImage.getName())
                        .setStream(Uri.fromFile(savedImage))
                        .startChooser();
            }
        });
    }
}
