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

package com.ayuget.redface.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ayuget.redface.R;
import com.ayuget.redface.data.api.MDLink;
import com.ayuget.redface.data.api.hfr.HFRUrlParser;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicStatus;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.EditPostEvent;
import com.ayuget.redface.ui.event.GoToTopicEvent;
import com.ayuget.redface.ui.event.InternalLinkClickedEvent;
import com.ayuget.redface.ui.event.MarkPostAsFavoriteEvent;
import com.ayuget.redface.ui.event.QuotePostEvent;
import com.ayuget.redface.ui.event.TopicContextItemSelectedEvent;
import com.ayuget.redface.ui.fragment.DefaultFragment;
import com.ayuget.redface.ui.fragment.DetailsDefaultFragment;
import com.ayuget.redface.ui.fragment.MetaPageFragmentBuilder;
import com.ayuget.redface.ui.fragment.TopicFragment;
import com.ayuget.redface.ui.fragment.TopicFragmentBuilder;
import com.ayuget.redface.ui.fragment.TopicListFragment;
import com.ayuget.redface.ui.fragment.TopicListFragmentBuilder;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.ui.misc.PagePosition;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TopicsActivity extends MultiPaneActivity implements TopicListFragment.OnTopicClickedListener {
    private static final String LOG_TAG = TopicsActivity.class.getSimpleName();

    private static final String DEFAULT_FRAGMENT_TAG = "default_fragment";

    private static final String DETAILS_DEFAULT_FRAGMENT_TAG = "details_default_fragment";

    private static final String TOPICS_FRAGMENT_TAG = "topics_fragment";

    private static final String TOPIC_FRAGMENT_TAG = "topic_fragment";

    private static final String ARG_TOPIC = "topic";

    private static final String ARG_CURRENT_CATEGORY = "currentCategory";

    private MaterialEditText goToPageEditText;

    private SubscriptionHandler<Integer, Topic> topicDetailsSearchHandler = new SubscriptionHandler<>();

    private SubscriptionHandler<Topic, String> quoteHandler = new SubscriptionHandler<>();

    @Inject
    CategoriesStore categoriesStore;

    @Inject
    HFRUrlParser urlParser;

    boolean restoredInstanceState = false;

    private Category currentCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_topics, savedInstanceState);

        if (getIntent().getData() != null) {
            restoredInstanceState = true;

            String url = getIntent().getData().toString();

            urlParser.parseUrl(url).ifTopicLink(new MDLink.IfIsTopicLink() {
                @Override
                public void call(final Category category, final int topicId, final int topicPage, final PagePosition pagePosition) {
                    Log.d(LOG_TAG, String.format("Parsed link for category='%s', topic='%d', page='%d'", category.getName(), topicId, topicPage));
                    onGoToTopicEvent(new GoToTopicEvent(category, topicId, topicPage, pagePosition));
                }
            });
        }
        else if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                Category savedCategory = extras.getParcelable(UIConstants.ARG_SELECTED_CATEGORY);
                if (savedCategory != null) {
                    if (savedCategory.getId() == categoriesStore.getMetaCategory().getId()) {
                        onMyTopicsClicked();
                    }
                    else {
                        onCategoryClicked(savedCategory);
                    }
                }
            }
        }
    }

    @Override
    protected void onSetupUiState() {
        Log.d(LOG_TAG, "Setting up initial state for TopicsActivity");

        DefaultFragment defaultFragment = DefaultFragment.newInstance();

        if (isTwoPaneMode()) {
            DetailsDefaultFragment detailsDefaultFragment = DetailsDefaultFragment.newInstance();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, defaultFragment, DEFAULT_FRAGMENT_TAG)
                    .replace(R.id.details_container, detailsDefaultFragment, DETAILS_DEFAULT_FRAGMENT_TAG)
                    .commit();
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, defaultFragment, DEFAULT_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onRestoreUiState(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Restoring UI state for TopicsActivity");

        // This will prevent categories loading eventfrom the navigation drawer to
        // mess-up with the UI (unnecessary reload) when the activity is re-created from
        // a previous state (like rotating the phone...)
        restoredInstanceState = true;
        currentCategory = savedInstanceState.getParcelable(ARG_CURRENT_CATEGORY);

        TopicListFragment topicListFragment = (TopicListFragment) getSupportFragmentManager().findFragmentByTag(TOPICS_FRAGMENT_TAG);
        TopicFragment topicFragment = (TopicFragment) getSupportFragmentManager().findFragmentByTag(TOPIC_FRAGMENT_TAG);

        if (topicListFragment != null) {
            // Register the callbacks again
            topicListFragment.addOnTopicClickedListener(this);
        }

        // Restore topic list fragment to the correct pane if we come from portrait mode
        if (isTwoPaneMode() && (topicListFragment != null && !topicListFragment.isVisible())) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, topicListFragment, TOPICS_FRAGMENT_TAG)
                        .commit();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentCategory != null) {
            outState.putParcelable(ARG_CURRENT_CATEGORY, currentCategory);
        }
    }

    /**
     * Callback invoked when categories have been loaded from cache or network.
     */
    @Override
    public void onCategoriesLoaded() {
        Log.d(LOG_TAG, "Categories have been loaded");

        if (currentCategory == null) {
            Log.d(LOG_TAG, "Loading default category");
            loadDefaultCategory();
        }
        else {
            Log.d(LOG_TAG, "Ignoring categories loaded event, state has been restored");
        }
    }

    /**
     * Loads default category
     */
    public void loadDefaultCategory() {
        int defaultCatId = getSettings().getDefaultCategoryId();

        // If current user is not logged in and if default category setting is on "My Topics", let's
        // redirect the user the another accessible cat. "My topics" is necessary empty when user
        // is not logged in, so having an empty landing screen is not the best user experience here
        if (!userManager.activeUserIsLoggedIn() && defaultCatId == CategoriesStore.META_CATEGORY_ID) {
            defaultCatId = getSettings().getNotLoggedInDefaultCategoryId();
        }

        Category defaultCategory = categoriesStore.getCategoryById(defaultCatId);
        if (defaultCategory == null) {
            Log.w(LOG_TAG, String.format("Category '%d' not found in cache", defaultCatId));
        }
        else if (defaultCategory.getId() == CategoriesStore.META_CATEGORY_ID) {
            onMyTopicsClicked();
        }
        else {
            onCategoryClicked(defaultCategory);
        }
    }

    @Override
    public void onCategoryClicked(Category category) {
        currentCategory = category;

        Log.d(LOG_TAG, String.format("Loading category '%s', with topicFilter='%s'", category.getName(), getSettings().getDefaultTopicFilter().toString()));

        TopicListFragment topicListFragment = new TopicListFragmentBuilder(category).topicFilter(getSettings().getDefaultTopicFilter()).build();
        topicListFragment.addOnTopicClickedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, topicListFragment, TOPICS_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void onMyTopicsClicked() {
        currentCategory = categoriesStore.getMetaCategory();

        Log.d(LOG_TAG, String.format("Loading meta category, with topicFilter='%s'", getSettings().getDefaultTopicFilter().toString()));
        TopicListFragment topicListFragment = new MetaPageFragmentBuilder(currentCategory).topicFilter(getSettings().getDefaultTopicFilter()).build();
        topicListFragment.addOnTopicClickedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, topicListFragment, TOPICS_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void onTopicClicked(Topic topic) {
        int pageToLoad;
        PagePosition pagePosition;

        if (topic.getStatus() == TopicStatus.FAVORITE_NEW_CONTENT || topic.getStatus() == TopicStatus.READ_NEW_CONTENT || topic.getStatus() == TopicStatus.FLAGGED_NEW_CONTENT) {
            pageToLoad = topic.getLastReadPostPage();
            pagePosition = new PagePosition(topic.getLastReadPostId());
        }
        else {
            pageToLoad = 1;
            pagePosition = new PagePosition(PagePosition.TOP);
        }

        loadTopic(topic, pageToLoad, pagePosition);
    }

    /**
     * Loads a topic in the appropriate panel for a given page and position
     */
    protected void loadTopic(Topic topic, int page, PagePosition pagePosition) {
        Log.d(LOG_TAG, String.format("Loading topic '%s' (page %d)", topic.getSubject(), page));
        TopicFragment topicFragment = new TopicFragmentBuilder(page, topic).currentPagePosition(pagePosition).build();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        int topicFragmentContainer = isTwoPaneMode() ? R.id.details_container : R.id.container;

        if (!isTwoPaneMode()) {
            Log.d(LOG_TAG, "Setting slide animation for topicFragment");
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }

        transaction.replace(topicFragmentContainer, topicFragment, TOPIC_FRAGMENT_TAG);
        transaction.addToBackStack(TOPIC_FRAGMENT_TAG);
        transaction.commit();
    }

    protected void loadAnonymousTopic(Topic topic, int page, PagePosition pagePosition) {
        TopicFragment anonymousTopicFragment = new TopicFragmentBuilder(page, topic).currentPagePosition(pagePosition).build();
        int topicFragmentContainer = isTwoPaneMode() ? R.id.details_container : R.id.container;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(topicFragmentContainer, anonymousTopicFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onUserSwitched(User newUser) {
        if (currentCategory != null) {
            onCategoryClicked(currentCategory);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "On Back Pressed");
        boolean consumedEvent = false;

        TopicFragment topicFragment = (TopicFragment) getSupportFragmentManager().findFragmentByTag(TOPIC_FRAGMENT_TAG);
        if (topicFragment != null) {
            consumedEvent = topicFragment.onBackPressed();
        }

        if (!consumedEvent) {
            super.onBackPressed();
        }
    }

    /**
     * Called when an item of the topic contextual menu has been clicked. This menu is accessible via long-press
     * on a topic and gives additional actions to the user.
     */
    @Subscribe public void onTopicContextItemSelected(TopicContextItemSelectedEvent event) {
        Log.d(LOG_TAG, String.format("Received topic contextItem event : %d for topic %s", event.getItemId(), event.getTopic().getSubject()));

        switch (event.getItemId()) {
            case UIConstants.TOPIC_ACTION_GO_TO_FIRST_PAGE:
                loadTopic(event.getTopic(), 1, new PagePosition(PagePosition.TOP));
                break;
            case UIConstants.TOPIC_ACTION_GO_TO_LAST_READ_PAGE:
                loadTopic(event.getTopic(), event.getTopic().getLastReadPostPage(), new PagePosition(event.getTopic().getLastReadPostId()));
                break;
            case UIConstants.TOPIC_ACTION_GO_TO_SPECIFIC_PAGE:
                showGoToPageDialog(event.getTopic());
                break;
            case UIConstants.TOPIC_ACTION_REPLY_TO_TOPIC:
                Intent intent = new Intent(this, ReplyActivity.class);
                intent.putExtra(ARG_TOPIC, event.getTopic());
                startActivity(intent);
                break;
            case UIConstants.TOPIC_ACTION_GO_TO_LAST_PAGE:
                loadTopic(event.getTopic(), event.getTopic().getPagesCount(), new PagePosition(PagePosition.TOP));
                break;
        }
    }

    /**
     * This event is fired if an internal link to a different post that the one displayed has been
     * clicked.
     */
    @Subscribe
    public void onGoToTopicEvent(final GoToTopicEvent event) {
        subscribe(topicDetailsSearchHandler.load(event.getTopicId(), mdService.getTopic(userManager.getActiveUser(), event.getCategory(), event.getTopicId()), new EndlessObserver<Topic>() {
            @Override
            public void onNext(Topic topic) {
                if (topic != null) {
                    topic.setCategory(event.getCategory());
                    loadAnonymousTopic(topic, event.getTopicPage(), event.getPagePosition());
                }
            }
        }));
    }

    /**
     * Callback used to handle internal URLs. This callback does not handle the link itself, but updates the page position of the
     * appropriate topic page in order to restore correct position on back press if a new topic is loaded (meaning a new topicFragment).
     * Kinda hacky, but seems to work...
     */
    @Subscribe
    public void onInternalLinkClicked(InternalLinkClickedEvent event) {
        TopicFragment topicFragment = (TopicFragment) getSupportFragmentManager().findFragmentByTag(TOPIC_FRAGMENT_TAG);
        if (topicFragment != null && event.getTopic() == topicFragment.getTopic() && event.getPage() == topicFragment.getCurrentPage()) {
            topicFragment.setCurrentPagePosition(event.getPagePosition());
        }
    }

    @Subscribe
    public void onQuotePost(final QuotePostEvent event) {
        subscribe(quoteHandler.load(event.getTopic(), mdService.getQuote(userManager.getActiveUser(), event.getTopic(), event.getPostId()), new EndlessObserver<String>() {
            @Override
            public void onNext(String quoteBBCode) {
                startReplyActivity(event.getTopic(), quoteBBCode);
            }
        }));
    }

    @Subscribe public void onEditPost(final EditPostEvent event) {
        subscribe(quoteHandler.load(event.getTopic(), mdService.getPostContent(userManager.getActiveUser(), event.getTopic(), event.getPostId()), new EndlessObserver<String>() {
            @Override
            public void onNext(String messageBBCode) {
                startEditActivity(event.getTopic(), event.getPostId(), messageBBCode);
            }
        }));
    }

    @Subscribe public void onMarkPostAsFavorite(final MarkPostAsFavoriteEvent event) {
        SnackbarHelper.make(TopicsActivity.this, R.string.marking_as_favorite_in_progress).show();

        subscribe(mdService.markPostAsFavorite(userManager.getActiveUser(), event.getTopic(), event.getPostId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EndlessObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        SnackbarHelper.make(TopicsActivity.this, R.string.mark_as_favorite_success).show();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(LOG_TAG, "Unexpected error while marking a post as favorite", throwable);
                        SnackbarHelper.makeError(TopicsActivity.this, R.string.mark_as_favorite_failed).show();
                    }
                }));
    }

    /**
     * Shows the "Go to page" dialog where the user can enter the page he wants to consult.
     * @param topic topic concerned by the action
     */
    public void showGoToPageDialog(final Topic topic) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_go_to_page, true)
                .positiveText(R.string.dialog_go_to_page_positive_text)
                .negativeText(android.R.string.cancel)
                .theme(themeManager.getMaterialDialogTheme())
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        try {
                            int pageNumber = Integer.valueOf(goToPageEditText.getText().toString());
                            loadTopic(topic, pageNumber, new PagePosition(PagePosition.TOP));
                        } catch (NumberFormatException e) {
                            Log.e(LOG_TAG, String.format("Invalid page number entered : %s", goToPageEditText.getText().toString()), e);
                            SnackbarHelper.makeError(TopicsActivity.this, R.string.invalid_page_number).show();
                        }
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
                    try {
                        int pageNumber = Integer.valueOf(s.toString());
                        positiveAction.setEnabled(pageNumber >= 1 && pageNumber <= topic.getPagesCount());
                    }
                    catch (NumberFormatException e) {
                        positiveAction.setEnabled(false);
                    }
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
