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

package com.ayuget.redface;

import com.ayuget.redface.data.DataModule;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.UIModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    includes = {
        DataModule.class,
        UIModule.class
    },
    injects = {
        RedfaceApp.class,
        DaggerWorkerFactory.class,
        RedfaceSettings.class
    }
)
class RedfaceModule {
    private final RedfaceApp app;

    RedfaceModule(RedfaceApp app) {
        this.app = app;
    }

    @Provides @Singleton
    RedfaceApp provideApp() {
        return app;
    }
}
