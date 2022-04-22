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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.View;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.ShareCompat;

import com.ayuget.redface.R;
import com.ayuget.redface.network.SecureHttpClientFactory;
import com.ayuget.redface.storage.StorageHelper;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.activity.BaseActivity;
import com.ayuget.redface.ui.activity.ExifDetailsActivity;
import com.ayuget.redface.util.ImageUtils;
import com.google.android.material.snackbar.Snackbar;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.functions.Action1;
import timber.log.Timber;

public class ImageMenuHandler {
	public static final String PNG_FILE_EXTENSION = ".png";
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
		saveImage(compressAsPng, true, true, true, null);
	}

	public void saveImage(final boolean compressAsPng, final boolean notifyUser, final boolean broadcastSave, final boolean overrideExisting, final ImageSavedCallback imageSavedCallback) {
		RxPermissions.getInstance(activity)
				.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
				.subscribe(new Action1<Boolean>() {
					@Override
					public void call(Boolean granted) {
						if (granted) {
							String imageOriginalName = StorageHelper.getFilenameFromUrl(imageUrl);
							final Bitmap.CompressFormat targetFormat = compressAsPng ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;

							// When compressing image as PNG, we need to make sure the file extension is ".png"
							final String imageName = compressAsPng ? ImageUtils.replaceExtensionWithPng(imageOriginalName) : imageOriginalName;

							// Images can be already stored locally so they are only downloaded from the network
							// if necessary.
							try {
								final File mediaFile = StorageHelper.getMediaFile(imageName);

								if (mediaFile.exists() && !overrideExisting) {
									Timber.d("Image '%s' already exists, it will not be redownloaded for efficiency reasons", mediaFile.getAbsolutePath());
									notifyImageWasSaved(imageSavedCallback, mediaFile, targetFormat);
								} else {
									saveImageFromNetwork(mediaFile, targetFormat, compressAsPng, notifyUser, broadcastSave, imageSavedCallback);
								}
							} catch (IOException e) {
								Timber.e(e, "Unable to save image to external storage");
								SnackbarHelper.makeError(activity, R.string.error_saving_image).show();
							}
						} else {
							Timber.w("WRITE_EXTERNAL_STORAGE denied, unable to save image");
							SnackbarHelper.makeError(activity, R.string.error_saving_image_permission_denied).show();
						}
					}
				});
	}

	/**
	 * Saves image from network using OkHttp. Glide is not used because it would strip away the
	 * EXIF data once the image is saved (Glide directly gives us a Bitmap).
	 */
	private void saveImageFromNetwork(final File imgMediaFile, final Bitmap.CompressFormat targetFormat, final boolean compressAsPng, final boolean notifyUser, final boolean broadcastSave, final ImageSavedCallback imageSavedCallback) {
		OkHttpClient okHttpClient = SecureHttpClientFactory.newBuilder().build();

		final Request request = new Request.Builder().url(imageUrl).build();
		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				SnackbarHelper.makeError(activity, R.string.error_saving_image).show();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				final byte[] imageBytes = response.body().bytes();

				Timber.d("Image successfully decoded, requesting WRITE_EXTERNAL_STORAGE permission to save image");

				RxPermissions.getInstance(activity)
						.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
						.subscribe(new Action1<Boolean>() {
							@Override
							public void call(Boolean granted) {
								if (granted) {
									Timber.d("WRITE_EXTERNAL_STORAGE granted, saving image to disk");
									File mediaFile = imgMediaFile;

									try {
										Timber.d("Saving image to %s", mediaFile.getAbsolutePath());

										if (compressAsPng) {
											Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
											StorageHelper.storeImageToFile(bitmap, mediaFile, targetFormat);
										} else {
											try {
												StorageHelper.storeImageToFile(imageBytes, mediaFile);
											} catch (IOException e) {
												// if there was no extension for the image, we hardcoded jpg in StorageHelper#getMediaFile() method
												// but it could be another extension, we have no way to guess so let's try one by one...
												final String[] extensions = {".png", ".jpeg", ".gif", ".bmp", ".tif", ".tiff", ".jif", ".jfif", ".jp2", ".svg", ".apng", ".pcd"};
												final String originalMediaFilename = mediaFile.getPath();
												boolean correctExtensionFound = false;
												for (String extension : extensions) {
													if (!correctExtensionFound) {
														mediaFile = new File(originalMediaFilename.replace(".jpg", extension));
														try {
															StorageHelper.storeImageToFile(imageBytes, mediaFile);
															correctExtensionFound = true;
														} catch (IOException e2) {
															Timber.d("IOException when trying to save " + mediaFile);
														}
													}
												}
												if (!correctExtensionFound) {
													throw new IOException("Unable to save " + originalMediaFilename + " (even after trying for several extensions...)");
												}
											}
										}

										if (broadcastSave) {
											// First, notify the system that a new image has been saved
											// to external storage. This is important for user experience
											// because it makes the image visible in the system gallery
											// app.
											StorageHelper.broadcastImageWasSaved(activity, mediaFile, targetFormat);
										}

										if (notifyUser) {
											final String mediaFileAbsolutePath = mediaFile.getAbsolutePath();
											// Then, notify the user with an enhanced snackbar, allowing
											// him (or her) to open the image in his favorite app.
											Snackbar snackbar = SnackbarHelper.makeWithAction(activity, R.string.image_saved_successfully, R.string.action_snackbar_open_image, new View.OnClickListener() {
												@Override
												public void onClick(View v) {
													Intent intent = new Intent();
													intent.setAction(Intent.ACTION_VIEW);
													intent.setDataAndType(Uri.parse("file://" + mediaFileAbsolutePath), "image/*");
													activity.startActivity(intent);
												}
											});
											snackbar.show();
										}

										notifyImageWasSaved(imageSavedCallback, mediaFile, targetFormat);
									} catch (IOException e) {
										Timber.e(e, "Unable to save image to external storage");
										SnackbarHelper.makeError(activity, R.string.error_saving_image).show();
									}
								} else {
									Timber.w("WRITE_EXTERNAL_STORAGE denied, unable to save image");
									SnackbarHelper.makeError(activity, R.string.error_saving_image_permission_denied).show();
								}
							}
						});
			}
		});
	}

	private void notifyImageWasSaved(final ImageSavedCallback imageSavedCallback, final File mediaFile, final Bitmap.CompressFormat targetFormat) {
		if (imageSavedCallback != null) {
			// We need to make sure to call the callback on the
			// main thread.
			Handler mainHandler = new Handler(activity.getMainLooper());
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					imageSavedCallback.onImageSaved(mediaFile, targetFormat);
				}
			});
		}
	}

	public void openImage() {
		Timber.d("Opening '%s' in browser (or custom tab if supported)", imageUrl);
		((BaseActivity) activity).openLink(imageUrl);
	}

	public void openExifData() {
		Intent intent = new Intent(activity, ExifDetailsActivity.class);
		intent.putExtra(UIConstants.ARG_EXIF_IMAGE, imageUrl);
		activity.startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_up, R.anim.slide_down).toBundle());
	}

	public void shareImage() {
		saveImage(false, false, true, true, new ImageSavedCallback() {
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
