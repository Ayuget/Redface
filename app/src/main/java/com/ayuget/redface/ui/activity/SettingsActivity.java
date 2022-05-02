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
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.event.NestedPreferenceSelectedEvent;
import com.ayuget.redface.ui.event.ThemeChangedEvent;
import com.ayuget.redface.ui.fragment.HomePreferenceFragment;
import com.ayuget.redface.ui.fragment.NestedPreferenceFragmentBuilder;
import com.squareup.otto.Subscribe;

import butterknife.BindView;

public class SettingsActivity extends BaseActivity {
    private static final String NESTED_FRAGMENT_TAG = "nested_fragment";

    @BindView(R.id.toolbar_actionbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar.setTitle(getResources().getString(R.string.settings_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomePreferenceFragment())
                    .commit();
        } else {
            savedInstanceState.clear();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Subscribe
    public void onNestedPreferenceFragmentSelected(NestedPreferenceSelectedEvent event) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new NestedPreferenceFragmentBuilder(event.getFragmentKey()).build(), NESTED_FRAGMENT_TAG)
                .addToBackStack(NESTED_FRAGMENT_TAG)
                .commit();
    }

    @Subscribe
    public void onThemeChanged(ThemeChangedEvent event) {
        themeManager.setRefreshNeeded(true);
    }
}
