package com.ayuget.redface.cache;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import rx.observers.TestSubscriber;

@RunWith(MockitoJUnitRunner.class)
public class DiskJsonCachingTierTest {
    private static final int TEST_APP_VERSION = 1;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    JsonAdapter<House> jsonAdapter;

    @Before
    public void setUp() throws Exception {
        Moshi moshi = new Moshi.Builder().build();
        jsonAdapter = moshi.adapter(House.class);
    }

    @Test
    public void testDataIsCached() throws Exception {
        JsonDiskCachingLayer<House> cachingTier = new JsonDiskCachingLayer.Builder<House>()
                .jsonAdapter(jsonAdapter)
                .cacheDirectory(temporaryFolder.getRoot())
                .cacheMaxSize(5 * 1024 * 1024)
                .build();

        House lannister = new House("Lannister", "Hear me roar !");

        cachingTier.save(lannister.cacheKey(), lannister).subscribe();

        TestSubscriber<Cached<House>> testSubscriber = new TestSubscriber<>();
        cachingTier.get(lannister.cacheKey()).subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(Cached.from(lannister)));
    }

    private static class House implements Cacheable<String> {
        private final String name;

        private final String motto;

        public House(String name, String motto) {
            this.name = name;
            this.motto = motto;
        }

        public String name() {
            return name;
        }

        public String motto() {
            return motto;
        }

        @Override
        public String cacheKey() {
            return name().toLowerCase();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            House house = (House) o;

            if (!name.equals(house.name)) return false;
            return motto.equals(house.motto);

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + motto.hashCode();
            return result;
        }
    }
}

