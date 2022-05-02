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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.activity.MultiPaneActivity;
import com.ayuget.redface.ui.activity.WritePrivateMessageActivity;
import com.ayuget.redface.ui.adapter.PrivateMessagesAdapter;
import com.ayuget.redface.ui.event.PrivateMessageContextItemSelectedEvent;
import com.ayuget.redface.ui.misc.DataPresenter;
import com.ayuget.redface.ui.misc.DividerItemDecoration;
import com.ayuget.redface.ui.misc.EndlessScrollListener;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.view.ContextMenuRecyclerView;
import com.squareup.phrase.Phrase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;

public class PrivateMessageListFragment extends ToggleToolbarFragment implements PrivateMessagesAdapter.OnPMClickedListener {
    private static final String ARG_PRIVATE_MESSAGES_LIST = "pms_list";

    private static final String ARG_LAST_LOADED_PAGE = "last_loaded_page";

    private PrivateMessagesAdapter pmAdapter;

    private LinearLayoutManager layoutManager;

    protected DataPresenter dataPresenter;

    private ArrayList<PrivateMessage> displayedPrivateMessages;

    private SubscriptionHandler<Integer, List<PrivateMessage>> pmSubscriptionHandler = new SubscriptionHandler<>();

    private int lastLoadedPage;

    /**
     * Interface definition for a callback to be invoked when a private message in
     * this fragment has been clicked.
     */
    public interface OnPrivateMessageClickedListener {
        void onPrivateMessageClicked(PrivateMessage privateMessage);
    }

    private OnPrivateMessageClickedListener onPrivateMessageClickedListener;

    @BindView(R.id.private_messages_list)
    ContextMenuRecyclerView pmRecyclerView;

    @BindView(R.id.pm_list_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.loading_indicator)
    View loadingIndicator;

    @BindView(R.id.error_layout)
    View errorView;

    @BindView(R.id.error_reload_button)
    Button errorReloadButton;

    @BindView(R.id.empty_reload_button)
    Button emptyReloadButton;

    @BindView(R.id.empty_content_layout)
    View noPrivateMessagesLayout;

    @BindView(R.id.empty_content_image)
    ImageView noPrivateMessagesImage;

    @Inject
    RedfaceSettings settings;

    @Inject
    MDService mdService;

    @Inject
    UserManager userManager;

    public static PrivateMessageListFragment newInstance() {
        PrivateMessageListFragment fragment = new PrivateMessageListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pmAdapter = new PrivateMessagesAdapter(new ContextThemeWrapper(getActivity(), themeManager.getActiveThemeStyle()), themeManager, settings.isCompactModeEnabled());
        pmAdapter.setOnPMClickedListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflateRootView(R.layout.fragment_private_message_list, inflater, container);

        Drawable listDivider = getActivity().getResources().getDrawable(themeManager.getListDividerDrawable());

        pmRecyclerView.setHasFixedSize(true);
        pmRecyclerView.addItemDecoration(new DividerItemDecoration(listDivider, false, false));
        registerForContextMenu(pmRecyclerView);

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        pmRecyclerView.setLayoutManager(layoutManager);
        pmRecyclerView.setAdapter(pmAdapter);

        dataPresenter = DataPresenter.from(rootView)
                .withDataView(R.id.pm_list_swipe_refresh_layout)
                .withEmptyView(R.id.empty_content_layout, R.id.empty_reload_button)
                .withErrorView(R.id.error_layout, R.id.error_reload_button)
                .withLoadingView(R.id.loading_indicator)
                .build();

        // Style refresh indicator and empty content view
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_primary, R.color.theme_primary_dark);
        UiUtils.setDrawableColor(noPrivateMessagesImage.getDrawable(), getResources().getColor(R.color.empty_view_image_color));

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.menu_pm_list);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_new_private_message:
                startNewPMActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onToolbarInitialized(Toolbar toolbar) {
        pmRecyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                int realPage = page + 1;

                if (realPage > lastLoadedPage) {
                    loadPrivateMessages(realPage);
                }
            }
        });

        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);

        swipeRefreshLayout.setProgressViewOffset(false, progressBarStartMargin, progressBarEndMargin);

        toolbar.setTitle(R.string.navdrawer_item_private_messages);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            displayedPrivateMessages = savedInstanceState.getParcelableArrayList(ARG_PRIVATE_MESSAGES_LIST);

            if (displayedPrivateMessages != null) {
                Timber.d("Restored %d private messages", displayedPrivateMessages.size());
                pmAdapter.replaceWith(displayedPrivateMessages);
                showPrivateMessages();
            }
            savedInstanceState.clear();
        }
    }

    /**
     * Refresh private messages list
     */
    public void refreshData() {
        loadPrivateMessages(1);
    }

    @Override
    public void onResume() {
        super.onResume();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Timber.d("Refreshing private messages");
            loadPrivateMessages(1);
        });

        dataPresenter.setOnRefreshRequestedListener(() -> {
            dataPresenter.showLoadingView();
            loadPrivateMessages(1);
        });

        if (displayedPrivateMessages == null || displayedPrivateMessages.size() == 0 || settings.refreshTopicList()) {
            displayedPrivateMessages = new ArrayList<>();
            loadPrivateMessages(1);
        } else {
            showPrivateMessages();
        }
    }

    @Override
    public void onPause() {
        swipeRefreshLayout.setOnRefreshListener(null);
        dataPresenter.setOnRefreshRequestedListener(null);

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARG_PRIVATE_MESSAGES_LIST, displayedPrivateMessages);
        outState.putInt(ARG_LAST_LOADED_PAGE, lastLoadedPage);
    }

    @Override
    public void onPrivateMessageClicked(PrivateMessage privateMessage) {
        // Propagate event to listener if registred
        if (onPrivateMessageClickedListener != null) {
            onPrivateMessageClickedListener.onPrivateMessageClicked(privateMessage);
        }
    }

    /**
     * Initialize context menu for long clicks on topics
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, UIConstants.TOPIC_ACTION_GO_TO_FIRST_PAGE, 0, getResources().getString(R.string.action_go_to_first_page));
        menu.add(0, UIConstants.TOPIC_ACTION_GO_TO_SPECIFIC_PAGE, 1, getResources().getString(R.string.action_go_to_specific_page));
        menu.add(0, UIConstants.TOPIC_ACTION_GO_TO_LAST_PAGE, 2, getResources().getString(R.string.action_go_to_last_page));
        menu.add(0, UIConstants.TOPIC_ACTION_REPLY_TO_TOPIC, 3, getResources().getString(R.string.action_reply_to_topic));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        bus.post(new PrivateMessageContextItemSelectedEvent(pmAdapter.getItem(info.getPosition()), item.getItemId()));
        return true;
    }

    /**
     * Shows the private messages list, or the empty view if there are no PMs to display
     */
    private void showPrivateMessages() {
        if (displayedPrivateMessages.size() > 0) {
            dataPresenter.showDataView();
        } else {
            dataPresenter.showEmptyView();
        }
    }

    /**
     * Loads private messages
     *
     * @param page page to load
     */
    private void loadPrivateMessages(final int page) {
        Timber.d("Loading private messages page %d", page);

        subscribe(pmSubscriptionHandler.load(1, mdService.listPrivateMessages(userManager.getActiveUser(), page), new EndlessObserver<List<PrivateMessage>>() {
            @Override
            public void onNext(List<PrivateMessage> privateMessages) {
                Timber.d("Loading request completed, %d private messages loaded", privateMessages.size());

                if (page == 1) {
                    displayedPrivateMessages.clear();
                }

                displayedPrivateMessages.addAll(privateMessages);

                if (page == 1) {
                    pmAdapter.replaceWith(privateMessages);
                } else {
                    pmAdapter.extendWith(privateMessages);
                }

                swipeRefreshLayout.setRefreshing(false);
                lastLoadedPage = page;
                showPrivateMessages();
            }

            @Override
            public void onError(Throwable throwable) {
                Timber.e(throwable, "Error loading private messages page %d", page);

                swipeRefreshLayout.setRefreshing(false);

                // Do not display error view because some private messages are displayed (we are "just" loading additional content)
                if (page == 1) {
                    dataPresenter.showErrorView();
                } else {
                    SnackbarHelper.make(
                            PrivateMessageListFragment.this,
                            Phrase.from(getActivity(), R.string.error_loading_private_messages_page).put("page", page).format()
                    ).show();
                }
            }
        }));
    }

    public void setOnPrivateMessageClickedListener(OnPrivateMessageClickedListener onPrivateMessageClickedListener) {
        this.onPrivateMessageClickedListener = onPrivateMessageClickedListener;
    }

    private void startNewPMActivity() {
        MultiPaneActivity hostActivity = (MultiPaneActivity) getActivity();

        if (hostActivity.canLaunchReplyActivity()) {
            hostActivity.setCanLaunchReplyActivity(false);

            Intent intent = new Intent(getActivity(), WritePrivateMessageActivity.class);
            getActivity().startActivityForResult(intent, UIConstants.NEW_PM_REQUEST_CODE);
        }
    }
}
