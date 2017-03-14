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
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.ayuget.redface.R;
import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.DataService;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.network.DownloadStrategy;
import com.ayuget.redface.settings.Blacklist;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.event.BlockUserEvent;
import com.ayuget.redface.ui.event.PageRefreshRequestEvent;
import com.ayuget.redface.ui.event.PageSelectedEvent;
import com.ayuget.redface.ui.event.ScrollToPostEvent;
import com.ayuget.redface.ui.event.ShowAllSpoilersEvent;
import com.ayuget.redface.ui.event.TopicPageCountUpdatedEvent;
import com.ayuget.redface.ui.event.UnquoteAllPostsEvent;
import com.ayuget.redface.ui.misc.ImageMenuHandler;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.ui.view.TopicPageView;
import com.ayuget.redface.util.Connectivity;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import timber.log.Timber;

@FragmentWithArgs
public class PostsFragment extends BaseFragment {
    private static final String ARG_POST_LIST = "post_list";

    private static final String ARG_TOPIC = "topic";

    private static final String ARG_SAVED_SCROLL_POSITION = "savedScrollPosition";

    @Arg
    Topic topic;

    @Arg
    int currentPage;

    @Arg
    int initialPage;

    @InjectView(R.id.loading_indicator)
    View loadingIndicator;

    @InjectView(R.id.error_layout)
    View errorView;

    @InjectView(R.id.error_reload_button)
    Button errorReloadButton;

    @InjectView(R.id.postsView)
    TopicPageView topicPageView;

    @InjectView(R.id.topic_list_swipe_refresh_layout)
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

    /**
     * Current scroll position in the webview.
     */
    private int currentScrollPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Timber.d("@%d -> Fragment(currentPage=%d) -> onCreate", System.identityHashCode(this), currentPage);

        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            currentScrollPosition = 0;
        }
        else {
            currentScrollPosition = savedInstanceState.getInt(ARG_SAVED_SCROLL_POSITION, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        Timber.d("@%d -> Fragment(currentPage=%d) -> onCreateView(page=%d)", System.identityHashCode(this), currentPage, currentPage);

        final View rootView = inflateRootView(R.layout.fragment_posts, inflater, container);

        topicPageView.setHostActivity(getActivity());

        // Default view is the loading indicator
        showLoadingIndicator();

        // Restore the list of posts when the fragment is recreated by the framework
        boolean restoredPosts = false;

        if (savedInstanceState != null) {
            Timber.d("@%d -> Fragment(currentPage=%d) -> trying to restore state", System.identityHashCode(this), currentPage);
            displayedPosts = savedInstanceState.getParcelableArrayList(ARG_POST_LIST);
            if (displayedPosts != null) {
                Timber.d("@%d -> Fragment(currentPage=%d) -> Restored %d posts to fragment", System.identityHashCode(this), displayedPosts.size(), currentPage);
                restoredPosts = displayedPosts.size() > 0;
            }
        }

        if (displayedPosts == null) {
            displayedPosts = new ArrayList<>();
        }
        else if (displayedPosts.size() > 0){
            topicPageView.setTopic(topic);
            topicPageView.setPage(currentPage);
            topicPageView.setPosts(displayedPosts);
            showPosts();
        }

        // Implement swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            savePageScrollPosition();
            Timber.d("Refreshing topic page '%d' for topic %s", currentPage, topic);
            loadPage(currentPage);
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_primary, R.color.theme_primary_dark);

        if (errorReloadButton != null) {
            errorReloadButton.setOnClickListener(v -> {
                Timber.d("Refreshing topic page '%d' for topic %s", currentPage, topic);
                showLoadingIndicator();
                loadPage(currentPage);
            });
        }

        // Deal with long-press actions on images inside the WebView
        setupImagesInteractions();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        topicPageView.setOnQuoteListener((TopicFragment)getParentFragment());
        topicPageView.setOnPageLoadedListener(() -> {
            if (currentScrollPosition > 0) {
                restorePageScrollPosition();
            }

            updateQuotedPostsStatus();
        });

        boolean hasLoadedPosts = displayedPosts != null && displayedPosts.size() > 0;
        boolean hasNoVisiblePosts = displayedPosts != null && displayedPosts.size() == 0;

        // Page is loaded instantly only if it's the initial page requested on topic load. Other
        // pages will be loaded once selected in the ViewPager
        if (isInitialPage() && (hasNoVisiblePosts || displayedPosts == null)) {
            showLoadingIndicator();
            loadPage(currentPage);
        }
        else if (hasLoadedPosts){
            updateQuotedPostsStatus();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        savePageScrollPosition();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (topicPageView != null) {
            unregisterForContextMenu(topicPageView);

            topicPageView.setOnScrollListener(null);
            topicPageView.setOnPageLoadedListener(null);

            // Unregister parent fragment as quote listener to avoid memory leaks
            topicPageView.setOnQuoteListener(null);

            topicPageView.removeAllViews();
            topicPageView.destroy();

            RefWatcher refWatcher = RedfaceApp.getRefWatcher(getActivity());
            refWatcher.watch(topicPageView);
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(null);
        }

        if (errorReloadButton != null) {
            errorReloadButton.setOnClickListener(null);
        }
    }

    private void savePageScrollPosition() {
        if (topicPageView != null) {
            currentScrollPosition = topicPageView.getScrollY();
            Timber.d("Saved scroll position = %d (currentPage=%d)", currentScrollPosition, currentPage);
        }
    }

    private void restorePageScrollPosition() {
        if (topicPageView != null) {
            topicPageView.setScrollY(currentScrollPosition);
            Timber.d("Restored scroll position = %d (currentPage=%d)", currentScrollPosition, currentPage);
        }
    }

    private boolean isInitialPage() {
        return initialPage == currentPage;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("@%d -> Fragment(currentPage=%d) Saving '%d' posts / scrollPosition = '%d'", System.identityHashCode(this), currentPage, displayedPosts.size(), currentScrollPosition);

        outState.putParcelableArrayList(ARG_POST_LIST, displayedPosts);
        outState.putInt(ARG_SAVED_SCROLL_POSITION, currentScrollPosition);
    }

    /**
     * Defer posts loading until current fragment is visible in the ViewPager. Avoids screwing with
     * forum's read/unread markers.
     */
    @Subscribe public void onPageSelectedEvent(PageSelectedEvent event) {
        if (! isInitialPage() && event.getTopic().id() == topic.id() && event.getPage() == currentPage && isVisible()) {
            Timber.d("@%d -> Fragment(currentPage=%d) received event for page %d selected", System.identityHashCode(this), currentPage, event.getPage());

            if (displayedPosts != null && displayedPosts.size() == 0) {
                loadPage(currentPage);
            }
        }
    }

    @Subscribe public void onPageRefreshRequestEvent(PageRefreshRequestEvent event) {
        if (event.getTopic().id() == topic.id() && isVisible()) {
            Timber.d("@%d -> Fragment(currentPage=%d) -> Refresh requested event", System.identityHashCode(this), currentPage);

            savePageScrollPosition();
            showLoadingIndicator();
            loadPage(currentPage);
        }
    }

    @Subscribe public void onShowAllSpoilersEvent(ShowAllSpoilersEvent event) {
        if (event.getTopic().id() == topic.id() && isVisible() && event.getCurrentPage() == currentPage) {
            Timber.d("@%d -> Fragment(currentPage=%d) -> Show all spoilers event", System.identityHashCode(this), currentPage);
            topicPageView.showAllSpoilers();
        }
    }

    /**
     * Event fired by the host {@link com.ayuget.redface.ui.fragment.TopicFragment} to notify
     * that multi-quote mode has been turned off and that posts UI should be updated accordingly.
     */
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
    @Subscribe public void onScrollToPost(ScrollToPostEvent event) {
        if (event.getTopic().id() == topic.id() && event.getPage() == currentPage) {
            topicPageView.setPagePosition(event.getPagePosition());
        }
    }

    /**
     * Event thrown by {@link com.ayuget.redface.ui.view.TopicPageView} to notify
     * that an user has been blocked.
     */
    @Subscribe public void onBlockUser(final BlockUserEvent event) {
        blacklist.addBlockedAuthor(event.getAuthor());
        SnackbarHelper.makeWithAction(PostsFragment.this, getString(R.string.user_blocked, event.getAuthor()),
                R.string.action_refresh_topic, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bus.post(new PageRefreshRequestEvent(topic));
                    }
                }).show();
    }

    @Subscribe
    public void onTopicPageCountUpdated(TopicPageCountUpdatedEvent event) {
        if (event.getTopic().id() == topic.id()) {
            topic = topic.withPagesCount(event.getNewPageCount());
        }
    }

    private void showLoadingIndicator() {
        Timber.d("@%d -> Showing loading layout", System.identityHashCode(this));
        if (errorView != null) { errorView.setVisibility(View.GONE); }
        if (loadingIndicator != null) { loadingIndicator.setVisibility(View.VISIBLE); }
        if (swipeRefreshLayout != null) { swipeRefreshLayout.setVisibility(View.GONE); }
    }

    private void showErrorView() {
        Timber.d("@%d -> Showing error layout", System.identityHashCode(this));
        if (errorView != null) { errorView.setVisibility(View.VISIBLE); }
        if (loadingIndicator != null) { loadingIndicator.setVisibility(View.GONE); }
        if (swipeRefreshLayout != null) { swipeRefreshLayout.setVisibility(View.GONE); }
    }

    private void showPosts() {
        Timber.d("@%d -> Showing posts layout", System.identityHashCode(this));
        if (errorView != null) { errorView.setVisibility(View.GONE); }
        if (loadingIndicator != null) { loadingIndicator.setVisibility(View.GONE); }
        if (swipeRefreshLayout != null) { swipeRefreshLayout.setVisibility(View.VISIBLE); }
    }

    private boolean isDownloadStrategyMatching(DownloadStrategy strategy) {
        return strategy != DownloadStrategy.NEVER &&
                (strategy == DownloadStrategy.ALWAYS ||
                 (strategy == DownloadStrategy.FASTCX && Connectivity.isConnectedFast(getContext())) ||
                 (strategy == DownloadStrategy.WIFI && Connectivity.isConnectedWifi(getContext())));
    }

    public void loadPage(final int page) {
        Timber.d("@%d -> Loading page '%d'", System.identityHashCode(this), page);
        subscribe(dataService.loadPosts(userManager.getActiveUser(), topic, page,
                isDownloadStrategyMatching(settings.getImagesStrategy()),
                isDownloadStrategyMatching(settings.getAvatarsStrategy()),
                isDownloadStrategyMatching(settings.getSmileysStrategy()),
                new EndlessObserver<List<Post>>() {
            @Override
            public void onNext(List<Post> posts) {
                swipeRefreshLayout.setRefreshing(false);

                displayedPosts.clear();
                displayedPosts.addAll(posts);

                topicPageView.setTopic(topic);
                topicPageView.setPage(currentPage);

                Timber.d("@%d -> Done loading page, settings posts", System.identityHashCode(PostsFragment.this));
                topicPageView.setPosts(posts);

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

    private void updateQuotedPostsStatus() {
        List<Long> quotedPosts = ((TopicFragment)getParentFragment()).getPageQuotedPosts(currentPage);
        Timber.d("Posts already quoted for page '%d' = %s", currentPage, quotedPosts);
        if (quotedPosts.size() > 0) {
            topicPageView.setQuotedPosts(quotedPosts);
        }
    }

    private void setupImagesInteractions() {
        registerForContextMenu(topicPageView);
        topicPageView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                if (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    final String url = result.getExtra();

                    final ImageMenuHandler imageMenuHandler = new ImageMenuHandler(getActivity(), url);
                    MenuItem.OnMenuItemClickListener itemClickListener = new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
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
                        }
                    };

                    if (! url.contains(mdEndpoints.baseurl())) {
                        menu.setHeaderTitle(url);
                        getActivity().getMenuInflater().inflate(R.menu.menu_save_image, menu);
                        for (int i = 0; i < menu.size(); i++) {
                            menu.getItem(i).setOnMenuItemClickListener(itemClickListener);
                        }
                    }
                }
            }
        });
    }


}
