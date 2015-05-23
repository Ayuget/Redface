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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.adapter.MetaPageTopicsAdapter;
import com.ayuget.redface.ui.misc.MetaPageOrdering;
import com.hannesdorfmann.fragmentargs.annotation.FragmentArgsInherited;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.List;

import javax.inject.Inject;

@FragmentArgsInherited
public class MetaPageFragment extends TopicListFragment {
    private static final String LOG_TAG = TopicListFragment.class.getSimpleName();

    private static final String ARG_META_PAGE_SORTED_BY_DATE = "meta_page_ordering";

    private  StickyRecyclerHeadersDecoration headerDecoration;

    private MetaPageOrdering pageOrdering;

    @Inject
    RedfaceSettings settings;

    @Override
    protected void initializeAdapters() {
        topicsAdapter = new MetaPageTopicsAdapter(new ContextThemeWrapper(getActivity(), themeManager.getActiveThemeStyle()), themeManager);
        topicsAdapter.setOnTopicClickedListener(this);
        topicsAdapter.setOnTopicLongClickListener(this);
    }

    @Override
    protected void initializeToolbarTitle(Toolbar toolbar) {
        // No need for spinner on meta page
        toolbar.setTitle(R.string.navdrawer_item_my_topics);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            pageOrdering = settings.getDefaultMetaPageOrdering();
        }
        else {
            boolean sortedByDate = savedInstanceState.getBoolean(ARG_META_PAGE_SORTED_BY_DATE, false);
            pageOrdering = sortedByDate ? MetaPageOrdering.SORT_BY_DATE : MetaPageOrdering.GROUP_BY_CATS;
        }

        resetAdapterDetails();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View createdView =  super.onCreateView(inflater, container, savedInstanceState);

        headerDecoration = new StickyRecyclerHeadersDecoration((MetaPageTopicsAdapter) topicsAdapter);
        topicsRecyclerView.addItemDecoration(headerDecoration);

        return createdView;
    }

    @Override
    public void onCreateOptionsMenu(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.menu_meta_page);
        toggleOrderingIcons(toolbar.getMenu());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_META_PAGE_SORTED_BY_DATE, areTopicsSortedByDate());
    }

    private void toggleOrderingIcons(Menu menu) {
        MenuItem sortByDateItem = menu.findItem(R.id.action_meta_sort_by_date);
        MenuItem groupByCatsItem = menu.findItem(R.id.action_meta_group_by_cats);

        if (pageOrdering == MetaPageOrdering.GROUP_BY_CATS) {
            sortByDateItem.setVisible(true);
            sortByDateItem.setEnabled(true);
            groupByCatsItem.setVisible(false);
            groupByCatsItem.setEnabled(false);
        }
        else if (pageOrdering == MetaPageOrdering.SORT_BY_DATE) {
            sortByDateItem.setVisible(false);
            sortByDateItem.setEnabled(false);
            groupByCatsItem.setVisible(true);
            groupByCatsItem.setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        boolean changedPageOrdering = false;

        switch (id) {
            case R.id.action_meta_group_by_cats:
                pageOrdering = MetaPageOrdering.GROUP_BY_CATS;
                changedPageOrdering = true;
                break;
            case R.id.action_meta_sort_by_date:
                pageOrdering = MetaPageOrdering.SORT_BY_DATE;
                changedPageOrdering = true;
                break;
        }

        if (changedPageOrdering) {
            toggleOrderingIcons(getToolbar().getMenu());
            resetAdapterDetails();
            showLoadingIndicator();
            loadTopics();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean areTopicsSortedByDate() {
        return pageOrdering == MetaPageOrdering.SORT_BY_DATE;
    }

    private void resetAdapterDetails() {
        ((MetaPageTopicsAdapter) topicsAdapter).setCategoryIconsAsTopicIcons(areTopicsSortedByDate());
    }

    /**
     * Loads topics for a given category, replacing current topics. Only loads a single topic page,
     * user has to swipe at the bottom of the list to load the next pages
     */
    public void loadTopics() {
        Log.d(LOG_TAG, String.format("Loading meta category and replacing current topics (with filter='%s')", topicFilter == null ? "null" : topicFilter.toString()));

        // Load categories for active user
        subscribe(dataService.loadMetaPageTopics(userManager.getActiveUser(), topicFilter, areTopicsSortedByDate(), new EndlessObserver<List<Topic>>() {
            @Override
            public void onNext(List<Topic> loadedTopics) {
                Log.d(LOG_TAG, String.format("Loading request completed, %d topics loaded", loadedTopics.size()));

                displayedTopics.clear();
                displayedTopics.addAll(loadedTopics);

                topicsAdapter.replaceWith(loadedTopics);
                headerDecoration.invalidateHeaders();

                swipeRefreshLayout.setRefreshing(false);
                lastLoadedPage = 1;
                showTopics();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(LOG_TAG, "Error loading meta category", throwable);

                swipeRefreshLayout.setRefreshing(false);

                if (displayedTopics.size() == 0) {
                    showErrorView();
                } else {
                    SnackbarManager.show(Snackbar.with(getActivity())
                                    .text(R.string.error_loading_topics)
                                    .colorResource(R.color.theme_primary_light)
                                    .textColorResource(R.color.tabs_text_color)
                    );
                }
            }
        }));
    }

    @Override
    protected void loadPage(int page) {
        // no second page for meta page
    }
}
