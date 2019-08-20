package com.ayuget.redface.privatemessages;

import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.storage.BoundedDiskCache;

import java.io.IOException;

import timber.log.Timber;

public class PrivateMessagesNotificationHandler {
    private BoundedDiskCache<Long, Integer> notificationsCache;

    public PrivateMessagesNotificationHandler(BoundedDiskCache<Long, Integer> notificationsCache) {
        this.notificationsCache = notificationsCache;
    }

    public boolean wasNotificationAlreadySent(User user, PrivateMessage privateMessage) {
        BoundedDiskCache.CacheEntry<Long, Integer> cacheEntry = notificationsCache.get(user, privateMessage.getId())
                .toBlocking()
                .firstOrDefault(null);

        if (cacheEntry == null) {
            return false;
        }

        return cacheEntry.value() == privateMessage.getTotalMessages();
    }

    public void storeNotificationAsSent(User user, PrivateMessage privateMessage) {
        Timber.d("Storing notification as sent for privateMessage(id=%d, totalMessages=%d)", privateMessage.getId(), privateMessage.getTotalMessages());

        try {
            notificationsCache.put(user, privateMessage.getId(), privateMessage.getTotalMessages());
        } catch (IOException e) {
            Timber.e(e, "Unable to store notification as sent for privateMessage = " + privateMessage.getId());
        }
    }
}
