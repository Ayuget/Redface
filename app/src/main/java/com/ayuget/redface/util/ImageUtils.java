package com.ayuget.redface.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ayuget.redface.storage.StorageHelper;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;
import okio.Sink;
import rx.Single;
import rx.exceptions.Exceptions;

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
    public static ByteString compressIfNeeded(final ByteString inputImage, final long maxSizeBytes) throws IOException {
        int sampleSize = INITIAL_SAMPLE_SIZE;
        ByteString compressedImage = inputImage;

        while (compressedImage.size() > maxSizeBytes) {
            compressedImage = compress(compressedImage, sampleSize++);
        }

        return compressedImage;
    }

    private static ByteString compress(ByteString inputImage, int sampleSize) throws IOException {
        Bitmap imgBitmap = createBitmap(inputImage, sampleSize);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imgBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);

        return ByteString.of(outputStream.toByteArray());
    }

    private static Bitmap createBitmap(ByteString inputImage, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        byte[] image = inputImage.toByteArray();
        return BitmapFactory.decodeByteArray(image, 0, image.length);
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

    public static ByteString readStreamFully(InputStream inputStream) {
        try {
            return ByteString.of(ByteStreams.toByteArray(inputStream));
        }
        catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
