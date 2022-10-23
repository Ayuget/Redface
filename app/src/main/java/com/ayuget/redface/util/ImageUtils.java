package com.ayuget.redface.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okio.ByteString;
import rx.exceptions.Exceptions;

public class ImageUtils {
	private static final int INITIAL_SAMPLE_SIZE = 2;

	/**
	 * Compresses image until its size is inferior the the {@code maxSizeBytes} parameter (in bytes).
	 * <p>
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
		imgBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);

		return ByteString.of(outputStream.toByteArray());
	}

	private static Bitmap createBitmap(ByteString inputImage, int sampleSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;
		byte[] image = inputImage.toByteArray();
		return BitmapFactory.decodeByteArray(image, 0, image.length, options);
	}

	public static ByteString readStreamFully(InputStream inputStream) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[1024];
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();

			return ByteString.of(buffer.toByteArray());
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}

	public static boolean isScopedStorageEnabled() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
	}
}
