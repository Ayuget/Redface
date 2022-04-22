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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ayuget.redface.BuildConfig;
import com.ayuget.redface.R;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.UrlParser;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.TopicPage;
import com.ayuget.redface.data.api.model.misc.PostAction;
import com.ayuget.redface.data.rx.RxUtils;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.network.HttpResponses;
import com.ayuget.redface.network.SecureHttpClientFactory;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.activity.BaseActivity;
import com.ayuget.redface.ui.event.BlockUserEvent;
import com.ayuget.redface.ui.event.EditPostEvent;
import com.ayuget.redface.ui.event.GoToPostEvent;
import com.ayuget.redface.ui.event.GoToTopicEvent;
import com.ayuget.redface.ui.event.InternalLinkClickedEvent;
import com.ayuget.redface.ui.event.PageRefreshRequestEvent;
import com.ayuget.redface.ui.event.PostActionEvent;
import com.ayuget.redface.ui.event.QuotePostEvent;
import com.ayuget.redface.ui.event.ReportPostEvent;
import com.ayuget.redface.ui.event.ViewUserProfileEvent;
import com.ayuget.redface.ui.event.WritePrivateMessageEvent;
import com.ayuget.redface.ui.misc.DummyGestureListener;
import com.ayuget.redface.ui.misc.NestedScrollingWebView;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.template.PostsTemplate;
import com.ayuget.redface.util.JsExecutor;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class TopicPageView extends NestedScrollingWebView implements View.OnTouchListener {
	/**
	 * Currently displayed topic page
	 */
	TopicPage topicPage;

	/**
	 * Flag indicating if the webview has already been initialized
	 */
	private boolean initialized;

	/**
	 * Activity in which the view is hosted
	 */
	private Activity hostActivity;

	/**
	 * Android framework utility class to detect gestures, used
	 * here to detect double-tab.
	 */
	private GestureDetector doubleTapGestureDetector;

	/**
	 * List of quoted messages, used for multi-quote feature
	 */
	private ArrayList<Long> quotedMessages;

	private HTTPClientProvider httpClientProvider;

	PostsTemplate postsTemplate;
	MDEndpoints mdEndpoints;
	UrlParser urlParser;
	Bus bus;
	ThemeManager themeManager;
	RedfaceSettings appSettings;

	boolean wasReloaded = false;

	/**
	 * Callback to be invoked when a post is quoted (or un-quoted) in multi-quote mode
	 */
	public interface OnQuoteListener {
		void onPostQuoted(int page, long postId);

		void onPostUnquoted(int page, long postId);
	}

	/**
	 * Callback to be invoked when the page is fully rendered
	 */
	public interface OnPageLoadedListener {
		void onPageLoaded();
	}

	private OnQuoteListener onQuoteListener;
	private OnPageLoadedListener onPageLoadedListener;

	@SuppressLint("SetJavaScriptEnabled")
	public TopicPageView(Context context) {
		super(context);
		initialized = false;
		initialize(context);
	}

	public TopicPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialized = false;
		initialize(context);
	}

	// It is safe here to use Javascript interface because we control entirely what's loaded in the
	// webview (javascript is bundled inside the app and not loaded externally)
	@SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
	private void initialize(Context context) {
		if (initialized) {
			throw new IllegalStateException("View is already initialized");
		} else {
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
						bus.post(new PageRefreshRequestEvent(topicPage.topic(), topicPage.page()));
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
			getSettings().setBuiltInZoomControls(true);
			getSettings().setDisplayZoomControls(false);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
			}

			// Making the WebView debuggable is insanely useful to debug what's happening in it.
			// Any WebView rendered in the app can then be inspected via "chrome://inspect" URL
			if (BuildConfig.DEBUG) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					WebView.setWebContentsDebuggingEnabled(true);
				}
			}

			addJavascriptInterface(new JsInterface(getContext()), "Android");

			setWebViewClient(new WebViewClient() {
				private final OkHttpClient httpClient = SecureHttpClientFactory.newBuilder()
						.build();

				@Override
				public void onPageFinished(WebView view, String url) {
					if (onPageLoadedListener != null && url.startsWith(mdEndpoints.baseurl())) {
						Timber.d("URL=%s, Page is done loading, notifying listeners", url);
						onPageLoadedListener.onPageLoaded();
					}
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url != null && url.startsWith(mdEndpoints.baseurl())) {
						Timber.d("Clicked on internal url = '%s'", url);
						return true;
					} else {
						((BaseActivity) hostActivity).openLink(url);
						return true;
					}
				}

				@Nullable
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
					boolean androidNOrLater = Build.VERSION.SDK_INT >= 24;

					if (androidNOrLater || shouldBeHandledByWebView(url)) {
						return super.shouldInterceptRequest(view, url);
					} else {
						// A remote URL in Android M or before
						return handleRequestViaOkHttp(url);
					}
				}

				private boolean shouldBeHandledByWebView(String url) {
					return url.startsWith("file:");
				}

				/**
				 * Fetch remote resource using a secure OkHttp client, with the
				 * manually added Let's Encrypt Root certificates. Necessary because
				 * the built-in LE certificates have expired.
				 */
				private WebResourceResponse handleRequestViaOkHttp(String url) {
					try {
						Call call = httpClient.newCall(new Request.Builder()
								.url(url)
								.build()
						);

						final Response response = call.execute();

						// Response body should not be null
						// noinspection ConstantConditions
						return new WebResourceResponse(
								HttpResponses.getHeaderOrDefault(response, "Content-Type", ""),
								HttpResponses.getHeaderOrDefault(response, "Content-Encoding", "utf-8"),
								response.body().byteStream()
						);
					} catch (Exception e) {
						return HttpResponses.newErrorResponse();
					}
				}
			});

			initialized = true;
		}
	}

	public void setPostsTemplate(PostsTemplate postsTemplate) {
		this.postsTemplate = postsTemplate;
	}

	public void setMdEndpoints(MDEndpoints mdEndpoints) {
		this.mdEndpoints = mdEndpoints;
	}

	public void setUrlParser(UrlParser urlParser) {
		this.urlParser = urlParser;
	}

	public void setBus(Bus bus) {
		this.bus = bus;
	}

	public void setThemeManager(ThemeManager themeManager) {
		this.themeManager = themeManager;
	}

	public void setAppSettings(RedfaceSettings appSettings) {
		this.appSettings = appSettings;
	}

	public void setOnQuoteListener(OnQuoteListener onQuoteListener) {
		this.onQuoteListener = onQuoteListener;
	}

	public void setOnPageLoadedListener(OnPageLoadedListener onPageLoadedListener) {
		this.onPageLoadedListener = onPageLoadedListener;
	}

	public void setHostActivity(Activity hostActivity) {
		this.hostActivity = hostActivity;
	}

	public void setHttpClientProvider(HTTPClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
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

	public void renderPage(TopicPage topicPage) {
		this.topicPage = topicPage;

		// Create page HTML buffer from template
		StringBuilder pageBuffer = new StringBuilder();
		postsTemplate.render(topicPage, pageBuffer);

		loadDataWithBaseURL(mdEndpoints.homepage(), pageBuffer.toString(), UIConstants.MIME_TYPE, UIConstants.POSTS_ENCODING, null);
	}

	public void setPagePosition(@NonNull PagePosition pagePosition) {
		Timber.d("Page position = %s", pagePosition);
		if (pagePosition.isBottom()) {
			scrollToBottom();
		} else if (pagePosition.isTop()) {
			scrollToTop();
		} else {
			scrollToPost(pagePosition.getPostId());
		}
	}

	public void scrollToBottom() {
		Timber.d("Scrolling to the bottom of the page");
		JsExecutor.execute(this, "scrollToBottom()");
	}

	public void scrollToTop() {
		Timber.d("Scrolling to the top of the page");
		JsExecutor.execute(this, "scrollToTop()");
	}

	public void scrollToPost(long postId) {
		JsExecutor.execute(this, String.format(Locale.getDefault(), "scrollToElement('post%d')", postId));
	}

	public void showAllSpoilers() {
		Timber.d("Showing all spoilers for current page");
		JsExecutor.execute(this, "showAllSpoilers()");
	}

	/**
	 * Sets the posts that are currently quoted
	 */
	public void setQuotedPosts(List<Long> posts) {
		Timber.d("Settings quoted posts for page %d (quoted count = %d)", topicPage.page(), posts.size());
		quotedMessages.clear();
		quotedMessages.addAll(posts);

		JsExecutor.execute(this, "setPostsAsQuoted([" + TextUtils.join(",", posts) + "])");
	}

	/**
	 * Unquotes all previously quoted posts for the given page
	 */
	public void clearQuotedPosts() {
		Timber.d("Clearing quoted posts");
		quotedMessages.clear();
		JsExecutor.execute(this, "clearQuotedMessages()");
	}

	@SuppressWarnings("unused") // Methods are not unused, they are called by Javascript code
	private class JsInterface {
		Context context;

		private JsInterface(Context context) {
			this.context = context;
		}

		@JavascriptInterface
		public void quotePost(final int postId) {
			Timber.d("Quoting post '%d'", postId);
			TopicPageView.this.post(() -> bus.post(new QuotePostEvent(topicPage.topic(), postId)));
		}

		@JavascriptInterface
		public void toggleQuoteStatus(final long postId) {
			Timber.d("Toggling quote status for post '%d'", postId);

			if (quotedMessages.contains(postId)) {
				quotedMessages.remove(postId);
				if (onQuoteListener != null) {
					TopicPageView.this.post(() -> onQuoteListener.onPostUnquoted(topicPage.page(), postId));
				}
			} else {
				quotedMessages.add(postId);
				if (onQuoteListener != null) {
					TopicPageView.this.post(() -> onQuoteListener.onPostQuoted(topicPage.page(), postId));
				}
			}
		}

		@JavascriptInterface
		public void editPost(final int postId) {
			Timber.d("Editing post '%d'", postId);
			TopicPageView.this.post(() -> bus.post(new EditPostEvent(topicPage.topic(), postId)));
		}

		@JavascriptInterface
		public void markPostAsFavorite(final int postId) {
			Timber.d("Marking post '%d' as favorite", postId);
			TopicPageView.this.post(() -> bus.post(new PostActionEvent(PostAction.FAVORITE, topicPage.topic(), postId)));
		}

		@JavascriptInterface
		public void deletePost(final int postId) {
			Timber.d("Deleting post '%d'", postId);
			TopicPageView.this.post(() -> bus.post(new PostActionEvent(PostAction.DELETE, topicPage.topic(), postId)));
		}

		@JavascriptInterface
		public void viewUserProfile(final int postId) {
			Timber.d("View user profile for post '%d'", postId);
			for (final Post post : topicPage.posts()) {
				if (post.getId() == postId && post.getAuthorId() != null) {
					TopicPageView.this.post(() -> bus.post(new ViewUserProfileEvent(post.getAuthorId())));
				}
			}
		}

		@JavascriptInterface
		public void writePrivateMessage(final int postId) {
			for (final Post post : topicPage.posts()) {
				if (post.getId() == postId) {
					TopicPageView.this.post(() -> bus.post(new WritePrivateMessageEvent(post.getAuthor())));
				}
			}
		}

		@JavascriptInterface
		public void reportPost(final int postId) {
			TopicPageView.this.post(() -> bus.post(new ReportPostEvent(topicPage.topic(), postId)));
		}

		@JavascriptInterface
		public void blockUser(final int postId) {
			for (final Post post : topicPage.posts()) {
				if (post.getId() == postId) {
					TopicPageView.this.post(() -> bus.post(new BlockUserEvent(post.getAuthor())));
				}
			}
		}

		@JavascriptInterface
		public void copyLinkToPost(final int postId) {
			Timber.d("Copying link to post '%d' in clipboard", postId);
			TopicPageView.this.post(() -> UiUtils.copyLinkToClipboard(getContext(), mdEndpoints.post(topicPage.topic().category(), topicPage.topic(), topicPage.page(), postId)));
		}

		@JavascriptInterface
		public void handleUrl(final int postId, final String url) {
			Timber.d("Clicked on internal url = '%s' (postId = %d)", url, postId);

			TopicPageView.this.post(() -> bus.post(new InternalLinkClickedEvent(topicPage.topic(), topicPage.page(), new PagePosition(postId))));

			urlParser.parseUrl(url).compose(RxUtils.applySchedulers())
					.subscribe(mdLink -> mdLink.ifTopicLink((category, topicId, targetPage, pagePosition) -> {
						// Action can take a few seconds to process, depending on target and on network quality,
						// we need to do something to indicate that we handled the event
						if (topicId != topicPage.topic().id()) {
							Toast.makeText(getContext(), R.string.topic_loading_message, Toast.LENGTH_SHORT).show();
						}

						if (topicPage.topic().id() == topicId) {
							int destinationPage = targetPage;
							PagePosition targetPagePosition = pagePosition;

							// Hack needed because we are hiding the first post of a page, which is equal
							// to the last post of previous page.
							if (!appSettings.showPreviousPageLastPost() && destinationPage > 1 && topicPage.posts().size() > 0 && targetPage == topicPage.page() && pagePosition.getPostId() < topicPage.posts().get(0).getId()) {
								targetPagePosition = new PagePosition(PagePosition.BOTTOM);
								destinationPage -= 1;
							}

							bus.post(new GoToPostEvent(destinationPage, targetPagePosition, TopicPageView.this));
						} else {
							bus.post(new GoToTopicEvent(category, topicId, targetPage, pagePosition));
						}
					}).ifInvalid(() -> {
						getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					}));
		}
	}
}
