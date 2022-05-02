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

package com.ayuget.redface.ui.fragment;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.DataService;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.UrlParser;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicPage;
import com.ayuget.redface.data.api.model.misc.SearchTerms;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.network.DownloadStrategy;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.settings.Blacklist;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.event.BlockUserEvent;
import com.ayuget.redface.ui.event.DisableSearchModeEvent;
import com.ayuget.redface.ui.event.OverriddenPagePosition;
import com.ayuget.redface.ui.event.PageRefreshRequestEvent;
import com.ayuget.redface.ui.event.PageSelectedEvent;
import com.ayuget.redface.ui.event.ScrollToPositionEvent;
import com.ayuget.redface.ui.event.ShowAllSpoilersEvent;
import com.ayuget.redface.ui.event.TopicPageCountUpdatedEvent;
import com.ayuget.redface.ui.event.UnquoteAllPostsEvent;
import com.ayuget.redface.ui.misc.ImageMenuHandler;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.SmileyFavoriteActionResult;
import com.ayuget.redface.ui.misc.SmileyRegistry;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.template.PostsTemplate;
import com.ayuget.redface.ui.view.TopicPageView;
import com.ayuget.redface.util.Connectivity;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@FragmentWithArgs
public class PostsFragment extends BaseFragment {
	private static final String ARG_SAVED_SCROLL_POSITION = "savedScrollPosition";
	private static final String ARG_POSTS_IN_CACHE_HINT = "postsInCacheHint";

	@Arg
	Topic topic;

	@Arg
	boolean isInitialPage;

	@Arg
	int pageNumber;

	@Arg
	PagePosition pageInitialPosition;

	@BindView(R.id.loading_indicator)
	View loadingIndicator;

	@BindView(R.id.error_layout)
	View errorView;

	@BindView(R.id.error_reload_button)
	Button errorReloadButton;

	@BindView(R.id.postsView)
	TopicPageView topicPageView;

	@BindView(R.id.posts_swipe_refresh_layout)
	SwipeRefreshLayout swipeRefreshLayout;

	private ArrayList<Post> displayedPosts = new ArrayList<>();

	@Inject
	DataService dataService;

	@Inject
	UserManager userManager;

	@Inject
	MDService mdService;

	@Inject
	MDEndpoints mdEndpoints;

	@Inject
	Blacklist blacklist;

	@Inject
	RedfaceSettings settings;

	@Inject
	SmileyRegistry smileyRegistry;

	@Inject
	PostsTemplate postsTemplate;

	@Inject
	UrlParser urlParser;

	@Inject
	ThemeManager themeManager;

	@Inject
	HTTPClientProvider httpClientProvider;

	/**
	 * Current scroll position in the webview.
	 */
	private int currentScrollPosition;

	/**
	 * Hint indicating if posts might be in cache.
	 */
	private boolean postsInCacheHint = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debugLog("onCreate");

		if (savedInstanceState == null) {
			currentScrollPosition = -1;
		} else {
			currentScrollPosition = savedInstanceState.getInt(ARG_SAVED_SCROLL_POSITION, -1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
		debugLog("onCreateView");

		final View rootView = inflateRootView(R.layout.fragment_posts, inflater, container);

		topicPageView.setHostActivity(getActivity());

		topicPageView.setAppSettings(settings);
		topicPageView.setBus(bus);
		topicPageView.setPostsTemplate(postsTemplate);
		topicPageView.setMdEndpoints(mdEndpoints);
		topicPageView.setUrlParser(urlParser);
		topicPageView.setThemeManager(themeManager);
		topicPageView.setHttpClientProvider(httpClientProvider);

		if (savedInstanceState != null) {
			debugLog("trying to restore state");
			postsInCacheHint = savedInstanceState.getBoolean(ARG_POSTS_IN_CACHE_HINT, false);
			debugLog("are posts probably in cache ? : %s", postsInCacheHint ? "true" : "false");
		}

		// Default view is the loading indicator
		if (!postsInCacheHint) {
			debugLog("posts probably not in cache, showing loading indicator");
			showLoadingIndicator();
		}

		displayedPosts = new ArrayList<>();

		swipeRefreshLayout.setColorSchemeResources(R.color.theme_primary, R.color.theme_primary_dark);

		if (errorReloadButton != null) {
			errorReloadButton.setOnClickListener(v -> {
				debugLog("Refreshing topic page '%d' for topic %s", pageNumber, topic);
				showLoadingIndicator();
				loadPage();
			});
		}

		if (savedInstanceState != null) {
			savedInstanceState.clear();
		}

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		debugLog("onResume (isInitialPage = %s)", isInitialPage);

		// Deal with long-press actions on images inside the WebView
		setupImagesInteractions();

		swipeRefreshLayout.setOnRefreshListener(() -> refreshPosts(false));

		topicPageView.setOnQuoteListener((TopicFragment) getParentFragment());
		topicPageView.setOnPageLoadedListener(() -> {
			if (currentScrollPosition > 0) {
				restorePageScrollPosition();
			}

			updateQuotedPostsStatus();
		});

		boolean isCurrentPage = ((TopicFragment) getParentFragment()).getCurrentPage() == pageNumber;
		if (isInitialPage || isCurrentPage) {
			boolean hasNoVisiblePosts = displayedPosts == null || displayedPosts.size() == 0;

			// Page is loaded instantly only if it's the initial page requested on topic load. Other
			// pages will be loaded once selected in the ViewPager
			if (hasNoVisiblePosts) {
				if (!postsInCacheHint) {
					debugLog("initial page and posts probably not in cache => show loading indicator");
					showLoadingIndicator();
				}
				loadPage();
			} else {
				updateQuotedPostsStatus();
			}
		} else {
			// If posts are probably in cache, it is safe to try to load them, because it definitely
			// means the page has already been loaded once, hence we won't mess up topic flags.
			if (postsInCacheHint) {
				loadPage();
			} else {
				debugLog("Posts are probably not in cache, and not initial page, waiting for user swipe");
			}
		}
	}

	@Override
	public void onPause() {
		if (isPageCurrentlyActive()) {
			savePageScrollPosition();
		}

		if (topicPageView != null) {
			unregisterForContextMenu(topicPageView);
			topicPageView.setOnPageLoadedListener(null);
			topicPageView.setOnQuoteListener(null);
		}

		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setOnRefreshListener(null);
		}

		if (errorReloadButton != null) {
			errorReloadButton.setOnClickListener(null);
		}

		super.onPause();
	}

	private void savePageScrollPosition() {
		if (topicPageView != null) {
			currentScrollPosition = topicPageView.getScrollY();
			debugLog("saved scroll position = %d", currentScrollPosition);
		}
	}

	private void restorePageScrollPosition() {
		if (topicPageView != null) {
			debugLog("about to restore scroll position (current position = %d)", topicPageView.getScrollY());
			topicPageView.setScrollY(currentScrollPosition);
			debugLog("restored scroll position = %d", currentScrollPosition);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (isPageCurrentlyActive()) {
			debugLog("saving '%d' posts / scrollPosition = '%d'", displayedPosts.size(), currentScrollPosition);
			debugLog("saving posts in cache hint : %s", postsInCacheHint ? "true" : "false");

			outState.putBoolean(ARG_POSTS_IN_CACHE_HINT, postsInCacheHint);
			outState.putInt(ARG_SAVED_SCROLL_POSITION, currentScrollPosition);
		}
	}

	/**
	 * Defer posts loading until current fragment is visible in the ViewPager. Avoids screwing with
	 * forum's read/unread markers.
	 */
	@SuppressWarnings("unused")
	@Subscribe
	public void onPageSelectedEvent(PageSelectedEvent event) {
		if (!isInitialPage && event.topic().id() == topic.id() && event.page() == pageNumber && isVisible()) {
			debugLog("'page selected event' received");

			if (displayedPosts != null && displayedPosts.size() == 0) {
				loadPage();
			}

			OverriddenPagePosition overriddenPagePosition = event.overriddenPagePosition();
			if (overriddenPagePosition != null) {
				if (overriddenPagePosition.shouldScrollToPost()) {
					pageInitialPosition = overriddenPagePosition.targetPost();
				}
				overridePagePosition(event.overriddenPagePosition());
			}

			if (event.activeSearchTerm() == null) {
				disableSearchMode();
			} else {
				enableSearchMode(event.activeSearchTerm());
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe
	public void onPageRefreshRequestEvent(PageRefreshRequestEvent event) {
		if (event.getTopic().id() == topic.id() && isVisible()) {
			boolean isPageConcerned = !event.isPageDefined() || event.getPage() == pageNumber;
			if (isPageConcerned) {
				refreshPosts(true);
			}
		}
	}

	void refreshPosts(boolean showLoadingIndicator) {
		debugLog("Refreshing topic for topic %s", topic);

		dataService.clearPostsCache(topic, pageNumber);

		if (showLoadingIndicator) {
			showLoadingIndicator();
		}

		savePageScrollPosition();
		topicPageView.setScrollY(0);
		loadPage();
	}

	@SuppressWarnings("unused")
	@Subscribe
	public void onShowAllSpoilersEvent(ShowAllSpoilersEvent event) {
		if (event.getTopic().id() == topic.id() && isVisible() && event.getCurrentPage() == pageNumber) {
			debugLog("'show all spoilers event' received");
			topicPageView.showAllSpoilers();
		}
	}

	/**
	 * Event fired by the host {@link com.ayuget.redface.ui.fragment.TopicFragment} to notify
	 * that multi-quote mode has been turned off and that posts UI should be updated accordingly.
	 */
	@SuppressWarnings("unused")
	@Subscribe
	public void onUnquoteAllPostsEvent(UnquoteAllPostsEvent event) {
		if (topicPageView != null) {
			topicPageView.clearQuotedPosts();
		}
	}

	/**
	 * Since we can't (without hacks) retrieve the currently displayed fragment in the viewpager,
	 * another choice is to use the event bus to subscribe to scrolling "events" and change the
	 * scroll position like this.
	 */
	@SuppressWarnings("unused")
	@Subscribe
	public void onScrollToPosition(ScrollToPositionEvent event) {
		if (event.topic().id() == topic.id() && event.page() == pageNumber) {
			OverriddenPagePosition overriddenPagePosition = event.overriddenPagePosition();

			debugLog("Received scroll to position event");
			overridePagePosition(overriddenPagePosition);

			if (event.activeSearchTerm() == null) {
				disableSearchMode();
			} else {
				enableSearchMode(event.activeSearchTerm());
			}
		}
	}

	private void overridePagePosition(OverriddenPagePosition overriddenPagePosition) {
		debugLog("Overriding page position to : %s", overriddenPagePosition);

		if (overriddenPagePosition.shouldScrollToPost()) {
			PagePosition targetPost = overriddenPagePosition.targetPost();
			if (targetPost != null) {
				topicPageView.setPagePosition(targetPost);
			}
		} else {
			Integer targetScrollY = overriddenPagePosition.targetScrollY();
			if (targetScrollY != null) {
				topicPageView.setScrollY(targetScrollY);
			}
		}
	}

	@Subscribe
	public void onDisableSearchModeEvent(DisableSearchModeEvent ignored) {
		disableSearchMode();
	}

	private void disableSearchMode() {
		Timber.d("Disabling search mode");
	}

	private void enableSearchMode(SearchTerms activeSearchTerm) {
		Timber.d("Enabling search mode : %s", activeSearchTerm);
	}

	/**
	 * Event thrown by {@link com.ayuget.redface.ui.view.TopicPageView} to notify
	 * that an user has been blocked.
	 */
	@SuppressWarnings("unused")
	@Subscribe
	public void onBlockUser(final BlockUserEvent event) {
		blacklist.addBlockedAuthor(event.getAuthor());
		SnackbarHelper.makeWithAction(PostsFragment.this, getString(R.string.user_blocked, event.getAuthor()),
				R.string.action_refresh_topic, v -> bus.post(new PageRefreshRequestEvent(topic))).show();
	}

	@SuppressWarnings("unused")
	@Subscribe
	public void onTopicPageCountUpdated(TopicPageCountUpdatedEvent event) {
		if (event.getTopic().id() == topic.id()) {
			topic = topic.withPagesCount(event.getNewPageCount());
		}
	}

	private void showLoadingIndicator() {
		debugLog("Showing loading layout");
		if (errorView != null) {
			errorView.setVisibility(View.GONE);
		}
		if (loadingIndicator != null) {
			loadingIndicator.setVisibility(View.VISIBLE);
		}
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setVisibility(View.GONE);
		}
	}

	private void showErrorView() {
		debugLog("Showing error layout");
		if (errorView != null) {
			errorView.setVisibility(View.VISIBLE);
		}
		if (loadingIndicator != null) {
			loadingIndicator.setVisibility(View.GONE);
		}
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setVisibility(View.GONE);
		}
	}

	private void showPosts() {
		debugLog("Showing posts layout");
		if (errorView != null) {
			errorView.setVisibility(View.GONE);
		}
		if (loadingIndicator != null) {
			loadingIndicator.setVisibility(View.GONE);
		}
		if (swipeRefreshLayout != null) {
			swipeRefreshLayout.setVisibility(View.VISIBLE);
		}
	}

	private boolean isDownloadStrategyMatching(DownloadStrategy strategy) {
		return strategy != DownloadStrategy.NEVER &&
				(strategy == DownloadStrategy.ALWAYS ||
						(strategy == DownloadStrategy.FASTCX && Connectivity.isConnectedFast(getContext())) ||
						(strategy == DownloadStrategy.WIFI && Connectivity.isConnectedWifi(getContext())));
	}

	public void loadPage() {
		debugLog("loading page");
		subscribe(dataService.loadPosts(userManager.getActiveUser(), topic, pageNumber,
				isDownloadStrategyMatching(settings.getImagesStrategy()),
				isDownloadStrategyMatching(settings.getAvatarsStrategy()),
				isDownloadStrategyMatching(settings.getSmileysStrategy()),
				new EndlessObserver<List<Post>>() {
					@Override
					public void onNext(List<Post> posts) {
						postsInCacheHint = true;
						swipeRefreshLayout.setRefreshing(false);

						displayedPosts.clear();
						displayedPosts.addAll(posts);

						debugLog("Done loading page, showing posts (currentScrollPosition=%d)", currentScrollPosition);

						// Scroll position is saved after a refresh or a configuration change. In this case,
						// we do not use the initial page position, since the user may have scrolled the
						// webview. In this case, we wait for the "onPageLoaded" event and restore the saved
						// position manually.
						boolean positionAfterPageLoad = currentScrollPosition == -1;

						TopicPage topicPage = TopicPage.create(topic, pageNumber, posts, pageInitialPosition, positionAfterPageLoad);
						topicPageView.renderPage(topicPage);

						notifyPageLoaded();
						showPosts();
					}

					@Override
					public void onError(Throwable throwable) {
						swipeRefreshLayout.setRefreshing(false);

						Timber.e(throwable, "Error displaying topic '%s'", topic);
						showErrorView();
					}
				}));
	}

	private void notifyPageLoaded() {
		if (displayedPosts.size() == 0) {
			Timber.w("Empty list of posts...");
			return;
		}

		long searchStartPostId = pageInitialPosition.getPostId();
		if (pageInitialPosition.isTop()) {
			searchStartPostId = displayedPosts.get(0).getId();
		} else if (pageInitialPosition.isBottom()) {
			searchStartPostId = displayedPosts.get(displayedPosts.size() - 1).getId();
		}

		TopicFragment topicFragment = (TopicFragment) getParentFragment();
		topicFragment.notifyPageLoaded(pageNumber, searchStartPostId);
	}

	private void updateQuotedPostsStatus() {
		List<Long> quotedPosts = ((TopicFragment) getParentFragment()).getPageQuotedPosts(pageNumber);
		debugLog("already quoted posts for page = %s", quotedPosts);
		if (quotedPosts.size() > 0) {
			topicPageView.setQuotedPosts(quotedPosts);
		}
	}

	private void setupImagesInteractions() {
		registerForContextMenu(topicPageView);
		topicPageView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
			WebView.HitTestResult result = ((WebView) v).getHitTestResult();
			if (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
				final String imageUrl = result.getExtra();

				Timber.w("Image url = " + imageUrl);

				if (mdEndpoints.isSmiliesUrl(imageUrl) && settings.areSmileyActionsEnabled()) {
					createSmileyContextMenu(menu, imageUrl);
				} else if (!imageUrl.contains(mdEndpoints.baseurl())) {
					createRegularImageContextMenu(menu, imageUrl);
				}
			}
		});
	}

	private void createRegularImageContextMenu(ContextMenu contextMenu, String imageUrl) {
		final ImageMenuHandler imageMenuHandler = new ImageMenuHandler(getActivity(), imageUrl);
		MenuItem.OnMenuItemClickListener itemClickListener = item -> {
			switch (item.getItemId()) {
				case R.id.action_save_original_image:
					imageMenuHandler.saveImage(false);
					break;
				case R.id.action_save_image_as_png:
					imageMenuHandler.saveImage(true);
					break;
				case R.id.action_open_image:
					imageMenuHandler.openImage();
					break;
				case R.id.action_share_image:
					imageMenuHandler.shareImage();
					break;
				case R.id.action_exif_data:
					imageMenuHandler.openExifData();
					break;
				default:
					Timber.e("Unknow menu item clicked");
			}

			return true;
		};

		contextMenu.setHeaderTitle(imageUrl);
		getActivity().getMenuInflater().inflate(R.menu.menu_save_image, contextMenu);
		for (int i = 0; i < contextMenu.size(); i++) {
			contextMenu.getItem(i).setOnMenuItemClickListener(itemClickListener);
		}
	}

	private void createSmileyContextMenu(ContextMenu contextMenu, String smileyUrl) {
		Smiley smiley = smileyRegistry.getSmileyFromUrl(smileyUrl);

		if (smiley == null) {
			Timber.w("Unable to find smiley in registry from URL: " + smileyUrl);
			return;
		} else if (mdEndpoints.isBuiltInSmileyUrl(smileyUrl)) {
			return;
		}

		MenuItem.OnMenuItemClickListener itemClickListener = item -> {
			switch (item.getItemId()) {
				case R.id.action_add_smiley_to_favorites:
					addSmileyToFavorites(smiley);
					break;
				case R.id.action_copy_smiley_code:
					UiUtils.copyTextToClipboard(getContext(), smiley.code(), R.string.profile_personal_smilies_code_copied);
					break;
			}

			return true;
		};

		contextMenu.setHeaderTitle(smiley.code());
		getActivity().getMenuInflater().inflate(R.menu.menu_smiley_actions, contextMenu);
		for (int i = 0; i < contextMenu.size(); i++) {
			contextMenu.getItem(i).setOnMenuItemClickListener(itemClickListener);
		}
	}

	private void debugLog(String message, Object... args) {
		Timber.d(String.format(Locale.getDefault(), "[Page: %d, PageInitialPosition: %s, Id: %d] ", pageNumber, pageInitialPosition, System.identityHashCode(this)) + message, args);
	}

	public TopicPageView getTopicPageView() {
		return topicPageView;
	}

	private boolean isPageCurrentlyActive() {
		return ((TopicFragment) getParentFragment()).getCurrentPage() == pageNumber;
	}

	private void addSmileyToFavorites(Smiley smiley) {
		subscribe(mdService.addSmileyToFavorites(userManager.getActiveUser(), smiley).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new EndlessObserver<SmileyFavoriteActionResult>() {
					@Override
					public void onNext(SmileyFavoriteActionResult result) {
						showAddSmileyAsFavoriteResult(result);
					}

					@Override
					public void onError(Throwable throwable) {
						Timber.e(throwable, "Error when adding smiley to favorites");
						SnackbarHelper.makeError(getActivity(), R.string.add_smiley_as_favorite_unknown_error).show();
					}
				}));
	}

	private void showAddSmileyAsFavoriteResult(SmileyFavoriteActionResult result) {
		switch (result) {
			case ADDED_AS_FAVORITE:
				SnackbarHelper.make(getActivity(), R.string.add_smiley_as_favorite_success).show();
				break;
			case NOT_ADDED_MAX_REACHED:
				SnackbarHelper.make(getActivity(), R.string.add_smiley_as_favorite_max_reached_out_error).show();
				break;
			case NOT_ADDED_ALREADY_IN_LIST:
				SnackbarHelper.make(getActivity(), R.string.add_smiley_as_favorite_already_added_error).show();
				break;
			default:
				SnackbarHelper.makeError(getActivity(), R.string.add_smiley_as_favorite_unknown_error).show();
		}
	}
}
