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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.DataService;
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.ui.ReplyActivity;
import com.ayuget.redface.ui.TopicsActivity;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.PageRefreshRequestEvent;
import com.ayuget.redface.ui.event.PageRefreshedEvent;
import com.ayuget.redface.ui.event.PageSelectedEvent;
import com.ayuget.redface.ui.event.ScrollToPostEvent;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.view.TopicPageView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;

public class PostsFragment extends BaseFragment {
    private static final String LOG_TAG = PostsFragment.class.getSimpleName();

    private static final String ARG_POST_LIST = "post_list";

    private static final String ARG_TOPIC = "topic";

    private static final String ARG_SAVED_PAGE_POSITION = "savedPagePosition";

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

    private ArrayList<Post> displayedPosts;

    @Inject DataService dataService;

    @Inject
    UserManager userManager;

    boolean wasRefreshed = false;

    private PagePosition currentPagePosition;

    private boolean animationInProgress = false;

    ValueAnimator replyButtonAnimator;

    private boolean replyButtonIsHidden = false;

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (topicPageView != null) {
            topicPageView.destroy();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            currentPagePosition = new PagePosition(PagePosition.BOTTOM);
        }
        else {
            currentPagePosition = savedInstanceState.getParcelable(ARG_SAVED_PAGE_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflateRootView(R.layout.fragment_posts, inflater, container);

        // Restore the list of posts when the fragment is recreated by the framework
        if (savedInstanceState != null) {
            displayedPosts = savedInstanceState.getParcelableArrayList(ARG_POST_LIST);
            if (displayedPosts != null) {
                Log.i(LOG_TAG, "Restored " + String.valueOf(displayedPosts.size()) + " posts to fragment");

                topicPageView.setTopic(topic);
                topicPageView.setPage(currentPage);
                topicPageView.setPosts(displayedPosts);

                showPosts();
            }
        }

        if (displayedPosts == null) {
            displayedPosts = new ArrayList<>();
        }

        // Implement swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, String.format("Refreshing topic page '%d' for topic %s", currentPage, topic));
                currentPagePosition = new PagePosition(PagePosition.TOP);
                bus.post(new PageRefreshedEvent(topic, currentPagePosition));
                loadPage(currentPage);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_primary, R.color.theme_primary_dark);

        if (errorReloadButton != null) {
            errorReloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG, String.format("Refreshing topic page '%d' for topic %s", currentPage, topic));
                    showLoadingIndicator();
                    loadPage(currentPage);
                }
            });
        }

        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReplyActivity(null);
            }
        });

        topicPageView.setOnScrollListener(new TopicPageView.OnScrollListener() {
            @Override
            public void onScrolled(int dx, int dy) {
                if (dy < 0) {
                    hideReplyButton();
                }
                else {
                    showReplyButton();
                }
            }
        });

        // Page is loaded instantly only if it's the initial page requested on topic load. Other
        // pages will be loaded once selected in the ViewPager
        if (isInitialPage()) {
            loadPage(currentPage);
        }

        if (userManager.getActiveUser().isGuest()) {
            replyButton.setVisibility(View.INVISIBLE);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
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
                    replyButtonIsHidden = toTranslationY != 0;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animationInProgress = false;
                    replyButtonIsHidden = toTranslationY != 0;
                }
            });

            replyButtonAnimator.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARG_POST_LIST, displayedPosts);
        outState.putParcelable(ARG_SAVED_PAGE_POSITION, currentPagePosition);
    }

    private void startReplyActivity(String initialContent) {
        TopicsActivity topicsActivity = (TopicsActivity) getActivity();

        if (topicsActivity.canLaunchReplyActivity()) {
            topicsActivity.setCanLaunchReplyActivity(false);

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
        Log.d(LOG_TAG, String.format("@%d -> Fragment(currentPage=%d) received event for page %d selected", System.identityHashCode(this), currentPage, event.getPage()));
        if (! isInitialPage() && event.getTopic() == topic && event.getPage() == currentPage) {
            loadPage(currentPage);
        }
    }

    @Subscribe public void onPageRefreshRequestEvent(PageRefreshRequestEvent event) {
        if (event.getTopic().getId() == topic.getId()) {
            wasRefreshed = true;

            currentPagePosition = new PagePosition(PagePosition.BOTTOM);
            bus.post(new PageRefreshedEvent(topic, currentPagePosition));

            showLoadingIndicator();
            loadPage(currentPage);
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
        Log.d(LOG_TAG, "Showing loading indicator");
        if (errorView != null) { errorView.setVisibility(View.GONE); }
        if (loadingIndicator != null) { loadingIndicator.setVisibility(View.VISIBLE); }
        if (swipeRefreshLayout != null) { swipeRefreshLayout.setVisibility(View.GONE); }
    }

    private void showErrorView() {
        Log.d(LOG_TAG, "Showing error layout");
        if (errorView != null) { errorView.setVisibility(View.VISIBLE); }
        if (loadingIndicator != null) { loadingIndicator.setVisibility(View.GONE); }
        if (swipeRefreshLayout != null) { swipeRefreshLayout.setVisibility(View.GONE); }
    }

    private void showPosts() {
        Log.d(LOG_TAG, "Showing posts layout");
        if (errorView != null) { errorView.setVisibility(View.GONE); }
        if (loadingIndicator != null) { loadingIndicator.setVisibility(View.GONE); }
        if (swipeRefreshLayout != null) { swipeRefreshLayout.setVisibility(View.VISIBLE); }
    }

    public void loadPage(int page) {
        Log.d(LOG_TAG, String.format("@%d -> Loading page '%d'", System.identityHashCode(this), page));
        subscribe(dataService.loadPosts(userManager.getActiveUser(), topic, page, new EndlessObserver<List<Post>>() {
            @Override
            public void onNext(List<Post> posts) {
                swipeRefreshLayout.setRefreshing(false);

                displayedPosts.clear();
                displayedPosts.addAll(posts);

                topicPageView.setTopic(topic);
                topicPageView.setPage(currentPage);

                Log.d(LOG_TAG, String.format("@%d -> Done loading page, settings posts", System.identityHashCode(PostsFragment.this)));
                topicPageView.setPosts(posts);

                showPosts();
            }

            @Override
            public void onError(Throwable throwable) {
                swipeRefreshLayout.setRefreshing(false);

                Log.e(LOG_TAG, String.format("Error displaying topic '%s'", topic), throwable);
                showErrorView();
            }
        }));
    }


}
