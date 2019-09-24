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

package com.ayuget.redface.ui;

import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.account.AccountModule;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.image.ImageModule;
import com.ayuget.redface.settings.Blacklist;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.template.AvatarTemplate;
import com.ayuget.redface.ui.template.PostActionsTemplate;
import com.ayuget.redface.ui.template.PostExtraDetailsTemplate;
import com.ayuget.redface.ui.template.PostTemplate;
import com.ayuget.redface.ui.template.PostsTemplate;
import com.ayuget.redface.ui.template.QuickActionsTemplate;
import com.ayuget.redface.ui.template.SmileyTemplate;
import com.ayuget.redface.ui.template.SmileysTemplate;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = {
                AccountModule.class,
                ImageModule.class,
                UiInjectionModule.class
        }
)
public class UIModule {
    @Provides
    @Singleton
    AvatarTemplate provideAvatarTemplate(RedfaceApp app) {
        return new AvatarTemplate(app.getApplicationContext());
    }

    @Provides
    @Singleton
    SmileyTemplate provideSmileyTemplate(RedfaceApp app) {
        return new SmileyTemplate(app.getApplicationContext());
    }

    @Provides
    @Singleton
    QuickActionsTemplate provideQuickActions(RedfaceApp app, UserManager userManager) {
        return new QuickActionsTemplate(app.getApplicationContext(), userManager);
    }

    @Provides
    @Singleton
    PostTemplate providePostTemplate(RedfaceApp app, UserManager userManager, AvatarTemplate avatarTemplate, PostExtraDetailsTemplate extraDetailsTemplate, PostActionsTemplate postActionsTemplate, QuickActionsTemplate quickActionsTemplate, RedfaceSettings appSettings, Blacklist blacklist) {
        return new PostTemplate(app.getApplicationContext(), userManager, avatarTemplate, extraDetailsTemplate, postActionsTemplate, quickActionsTemplate, appSettings, blacklist);
    }

    @Provides
    @Singleton
    PostsTemplate providePostsTemplate(RedfaceApp app, PostTemplate postTemplate, ThemeManager themeManager) {
        return new PostsTemplate(app.getApplicationContext(), postTemplate, themeManager);
    }

    @Provides
    @Singleton
    SmileysTemplate provideSmileysTemplate(RedfaceApp app, SmileyTemplate smileyTemplate, ThemeManager themeManager) {
        return new SmileysTemplate(app.getApplicationContext(), smileyTemplate, themeManager);
    }

    @Provides
    @Singleton
    PostActionsTemplate providePostActionsTemplate(RedfaceApp app, UserManager userManager) {
        return new PostActionsTemplate(app.getApplicationContext(), userManager);
    }

    @Provides
    @Singleton
    PostExtraDetailsTemplate providePostExtraDetailsTemplate(RedfaceApp app) {
        return new PostExtraDetailsTemplate(app.getApplicationContext());
    }

    @Provides
    @Singleton
    ThemeManager provideThemeManager(RedfaceSettings settings) {
        return new ThemeManager(settings);
    }
}
