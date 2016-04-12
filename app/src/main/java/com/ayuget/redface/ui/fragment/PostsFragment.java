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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
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
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.ui.activity.MultiPaneActivity;
import com.ayuget.redface.ui.activity.ReplyActivity;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.PageRefreshRequestEvent;
import com.ayuget.redface.ui.event.PageSelectedEvent;
import com.ayuget.redface.ui.event.ScrollToPostEvent;
import com.ayuget.redface.ui.event.UnquoteAllPostsEvent;
import com.ayuget.redface.ui.misc.ImageMenuHandler;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.view.TopicPageView;
import com.getbase.floatingactionbutton.FloatingActionButton;
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

    @InjectView(R.id.reply_button)
    FloatingActionButton replyButton;

    private ArrayList<Post> displayedPosts = new ArrayList<>();

    @Inject
    DataService dataService;

    @Inject
    UserManager userManager;

    @Inject
    MDService mdService;

    @Inject
    MDEndpoints mdEndpoints;

    /**
     * Current scroll position in the webview.
     */
    private int currentScrollPosition;

    private boolean animationInProgress = false;

    ValueAnimator replyButtonAnimator;

    private boolean restoredPosts = false;

    private SubscriptionHandler<Long, String> quoteHandler = new SubscriptionHandler<>();

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
        restoredPosts = false;

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
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                savePageScrollPosition();
                Timber.d("Refreshing topic page '%d' for topic %s", currentPage, topic);
                loadPage(currentPage);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_primary, R.color.theme_primary_dark);

        if (errorReloadButton != null) {
            errorReloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timber.d("Refreshing topic page '%d' for topic %s", currentPage, topic);
                    showLoadingIndicator();
                    loadPage(currentPage);
                }
            });
        }

        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TopicFragment)getParentFragment()).replyToTopic();
            }
        });

        topicPageView.setOnScrollListener(new TopicPageView.OnScrollListener() {
            @Override
            public void onScrolled(int dx, int dy) {
                if (dy < 0) {
                    hideReplyButton();
                } else {
                    showReplyButton();
                }
            }
        });

        topicPageView.setOnPageLoadedListener(new TopicPageView.OnPageLoadedListener() {
            @Override
            public void onPageLoaded() {
                if (currentScrollPosition > 0) {
                    restorePageScrollPosition();
                }

                updateQuotedPostsStatus();
            }
        });

        if (userManager.getActiveUser().isGuest()) {
            replyButton.setVisibility(View.INVISIBLE);
        }

        // Deal with long-press actions on images inside the WebView
        setupImagesInteractions();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        topicPageView.setOnQuoteListener((TopicFragment)getParentFragment());

        // Page is loaded instantly only if it's the initial page requested on topic load. Other
        // pages will be loaded once selected in the ViewPager
        if (isInitialPage() && ((displayedPosts != null && displayedPosts.size() == 0) || displayedPosts == null)) {
            showLoadingIndicator();
            loadPage(currentPage);
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

        if (replyButton != null) {
            replyButton.setOnClickListener(null);
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

    protected void hideReplyButton() {
        moveReplyButton(UiUtils.dpToPx(getActivity(), 100));
    }

    protected void showReplyButton() {
        moveReplyButton(0);
    }

    private void moveReplyButton(final float toTranslationY) {
        if (replyButton.getTranslationY() == toTranslationY) {
            return;
        }
        if (! animationInProgress) {
            replyButtonAnimator = ValueAnimator.ofFloat(replyButton.getTranslationY(), toTranslationY).setDuration(200);

            replyButtonAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    replyButton.setTranslationY(translationY);
                }
            });
            replyButtonAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    animationInProgress = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animationInProgress = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animationInProgress = false;
                }
            });

            replyButtonAnimator.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("@%d -> Fragment(currentPage=%d) Saving '%d' posts / scrollPosition = '%d'", System.identityHashCode(this), currentPage, displayedPosts.size(), currentScrollPosition);

        outState.putParcelableArrayList(ARG_POST_LIST, displayedPosts);
        outState.putInt(ARG_SAVED_SCROLL_POSITION, currentScrollPosition);
    }

    private void startReplyActivity(String initialContent) {
        MultiPaneActivity hostActivity = (MultiPaneActivity) getActivity();

        if (hostActivity.canLaunchReplyActivity()) {
            hostActivity.setCanLaunchReplyActivity(false);

            Intent intent = new Intent(getActivity(), ReplyActivity.class);
            intent.putExtra(ARG_TOPIC, topic);

            if (initialContent != null) {
                intent.putExtra(UIConstants.ARG_REPLY_CONTENT, initialContent);
            }

            getActivity().startActivityForResult(intent, UIConstants.REPLY_REQUEST_CODE);
        }
    }

    /**
     * Defer posts loading until current fragment is visible in the ViewPager. Avoids screwing with
     * forum's read/unread markers.
     */
    @Subscribe public void onPageSelectedEvent(PageSelectedEvent event) {
        if (! isInitialPage() && event.getTopic() == topic && event.getPage() == currentPage && isVisible()) {
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

    /**
     * Event fired by the host {@link com.ayuget.redface.ui.fragment.TopicFragment} to indicate
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
        if (event.getTopic() == topic && event.getPage() == currentPage) {
            topicPageView.setPagePosition(event.getPagePosition());
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

    public void loadPage(final int page) {
        Timber.d("@%d -> Loading page '%d'", System.identityHashCode(this), page);
        subscribe(dataService.loadPosts(userManager.getActiveUser(), topic, page, new EndlessObserver<List<Post>>() {
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
