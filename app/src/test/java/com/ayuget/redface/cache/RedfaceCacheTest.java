package com.ayuget.redface.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import rx.Completable;
import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RedfaceCacheTest {
    @Mock
    DataProvider<String, User> userProvider;

    @Mock
    CachingLayer<String, User> firstCachingTier;

    @Mock
    CachingLayer<String, User> secondCachingTier;

    @Test
    public void testWithEmptyResponse() throws Exception {
        CachingDataProvider<String, User> cachingDataProvider = CachingDataProvider.provideWith(userProvider)
                .build();

        String missingKey = "foo bar";

        when(userProvider.get(missingKey)).thenReturn(Observable.<User>empty());

        TestSubscriber<User> testSubscriber = new TestSubscriber<>();

        cachingDataProvider.get(missingKey)
                .subscribe(testSubscriber);

        testSubscriber.assertError(NoSuchElementException.class);
    }

    @Test
    public void testWithNoCachingTier() throws Exception {
        User jaime = new User("Jaime", "Lannister");

        CachingDataProvider<String, User> cachingDataProvider = CachingDataProvider.provideWith(userProvider)
                .build();

        when(userProvider.get(jaime.cacheKey())).thenReturn(Observable.just(jaime));

        TestSubscriber<User> testSubscriber = new TestSubscriber<>();

        cachingDataProvider.get(jaime.cacheKey())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(jaime));
    }

    @Test
    public void testWithCachingTiersAndNotInCache() throws Exception {
        User jaime = new User("Jaime", "Lannister");

        CachingDataProvider<String, User> cachingDataProvider = CachingDataProvider.provideWith(userProvider)
                .addCachingLayer(firstCachingTier)
                .addCachingLayer(secondCachingTier)
                .build();

        when(secondCachingTier.get(jaime.cacheKey())).thenReturn(Observable.<Cached<User>>empty());
        when(firstCachingTier.get(jaime.cacheKey())).thenReturn(Observable.<Cached<User>>empty());
        when(firstCachingTier.save(jaime.cacheKey(), jaime)).thenReturn(Completable.complete());
        when(secondCachingTier.save(jaime.cacheKey(), jaime)).thenReturn(Completable.complete());
        when(userProvider.get(jaime.cacheKey())).thenReturn(Observable.just(jaime));

        TestSubscriber<User> testSubscriber = new TestSubscriber<>();

        cachingDataProvider.get(jaime.cacheKey())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(jaime));

        verify(firstCachingTier).get(jaime.cacheKey());
        verify(secondCachingTier).get(jaime.cacheKey());
        verify(userProvider).get(jaime.cacheKey());
        verify(firstCachingTier).save(jaime.cacheKey(), jaime);
        verify(secondCachingTier).save(jaime.cacheKey(), jaime);
    }

    @Test
    public void testWithCachingTiersAndInFirstCache() throws Exception {
        User jaime = new User("Jaime", "Lannister");

        CachingDataProvider<String, User> cachingDataProvider = CachingDataProvider.provideWith(userProvider)
                .addCachingLayer(firstCachingTier)
                .addCachingLayer(secondCachingTier)
                .expireValuesAfter(20, TimeUnit.DAYS)
                .build();

        when(secondCachingTier.get(jaime.cacheKey())).thenReturn(Observable.<Cached<User>>empty());
        when(firstCachingTier.save(jaime.cacheKey(), jaime)).thenReturn(Completable.complete());
        when(secondCachingTier.save(jaime.cacheKey(), jaime)).thenReturn(Completable.complete());
        when(firstCachingTier.get(jaime.cacheKey())).thenReturn(Observable.just(Cached.from(jaime)));

        // DataProvider should not be hit, data is in cache !
        when(userProvider.get(jaime.cacheKey())).thenReturn(Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                subscriber.onError(new IllegalStateException("boom"));
            }
        }));

        TestSubscriber<User> testSubscriber = new TestSubscriber<>();

        cachingDataProvider.get(jaime.cacheKey())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(jaime));

        verify(firstCachingTier).get(jaime.cacheKey());
        verify(secondCachingTier).get(jaime.cacheKey());
        verify(secondCachingTier).save(jaime.cacheKey(), jaime);
    }

    @Test
    public void testWithCachingTiersAndExpiredValueInCache() throws Exception {
        User jaime = new User("Jaime", "Lannister");

        CachingDataProvider<String, User> cachingDataProvider = CachingDataProvider.provideWith(userProvider)
                .addCachingLayer(firstCachingTier)
                .addCachingLayer(secondCachingTier)
                .expireValuesAfter(1, TimeUnit.HOURS)
                .build();

        when(secondCachingTier.get(jaime.cacheKey())).thenReturn(Observable.<Cached<User>>empty());
        when(firstCachingTier.save(jaime.cacheKey(), jaime)).thenReturn(Completable.complete());
        when(secondCachingTier.save(jaime.cacheKey(), jaime)).thenReturn(Completable.complete());
        when(firstCachingTier.get(jaime.cacheKey())).thenReturn(Observable.just(Cached.from(jaime, 7200)));
        when(userProvider.get(jaime.cacheKey())).thenReturn(Observable.just(jaime));

        TestSubscriber<User> testSubscriber = new TestSubscriber<>();

        cachingDataProvider.get(jaime.cacheKey())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(jaime));

        verify(firstCachingTier).get(jaime.cacheKey());
        verify(secondCachingTier).get(jaime.cacheKey());
        verify(userProvider).get(jaime.cacheKey());
        verify(firstCachingTier).save(jaime.cacheKey(), jaime);
        verify(secondCachingTier).save(jaime.cacheKey(), jaime);
    }

    private static class User implements Cacheable<String> {
        private final String firstName;
        private final String lastName;

        public User(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String firstName() {
            return firstName;
        }

        public String lastName() {
            return lastName;
        }

        @Override
        public String cacheKey() {
            return firstName() + " " + lastName;
        }
    }
}

