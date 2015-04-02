package com.ayuget.redface.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class RedfaceAuthenticatorService extends Service {
    private static final String LOG_TAG = RedfaceAuthenticatorService.class.getSimpleName();

    private RedfaceAccountAuthenticator accountAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Service started");
        accountAuthenticator = new RedfaceAccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return accountAuthenticator.getIBinder();
    }
}
