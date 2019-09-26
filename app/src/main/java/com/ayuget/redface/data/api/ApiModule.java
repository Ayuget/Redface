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

package com.ayuget.redface.data.api;

import com.ayuget.redface.data.api.hfr.HFRAuthenticator;
import com.ayuget.redface.data.api.hfr.HFREndpoints;
import com.ayuget.redface.data.api.hfr.HFRForumService;
import com.ayuget.redface.data.api.hfr.HFRMessageSender;
import com.ayuget.redface.data.api.hfr.HFRUrlParser;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.ui.misc.SmileyRegistry;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApiModule {
    @Provides
    @Singleton
    MDService provideMDService(HFRForumService hfrForumService) {
        return hfrForumService;
    }

    @Provides
    @Singleton
    MDEndpoints provideEndpoints() {
        return new HFREndpoints();
    }

    @Provides
    @Singleton
    MDAuthenticator provideAuthenticator(HTTPClientProvider httpClientProvider, MDEndpoints mdEndpoints) {
        return new HFRAuthenticator(httpClientProvider, mdEndpoints);
    }

    @Provides
    @Singleton
    MDMessageSender provideMessageSender(HTTPClientProvider httpClientProvider, MDEndpoints mdEndpoints) {
        return new HFRMessageSender(httpClientProvider, mdEndpoints);
    }

    @Provides
    @Singleton
    UrlParser provideUrlParser(MDEndpoints endpoints, CategoriesStore categoriesStore) {
        return new HFRUrlParser(endpoints, categoriesStore);
    }


    @Provides
    @Singleton
    SmileyRegistry provideSmileyRegistry() {
        return new SmileyRegistry();
    }
}
