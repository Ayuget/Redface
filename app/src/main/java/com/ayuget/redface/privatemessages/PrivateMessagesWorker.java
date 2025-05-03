package com.ayuget.redface.privatemessages;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.ayuget.redface.R;
import com.ayuget.redface.RedfaceNotifications;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.activity.PrivateMessagesActivity;
import com.squareup.phrase.Phrase;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import timber.log.Timber;

public class PrivateMessagesWorker extends Worker {
    private UserManager userManager;
    private MDService mdService;
    private RedfaceSettings appSettings;
    private PrivateMessagesNotificationHandler privateMessagesNotificationHandler;

    public PrivateMessagesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setMdService(MDService mdService) {
        this.mdService = mdService;
    }

    public void setSettings(RedfaceSettings settings) {
        this.appSettings = settings;
    }

    public void setPrivateMessagesNotificationHandler(PrivateMessagesNotificationHandler privateMessagesNotificationHandler) {
        this.privateMessagesNotificationHandler = privateMessagesNotificationHandler;
    }

    @NonNull
    @Override
    public Result doWork() {
        Timber.d("PrivateMessagesWorker is running");

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        if (!appSettings.arePrivateMessagesNoticationsEnabled() || !notificationManager.areNotificationsEnabled()) {
            Timber.d("Private message notifications are disabled, exiting worker");
            return Result.success();
        }

        Timber.d("Notifications are enabled, checking new private messages for all app users");

        for (User appUser : userManager.getRealUsers()) {
            Timber.d("Checking new private messages for user '%s'", appUser.getUsername());

            List<PrivateMessage> userPrivateMessages = mdService.getNewPrivateMessages(appUser)
                    .toBlocking()
                    .first();

            if (userPrivateMessages.isEmpty()) {
                continue;
            }

            String userNotificationsGroup = RedfaceNotifications.PRIVATE_MESSAGES_GROUP + appUser.getUsername();

            for (PrivateMessage privateMessage : userPrivateMessages) {
                if (privateMessagesNotificationHandler.wasNotificationAlreadySent(appUser, privateMessage)) {
                    Timber.d("Notification already sent for privateMessage(id=%d, totalMessages=%d)", privateMessage.getId(), privateMessage.getTotalMessages());

                    continue;
                }

                Notification notificationForPrivateMessage = createNotificationForPrivateMessage(userNotificationsGroup, privateMessage);
                int privateMessageId = (int) privateMessage.getId();

                // Safe to call here, as we called areNotificationEnabled() above
                notificationManager.notify(privateMessageId, notificationForPrivateMessage);

                privateMessagesNotificationHandler.storeNotificationAsSent(appUser, privateMessage);
            }
        }

        return Result.success();
    }

    private PendingIntent buildPrivateMessageNotificationIntent(Context context, PrivateMessage privateMessage) {
        Intent resultIntent = new Intent(context, PrivateMessagesActivity.class);
        resultIntent.setAction(String.valueOf(privateMessage.getId()));
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra(UIConstants.ARG_SELECTED_PM, privateMessage);
        return PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    private Notification createNotificationForPrivateMessage(String messagesGroup, PrivateMessage privateMessage) {
        CharSequence notificationText = Phrase.from(getApplicationContext(), R.string.new_private_messages_title)
                .put("username", privateMessage.getRecipient())
                .format();

        return new NotificationCompat.Builder(getApplicationContext(), RedfaceNotifications.PRIVATE_MESSAGES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_action_emo_wonder)
                .setColor(getApplicationContext().getResources().getColor(R.color.theme_primary))
                .setContentTitle(privateMessage.getSubject())
                .setContentText(notificationText)
                .setContentIntent(buildPrivateMessageNotificationIntent(getApplicationContext(), privateMessage))
                .setGroup(messagesGroup)
                .setAutoCancel(true)
                .build();
    }

    private PendingIntent buildPrivateMessageSummaryNotificationIntent(Context context) {
        Intent resultIntent = new Intent(context, PrivateMessagesActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
    }
}
