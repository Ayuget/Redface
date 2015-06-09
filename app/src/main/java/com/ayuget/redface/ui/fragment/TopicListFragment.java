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

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.DataService;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.ui.BaseActivity;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.adapter.SubcategoriesAdapter;
import com.ayuget.redface.ui.adapter.TopicsAdapter;
import com.ayuget.redface.ui.event.TopicContextItemSelectedEvent;
import com.ayuget.redface.ui.misc.DividerItemDecoration;
import com.ayuget.redface.ui.misc.EndlessScrollListener;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.view.ContextMenuRecyclerView;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.squareup.phrase.Phrase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import hugo.weaving.DebugLog;

public class TopicListFragment extends ToolbarFragment implements TopicsAdapter.OnTopicClickedListener, TopicsAdapter.OnTopicLongClickListener {
    private static final String LOG_TAG = TopicListFragment.class.getSimpleName();

    private static final String ARG_TOPIC_LIST = "topic_list";

    /**
     * Interface definition for a callback to be invoked when a topic in this fragment has
     * been clicked.
     */
    public interface OnTopicClickedListener {
        void onTopicClicked(Topic topic);
    }

    @Arg
    Category category;

    @Arg(required = false)
    Subcategory subcategory;

    @Arg(required = false)
    TopicFilter topicFilter;

    /**
     * List of topics displayed in the fragment
     */
    protected ArrayList<Topic> displayedTopics;

    protected TopicsAdapter topicsAdapter;

    private SubcategoriesAdapter subcategoriesAdapter;

    @InjectView(R.id.topics_list)
    ContextMenuRecyclerView topicsRecyclerView;

    @InjectView(R.id.topic_list_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @InjectView(R.id.loading_indicator)
    View loadingIndicator;

    @InjectView(R.id.error_layout)
    View errorView;

    @InjectView(R.id.error_reload_button)
    Button errorReloadButton;

    @InjectView(R.id.empty_reload_button)
    Button emptyReloadButton;

    @InjectView(R.id.empty_content_layout)
    View emptyTopicsLayout;

    @InjectView(R.id.empty_content_image)
    ImageView emptyTopicsImage;

    @Inject
    UserManager userManager;

    ActionBarDrawerToggle drawerToggle;

    LinearLayoutManager layoutManager;

    int lastLoadedPage = 0;

    boolean topicContextMenuInitialized = false;

    /**
     * Listener invoked when a topic is clicked
     */
    private List<OnTopicClickedListener> onTopicClickedListeners;

    @Inject DataService dataService;

    public TopicListFragment() {
        onTopicClickedListeners = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (topicFilter == null) {
            topicFilter = TopicFilter.NONE;
        }

        initializeAdapters();

        setHasOptionsMenu(true);
    }

    protected void initializeAdapters() {
        subcategoriesAdapter = new SubcategoriesAdapter(getActivity(), topicFilter);
        subcategoriesAdapter.replaceWith(category);

        topicsAdapter = new TopicsAdapter(new ContextThemeWrapper(getActivity(), themeManager.getActiveThemeStyle()), themeManager);
        topicsAdapter.setOnTopicClickedListener(this);
        topicsAdapter.setOnTopicLongClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateRootView(R.layout.fragment_topic_list, inflater, container);

        Drawable listDivider = getActivity().getResources().getDrawable(themeManager.getListDividerDrawable());

        topicsRecyclerView.setHasFixedSize(true);
        topicsRecyclerView.addItemDecoration(new DividerItemDecoration(listDivider, false, false));
        registerForContextMenu(topicsRecyclerView);

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        topicsRecyclerView.setLayoutManager(layoutManager);
        topicsRecyclerView.setAdapter(topicsAdapter);

        // Implement swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, String.format("Refreshing topic list for category %s (refresh)", category));
                loadTopics();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.theme_primary, R.color.theme_primary_dark);

        errorReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingIndicator();
                loadTopics();
            }
        });

        emptyReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingIndicator();
                loadTopics();
            }
        });

        UiUtils.setDrawableColor(emptyTopicsImage.getDrawable(), getResources().getColor(R.color.empty_view_image_color));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Restore the list of topics when the fragment is recreated by the framework
        if (savedInstanceState != null) {
            displayedTopics = savedInstanceState.getParcelableArrayList(ARG_TOPIC_LIST);
            if (displayedTopics != null) {
                Log.i(LOG_TAG, "Restored " + String.valueOf(displayedTopics.size()) + " topics to fragment");
                topicsAdapter.replaceWith(displayedTopics);
                showTopics();
            }
        }

        if (displayedTopics == null) {
            displayedTopics = new ArrayList<>();
            loadTopics();
        }
        else {
            showTopics();
        }

        BaseActivity activity = (BaseActivity) getActivity();

        DrawerLayout drawerLayout = activity.getDrawerLayout();
        drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, getToolbar(), R.string.drawer_open, R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    public void onToolbarInitialized(Toolbar toolbar) {
        initializeToolbarTitle(toolbar);

        // Deal with endless scrolling & toolbar hide toolbar on scroll
        topicsRecyclerView.setOnScrollListener(new EndlessScrollListener(layoutManager, getToolbar(), true) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // When a topic filter is selected, all topics are displayed on one single page,
                // and trying to load more will not result in a 404 error, but it will instead load
                // the same topics over and over
                if (topicFilter == TopicFilter.NONE) {
                    int realPage = page + 1;

                    if (realPage > lastLoadedPage) {
                        loadPage(realPage);
                    }
                }
            }
        });

        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);

        swipeRefreshLayout.setProgressViewOffset(false, progressBarStartMargin, progressBarEndMargin);
    }

    protected void initializeToolbarTitle(Toolbar toolbar) {
        View spinnerContainer = LayoutInflater.from(getActivity()).inflate(R.layout.actionbar_spinner, toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, 0, lp);

        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);
        spinner.setAdapter(subcategoriesAdapter);

        // "Hack to avoid onItemSelected to be fired off on spinner instantiation
        spinner.setSelection(0, false);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            @DebugLog
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {

                if (subcategoriesAdapter.getItemViewType(position) == SubcategoriesAdapter.TYPE_CATEGORY) {
                    subcategory = null; // Reset subcategory filtering
                } else {
                    subcategory = (Subcategory) subcategoriesAdapter.getItem(position);
                }

                loadTopics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.menu_topics);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARG_TOPIC_LIST, displayedTopics);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        boolean changedTopicFilter = false;

        switch (id) {
            case R.id.action_topics_filter_all:
                topicFilter = TopicFilter.NONE;
                changedTopicFilter = true;
                break;
            case R.id.action_topics_filter_favorites:
                topicFilter = TopicFilter.FAVORITE;
                changedTopicFilter = true;
                break;
            case R.id.action_topics_filter_participated:
                topicFilter = TopicFilter.PARTICIPATED;
                changedTopicFilter = true;
                break;
            case R.id.action_topics_filter_read:
                topicFilter = TopicFilter.READ;
                changedTopicFilter = true;
                break;
        }

        if (changedTopicFilter) {
            showLoadingIndicator();
            loadTopics();

            if (subcategoriesAdapter != null) {
                subcategoriesAdapter.setActiveTopicFilter(topicFilter);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Loads topics for a given category, replacing current topics. Only loads a single topic page,
     * user has to swipe at the bottom of the list to load the next pages
     */
    public void loadTopics() {
        Log.d(LOG_TAG, String.format("Loading first page for category '%s' (subcategory: '%s') and replacing current topics (with filter='%s')", category.getName(), subcategory, topicFilter == null ? "null" : topicFilter.toString()));

        // Load categories for active user
        subscribe(dataService.loadTopics(userManager.getActiveUser(), category, subcategory, 1, topicFilter, new EndlessObserver<List<Topic>>() {
            @Override
            public void onNext(List<Topic> loadedTopics) {
                Log.d(LOG_TAG, String.format("Loading request completed, %d topics loaded", loadedTopics.size()));

                displayedTopics.clear();
                displayedTopics.addAll(loadedTopics);

                topicsAdapter.replaceWith(loadedTopics);

                swipeRefreshLayout.setRefreshing(false);
                lastLoadedPage = 1;
                showTopics();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(LOG_TAG, String.format("Error loading first page for category '%s', subcategory '%s'", category.getName(), subcategory), throwable);

                swipeRefreshLayout.setRefreshing(false);

                if (displayedTopics.size() == 0) {
                    showErrorView();
                }
                else {
                    SnackbarHelper.make(TopicListFragment.this, R.string.error_loading_topics).show();
                }
            }
        }));
    }

    /**
     * Loads a given topics page for the current category, subcategory and topic filter
     * @param page page to load (1..n)
     */
    protected void loadPage(final int page) {
        Log.d(LOG_TAG, String.format("Loading page '%d' for category '%s', subcategory '%s'", page, category, subcategory));

        if (category == null) {
            Log.e(LOG_TAG, "Category is null cannot load page");
            return;
        }

        subscribe(dataService.loadTopics(userManager.getActiveUser(), category, subcategory, page, topicFilter, new EndlessObserver<List<Topic>>() {
            @Override
            public void onNext(List<Topic> loadedTopics) {
                Log.d(LOG_TAG, String.format("Loading request completed, %d topics loaded", loadedTopics.size()));

                displayedTopics.addAll(loadedTopics);
                topicsAdapter.extendWith(loadedTopics);

                swipeRefreshLayout.setRefreshing(false);
                lastLoadedPage = page;
                showTopics();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(LOG_TAG, String.format("Error loading page '%d' for category '%s', subcategory '%s'", page, category.getName(), subcategory), throwable);

                swipeRefreshLayout.setRefreshing(false);

                // Do not display error view because topics are displayed (we are "just" loading additional content)
                SnackbarHelper.make(
                        TopicListFragment.this,
                        Phrase.from(getActivity(), R.string.error_loading_topics_page).put("page", page).format()
                ).show();
            }
        }));
    }

    public void addOnTopicClickedListener(OnTopicClickedListener onTopicClickedListener) {
        if (! onTopicClickedListeners.contains(onTopicClickedListener)) {
            onTopicClickedListeners.add(onTopicClickedListener);
        }
    }

    @Override
    public void onTopicClicked(Topic topic) {
        // Dispatch event to all subscribers
        for(OnTopicClickedListener listener : onTopicClickedListeners) {
            listener.onTopicClicked(topic);
        }
    }

    protected void showLoadingIndicator() {
        errorView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);
        emptyTopicsLayout.setVisibility(View.GONE);
    }

    protected void showErrorView() {
        errorView.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        emptyTopicsLayout.setVisibility(View.GONE);
    }

    protected void showTopics() {
        if (displayedTopics.size() > 0) {
            errorView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            emptyTopicsLayout.setVisibility(View.GONE);
        }
        else {
            errorView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.GONE);
            emptyTopicsLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Initialize context menu for long clicks on topics
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, UIConstants.TOPIC_ACTION_GO_TO_LAST_READ_PAGE, 0, getResources().getString(R.string.action_go_to_last_read_page));
        menu.add(0, UIConstants.TOPIC_ACTION_GO_TO_FIRST_PAGE, 1, getResources().getString(R.string.action_go_to_first_page));
        menu.add(0, UIConstants.TOPIC_ACTION_GO_TO_SPECIFIC_PAGE, 2, getResources().getString(R.string.action_go_to_specific_page));
        menu.add(0, UIConstants.TOPIC_ACTION_GO_TO_LAST_PAGE, 3, getResources().getString(R.string.action_go_to_last_page));
        menu.add(0, UIConstants.TOPIC_ACTION_REPLY_TO_TOPIC, 4, getResources().getString(R.string.action_reply_to_topic));
    }

    @Override
    public void onTopicLongClick(int position) {
        topicsRecyclerView.showContextMenuForPosition(position);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        bus.post(new TopicContextItemSelectedEvent(topicsAdapter.getItem(info.getPosition()), item.getItemId()));
        return true;
    }
}
