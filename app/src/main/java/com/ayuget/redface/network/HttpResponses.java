package com.ayuget.redface.network;

import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import okhttp3.Response;

public class HttpResponses {

	public static WebResourceResponse newErrorResponse() {
		try {
			ByteArrayInputStream errorStream = new ByteArrayInputStream("Unknown error".getBytes("UTF-8"));
			return new WebResourceResponse("text/plain", "UTF-8", errorStream);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding: 'UTF-8'");
		}
	}

	public static String getHeaderOrDefault(Response response, String headerName, String defaultValue) {
		String headerValue = response.headers().get(headerName);

		if (headerValue == null) {
			return defaultValue;
		} else {
			return headerValue;
		}
	}
}
