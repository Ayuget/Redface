package com.ayuget.redface;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.privatemessages.PrivateMessagesWorker;
import com.ayuget.redface.settings.RedfaceSettings;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class RedfaceNotifications {
    public static final String PRIVATE_MESSAGES_CHANNEL_ID = "PRIVATE_MESSAGES";
    public static final String PRIVATE_MESSAGES_WORK_TASK_TAG = "PRIVATE_MESSAGES";
    public static final String PRIVATE_MESSAGES_GROUP = "com.ayuget.redface.PRIVATE_MESSAGES";

    public static void setupNotifications(Context context) {
        RedfaceSettings appSettings = ((RedfaceApp) context.getApplicationContext()).getSettings();

        registerNotificationChannels(context);
        launchPrivateMessagesWorker(context, appSettings.getPrivateMessagesPollingFrequency());
    }

    private static void registerNotificationChannels(Context context) {
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

    private static void launchPrivateMessagesWorker(Context context, int privateMessagesPollingFrequency) {
        Timber.d("Launching private messages worker with a polling frequency of %d minutes", privateMessagesPollingFrequency);

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        PRIVATE_MESSAGES_WORK_TASK_TAG,
                        ExistingPeriodicWorkPolicy.KEEP,
                        new PeriodicWorkRequest.Builder(PrivateMessagesWorker.class, privateMessagesPollingFrequency, TimeUnit.MINUTES, 5, TimeUnit.MINUTES).build()
                );
    }

    public static void disablePrivateMessagesNotifications() {
        WorkManager.getInstance()
                .cancelAllWorkByTag(RedfaceNotifications.PRIVATE_MESSAGES_GROUP);
    }

    public static void updateOrLaunchPrivateMessagesWorker(Context context, int frequencyInMinutes) {
        disablePrivateMessagesNotifications();
        launchPrivateMessagesWorker(context, frequencyInMinutes);
    }

    public static void dismissPrivateMessageNotificationIfNeeded(Context context, PrivateMessage privateMessage) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel((int) privateMessage.getId());
    }
}
