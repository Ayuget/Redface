package com.ayuget.redface.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ayuget.redface.BuildConfig;
import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.R;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.MDLink;
import com.ayuget.redface.data.api.UrlParser;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.EditPostEvent;
import com.ayuget.redface.ui.event.GoToPostEvent;
import com.ayuget.redface.ui.event.GoToTopicEvent;
import com.ayuget.redface.ui.event.InternalLinkClickedEvent;
import com.ayuget.redface.ui.event.PageLoadedEvent;
import com.ayuget.redface.ui.event.QuotePostEvent;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.template.PostsTemplate;
import com.ayuget.redface.util.JsExecutor;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;


public class TopicPageView extends WebView {
    private static final String LOG_TAG = TopicPageView.class.getSimpleName();

    private List<Post> posts;

    private boolean initialized;

    private Topic topic;

    private int page;

    @Inject PostsTemplate postsTemplate;

    @Inject MDEndpoints mdEndpoints;

    @Inject UrlParser urlParser;

    @Inject Bus bus;

    @Inject ThemeManager themeManager;

    public interface OnScrollListener {
        public void onScrolled(int dx, int dy);
    }

    private OnScrollListener onScrollListener;

    @SuppressLint("SetJavaScriptEnabled")
    public TopicPageView(Context context) {
        super(context);
        initialized = false;

        setupDependencyInjection(context);
        initialize();
    }

    public TopicPageView(Context context, AttributeSet attrs) {
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
            getSettings().setAllowFileAccessFromFileURLs(true);

            if(BuildConfig.DEBUG) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
            }

            addJavascriptInterface(new JsInterface(getContext()), "Android");

            setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    TopicPageView.this.post(new Runnable() {
                        @Override
                        public void run() {
                            bus.post(new PageLoadedEvent(topic, page, TopicPageView.this));
                        }
                    });
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url != null && url.startsWith(mdEndpoints.baseurl())) {
                        Log.d(LOG_TAG, String.format("Clicked on internal url = '%s'", url));
                        urlParser.parseUrl(url);
                        return true;
                    }
                    else {
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

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.onScrollListener != null) {
            this.onScrollListener.onScrolled(oldl - l, oldt - t);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        setBackgroundColor(UiUtils.getAppBackgroundColor(getContext()));
        super.onLayout(changed, l, t, r, b);
    }

    public void setPosts(List<Post> posts) {
        Log.d(LOG_TAG, String.format("setPosts(posts.size() = %d", posts.size()));
        this.posts = posts;
        renderPosts();
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @DebugLog
    private void renderPosts() {
        StringBuilder pageBuffer = new StringBuilder();
        postsTemplate.render(this.posts, pageBuffer);

        loadDataWithBaseURL(mdEndpoints.homepage(), pageBuffer.toString(), UIConstants.MIME_TYPE, UIConstants.POSTS_ENCODING, null);
    }

    public void setPagePosition(PagePosition pagePosition) {
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
        public void showProfile(String username) {
            Log.d(LOG_TAG, String.format("Profile requested for user '%s'", username));
        }

        @JavascriptInterface
        public void handleUrl(final int postId, String url) {
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
                            if (topic.getId() == topicId) {
                                int destinationPage = topicPage;
                                PagePosition targetPagePosition = pagePosition;

                                // Hack needed because we are hiding the first post of a page, which is equal
                                // to the last post of previous page.
                                if (destinationPage > 1 && posts.size() > 0 && topicPage == page && pagePosition.getPostId() < posts.get(0).getId()) {
                                    targetPagePosition = new PagePosition(PagePosition.BOTTOM);
                                    destinationPage -= 1;
                                }

                                bus.post(new GoToPostEvent(destinationPage, targetPagePosition, TopicPageView.this));
                            }
                            else {
                                bus.post(new GoToTopicEvent(category, topicId, topicPage, pagePosition));
                            }
                        }
                    });
                }
            });
        }
    }
}
