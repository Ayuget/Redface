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

package com.ayuget.redface.ui.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.ayuget.redface.R;
import com.ayuget.redface.settings.SettingsConstants;
import com.ayuget.redface.ui.event.NestedPreferenceSelectedEvent;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import timber.log.Timber;

@FragmentWithArgs
public class HomePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    @Inject
    Bus bus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read fragment args from bundle
        FragmentArgs.inject(this);

        // Inject dependencies
        AndroidInjection.inject(this);

        addPreferencesFromResource(R.xml.home_preferences);

        Preference generalPreference = findPreference(SettingsConstants.KEY_GENERAL_PREFERENCES);
        if (generalPreference != null) {
            generalPreference.setOnPreferenceClickListener(this);
        }

        Preference appearancePreference = findPreference(SettingsConstants.KEY_APPEARANCE_PREFERENCES);
        if (appearancePreference != null) {
            appearancePreference.setOnPreferenceClickListener(this);
        }

        Preference networkPreference = findPreference(SettingsConstants.KEY_NETWORK_PREFERENCES);
        if (networkPreference != null) {
            networkPreference.setOnPreferenceClickListener(this);
        }

        Preference blacklistPreference = findPreference(SettingsConstants.KEY_BLACKLIST_PREFERENCES);
        if (blacklistPreference != null) {
            blacklistPreference.setOnPreferenceClickListener(this);
        }

        if (savedInstanceState != null) {
            savedInstanceState.clear();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Timber.d("Preference '%s' clicked", preference.getKey());
        bus.post(new NestedPreferenceSelectedEvent(preference.getKey()));
        return false;
    }
}
