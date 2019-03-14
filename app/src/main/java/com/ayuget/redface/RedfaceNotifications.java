package com.ayuget.redface;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.ayuget.redface.privatemessages.PrivateMessagesWorker;
import com.ayuget.redface.settings.RedfaceSettings;

import java.util.concurrent.TimeUnit;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class RedfaceNotifications {
    private static final String PRIVATE_MESSAGES_WORKER_TAG = "PRIVATE_MESSAGES";
    public static final String PRIVATE_MESSAGES_CHANNEL_ID = "PRIVATE_MESSAGES";
    public static final int PRIVATE_MESSAGES_SUMMARY_ID = 0;
    public static final String PRIVATE_MESSAGES_GROUP = "com.ayuget.redface.PRIVATE_MESSAGES";

    public static void setupNotifications(Context context) {
        RedfaceSettings appSettings = ((RedfaceApp) context.getApplicationContext()).getFromGraph(RedfaceSettings.class);

        registerNotificationChannels(context);
        launchPrivateMessagesWorker(appSettings.getPrivateMessagesPollingFrequency());
    }

    private static void registerNotificationChannels (Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.private_messages_notification_channel);
            String description = context.getString(R.string.pref_enable_pm_notifications_summary);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(PRIVATE_MESSAGES_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static void launchPrivateMessagesWorker(int privateMessagesPollingFrequency) {
        PeriodicWorkRequest privateMessagesWorkRequest =
                new PeriodicWorkRequest.Builder(PrivateMessagesWorker.class, privateMessagesPollingFrequency, TimeUnit.MINUTES)
                        .addTag(PRIVATE_MESSAGES_WORKER_TAG)
                        .build();

        WorkManager.getInstance()
                .enqueue(privateMessagesWorkRequest);
    }

    public static void disablePrivateMessagesNotifications() {
        WorkManager.getInstance()
                .cancelAllWorkByTag(RedfaceNotifications.PRIVATE_MESSAGES_GROUP);
    }

    public static void updateOrLaunchPrivateMessagesWorker(int frequencyInMinutes) {
        disablePrivateMessagesNotifications();
        launchPrivateMessagesWorker(frequencyInMinutes);
    }
}
