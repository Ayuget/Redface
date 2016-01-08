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
import android.util.Log;
import android.widget.Toast;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.PageRefreshRequestEvent;
import com.ayuget.redface.ui.misc.SnackbarHelper;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MultiPaneActivity extends BaseDrawerActivity {
    private static final String ARG_TOPIC = "topic";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPaneMode = false;

    /**
     * Flag to prevent reply activity from being launched multiple times. That activity definitely
     * needs to be rewritten properly as a custom view (bonus: it would probably be also easier to
     * add fancy animations...)
     */
    boolean canLaunchReplyActivity = true;

    /**
     * Event which will be fired once a reply / edit is successfully posted to the server
     */
    private PageRefreshRequestEvent refreshRequestEvent;

    @Inject
    MDService mdService;

    @Override
    protected void onInitUiState() {
        Timber.d("Initializing state");

        if (findViewById(R.id.details_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            twoPaneMode = true;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (refreshRequestEvent != null) {
            Timber.d("Posting refreshRequestEvent");
            bus.post(refreshRequestEvent);
            refreshRequestEvent = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        canLaunchReplyActivity = true;

        if (requestCode == UIConstants.REPLY_REQUEST_CODE) {
            boolean wasEdit = (data != null) && data.getBooleanExtra(UIConstants.ARG_REPLY_WAS_EDIT, false);

            if (data != null && resultCode == RESULT_OK) {
                SnackbarHelper.make(this, wasEdit ? R.string.message_successfully_edited : R.string.reply_successfully_posted).show();

                // Refresh page
                Topic topic = data.getParcelableExtra(UIConstants.ARG_REPLY_TOPIC);

                if (topic == null) {
                    Timber.e("topic is null in onActivityResult");
                }
                else {
                    Timber.d("Requesting refresh for topic : %s", topic.getSubject());

                    // Deferring event posting until onResume() is called, otherwise inner fragments
                    // won't get the event.
                    refreshRequestEvent = new PageRefreshRequestEvent(topic);
                }
            }
            else if (resultCode == UIConstants.REPLY_RESULT_KO) {
                SnackbarHelper.makeError(this, wasEdit? R.string.message_edit_failure : R.string.reply_post_failure).show();
            }
        }
        else if (requestCode == UIConstants.NEW_PM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                SnackbarHelper.make(this, R.string.pm_successfully_sent).show();
                requestMasterPaneRefresh();
            }
            else if (resultCode == UIConstants.REPLY_RESULT_KO) {
                SnackbarHelper.makeError(this, R.string.error_sending_pm).show();
            }
        }
    }

    /**
     * Request a data refresh for the master pane
     */
    protected void requestMasterPaneRefresh() {
    }

    /**
     * Starts the reply activity with or without an initial content
     */
    protected synchronized void startReplyActivity(Topic topic, String initialContent) {
        if (canLaunchReplyActivity()) {
            setCanLaunchReplyActivity(false);

            Intent intent = new Intent(this, ReplyActivity.class);
            intent.putExtra(ARG_TOPIC, topic);

            if (initialContent != null) {
                intent.putExtra(UIConstants.ARG_REPLY_CONTENT, initialContent);
            }

            startActivityForResult(intent, UIConstants.REPLY_REQUEST_CODE);
        }
    }

    /**
     * Starts the edit activity
     */
    protected synchronized void startEditActivity(Topic topic, int postId, String actualContent) {
        if (canLaunchReplyActivity) {
            setCanLaunchReplyActivity(false);

            Intent intent = new Intent(this, EditPostActivity.class);

            intent.putExtra(ARG_TOPIC, topic);
            intent.putExtra(UIConstants.ARG_EDITED_POST_ID, postId);
            intent.putExtra(UIConstants.ARG_REPLY_CONTENT, actualContent);

            startActivityForResult(intent, UIConstants.REPLY_REQUEST_CODE);
        }
    }

    /**
     * Deletes a post
     */
    public void deletePost(final Topic topic, int postId) {
        Toast.makeText(this, R.string.delete_post_in_progress, Toast.LENGTH_SHORT).show();

        subscribe(mdService.deletePost(userManager.getActiveUser(), topic, postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EndlessObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean success) {
                        if (success) {
                            bus.post(new PageRefreshRequestEvent(topic));
                        }
                        else {
                            Toast.makeText(MultiPaneActivity.this, R.string.delete_post_failed, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Timber.e(throwable, "Unexpected error while deleting post");
                        Toast.makeText(MultiPaneActivity.this, R.string.delete_post_failed, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    public boolean isTwoPaneMode() {
        return twoPaneMode;
    }

    public boolean canLaunchReplyActivity() {
        return canLaunchReplyActivity;
    }

    public void setCanLaunchReplyActivity(boolean canLaunchReplyActivity) {
        this.canLaunchReplyActivity = canLaunchReplyActivity;
    }
}
