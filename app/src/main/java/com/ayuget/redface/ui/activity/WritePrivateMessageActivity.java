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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.SmileySelectedEvent;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.squareup.otto.Subscribe;

import timber.log.Timber;

public class WritePrivateMessageActivity extends ReplyActivity {
    private EditText recipientUsername;

    private EditText pmSubject;

    private SubscriptionHandler<User, Response> newPMSubscriptionHandler = new SubscriptionHandler<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            String initialRecipient = intent.getStringExtra(UIConstants.ARG_PM_RECIPIENT);
            if (initialRecipient != null) {
                recipientUsername.setText(initialRecipient);
            }
        }

        if (savedInstanceState != null) {
            savedInstanceState.clear();
        }
    }

    @Override
    protected boolean canSwitchUser() {
        return false;
    }

    @Override
    protected View setupUserView(LayoutInflater inflater, User user) {
        View recipientView = inflater.inflate(R.layout.dialog_compose_pm, actionsToolbar, false);
        recipientUsername = (EditText) recipientView.findViewById(R.id.private_message_recipient);
        pmSubject = (EditText) recipientView.findViewById(R.id.private_message_subject);
        return recipientView;
    }

    @Override
    protected void postReply() {
        if (recipientUsername == null || recipientUsername.getText().length() == 0) {
            hideSendingMessageSpinner();
            SnackbarHelper.makeError(this, R.string.pm_no_recipient).show();
        }
        else if (pmSubject == null || pmSubject.getText().length() == 0) {
            hideSendingMessageSpinner();
            SnackbarHelper.makeError(this, R.string.pm_no_subject).show();
        }

        String pmRecipient = recipientUsername.getText().toString();
        String subject = pmSubject.getText().toString();
        User activeUser = userManager.getActiveUser();
        String message = replyEditText.getText().toString();

        subscribe(newPMSubscriptionHandler.load(activeUser, mdService.sendNewPrivateMessage(activeUser, subject, pmRecipient, message, true), new EndlessObserver<Response>() {
            @Override
            public void onNext(Response response) {
                if (response.isSuccessful()) {
                    onReplySuccess();
                } else {
                    onReplyFailure();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Timber.e(throwable, "Unknown exception while sending new private message");
                onReplyFailure();
            }
        }));
    }

    /**
     * Overrides the implementation from ReplyActivity because Otto subscriptions don't play
     * well with inheritance
     */
    @Subscribe
    public void smileySelected(SmileySelectedEvent event) {
        insertSmiley(event.getSmileyCode());
    }

    @Override
    protected void onReplySuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onReplyFailure() {
        setResult(UIConstants.REPLY_RESULT_KO);
        finish();
    }
}
