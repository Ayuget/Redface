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
public class StoreBuilderTest {
    @Mock
    Fetcher<String, User> userProvider;

    @Mock
    CachingLayer<String, User> firstCachingTier;

    @Mock
    CachingLayer<String, User> secondCachingTier;

    @Test
    public void testWithEmptyResponse() throws Exception {
        Store<String, User> cachingDataProvider = Store.provideWith(userProvider)
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

        Store<String, User> cachingDataProvider = Store.provideWith(userProvider)
                .build();

        when(userProvider.get(jaime.firstName())).thenReturn(Observable.just(jaime));

        TestSubscriber<User> testSubscriber = new TestSubscriber<>();

        cachingDataProvider.get(jaime.firstName())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(jaime));
    }

    @Test
    public void testWithCachingTiersAndNotInCache() throws Exception {
        User jaime = new User("Jaime", "Lannister");

        Store<String, User> cachingDataProvider = Store.provideWith(userProvider)
                .addCachingLayer(firstCachingTier)
                .addCachingLayer(secondCachingTier)
                .build();

        when(secondCachingTier.get(jaime.firstName())).thenReturn(Observable.<CachedValue<User>>empty());
        when(firstCachingTier.get(jaime.firstName())).thenReturn(Observable.<CachedValue<User>>empty());
        when(firstCachingTier.save(jaime.firstName(), jaime)).thenReturn(Completable.complete());
        when(secondCachingTier.save(jaime.firstName(), jaime)).thenReturn(Completable.complete());
        when(userProvider.get(jaime.firstName())).thenReturn(Observable.just(jaime));

        TestSubscriber<User> testSubscriber = new TestSubscriber<>();

        cachingDataProvider.get(jaime.firstName())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(jaime));

        verify(firstCachingTier).get(jaime.firstName());
        verify(secondCachingTier).get(jaime.firstName());
        verify(userProvider).get(jaime.firstName());
        verify(firstCachingTier).save(jaime.firstName(), jaime);
        verify(secondCachingTier).save(jaime.firstName(), jaime);
    }

    @Test
    public void testWithCachingTiersAndInFirstCache() throws Exception {
        User jaime = new User("Jaime", "Lannister");

        Store<String, User> cachingDataProvider = Store.provideWith(userProvider)
                .addCachingLayer(firstCachingTier)
                .addCachingLayer(secondCachingTier)
                .expireValuesAfter(20, TimeUnit.DAYS)
                .build();

        when(secondCachingTier.get(jaime.firstName())).thenReturn(Observable.<CachedValue<User>>empty());
        when(firstCachingTier.save(jaime.firstName(), jaime)).thenReturn(Completable.complete());
        when(secondCachingTier.save(jaime.firstName(), jaime)).thenReturn(Completable.complete());
        when(firstCachingTier.get(jaime.firstName())).thenReturn(Observable.just(CachedValue.from(jaime)));

        // DataProvider should not be hit, data is in cache !
        when(userProvider.get(jaime.firstName())).thenReturn(Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                subscriber.onError(new IllegalStateException("boom"));
            }
        }));

        TestSubscriber<User> testSubscriber = new TestSubscriber<>();

        cachingDataProvider.get(jaime.firstName())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(jaime));

        verify(firstCachingTier).get(jaime.firstName());
        verify(secondCachingTier).get(jaime.firstName());
        verify(secondCachingTier).save(jaime.firstName(), jaime);
    }

    @Test
    public void testWithCachingTiersAndExpiredValueInCache() throws Exception {
        User jaime = new User("Jaime", "Lannister");

        Store<String, User> cachingDataProvider = Store.provideWith(userProvider)
                .addCachingLayer(firstCachingTier)
                .addCachingLayer(secondCachingTier)
                .expireValuesAfter(1, TimeUnit.HOURS)
                .build();

        when(secondCachingTier.get(jaime.firstName())).thenReturn(Observable.<CachedValue<User>>empty());
        when(firstCachingTier.save(jaime.firstName(), jaime)).thenReturn(Completable.complete());
        when(secondCachingTier.save(jaime.firstName(), jaime)).thenReturn(Completable.complete());
        when(firstCachingTier.get(jaime.firstName())).thenReturn(Observable.just(CachedValue.from(jaime, 7200)));
        when(userProvider.get(jaime.firstName())).thenReturn(Observable.just(jaime));

        TestSubscriber<User> testSubscriber = new TestSubscriber<>();

        cachingDataProvider.get(jaime.firstName())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(jaime));

        verify(firstCachingTier).get(jaime.firstName());
        verify(secondCachingTier).get(jaime.firstName());
        verify(userProvider).get(jaime.firstName());
        verify(firstCachingTier).save(jaime.firstName(), jaime);
        verify(secondCachingTier).save(jaime.firstName(), jaime);
    }

    private static class User {
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
    }
}

