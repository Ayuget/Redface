package com.ayuget.redface.data.api;

import com.ayuget.redface.data.api.hfr.HFRAuthenticator;
import com.ayuget.redface.data.api.hfr.HFREndpoints;
import com.ayuget.redface.data.api.hfr.HFRForumService;
import com.ayuget.redface.data.api.hfr.HFRMessageSender;
import com.ayuget.redface.data.api.hfr.HFRUrlParser;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.network.HTTPClientProvider;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    complete = false,
    library = true
)
public class ApiModule {
    @Provides @Singleton MDService provideMDService(HFRForumService hfrForumService) {
        return hfrForumService;
    }

    @Provides @Singleton MDEndpoints provideEndpoints() {
        return new HFREndpoints();
    }

    @Provides @Singleton MDAuthenticator provideAuthenticator(HTTPClientProvider httpClientProvider, MDEndpoints mdEndpoints) {
        return new HFRAuthenticator(httpClientProvider, mdEndpoints);
    }

    @Provides @Singleton MDMessageSender provideMessageSender(HTTPClientProvider httpClientProvider, MDEndpoints mdEndpoints) {
        return new HFRMessageSender(httpClientProvider, mdEndpoints);
    }

    @Provides @Singleton UrlParser provideUrlParser(MDEndpoints endpoints, MDService mdService, Bus bus, CategoriesStore categoriesStore) {
        return new HFRUrlParser(endpoints, mdService, bus, categoriesStore);
    }
}
