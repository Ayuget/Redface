package com.ayuget.redface.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.TopicsActivity;
import com.ayuget.redface.ui.adapter.TopicPageAdapter;
import com.ayuget.redface.ui.event.GoToPostEvent;
import com.ayuget.redface.ui.event.PageLoadedEvent;
import com.ayuget.redface.ui.event.PageSelectedEvent;
import com.ayuget.redface.ui.event.ScrollToPostEvent;
import com.ayuget.redface.ui.event.TopicPageCountUpdatedEvent;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.PageSelectedListener;
import com.ayuget.redface.ui.misc.TopicPosition;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.InjectView;

public class TopicFragment extends ToolbarFragment {
    private static final String LOG_TAG = TopicFragment.class.getSimpleName();

    private static final String ARG_TOPIC_POSITIONS_STACK = "topicPositionsStack";

    private TopicPageAdapter topicPageAdapter;

    private MaterialEditText goToPageEditText;

    private ArrayList<TopicPosition> topicPositionsStack;

    @InjectView(R.id.pager)
    ViewPager pager;

    /**
     * Topic currently displayed
     */
    @Arg
    Topic topic;

    /**
     * Page currently displayed in the viewPager
     */
    @Arg
    int currentPage;

    /**
     * Current page position
     */
    @Arg(required = false)
    PagePosition currentPagePosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (topicPageAdapter == null) {
            topicPageAdapter = new TopicPageAdapter(getChildFragmentManager(), topic, currentPage);
        }

        if (savedInstanceState != null) {
            topicPositionsStack = savedInstanceState.getParcelableArrayList(ARG_TOPIC_POSITIONS_STACK);
        }

        if (savedInstanceState == null) {
            topicPositionsStack = new ArrayList<>();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = inflateRootView(R.layout.fragment_topic, inflater, container);

        pager.setAdapter(topicPageAdapter);
        pager.setOnPageChangeListener(new PageSelectedListener() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position + 1;
                bus.post(new PageSelectedEvent(topic, currentPage));
            }
        });
        pager.setCurrentItem(currentPage - 1);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(ARG_TOPIC_POSITIONS_STACK, topicPositionsStack);
    }

    @Override
    public void onCreateOptionsMenu(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.menu_topic);
    }

    @Override
    public void onToolbarInitialized(Toolbar toolbar) {
        TopicsActivity hostActivity = (TopicsActivity) getActivity();

        if (! hostActivity.isTwoPaneMode()) {
            showUpButton();
        }

        toolbar.setTitle(topic.getSubject());
    }

    /**
     * Event fired by the webview contained in the viewpager child fragments once the page has
     * been loaded. It allow us to set the page position only once the DOM is ready, otherwise
     * initial posiion is broken.
     */
    @Subscribe
    public void onTopicPageLoaded(PageLoadedEvent event) {
        Log.d(LOG_TAG, String.format("@%d -> Received topicPageLoaded event (topic='%s', page='%d'), current(topic='%s', page='%d', currentPagePosition='%s')", System.identityHashCode(this), event.getTopic().getSubject(), event.getPage(), topic.getSubject(), currentPage, currentPagePosition));
        if (event.getTopic().equals(topic) && event.getPage() == currentPage) {
            if (currentPagePosition != null) {
                event.getTopicPageView().setPagePosition(currentPagePosition);
            }
        }
    }

    /**
     *
     * @param event
     */
    @Subscribe
    public void onTopicPageCountUpdated(TopicPageCountUpdatedEvent event) {
        if (event.getTopic() == topic) {
            topic.setPagesCount(event.getNewPageCount());
            topicPageAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onGoToPost(GoToPostEvent event) {
        Log.d(LOG_TAG, String.format("Received go to post event : %s", event));

        topicPositionsStack.add(new TopicPosition(currentPage, currentPagePosition));

        currentPagePosition = event.getPagePosition();

        if (currentPage == event.getPage()) {
            Log.d(LOG_TAG, String.format("Event is for same page (%d), scrolling", currentPage));
            event.getTopicPageView().setPagePosition(currentPagePosition);
        }
        else {
            Log.d(LOG_TAG, String.format("Event is for another page (current=%d, target=%d), changing page in ViewPager", currentPage, event.getPage()));
            currentPage = event.getPage();

            if (pager != null) {
                pager.setCurrentItem(currentPage - 1);
            }
        }
    }

    /**
     * Callback called by the activity when the back key has been pressed
     * @return true if event was consumed, false otherwise
     */
    public boolean onBackPressed() {
        if (topicPositionsStack.size() == 0) {
            return false;
        }
        else {
            TopicPosition topicPosition = topicPositionsStack.remove(topicPositionsStack.size() - 1);

            currentPagePosition = topicPosition.getPagePosition();

            if (currentPage == topicPosition.getPage()) {
                bus.post(new ScrollToPostEvent(topic, currentPage, currentPagePosition));
            }
            else {
                currentPage = topicPosition.getPage();
                pager.setCurrentItem(currentPage - 1);
            }

            return true;
        }
    }

    /**
     * Updates position of currently displayed topic page
     * @param position new position
     */
    public void setCurrentPagePosition(PagePosition position) {
        currentPagePosition = position;
    }

    /**
     * Returns the currently displayed topic
     */
    public Topic getTopic() {
        return topic;
    }

    /**
     * Returns the initial displayed page
     */
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_go_to_first_page:
                pager.setCurrentItem(0);
                return true;
            case R.id.action_go_to_last_page:
                pager.setCurrentItem(topic.getPagesCount() - 1);
                return true;

            case R.id.action_go_to_specific_page:
                showGoToPageDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void clearInternalStack() {
        topicPositionsStack.clear();
    }

    public void showGoToPageDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_go_to_page, true)
                .positiveText(R.string.dialog_go_to_page_positive_text)
                .negativeText(android.R.string.cancel)
                .theme(themeManager.getMaterialDialogTheme())
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        int pageNumber = Integer.valueOf(goToPageEditText.getText().toString());
                        pager.setCurrentItem(pageNumber - 1);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).build();


        final View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        goToPageEditText = (MaterialEditText) dialog.getCustomView().findViewById(R.id.page_number);

        goToPageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    int pageNumber = Integer.valueOf(s.toString());
                    positiveAction.setEnabled(pageNumber >= 1 && pageNumber <= topic.getPagesCount());
                }
                else {
                    positiveAction.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialog.show();
        positiveAction.setEnabled(false);
    }
}
