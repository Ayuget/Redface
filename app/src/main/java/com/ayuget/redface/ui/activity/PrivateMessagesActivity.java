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

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ayuget.redface.R;
import com.ayuget.redface.RedfaceNotifications;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.EditPostEvent;
import com.ayuget.redface.ui.event.PostActionEvent;
import com.ayuget.redface.ui.event.PrivateMessageContextItemSelectedEvent;
import com.ayuget.redface.ui.event.QuotePostEvent;
import com.ayuget.redface.ui.fragment.DetailsDefaultFragment;
import com.ayuget.redface.ui.fragment.PrivateMessageListFragment;
import com.ayuget.redface.ui.fragment.TopicFragment;
import com.ayuget.redface.ui.fragment.TopicFragmentBuilder;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.util.GoToPageDialog;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import timber.log.Timber;

public class PrivateMessagesActivity extends MultiPaneActivity implements PrivateMessageListFragment.OnPrivateMessageClickedListener {
    private static final String DEFAULT_FRAGMENT_TAG = "default_fragment";

    private static final String DETAILS_DEFAULT_FRAGMENT_TAG = "details_default_fragment";

    private static final String PM_LIST_FRAGMENT_TAG = "private_messages_list_fragment";

    private static final String PM_FRAGMENT_TAG = "private_message_fragment";

    private static final String ARG_TOPIC = "topic";

    private SubscriptionHandler<Topic, String> quoteHandler = new SubscriptionHandler<>();

    @Inject
    CategoriesStore categoriesStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_private_messages, savedInstanceState);

        if (getIntent() != null) {
            PrivateMessage privateMessage = getIntent().getParcelableExtra(UIConstants.ARG_SELECTED_PM);

            if (privateMessage != null) {
                Timber.d("Received private message : '%s'", privateMessage);
                loadPrivateMessage(privateMessage, privateMessage.getPagesCount(), PagePosition.bottom());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getExtras() != null) {
            Timber.d("Trying to display private message from intent");
            PrivateMessage privateMessage = intent.getExtras().getParcelable(UIConstants.ARG_SELECTED_PM);

            if (privateMessage != null) {
                Timber.d("Received private message : '%s'", privateMessage);

                loadPrivateMessage(privateMessage, privateMessage.getPagesCount(), PagePosition.bottom());
            }
        }
    }

    @Override
    protected void onSetupUiState() {
        Timber.d("Setting up initial state");

        PrivateMessageListFragment pmListFragment = PrivateMessageListFragment.newInstance();
        pmListFragment.setOnPrivateMessageClickedListener(this);

        if (isTwoPaneMode()) {
            DetailsDefaultFragment detailsDefaultFragment = DetailsDefaultFragment.newInstance();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, pmListFragment, DEFAULT_FRAGMENT_TAG)
                    .replace(R.id.details_container, detailsDefaultFragment, DETAILS_DEFAULT_FRAGMENT_TAG)
                    .commit();
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, pmListFragment, DEFAULT_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onRestoreUiState(Bundle savedInstanceState) {
        Timber.d("Restoring UI state");

        PrivateMessageListFragment pmListFragment = (PrivateMessageListFragment) getSupportFragmentManager().findFragmentByTag(PM_LIST_FRAGMENT_TAG);
        if (pmListFragment != null) {
            pmListFragment.setOnPrivateMessageClickedListener(this);
        } else {
            Fragment defaultFragment = getSupportFragmentManager().findFragmentByTag(DEFAULT_FRAGMENT_TAG);
            if (defaultFragment instanceof PrivateMessageListFragment) {
                ((PrivateMessageListFragment)defaultFragment).setOnPrivateMessageClickedListener(this);
            }
        }
    }

    @Override
    public void onPrivateMessageClicked(PrivateMessage privateMessage) {
        int pageToLoad;
        PagePosition pagePosition;

        if (privateMessage.hasUnreadMessages()) {
            pageToLoad = privateMessage.getPagesCount();
            pagePosition = new PagePosition(PagePosition.BOTTOM);
        }
        else {
            pageToLoad = 1;
            pagePosition = new PagePosition(PagePosition.BOTTOM);
        }

        loadPrivateMessage(privateMessage, pageToLoad, pagePosition);
    }

    @Subscribe
    public void onPrivateMessageContextItemSelected(PrivateMessageContextItemSelectedEvent event) {
        Timber.d("Received private message contextItem event : %d for PM %s", event.getItemId(), event.getPrivateMessage().toString());

        switch (event.getItemId()) {
            case UIConstants.TOPIC_ACTION_GO_TO_FIRST_PAGE:
                loadPrivateMessage(event.getPrivateMessage(), 1, new PagePosition(PagePosition.TOP));
                break;
            case UIConstants.TOPIC_ACTION_GO_TO_SPECIFIC_PAGE:
                GoToPageDialog goToPageDialog = new GoToPageDialog(this, themeManager, event.getPrivateMessage().getPagesCount(),
                        new GoToPageDialog.GoToPageDialogCallback() {
                            @Override
                            public void onSuccess(int pageNumber) {
                                loadPrivateMessage(event.getPrivateMessage(), pageNumber, new PagePosition(PagePosition.TOP));
                            }

                            @Override
                            public void onError() {
                                SnackbarHelper.makeError(PrivateMessagesActivity.this, R.string.invalid_page_number).show();
                            }
                        });
                goToPageDialog.show();
                break;
            case UIConstants.TOPIC_ACTION_GO_TO_LAST_PAGE:
                loadPrivateMessage(event.getPrivateMessage(),
                        event.getPrivateMessage().getPagesCount(),
                        new PagePosition(PagePosition.BOTTOM));
                break;
            case UIConstants.TOPIC_ACTION_REPLY_TO_TOPIC:
                Topic pmAsTopic = event.getPrivateMessage().asTopic()
                        .withCategory(categoriesStore.getPrivateMessagesCategory());
                Intent intent = new Intent(this, ReplyActivity.class);
                intent.putExtra(ARG_TOPIC, pmAsTopic);
                startActivity(intent);
                break;
        }

    }

    /**
     * Code is duplicated with TopicsActivity because Otto doesn't support settings @Subscribe annotations
     * on base classes (pull request #135 still not merged)
     */
    @Subscribe
    public void onQuotePost(final QuotePostEvent event) {
        subscribe(quoteHandler.load(event.getTopic(), mdService.getQuote(userManager.getActiveUser(), event.getTopic(), event.getPostId()), new EndlessObserver<String>() {
            @Override
            public void onNext(String quoteBBCode) {
                startReplyActivity(event.getTopic(), quoteBBCode);
            }
        }));
    }

    /**
     * fixme: Code is duplicated with TopicsActivity because Otto doesn't support settings @Subscribe annotations
     * on base classes (pull request #135 still not merged)
     */
    @Subscribe public void onEditPost(final EditPostEvent event) {
        subscribe(quoteHandler.load(event.getTopic(), mdService.getPostContent(userManager.getActiveUser(), event.getTopic(), event.getPostId()), new EndlessObserver<String>() {
            @Override
            public void onNext(String messageBBCode) {
                startEditActivity(event.getTopic(), event.getPostId(), messageBBCode);
            }
        }));
    }

    /**
     * fixme: Code is duplicated with TopicsActivity because Otto doesn't support settings @Subscribe annotations
     * on base classes (pull request #135 still not merged)
     */
    @Subscribe public void onPostActionEvent(final PostActionEvent event) {
        switch (event.getPostAction()) {
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
     * Loads a private message in the appropriate pane.
     */
    private void loadPrivateMessage(PrivateMessage privateMessage, int page, PagePosition pagePosition) {
        Timber.d("Loading private message '%s' at page '%d'", privateMessage.getSubject(), page);

        RedfaceNotifications.dismissPrivateMessageNotificationIfNeeded(this, privateMessage);

        // Mask private message as a regular topic (kinda ugly, btw...)
        Topic pmAsTopic = privateMessage.asTopic().withCategory(categoriesStore.getPrivateMessagesCategory());

        TopicFragment topicFragment = new TopicFragmentBuilder(page, pagePosition, pmAsTopic).build();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        int topicFragmentContainer = isTwoPaneMode() ? R.id.details_container : R.id.container;

        if (!isTwoPaneMode()) {
            Timber.d("Setting slide animation for topicFragment (private message)");
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }

        transaction.replace(topicFragmentContainer, topicFragment, PM_FRAGMENT_TAG);
        transaction.addToBackStack(PM_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    protected void requestMasterPaneRefresh() {
        PrivateMessageListFragment privateMessageListFragment = (PrivateMessageListFragment) getSupportFragmentManager().findFragmentByTag(PM_LIST_FRAGMENT_TAG);
        if (privateMessageListFragment != null) {
            privateMessageListFragment.refreshData();
        }
    }
}
