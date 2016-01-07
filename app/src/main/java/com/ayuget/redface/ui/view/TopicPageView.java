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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ayuget.redface.BuildConfig;
import com.ayuget.redface.R;
import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.MDLink;
import com.ayuget.redface.data.api.UrlParser;
import com.ayuget.redface.data.api.hfr.HFREndpoints;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.misc.PostAction;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.EditPostEvent;
import com.ayuget.redface.ui.event.GoToPostEvent;
import com.ayuget.redface.ui.event.GoToTopicEvent;
import com.ayuget.redface.ui.event.InternalLinkClickedEvent;
import com.ayuget.redface.ui.event.PageLoadedEvent;
import com.ayuget.redface.ui.event.PageRefreshRequestEvent;
import com.ayuget.redface.ui.event.PostActionEvent;
import com.ayuget.redface.ui.event.QuotePostEvent;
import com.ayuget.redface.ui.event.WritePrivateMessageEvent;
import com.ayuget.redface.ui.misc.DummyGestureListener;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.template.PostsTemplate;
import com.ayuget.redface.util.JsExecutor;
import com.squareup.otto.Bus;
import com.squareup.phrase.Phrase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class TopicPageView extends WebView implements View.OnTouchListener {
    private static final String LOG_TAG = TopicPageView.class.getSimpleName();

    private static final String ARG_QUOTED_MESSAGES = "quoted_messages";

    /**
     * The post currently displayed in the webview. These posts will be encoded to HTML with
     * specific {@link com.ayuget.redface.ui.template.HTMLTemplate} classes.
     */
    private List<Post> posts;

    /**
     * Flag indicating if the webview has already been initialized
     */
    private boolean initialized;

    /**
     * Currently displayed topic
     */
    private Topic topic;

    /**
     * Topic's page currently displayed in the webview
     */
    private int page;

    /**
     * Android framework utility class to detect gestures, used
     * here to detect double-tab.
     */
    private GestureDetector doubleTapGestureDetector;

    /**
     * List of quoted messages, used for multi-quote feature
     */
    private ArrayList<Long> quotedMessages;

    /**
     * Used to display a contextual action bar when multi-quote mode is enabled
     */
    private ActionMode quoteActionMode;

    @Inject PostsTemplate postsTemplate;

    @Inject MDEndpoints mdEndpoints;

    @Inject UrlParser urlParser;

    @Inject Bus bus;

    @Inject ThemeManager themeManager;

    @Inject RedfaceSettings appSettings;

    boolean actionModeIsActive = false;

    boolean wasReloaded = false;

    /**
     * Callback to be invoked when webview is scrolled
     */
    public interface OnScrollListener {
        void onScrolled(int dx, int dy);
    }

    /**
     * Callback to be invoked when multi-quote mode is
     * triggered from the webview (by post actions buttons)
     */
    public interface OnMultiQuoteModeListener {
        void onMultiQuoteModeToggled(boolean active);
        void onPostAdded(long postId);
        void onPostRemoved(long postId);
        void onQuote();
    }

    /**
     * Callback to be invoked when the page is fully rendered
     */
    public interface OnPageLoadedListener {
        void onPageLoaded();
    }

    private OnScrollListener onScrollListener;

    private OnMultiQuoteModeListener onMultiQuoteModeListener;

    private OnPageLoadedListener onPageLoadedListener;

    @SuppressLint("SetJavaScriptEnabled")
    public TopicPageView(Context context) {
        super(context);
        initialized = false;

        setupDependencyInjection(context);
        initialize(context);
    }

    public TopicPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialized = false;
        setupDependencyInjection(context);
        initialize(context);
    }

    private void setupDependencyInjection(Context context) {
        RedfaceApp.get(context).inject(this);
    }

    private void initialize(Context context) {
        if (initialized) {
            throw new IllegalStateException("View is already initialized");
        }
        else {
            quotedMessages = new ArrayList<>();

            // Deal with double-tap to refresh
            doubleTapGestureDetector = new GestureDetector(context, new DummyGestureListener());
            doubleTapGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return false;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (appSettings.isDoubleTapToRefreshEnabled()) {
                        wasReloaded = true;
                        bus.post(new PageRefreshRequestEvent(topic));
                    }
                    return true;
                }

                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {
                    return false;
                }
            });
            setOnTouchListener(this);

            getSettings().setJavaScriptEnabled(true);
            getSettings().setAllowFileAccessFromFileURLs(true);

            // Making the WebView debuggable is insanely useful to debug what's happening in it.
            // Any WebView rendered in the app can then be inspected via "chrome://inspect" URL
            if(BuildConfig.DEBUG) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
            }

            addJavascriptInterface(new JsInterface(getContext()), "Android");

            setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    if (posts.size() > 0) {
                        Log.d(LOG_TAG, String.format("Page Loaded Event fired (page=%d)", page));
                        TopicPageView.this.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!wasReloaded) {
                                    // Triggerring the event will allow the fragment in which this
                                    // webview is contained to initiate page position events
                                    // This has to be triggered only once
                                    bus.post(new PageLoadedEvent(topic, page, TopicPageView.this));
                                }

                                if (onPageLoadedListener != null) {
                                    onPageLoadedListener.onPageLoaded();
                                }
                            }
                        });
                    }
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url != null && url.startsWith(mdEndpoints.baseurl())) {
                        Log.d(LOG_TAG, String.format("Clicked on internal url = '%s'", url));
                        return true;
                    } else {
                        getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    }
                }
            });

            initialized = true;
        }
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public void setOnMultiQuoteModeListener(OnMultiQuoteModeListener onMultiQuoteModeListener) {
        this.onMultiQuoteModeListener = onMultiQuoteModeListener;
    }

    public void setOnPageLoadedListener(OnPageLoadedListener onPageLoadedListener) {
        this.onPageLoadedListener = onPageLoadedListener;
    }

    private void updateActionModeTitle() {
        if (quoteActionMode != null) {
            quoteActionMode.setTitle(Phrase.from(getContext(), R.string.quoted_messages_plural).put("count", quotedMessages.size()).format());
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.onScrollListener != null) {
            this.onScrollListener.onScrolled(oldl - l, oldt - t);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Delegate touch event to the gesture detector
        return doubleTapGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        setBackgroundColor(UiUtils.getAppBackgroundColor(getContext()));
        super.onLayout(changed, l, t, r, b);
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        renderPosts();
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    private void renderPosts() {
        StringBuilder pageBuffer = new StringBuilder();
        postsTemplate.render(this.posts, pageBuffer);

        loadDataWithBaseURL(mdEndpoints.homepage(), pageBuffer.toString(), UIConstants.MIME_TYPE, UIConstants.POSTS_ENCODING, null);
    }

    public void setPagePosition(PagePosition pagePosition) {
        Log.d(LOG_TAG, String.format("setPagePosition called !!! (page=%d)", page));
        if (pagePosition != null) {
            if (pagePosition.isBottom()) {
                scrollToBottom();
            }
            else {
                scrollToPost(pagePosition.getPostId());
            }
        }
    }

    public void scrollToBottom() {
        Log.d(LOG_TAG, "Scrolling to the bottom of the page");
        JsExecutor.execute(this, "scrollToBottom()");
    }

    public void scrollToPost(long postId) {
        JsExecutor.execute(this, String.format("scrollToElement('post%d')", postId));
    }

    private void startMultiQuoteMode() {
        quoteActionMode = startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                actionModeIsActive = true;

                if (quotedMessages.size() > 1) {
                    mode.setTitle(Phrase.from(getContext(), R.string.quoted_messages_plural).put("count", quotedMessages.size()).format());
                } else {
                    mode.setTitle(R.string.quoted_messages);
                }

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_multi_quote, menu);

                if (onMultiQuoteModeListener != null) {
                    onMultiQuoteModeListener.onMultiQuoteModeToggled(true);
                }

                inflater = null; // Force GC

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_multiquote:
                        if (onMultiQuoteModeListener != null) {
                            onMultiQuoteModeListener.onQuote();
                        }
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionModeIsActive = false;
                quotedMessages.clear();
                JsExecutor.execute(TopicPageView.this, "clearQuotedMessages()");

                if (onMultiQuoteModeListener != null) {
                    onMultiQuoteModeListener.onMultiQuoteModeToggled(false);
                }
            }
        });
    }

    /**
     * Disables view current batch mode, if active
     */
    public void disableBatchActions() {
        if (quoteActionMode != null) {
            quoteActionMode.finish();
        }

        if (onMultiQuoteModeListener != null) {
            onMultiQuoteModeListener.onMultiQuoteModeToggled(false);
        }
    }

    private class JsInterface {
        Context context;

        private JsInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void quotePost(final int postId) {
            Log.d(LOG_TAG, String.format("Quoting post '%d'", postId));
            TopicPageView.this.post(new Runnable() {
                @Override
                public void run() {
                    bus.post(new QuotePostEvent(topic, postId));
                }
            });
        }

        @JavascriptInterface
        public void toggleQuoteStatus(final long postId) {
            Log.d(LOG_TAG, String.format("Toggling quote status for post '%d'", postId));

            if (quotedMessages.contains(postId)) {
                quotedMessages.remove(postId);
                if (onMultiQuoteModeListener != null) {
                    onMultiQuoteModeListener.onPostRemoved(postId);
                }
            }
            else {
                quotedMessages.add(postId);
                if (onMultiQuoteModeListener != null) {
                    onMultiQuoteModeListener.onPostAdded(postId);
                }
            }

            if (quotedMessages.size() == 0 && quoteActionMode != null) {
                TopicPageView.this.post(new Runnable() {
                    @Override
                    public void run() {
                        quoteActionMode.finish();
                        quoteActionMode = null;
                        if (onMultiQuoteModeListener != null) {
                            onMultiQuoteModeListener.onMultiQuoteModeToggled(false);
                        }
                    }
                });
            }
            else {
                if (! actionModeIsActive) {
                    TopicPageView.this.post(new Runnable() {
                        @Override
                        public void run() {
                            startMultiQuoteMode();
                        }
                    });
                }
                else {
                    TopicPageView.this.post(new Runnable() {
                        @Override
                        public void run() {
                            updateActionModeTitle();
                            }

                    });
                }
            }
        }

        @JavascriptInterface
        public void editPost(final int postId) {
            Log.d(LOG_TAG, String.format("Editing post '%d'", postId));
            TopicPageView.this.post(new Runnable() {
                @Override
                public void run() {
                    bus.post(new EditPostEvent(topic, postId));
                }
            });
        }

        @JavascriptInterface
        public void markPostAsFavorite(final int postId) {
            Log.d(LOG_TAG, String.format("Marking post '%d' as favorite", postId));
            TopicPageView.this.post(new Runnable() {
                @Override
                public void run() {
                    bus.post(new PostActionEvent(PostAction.FAVORITE, topic, postId));
                }
            });
        }

        @JavascriptInterface
        public void deletePost(final int postId) {
            Log.d(LOG_TAG, String.format("Deleting post '%d'", postId));
            TopicPageView.this.post(new Runnable() {
                @Override
                public void run() {
                    bus.post(new PostActionEvent(PostAction.DELETE, topic, postId));
                }
            });
        }

        @JavascriptInterface
        public void writePrivateMessage(final int postId) {
            for (final Post post : posts) {
                if (post.getId() == postId) {
                    TopicPageView.this.post(new Runnable() {
                        @Override
                        public void run() {
                            bus.post(new WritePrivateMessageEvent(post.getAuthor()));
                        }
                    });
                }
            }
        }

        @JavascriptInterface
        public void showProfile (String username){
                Log.d(LOG_TAG, String.format("Profile requested for user '%s'", username));
        }

        @JavascriptInterface
        public void handleUrl(final int postId, final String url) {
            Log.d(LOG_TAG, String.format("Clicked on internal url = '%s' (postId = %d)", url, postId));

            TopicPageView.this.post(new Runnable() {
                @Override
                public void run() {
                    bus.post(new InternalLinkClickedEvent(topic, page, new PagePosition(postId)));
                }
            });

            urlParser.parseUrl(url).ifTopicLink(new MDLink.IfIsTopicLink() {
                @Override
                public void call(final Category category, final int topicId, final int topicPage, final PagePosition pagePosition) {
                    TopicPageView.this.post(new Runnable() {
                        @Override
                        public void run() {
                            // Action can take a few seconds to process, depending on target and on network quality,
                            // we need to do something to indicate that we handled the event
                            if (topicId != topic.getId()) {
                                Toast.makeText(getContext(), R.string.topic_loading_message, Toast.LENGTH_SHORT).show();
                            }

                            if (topic.getId() == topicId) {
                                int destinationPage = topicPage;
                                PagePosition targetPagePosition = pagePosition;

                                // Hack needed because we are hiding the first post of a page, which is equal
                                // to the last post of previous page.
                                if (!appSettings.showPreviousPageLastPost() && destinationPage > 1 && posts.size() > 0 && topicPage == page && pagePosition.getPostId() < posts.get(0).getId()) {
                                    targetPagePosition = new PagePosition(PagePosition.BOTTOM);
                                    destinationPage -= 1;
                                }

                                bus.post(new GoToPostEvent(destinationPage, targetPagePosition, TopicPageView.this));
                            } else {
                                bus.post(new GoToTopicEvent(category, topicId, topicPage, pagePosition));
                            }
                        }
                    });
                }
            }).ifInvalid(new MDLink.IfIsInvalidLink() {
                @Override
                public void call() {
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
        }
    }
}
