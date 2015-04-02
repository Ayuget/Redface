package com.ayuget.redface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.ayuget.redface.BuildConfig;
import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.R;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.util.ViewServer;
import com.crashlytics.android.Crashlytics;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;


public class BaseActivity extends ActionBarActivity {
    private static final String LOG_TAG = BaseActivity.class.getSimpleName();

    @Inject
    RedfaceSettings settings;

    @Inject
    Bus bus;

    @Inject
    ThemeManager themeManager;

    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crashlytics.start(this);

        RedfaceApp app = RedfaceApp.get(this);
        app.inject(this);
        bus.register(this);

        initializeTheme();

        // Proper RxJava subscriptions management with CompositeSubscription
        subscriptions = new CompositeSubscription();

        if (BuildConfig.DEBUG) {
            ViewServer.get(this).addWindow(this);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
    }

    /**
     * Sets the content view and calls the appropriate Ui initialization callbacks
     * based on the saved instance state
     */
    public void setContentView(int layoutResID, Bundle savedInstanceState) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);

        onInitUiState();

        if (savedInstanceState == null) {
            onSetupUiState();
        }
        else {
            onRestoreUiState(savedInstanceState);
        }
    }

    /**
     * Initializes UI state. Always called when setContentView(layoutResID, savedInstanceState) is called
     */
    protected void onInitUiState() {
    }

    /**
     * Sets up UI state, called if no saved instance state was provided when the activity was created
     */
    protected void onSetupUiState() {
    }

    /**
     * Custom callback to restore (mostly fragments) state, because onRestoreInstanceState() is called
     * too late in the activity lifecycle
     * @param savedInstanceState saved state
     */
    protected void onRestoreUiState(Bundle savedInstanceState) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (BuildConfig.DEBUG) {
            ViewServer.get(this).removeWindow(this);
        }

        bus.unregister(this);
        subscriptions.unsubscribe();
    }

    protected void initializeTheme() {
        getWindow().setBackgroundDrawable(null);
        setTheme(themeManager.getActiveThemeStyle());
    }

    protected void subscribe(Subscription s) {
        subscriptions.add(s);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (themeManager.isRefreshNeeded()) {
            themeManager.setRefreshNeeded(false);
            refreshTheme();
        }

        if (BuildConfig.DEBUG) {
            ViewServer.get(this).setFocusedWindow(this);
        }
    }

    public void refreshTheme() {
        finish();
        Intent intent = new Intent(this, TopicsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public DrawerLayout getDrawerLayout() {
        return (DrawerLayout) findViewById(R.id.hfr_drawer_layout);
    }

    public RedfaceSettings getSettings() {
        return settings;
    }
}
