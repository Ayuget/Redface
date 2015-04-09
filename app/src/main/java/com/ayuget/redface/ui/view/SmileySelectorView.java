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

package com.ayuget.redface.ui.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ayuget.redface.BuildConfig;
import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.SmileySelectedEvent;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.template.SmileysTemplate;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

public class SmileySelectorView extends WebView {
    private static final String LOG_TAG = TopicPageView.class.getSimpleName();

    private boolean initialized;

    @Inject
    MDEndpoints mdEndpoints;

    @Inject
    Bus bus;

    @Inject
    SmileysTemplate smileysTemplate;

    public SmileySelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialized = false;
        setupDependencyInjection(context);
        initialize();
    }

    private void setupDependencyInjection(Context context) {
        RedfaceApp.get(context).inject(this);
    }

    private void initialize() {
        if (initialized) {
            throw new IllegalStateException("View is already initialized");
        }
        else {
            getSettings().setJavaScriptEnabled(true);
            getSettings().setBuiltInZoomControls(false);
            getSettings().setAllowFileAccessFromFileURLs(true);

            if(BuildConfig.DEBUG) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
            }

            addJavascriptInterface(new JsInterface(getContext()), "Android");

            initialized = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        setBackgroundColor(UiUtils.getAppBackgroundColor(getContext()));
        super.onLayout(changed, l, t, r, b);
    }

    public void setSmileys(List<Smiley> smileys) {
        StringBuilder pageBuffer = new StringBuilder();
        smileysTemplate.render(smileys, pageBuffer);
        loadDataWithBaseURL(mdEndpoints.homepage(), pageBuffer.toString(), UIConstants.MIME_TYPE, UIConstants.POSTS_ENCODING, null);
    }

    private class JsInterface {
        Context context;

        private JsInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void addSmiley(final String smileyCode) {
            SmileySelectorView.this.post(new Runnable() {
                @Override
                public void run() {
                    bus.post(new SmileySelectedEvent(smileyCode));
                }
            });
        }
    }

}
