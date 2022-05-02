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

package com.ayuget.redface.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.Toolbar;
import androidx.exifinterface.media.ExifInterface;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.misc.ImageMenuHandler;
import com.ayuget.redface.ui.view.ImageDetailsItemView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

@SuppressWarnings("Convert2MethodRef")
public class ExifDetailsActivity extends BaseActivity {
    public static final String FIELD_SEPARATOR = "  ·  ";

    @BindView(R.id.toolbar_actionbar)
    Toolbar toolbar;

    @BindView(R.id.image_attributes)
    LinearLayout imageAttributes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exif_details);

        toolbar.setTitle(R.string.exif_data_title);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent == null) {
            throw new IllegalStateException("No intent passed");
        } else {
            String imageUrl = intent.getStringExtra(UIConstants.ARG_EXIF_IMAGE);

            if (imageUrl == null) {
                throw new IllegalStateException("Invalid intent passed, no image url");
            }

            ImageMenuHandler imageMenuHandler = new ImageMenuHandler(this, imageUrl);
            imageMenuHandler.saveImage(false, false, false, false, (savedImage, format) -> loadExifDetails(savedImage));
        }
        if (savedInstanceState != null) {
            savedInstanceState.clear();
        }
    }

    /**
     * Scans all exif details and appends corresponding views to the activity
     */
    protected void loadExifDetails(File imageFile) {
        try {
            Timber.i("Loading exif details for image '%s'", imageFile.getAbsolutePath());
            ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());

            addDetailIfPresent(imageFile.getName(), extractImageSize(imageFile, exifInterface), R.drawable.ic_photo_white_24dp);
            addDetailIfPresent(extractCameraModel(exifInterface), extractTechnicalDetails(exifInterface), R.drawable.ic_camera_white_24dp);
        } catch (IOException e) {
            Timber.e(e, "Unable to extract EXIF information from file '%s'", imageFile.getAbsolutePath());
        }
    }

    /**
     * Adds an image detail with it's dedicated icon, if both main and secondary texts are present.
     */
    protected void addDetailIfPresent(String mainText, String secondaryText, @DrawableRes int icon) {
        if (mainText != null && secondaryText != null) {
            ImageDetailsItemView detailView = ImageDetailsItemView.from(this)
                    .withMainText(mainText)
                    .withSecondaryText(secondaryText)
                    .withIcon(icon)
                    .build();

            imageAttributes.addView(detailView);
        } else {
            Timber.d("Missing information : mainText = %s, secondaryText = %s", mainText, secondaryText);
        }
    }

    /**
     * Extracts the camera model from the exif informations, or "unknown model" if absent.
     */
    protected String extractCameraModel(ExifInterface exifInterface) {
        String cameraBrand = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
        String cameraModel = exifInterface.getAttribute(ExifInterface.TAG_MODEL);

        if (cameraModel == null) {
            cameraModel = getResources().getString(R.string.unknown_model);
        }


        return cameraBrand == null ? cameraModel : cameraBrand + FIELD_SEPARATOR + cameraModel;

    }

    protected String extractImageSize(File imageFile, ExifInterface exifInterface) {
        String width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
        String height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);

        String dimensions = null;
        String megaPixels = null;

        if (width != null && height != null) {
            dimensions = width + " x " + height;
            megaPixels = Math.round((Long.parseLong(width) * Long.parseLong(height)) / 102400.0) / 10.0 + " MP";
        }

        Timber.d("Image length = %d", imageFile.length());

        String imageSize;
        if (imageFile.length() < 1024 * 1024) {
            imageSize = Math.round(10.0 * imageFile.length() / 1024.0) / 10.0 + " Ko";
        } else {
            imageSize = Math.round(10.0 * imageFile.length() / 1024 * 1024.0) / 10.0 + " Mo";
        }


        List<String> technicalDetails = new ArrayList<>();

        if (megaPixels != null) {
            technicalDetails.add(megaPixels);
        }

        if (dimensions != null) {
            technicalDetails.add(dimensions);
        }

        technicalDetails.add(imageSize);

        if (technicalDetails.size() == 0) {
            return null;
        } else {
            return TextUtils.join(FIELD_SEPARATOR, technicalDetails);
        }
    }

    /**
     * Extracts photo technical details like aperture, focal length, ... Useful for photographers !
     */
    protected String extractTechnicalDetails(ExifInterface exifInterface) {
        String aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE_VALUE);
        String focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        String iso = exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED);
        String exposureTime = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);

        Timber.d("Aperture = %s, Focal Length = %s, ISO = %s, Exposure Time = %s", aperture, focalLength, iso, exposureTime);

        if (iso != null) {
            iso = "ISO" + iso;
        }

        // Focal length is returned as "22/1" instead of "22mm"
        if (focalLength != null) {
            focalLength = focalLength.replace("/1", "") + " mm";
        }

        // Aperture values is returned as "5.600" instead of "f5.6"
        if (aperture != null) {
            int dotPos = aperture.indexOf(".");

            if (dotPos > 0) {
                aperture = "f/" + aperture.substring(0, dotPos + 2);
            } else {
                aperture = "f/" + aperture;
            }
        }

        if (exposureTime != null) {
            exposureTime = "1/" + Math.round((1.0 / Double.parseDouble(exposureTime)));
        }

        List<String> technicalDetails = new ArrayList<>();

        if (aperture != null) {
            technicalDetails.add(aperture);
        }
        if (exposureTime != null) {
            technicalDetails.add(exposureTime);
        }
        if (focalLength != null) {
            technicalDetails.add(focalLength);
        }
        if (iso != null) {
            technicalDetails.add(iso);
        }

        if (technicalDetails.size() == 0) {
            return null;
        } else {
            return TextUtils.join(FIELD_SEPARATOR, technicalDetails);
        }
    }
}
