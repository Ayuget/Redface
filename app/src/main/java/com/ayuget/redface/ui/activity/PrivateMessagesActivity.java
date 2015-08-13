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

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.ui.event.EditPostEvent;
import com.ayuget.redface.ui.event.QuotePostEvent;
import com.ayuget.redface.ui.fragment.DetailsDefaultFragment;
import com.ayuget.redface.ui.fragment.PrivateMessageListFragment;
import com.ayuget.redface.ui.fragment.TopicFragment;
import com.ayuget.redface.ui.fragment.TopicFragmentBuilder;
import com.ayuget.redface.ui.misc.PagePosition;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

public class PrivateMessagesActivity extends MultiPaneActivity implements PrivateMessageListFragment.OnPrivateMessageClickedListener {
    private static final String LOG_TAG = PrivateMessagesActivity.class.getSimpleName();

    private static final String DEFAULT_FRAGMENT_TAG = "default_fragment";

    private static final String DETAILS_DEFAULT_FRAGMENT_TAG = "details_default_fragment";

    private static final String PM_LIST_FRAGMENT_TAG = "private_messages_list_fragment";

    private static final String PM_FRAGMENT_TAG = "private_message_fragment";

    private SubscriptionHandler<Topic, String> quoteHandler = new SubscriptionHandler<>();

    @Inject
    CategoriesStore categoriesStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_private_messages, savedInstanceState);
    }

    @Override
    protected void onSetupUiState() {
        Log.d(LOG_TAG, "Setting up initial state");

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
        Log.d(LOG_TAG, "Restoring UI state");

        PrivateMessageListFragment pmListFragment = (PrivateMessageListFragment) getSupportFragmentManager().findFragmentByTag(PM_LIST_FRAGMENT_TAG);
        if (pmListFragment != null) {
            pmListFragment.setOnPrivateMessageClickedListener(this);
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
     * Code is duplicated with TopicsActivity because Otto doesn't support settings @Subscribe annotations
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
     * Loads a private message in the appropriate pane.
     */
    private void loadPrivateMessage(PrivateMessage privateMessage, int page, PagePosition pagePosition) {
        Log.d(LOG_TAG, String.format("Loading private message '%s' at page '%d'", privateMessage.getSubject(), page));

        // Mask private message as a regular topic (kinda ugly, btw...)
        Topic pmAsTopic = privateMessage.asTopic();
        pmAsTopic.setCategory(categoriesStore.getPrivateMessagesCategory());

        TopicFragment topicFragment = new TopicFragmentBuilder(page, pmAsTopic).currentPagePosition(pagePosition).build();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        int topicFragmentContainer = isTwoPaneMode() ? R.id.details_container : R.id.container;

        if (!isTwoPaneMode()) {
            Log.d(LOG_TAG, "Setting slide animation for topicFragment (private message)");
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }

        transaction.replace(topicFragmentContainer, topicFragment, PM_FRAGMENT_TAG);
        transaction.addToBackStack(PM_FRAGMENT_TAG);
        transaction.commit();
    }
}
