package com.ayuget.redface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;

public class EditPostActivity extends ReplyActivity {
    private static final String LOG_TAG = EditPostActivity.class.getSimpleName();

    private SubscriptionHandler<User, Response> editSubscriptionHandler = new SubscriptionHandler<>();

    private int editedPostId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            editedPostId = intent.getIntExtra(UIConstants.ARG_EDITED_POST_ID, 0);
        }
    }

    @Override
    protected boolean canSwitchUser() {
        return false;
    }

    @Override
    protected void postReply() {
        User activeUser = userManager.getActiveUser();
        String message = replyEditText.getText().toString();

        subscribe(editSubscriptionHandler.load(activeUser, mdService.editPost(activeUser, getCurrentTopic(), editedPostId, message, true), new EndlessObserver<Response>() {
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
                Log.e(LOG_TAG, "Unknown exception while editing post", throwable);
                onReplyFailure();
            }
        }));
    }

    protected void onReplySuccess() {
        clearResponseFromCache(userManager.getActiveUser());
        replyToActivity(RESULT_OK, true);
    }

    protected void onReplyFailure() {
        replyToActivity(UIConstants.REPLY_RESULT_KO, true);
    }
}
