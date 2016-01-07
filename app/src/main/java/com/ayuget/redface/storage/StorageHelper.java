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

package com.ayuget.redface.storage;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class StorageHelper {
    private static final String APP_STORAGE_DIR  = "/Redface/";

    /**
     * Saved images quality : 100 = maximum quality
     */
    private static final int SAVED_IMAGES_QUALITY = 100;

    public static File getMediaFile(String filename) throws IOException {
        if (Environment.getExternalStorageDirectory().canWrite()) {
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + APP_STORAGE_DIR);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    throw new IOException("Unable to create media storage directory");
                }
            }

            return new File(mediaStorageDir.getPath() + File.separator + filename);
        }
        else {
            throw new IOException("External storage is not writable");
        }
    }

    public static String removeExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return filename.substring(0, lastDot == -1 ? filename.length() : lastDot);
    }

    public static String getFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    public static void storeImageToFile(Bitmap bitmap, File targetFile, Bitmap.CompressFormat compressFormat) throws IOException {
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(targetFile);
            bitmap.compress(compressFormat, SAVED_IMAGES_QUALITY, fileOutputStream);
        }
        finally {
            if (fileOutputStream != null) { fileOutputStream.close(); }
        }
    }

    public static void broadcastImageWasSaved(Context context, File image, Bitmap.CompressFormat compressFormat) {
        String codec = "image/" + compressFormat.name().toLowerCase();
        MediaScannerConnection.scanFile(context, new String[] { image.getPath() }, new String[] { codec }, null);
    }
}
