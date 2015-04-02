package com.ayuget.redface.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.event.NestedPreferenceSelectedEvent;
import com.ayuget.redface.ui.event.ThemeChangedEvent;
import com.ayuget.redface.ui.fragment.HomePreferenceFragment;
import com.ayuget.redface.ui.fragment.NestedPreferenceFragmentBuilder;

import com.squareup.otto.Subscribe;
import butterknife.InjectView;

public class SettingsActivity extends BaseActivity {
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    private static final String NESTED_FRAGMENT_TAG = "nested_fragment";

    @InjectView(R.id.toolbar_actionbar)
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

    @Subscribe public void onNestedPreferenceFragmentSelected(NestedPreferenceSelectedEvent event) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new NestedPreferenceFragmentBuilder(event.getFragmentKey()).build(), NESTED_FRAGMENT_TAG)
                .addToBackStack(NESTED_FRAGMENT_TAG)
                .commit();
    }

    @Subscribe public void onThemeChanged(ThemeChangedEvent event) {
        themeManager.setRefreshNeeded(true);
    }
}
