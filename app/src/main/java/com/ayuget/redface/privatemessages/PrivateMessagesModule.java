package com.ayuget.redface.privatemessages;

import com.ayuget.redface.storage.BoundedDiskCache;
import com.ayuget.redface.storage.DiskLruCacheFactory;
import com.squareup.moshi.Moshi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PrivateMessagesModule {
    @Provides
    @Singleton
    PrivateMessagesNotificationHandler providePrivateMessagesNotificationHandler(DiskLruCacheFactory diskLruCacheFactory, Moshi moshi) {
        BoundedDiskCache<Long, Integer> pmNotificationsCache = new BoundedDiskCache<>(diskLruCacheFactory.create("notifications", 64 * 1024), moshi.adapter(Integer.class));
        return new PrivateMessagesNotificationHandler(pmNotificationsCache);
    }
}
