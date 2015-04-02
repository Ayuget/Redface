package com.ayuget.redface.util;

import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebView;

public class JsExecutor {
    /**
     * Executes javascript expression on the webview
     */
    public static void execute(WebView webView, String jsExpression) {
        execute(webView, jsExpression, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                // ignore
            }
        });
    }

    /**
     * Executes javascript expression on the webview and calls the callback with the result
     */
    public static void execute(WebView webView, String jsExpression, ValueCallback<String> resultCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(jsExpression, resultCallback);
        }
        else {
            webView.loadUrl("javascript:" + jsExpression);
        }
    }
}
