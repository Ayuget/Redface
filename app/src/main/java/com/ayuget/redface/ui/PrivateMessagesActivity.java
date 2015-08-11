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

package com.ayuget.redface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.ui.fragment.DefaultFragment;
import com.ayuget.redface.ui.fragment.DetailsDefaultFragment;
import com.ayuget.redface.ui.fragment.PrivateMessageListFragment;
import com.ayuget.redface.ui.fragment.TopicListFragment;

public class PrivateMessagesActivity extends BaseDrawerActivity implements PrivateMessageListFragment.OnPrivateMessageClickedListener {
    private static final String LOG_TAG = PrivateMessagesActivity.class.getSimpleName();

    private static final String DEFAULT_FRAGMENT_TAG = "default_fragment";

    private static final String DETAILS_DEFAULT_FRAGMENT_TAG = "details_default_fragment";

    private static final String PM_LIST_FRAGMENT_TAG = "private_messages_list_fragment";

    private static final String PM_FRAGMENT_TAG = "private_message_fragment";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPaneMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_private_messages, savedInstanceState);
    }

    @Override
    protected void onInitUiState() {
        Log.d(LOG_TAG, "Initializing state");

        if (findViewById(R.id.details_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            twoPaneMode = true;
        }
    }

    @Override
    protected void onSetupUiState() {
        Log.d(LOG_TAG, "Setting up initial state");

        PrivateMessageListFragment pmListFragment = PrivateMessageListFragment.newInstance();

        if (twoPaneMode) {
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
        // TODO
    }
}
