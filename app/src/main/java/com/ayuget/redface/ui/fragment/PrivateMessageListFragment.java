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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.adapter.PrivateMessagesAdapter;
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

import butterknife.InjectView;

public class PrivateMessageListFragment extends ToggleToolbarFragment implements PrivateMessagesAdapter.OnPMClickedListener, PrivateMessagesAdapter.OnPMLongClickListener {
    private static final String LOG_TAG = PrivateMessageListFragment.class.getSimpleName();

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

    @InjectView(R.id.private_messages_list)
    ContextMenuRecyclerView pmRecyclerView;

    @InjectView(R.id.pm_list_swipe_refresh_layout)
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
    View noPrivateMessagesLayout;

    @InjectView(R.id.empty_content_image)
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
        pmAdapter.setOnPMLongClickListener(this);
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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(LOG_TAG, "Refreshing private messages");
                loadPrivateMessages(1);
            }
        });

        dataPresenter = DataPresenter.from(rootView)
                .withDataView(R.id.pm_list_swipe_refresh_layout)
                .withEmptyView(R.id.empty_content_layout, R.id.empty_reload_button)
                .withErrorView(R.id.error_layout, R.id.error_reload_button)
                .withLoadingView(R.id.loading_indicator)
                .build();

        dataPresenter.setOnRefreshRequestedListener(new DataPresenter.OnRefreshRequestedListener() {
            @Override
            public void onRefresh() {
                dataPresenter.showLoadingView();
                loadPrivateMessages(1);
            }
        });

        UiUtils.setDrawableColor(noPrivateMessagesImage.getDrawable(), getResources().getColor(R.color.empty_view_image_color));

        return rootView;
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
                Log.d(LOG_TAG, String.format("Restored %d private messages", displayedPrivateMessages.size()));
                pmAdapter.replaceWith(displayedPrivateMessages);
                showPrivateMessages();
            }
        }

        if (displayedPrivateMessages == null) {
            displayedPrivateMessages = new ArrayList<>();
            loadPrivateMessages(1);
        }
        else {
            showPrivateMessages();
        }
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

    @Override
    public void onPrivateMessageLongClick(int position) {

    }

    /**
     * Shows the private messages list, or the empty view if there are no PMs to display
     */
    private void showPrivateMessages() {
        if (displayedPrivateMessages.size() > 0) {
            dataPresenter.showDataView();
        }
        else {
            dataPresenter.showEmptyView();
        }
    }

    /**
     * Loads private messages
     * @param page page to load
     */
    private void loadPrivateMessages(final int page) {
        Log.d(LOG_TAG, String.format("Loading private messages page %d", page));

        subscribe(pmSubscriptionHandler.load(1, mdService.listPrivateMessages(userManager.getActiveUser(), page), new EndlessObserver<List<PrivateMessage>>() {
            @Override
            public void onNext(List<PrivateMessage> privateMessages) {
                Log.d(LOG_TAG, String.format("Loading request completed, %d private messages loaded", privateMessages.size()));

                if (page == 1) {
                    displayedPrivateMessages.clear();
                }

                displayedPrivateMessages.addAll(privateMessages);

                if (page == 1) {
                    pmAdapter.replaceWith(privateMessages);
                }
                else {
                    pmAdapter.extendWith(privateMessages);
                }

                swipeRefreshLayout.setRefreshing(false);
                lastLoadedPage = page;
                showPrivateMessages();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(LOG_TAG, String.format("Error loading private messages page %d", page), throwable);

                swipeRefreshLayout.setRefreshing(false);

                // Do not display error view because some private messages are displayed (we are "just" loading additional content)
                if (page == 1) {
                    dataPresenter.showErrorView();
                }
                else {
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
}
