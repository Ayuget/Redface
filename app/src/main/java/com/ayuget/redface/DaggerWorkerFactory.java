package com.ayuget.redface;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.privatemessages.PrivateMessagesNotificationHandler;
import com.ayuget.redface.privatemessages.PrivateMessagesWorker;
import com.ayuget.redface.settings.RedfaceSettings;

import javax.inject.Inject;

import androidx.work.ListenableWorker;
import androidx.work.WorkerFactory;
import androidx.work.WorkerParameters;

public class DaggerWorkerFactory extends WorkerFactory {
    private final UserManager userManager;
    private final MDService mdService;
    private final RedfaceSettings appSettings;
    private final PrivateMessagesNotificationHandler privateMessagesNotificationHandler;

    @Inject public DaggerWorkerFactory(UserManager userManager, MDService mdService, RedfaceSettings appSettings, PrivateMessagesNotificationHandler privateMessagesNotificationHandler) {
        this.userManager = userManager;
        this.mdService = mdService;
        this.appSettings = appSettings;
        this.privateMessagesNotificationHandler = privateMessagesNotificationHandler;
    }

    @Nullable
    @Override
    public ListenableWorker createWorker(@NonNull Context appContext, @NonNull String workerClassName, @NonNull WorkerParameters workerParameters) {
        PrivateMessagesWorker privateMessagesWorker = new PrivateMessagesWorker(appContext, workerParameters);

        privateMessagesWorker.setUserManager(userManager);
        privateMessagesWorker.setMdService(mdService);
        privateMessagesWorker.setSettings(appSettings);
        privateMessagesWorker.setPrivateMessagesNotificationHandler(privateMessagesNotificationHandler);

        return privateMessagesWorker;
    }
}
