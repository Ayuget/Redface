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
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.DataService;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Subcategory;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicFilter;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.adapter.SubcategoriesAdapter;
import com.ayuget.redface.ui.adapter.TopicsAdapter;
import com.ayuget.redface.ui.event.TopicContextItemSelectedEvent;
import com.ayuget.redface.ui.misc.DataPresenter;
import com.ayuget.redface.ui.misc.DividerItemDecoration;
import com.ayuget.redface.ui.misc.EndlessScrollListener;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.view.ContextMenuRecyclerView;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;
import com.squareup.phrase.Phrase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;

@FragmentWithArgs
public class TopicListFragment extends ToggleToolbarFragment implements TopicsAdapter.OnTopicClickedListener {
	private static final String ARG_LAST_LOADED_PAGE = "last_loaded_page";

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

	@BindView(R.id.topics_list)
	ContextMenuRecyclerView topicsRecyclerView;

	@BindView(R.id.topic_list_swipe_refresh_layout)
	SwipeRefreshLayout swipeRefreshLayout;

	@BindView(R.id.empty_content_image)
	ImageView emptyTopicsImage;

	@Inject
	UserManager userManager;

	protected LinearLayoutManager layoutManager;

	protected int lastLoadedPage = 0;

	protected DataPresenter dataPresenter;

	/**
	 * Listener invoked when a topic is clicked
	 */
	private List<OnTopicClickedListener> onTopicClickedListeners;

	@Inject
	DataService dataService;

	@Inject
	RedfaceSettings settings;

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

		topicsAdapter = createTopicsAdapter();
	}

	protected TopicsAdapter createTopicsAdapter() {
		return new TopicsAdapter(new ContextThemeWrapper(getActivity(), themeManager.getActiveThemeStyle()), themeManager, settings.isCompactModeEnabled(), settings.isEnhancedCompactModeEnabled());
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

		swipeRefreshLayout.setColorSchemeResources(R.color.theme_primary, R.color.theme_primary_dark);

		dataPresenter = DataPresenter.from(rootView)
				.withDataView(R.id.topic_list_swipe_refresh_layout)
				.withEmptyView(R.id.empty_content_layout, R.id.empty_reload_button)
				.withErrorView(R.id.error_layout, R.id.error_reload_button)
				.withLoadingView(R.id.loading_indicator)
				.build();

		UiUtils.setDrawableColor(emptyTopicsImage.getDrawable(), getResources().getColor(R.color.empty_view_image_color));

		return rootView;
	}

	private void refreshTopicList() {
		Timber.d("Refreshing topic list for category %s (refresh)", category);
		dataService.clearTopicListCache();
		loadTopics();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Restore the list of topics when the fragment is recreated by the framework
		if (savedInstanceState != null) {
			lastLoadedPage = savedInstanceState.getInt(ARG_LAST_LOADED_PAGE, 0);
			savedInstanceState.clear();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		swipeRefreshLayout.setOnRefreshListener(this::refreshTopicList);
		dataPresenter.setOnRefreshRequestedListener(() -> {
			dataPresenter.showLoadingView();
			refreshTopicList();
		});

		if (displayedTopics == null || displayedTopics.size() == 0 || settings.refreshTopicList()) {
			displayedTopics = new ArrayList<>();
			loadTopics();
		} else {
			showTopics();
		}

		topicsAdapter.setOnTopicClickedListener(this);
	}

	@Override
	public void onPause() {
		swipeRefreshLayout.setOnRefreshListener(null);
		dataPresenter.setOnRefreshRequestedListener(null);
		topicsAdapter.setOnTopicClickedListener(null);

		super.onPause();
	}

	@Override
	public void onToolbarInitialized(Toolbar toolbar) {
		initializeToolbarTitle(toolbar);

		// Deal with endless scrolling
		topicsRecyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
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

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			boolean first = true;

			@Override
			public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
				if (first) {
					first = false;
					return;
				}

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
		outState.putInt(ARG_LAST_LOADED_PAGE, lastLoadedPage);
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
			case R.id.action_refresh_topic_list:
				dataPresenter.showLoadingView();
				loadTopics();
				break;
		}

		if (changedTopicFilter) {
			dataPresenter.showLoadingView();
			loadTopics();

			if (subcategoriesAdapter != null) {
				subcategoriesAdapter.setActiveTopicFilter(topicFilter);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	protected void hideSwipeToRefreshIndicator() {
		if (swipeRefreshLayout.isRefreshing()) {
			swipeRefreshLayout.setRefreshing(false);
		}
	}

	/**
	 * Loads topics for a given category, replacing current topics. Only loads a single topic page,
	 * user has to swipe at the bottom of the list to load the next pages
	 */
	public void loadTopics() {
		Timber.d("Loading first page for category '%s' (subcategory: '%s') and replacing current topics (with filter='%s')", category.name(), subcategory, topicFilter == null ? "null" : topicFilter.toString());

		// Load categories for active user
		subscribe(dataService.loadTopics(userManager.getActiveUser(), category, subcategory, 1, topicFilter, new EndlessObserver<List<Topic>>() {
			@Override
			public void onNext(List<Topic> loadedTopics) {
				Timber.d("Loading request completed, %d topics loaded", loadedTopics.size());

				displayedTopics.clear();
				displayedTopics.addAll(loadedTopics);

				topicsAdapter.replaceWith(loadedTopics);

				lastLoadedPage = 1;
				layoutManager.scrollToPosition(0);

				showTopics();
			}

			@Override
			public void onError(Throwable throwable) {
				Timber.e(throwable, "Error loading first page for category '%s', subcategory '%s'", category.name(), subcategory);

				if (displayedTopics.size() == 0) {
					dataPresenter.showErrorView();
				} else {
					SnackbarHelper.make(TopicListFragment.this, R.string.error_loading_topics).show();
				}

				hideSwipeToRefreshIndicator();
			}
		}));
	}

	/**
	 * Loads a given topics page for the current category, subcategory and topic filter
	 *
	 * @param page page to load (1..n)
	 */
	protected void loadPage(final int page) {
		Timber.d("Loading page '%d' for category '%s', subcategory '%s'", page, category, subcategory);

		if (category == null) {
			Timber.e("Category is null cannot load page");
			return;
		}

		subscribe(dataService.loadTopics(userManager.getActiveUser(), category, subcategory, page, topicFilter, new EndlessObserver<List<Topic>>() {
			@Override
			public void onNext(List<Topic> loadedTopics) {
				Timber.d("Loading request completed, %d topics loaded", loadedTopics.size());

				displayedTopics.addAll(loadedTopics);
				topicsAdapter.extendWith(loadedTopics);

				lastLoadedPage = page;
				showTopics();
			}

			@Override
			public void onError(Throwable throwable) {
				Timber.e(throwable, "Error loading page '%d' for category '%s', subcategory '%s'", page, category.name(), subcategory);

				// Do not display error view because topics are displayed (we are "just" loading additional content)
				SnackbarHelper.make(
						TopicListFragment.this,
						Phrase.from(getActivity(), R.string.error_loading_topics_page).put("page", page).format()
				).show();

				hideSwipeToRefreshIndicator();
			}
		}));
	}

	public void addOnTopicClickedListener(OnTopicClickedListener onTopicClickedListener) {
		if (!onTopicClickedListeners.contains(onTopicClickedListener)) {
			onTopicClickedListeners.add(onTopicClickedListener);
		}
	}

	@Override
	public void onTopicClicked(Topic topic) {
		// Dispatch event to all subscribers
		for (OnTopicClickedListener listener : onTopicClickedListeners) {
			listener.onTopicClicked(topic);
		}
	}

	protected void showTopics() {
		if (displayedTopics.size() > 0) {
			dataPresenter.showDataView();
		} else {
			dataPresenter.showEmptyView();
		}

		hideSwipeToRefreshIndicator();
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
		menu.add(0, UIConstants.TOPIC_ACTION_COPY_LINK, 5, getResources().getString(R.string.action_copy_link));
		menu.add(0, UIConstants.TOPIC_ACTION_SHARE, 6, getResources().getString(R.string.action_share));
		menu.add(0, UIConstants.TOPIC_ACTION_UNFLAG, 7, getResources().getString(R.string.action_unflag_topic));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
		bus.post(new TopicContextItemSelectedEvent(topicsAdapter.getItem(info.getPosition()), item.getItemId()));
		return true;
	}
}
