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

package com.ayuget.redface.job;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.ayuget.redface.R;
import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.activity.PrivateMessagesActivity;
import com.uwetrottmann.androidutils.AndroidUtils;

import java.util.List;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PrivateMessagesService extends IntentService {
    private static final long[] VIBRATION_PATTERN = new long[] {
            0, 100, 200, 100, 100, 100
    };

    @Inject
    UserManager userManager;

    @Inject
    MDService mdService;

    @Inject
    RedfaceSettings settings;

    private CompositeSubscription subscriptions;

    public PrivateMessagesService() {
        super("Private Messages Notification Service");
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup dependency injection
        ((RedfaceApp) getApplication()).inject(this);

        // Proper RxJava subscriptions management with CompositeSubscription
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        subscriptions.unsubscribe();
    }

    @TargetApi(android.os.Build.VERSION_CODES.KITKAT)
    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Handling intent");

        if (! settings.arePrivateMessagesNoticationsEnabled()) {
            return;
        }

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        for (User redfaceUser : userManager.getRealUsers()) {
            subscriptions.add(mdService.getNewPrivateMessages(redfaceUser).subscribe(new EndlessObserver<List<PrivateMessage>>() {
                @Override
                public void onNext(List<PrivateMessage> privateMessages) {
                    for (PrivateMessage privateMessage : privateMessages) {
                        // Prepare intent to deal with clicks
                        Intent resultIntent = new Intent(PrivateMessagesService.this, PrivateMessagesActivity.class);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        resultIntent.putExtra(UIConstants.ARG_SELECTED_PM, privateMessage);
                        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_action_emo_wonder)
                                .setColor(getResources().getColor(R.color.theme_primary))
                                .setContentTitle(privateMessage.getRecipient())
                                .setContentText(privateMessage.getSubject())
                                .setContentIntent(resultPendingIntent)
                                .setAutoCancel(true);

                        builder.setVibrate(VIBRATION_PATTERN);

                        notificationManager.notify((int) privateMessage.getId(), builder.build());
                    }
                }
            }));
        }

        // Setup next alarm
        long wakeUpTime = System.currentTimeMillis() + settings.getPrivateMessagesPollingFrequency() * DateUtils.MINUTE_IN_MILLIS;
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, PrivateMessagesService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        Timber.d("Going to sleep, setting wake-up alarm to: %d", wakeUpTime);
        if (AndroidUtils.isKitKatOrHigher()) {
            am.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, wakeUpTime, pi);
        }

    }
}
