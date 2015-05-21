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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.settings.ProxySettingsChangedEvent;
import com.ayuget.redface.settings.SettingsConstants;
import com.ayuget.redface.ui.event.ThemeChangedEvent;
import com.google.common.collect.ObjectArrays;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

public class NestedPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = NestedPreferenceFragment.class.getSimpleName();

    @Inject
    Bus bus;

    @Inject
    CategoriesStore categoriesStore;

    @Inject
    UserManager userManager;

    @Arg
    String fragmentKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read fragment args from bundle
        FragmentArgs.inject(this);

        // Inject dependencies
        RedfaceApp app = RedfaceApp.get(getActivity());
        app.inject(this);

        checkPreferenceResource();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        initSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void checkPreferenceResource() {
        // Load the preferences from an XML resource
        switch (fragmentKey) {
            case SettingsConstants.KEY_GENERAL_PREFERENCES:
                addPreferencesFromResource(R.xml.general_preferences);
                populateDefaultCategoriesPref();
                break;

            case SettingsConstants.KEY_APPEARANCE_PREFERENCES:
                addPreferencesFromResource(R.xml.appearance_preferences);
                break;
            case SettingsConstants.KEY_NETWORK_PREFERENCES:
                addPreferencesFromResource(R.xml.network_preferences);
                break;
            default:
                break;
        }
    }

    protected void populateDefaultCategoriesPref() {
        ListPreference defaultCatPreference = (ListPreference) findPreference(SettingsConstants.KEY_DEFAULT_CATEGORY);

        List<Category> guestCategories = categoriesStore.getCategories(userManager.getGuestUser());

        if (guestCategories != null && defaultCatPreference != null) {
            CharSequence entries[] = new String[guestCategories.size()];
            CharSequence values[] = new String[guestCategories.size()];

            int i = 0;
            for (Category category : guestCategories) {
                entries[i] = category.getName();
                values[i] = Integer.toString(category.getId());
                i++;
            }

            defaultCatPreference.setEntries(ObjectArrays.concat(defaultCatPreference.getEntries(), entries, CharSequence.class));
            defaultCatPreference.setEntryValues(ObjectArrays.concat(defaultCatPreference.getEntryValues(), values, CharSequence.class));
        }
    }

    protected void initSummary() {
        for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initPrefsSummary(getPreferenceManager().getSharedPreferences(), getPreferenceScreen().getPreference(i));
        }
    }

    protected void initPrefsSummary(SharedPreferences sharedPreferences, Preference pref) {
        if (pref instanceof PreferenceCategory) {
            PreferenceCategory pCat = (PreferenceCategory) pref;
            for (int i = 0; i < pCat.getPreferenceCount(); i++) {
                initPrefsSummary(sharedPreferences, pCat.getPreference(i));
            }
        } else {
            updatePreferenceSummary(sharedPreferences, pref);
        }
    }

    protected void updatePreferenceSummary(SharedPreferences sharedPreferences, Preference pref) {
        if (pref == null) {
            return;
        }

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            listPref.setSummary(listPref.getEntry());
        }
        else if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            editTextPref.setSummary(editTextPref.getText());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(LOG_TAG, String.format("Settings key '%s' changed", key));
        boolean proxySettingsChanged = key.equals(SettingsConstants.KEY_ENABLE_PROXY) || key.equals(SettingsConstants.KEY_PROXY_HOST) || key.equals(SettingsConstants.KEY_PROXY_PORT);

        if (proxySettingsChanged) {
            bus.post(new ProxySettingsChangedEvent());
        }

        if (key.equals(SettingsConstants.KEY_THEME)) {
            Log.d(LOG_TAG, "Posting theme changed event");
            bus.post(new ThemeChangedEvent());
        }

        updatePreferenceSummary(sharedPreferences, findPreference(key));
    }

}
