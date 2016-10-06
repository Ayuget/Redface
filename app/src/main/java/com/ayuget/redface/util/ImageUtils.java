package com.ayuget.redface.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ayuget.redface.storage.StorageHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import rx.Single;

public class ImageUtils {
    private static final String PNG_FILE_EXTENSION = ".png";
    private static final String COMPRESSED_EXTENSION = ".compressed.png";
    private static final int INITIAL_SAMPLE_SIZE = 2;

    /**
     * Compresses image until its size is inferior the the {@code maxSizeBytes} parameter (in bytes).
     *
     * Input image will not be modified in any way, the compressed version will be stored independently
     * in application's storage directory.
     */
    public static File compressIfNeeded(final File inputImage, final long maxSizeBytes) throws IOException {
        int sampleSize = INITIAL_SAMPLE_SIZE;
        File compressedImage = inputImage;

        while (compressedImage.length() > maxSizeBytes) {
            compressedImage = compress(compressedImage, sampleSize++);
        }

        return compressedImage;
    }

    private static File compress(File inputImage, int sampleSize) throws IOException {
        File compressedImage = StorageHelper.getMediaFile(inputImage.getName() + COMPRESSED_EXTENSION);

        Bitmap imgBitmap = createBitmap(inputImage, sampleSize);

        OutputStream fos = new FileOutputStream(compressedImage);
        imgBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.close();

        return compressedImage;
    }

    private static Bitmap createBitmap(File inputImage, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(inputImage.getAbsolutePath(), options);
    }

    public static String replaceExtensionWithPng(String imageName) {
        int dotIndex = imageName.lastIndexOf('.');

        if (dotIndex >= 0) {
            return imageName.substring(0, imageName.lastIndexOf('.')) + PNG_FILE_EXTENSION;
        }
        else {
            return imageName + PNG_FILE_EXTENSION;
        }
    }
}
