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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.hfr.HFRUrlParser;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.TopicStatus;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.RxUtils;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.EditPostEvent;
import com.ayuget.redface.ui.event.GoToTopicEvent;
import com.ayuget.redface.ui.event.InternalLinkClickedEvent;
import com.ayuget.redface.ui.event.PostActionEvent;
import com.ayuget.redface.ui.event.QuotePostEvent;
import com.ayuget.redface.ui.event.TopicContextItemSelectedEvent;
import com.ayuget.redface.ui.event.ViewUserProfileEvent;
import com.ayuget.redface.ui.fragment.DefaultFragment;
import com.ayuget.redface.ui.fragment.DetailsDefaultFragment;
import com.ayuget.redface.ui.fragment.MetaPageFragmentBuilder;
import com.ayuget.redface.ui.fragment.TopicFragment;
import com.ayuget.redface.ui.fragment.TopicFragmentBuilder;
import com.ayuget.redface.ui.fragment.TopicListFragment;
import com.ayuget.redface.ui.fragment.TopicListFragmentBuilder;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.util.GoToPageDialog;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class TopicsActivity extends MultiPaneActivity implements TopicListFragment.OnTopicClickedListener {
    private static final String DEFAULT_FRAGMENT_TAG = "default_fragment";

    private static final String DETAILS_DEFAULT_FRAGMENT_TAG = "details_default_fragment";

    private static final String TOPICS_FRAGMENT_TAG = "topics_fragment";

    private static final String TOPIC_FRAGMENT_TAG = "topic_fragment";

    private static final String ARG_TOPIC = "topic";

    private static final String ARG_CURRENT_CATEGORY = "currentCategory";

    private SubscriptionHandler<Integer, Topic> topicDetailsSearchHandler = new SubscriptionHandler<>();

    private SubscriptionHandler<Topic, String> quoteHandler = new SubscriptionHandler<>();

    private SubscriptionHandler<User, Boolean> unflagSubscriptionHandler = new SubscriptionHandler<>();

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
            parseIntentUrl(getIntent().getData().toString());
        } else if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                parseSelectedCategoryFromBundle(extras);
            }
        }
    }

    private void parseSelectedCategoryFromBundle(Bundle intentBundle) {
        Category selectedCategory = intentBundle.getParcelable(UIConstants.ARG_SELECTED_CATEGORY);
        if (selectedCategory != null) {
            if (selectedCategory.id() == categoriesStore.getMetaCategory().id()) {
                onMyTopicsClicked();
            } else {
                onCategoryClicked(selectedCategory);
            }
        }
        intentBundle.clear();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getData() != null) {
            parseIntentUrl(intent.getData().toString());
        } else if (intent.getExtras() != null) {
            parseSelectedCategoryFromBundle(intent.getExtras());
        }
    }

    /**
     * Parses an URL received from the incoming intent upon activity creation or restart
     */
    private void parseIntentUrl(String intentUrl) {
        Timber.d("Parsing URL from intent : '%s'", intentUrl);
        urlParser.parseUrl(intentUrl).compose(RxUtils.applySchedulers())
                .subscribe(mdLink -> mdLink.ifTopicLink((category, topicId, topicPage, pagePosition) -> {
                    Timber.d("Parsed link for category='%s', topic='%d', page='%d'", category.name(), topicId, topicPage);
                    onGoToTopicEvent(new GoToTopicEvent(category, topicId, topicPage, pagePosition));
                }));
    }

    @Override
    protected void onSetupUiState() {
        Timber.d("Setting up initial state for TopicsActivity");

        DefaultFragment defaultFragment = DefaultFragment.newInstance();

        if (isTwoPaneMode()) {
            DetailsDefaultFragment detailsDefaultFragment = DetailsDefaultFragment.newInstance();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, defaultFragment, DEFAULT_FRAGMENT_TAG)
                    .replace(R.id.details_container, detailsDefaultFragment, DETAILS_DEFAULT_FRAGMENT_TAG)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, defaultFragment, DEFAULT_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onRestoreUiState(Bundle savedInstanceState) {
        Timber.d("Restoring UI state for TopicsActivity");

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
    protected void onSaveInstanceState(Bundle outState) {
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
        Timber.d("Categories have been loaded");

        if (currentCategory == null) {
            Timber.d("Loading default category");
            loadDefaultCategory();
        } else {
            Timber.d("Ignoring categories loaded event, state has been restored");
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
        if (!userManager.isActiveUserLoggedIn() && defaultCatId == CategoriesStore.META_CATEGORY_ID) {
            defaultCatId = getSettings().getNotLoggedInDefaultCategoryId();
        }

        Category defaultCategory = categoriesStore.getCategoryById(defaultCatId);
        if (defaultCategory == null) {
            Timber.w("Category '%d' not found in cache", defaultCatId);
        } else if (defaultCategory.id() == CategoriesStore.META_CATEGORY_ID) {
            onMyTopicsClicked();
        } else {
            onCategoryClicked(defaultCategory);
        }
    }

    @Override
    public void onCategoryClicked(Category category, boolean addToBackstack) {
        currentCategory = category;

        Timber.d("Loading category '%s', with topicFilter='%s'", category.name(), getSettings().getDefaultTopicFilter().toString());

        TopicListFragment topicListFragment = new TopicListFragmentBuilder(category).topicFilter(getSettings().getDefaultTopicFilter()).build();
        topicListFragment.addOnTopicClickedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, topicListFragment, TOPICS_FRAGMENT_TAG);

        if (addToBackstack) {
            transaction.addToBackStack(TOPICS_FRAGMENT_TAG);
        }

        transaction.commit();
    }

    @Override
    public void onMyTopicsClicked() {
        currentCategory = categoriesStore.getMetaCategory();

        Timber.d("Loading meta category, with topicFilter='%s'", getSettings().getDefaultTopicFilter().toString());
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

        if (topic.status() == TopicStatus.FAVORITE_NEW_CONTENT || topic.status() == TopicStatus.READ_NEW_CONTENT || topic.status() == TopicStatus.FLAGGED_NEW_CONTENT) {
            pageToLoad = topic.lastReadPage();
            pagePosition = new PagePosition(topic.lastReadPostId());
        } else {
            pageToLoad = 1;
            pagePosition = new PagePosition(PagePosition.TOP);
        }

        loadTopic(topic, pageToLoad, pagePosition);
    }

    /**
     * Loads a topic in the appropriate panel for a given page and position
     */
    protected void loadTopic(Topic topic, int page, PagePosition pagePosition) {
        Timber.d("Loading topic '%s' (page %d)", topic.title(), page);
        TopicFragment topicFragment = new TopicFragmentBuilder(page, pagePosition, topic).build();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        int topicFragmentContainer = isTwoPaneMode() ? R.id.details_container : R.id.container;

        if (!isTwoPaneMode()) {
            Timber.d("Setting slide animation for topicFragment");
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }

        transaction.replace(topicFragmentContainer, topicFragment, TOPIC_FRAGMENT_TAG);
        transaction.addToBackStack(TOPIC_FRAGMENT_TAG);
        transaction.commit();
    }

    protected void loadAnonymousTopic(Topic topic, int page, PagePosition pagePosition) {
        TopicFragment anonymousTopicFragment = new TopicFragmentBuilder(page, pagePosition, topic).build();
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
        Timber.d("On Back Pressed");
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
    @Subscribe
    public void onTopicContextItemSelected(TopicContextItemSelectedEvent event) {
        Timber.d("Received topic contextItem event : %d for topic %s", event.getItemId(), event.getTopic().title());

        switch (event.getItemId()) {
            case UIConstants.TOPIC_ACTION_GO_TO_FIRST_PAGE:
                loadTopic(event.getTopic(), 1, new PagePosition(PagePosition.TOP));
                break;
            case UIConstants.TOPIC_ACTION_GO_TO_LAST_READ_PAGE:
                loadTopic(event.getTopic(), event.getTopic().lastReadPage(), new PagePosition(event.getTopic().lastReadPostId()));
                break;
            case UIConstants.TOPIC_ACTION_GO_TO_SPECIFIC_PAGE:
                GoToPageDialog goToPageDialog = new GoToPageDialog(this, themeManager, event.getTopic().pagesCount(),
                        new GoToPageDialog.GoToPageDialogCallback() {
                            @Override
                            public void onSuccess(int pageNumber) {
                                loadTopic(event.getTopic(), pageNumber, new PagePosition(PagePosition.TOP));
                            }

                            @Override
                            public void onError() {
                                SnackbarHelper.makeError(TopicsActivity.this, R.string.invalid_page_number).show();
                            }
                        });
                goToPageDialog.show();
                break;
            case UIConstants.TOPIC_ACTION_REPLY_TO_TOPIC: {
                Intent intent = new Intent(this, ReplyActivity.class);
                intent.putExtra(ARG_TOPIC, event.getTopic());
                startActivity(intent);
                break;
            }
            case UIConstants.TOPIC_ACTION_GO_TO_LAST_PAGE:
                loadTopic(event.getTopic(), event.getTopic().pagesCount(), new PagePosition(PagePosition.BOTTOM));
                break;
            case UIConstants.TOPIC_ACTION_COPY_LINK:
                UiUtils.copyLinkToClipboard(this, mdEndpoints.topic(event.getTopic()));
                break;
            case UIConstants.TOPIC_ACTION_SHARE: {
                UiUtils.shareText(this, mdEndpoints.topic(event.getTopic()));
                break;
            }
            case UIConstants.TOPIC_ACTION_UNFLAG:
                User activeUser = userManager.getActiveUser();
                subscribe(unflagSubscriptionHandler.load(activeUser, mdService.unflagTopic(activeUser, event.getTopic()), new EndlessObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        SnackbarHelper.make(TopicsActivity.this, R.string.flag_successfully_removed).show();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Timber.e(throwable, "Error while removing flag");
                        SnackbarHelper.makeError(TopicsActivity.this, R.string.error_removing_flag).show();
                    }
                }));
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
                    Topic enhancedTopic = topic.withCategory(event.getCategory());
                    loadAnonymousTopic(enhancedTopic, event.getTopicPage(), event.getPagePosition());
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
        if (topicFragment != null && event.getTopic().id() == topicFragment.getTopic().id() && event.getPage() == topicFragment.getCurrentPage()) {
            //topicFragment.setCurrentPagePosition(event.getPagePosition());
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

    @Subscribe
    public void onEditPost(final EditPostEvent event) {
        subscribe(quoteHandler.load(event.getTopic(), mdService.getPostContent(userManager.getActiveUser(), event.getTopic(), event.getPostId()), new EndlessObserver<String>() {
            @Override
            public void onNext(String messageBBCode) {
                startEditActivity(event.getTopic(), event.getPostId(), messageBBCode);
            }
        }));
    }

    @Subscribe
    public void onViewUserProfile(final ViewUserProfileEvent event) {
        startViewUserProfileActivity(event.getUserId());
    }

    @Subscribe
    public void onPostActionEvent(final PostActionEvent event) {
        switch (event.getPostAction()) {
            case FAVORITE:
                Timber.d("About to mark post as favorite");
                markPostAsFavorite(event.getTopic(), event.getPostId());
                break;
            case DELETE:
                Timber.d("About to delete post");
                new AlertDialog.Builder(this)
                        .setTitle(R.string.post_delete_confirmation)
                        .setPositiveButton(R.string.post_delete_yes, (dialog, which) -> deletePost(event.getTopic(), event.getPostId()))
                        .setNegativeButton(R.string.post_delete_no, null)
                        .show();
                break;
            default:
                Timber.e("Action not handled");
                break;
        }
    }

    /**
     * Marks a given post as favorite
     */
    public void markPostAsFavorite(Topic topic, int postId) {
        Toast.makeText(TopicsActivity.this, R.string.marking_as_favorite_in_progress, Toast.LENGTH_SHORT).show();

        subscribe(mdService.markPostAsFavorite(userManager.getActiveUser(), topic, postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EndlessObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean success) {
                        if (success) {
                            Toast.makeText(TopicsActivity.this, R.string.mark_as_favorite_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TopicsActivity.this, R.string.mark_as_favorite_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Timber.e(throwable, "Unexpected error while marking a post as favorite");
                        Toast.makeText(TopicsActivity.this, R.string.mark_as_favorite_failed, Toast.LENGTH_SHORT).show();
                    }
                }));
    }
}
