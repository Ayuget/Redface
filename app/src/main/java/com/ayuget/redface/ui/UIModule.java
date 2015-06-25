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
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.fragment.DefaultFragment;
import com.ayuget.redface.ui.fragment.DetailsDefaultFragment;
import com.ayuget.redface.ui.fragment.HomePreferenceFragment;
import com.ayuget.redface.ui.fragment.MetaPageFragment;
import com.ayuget.redface.ui.fragment.NestedPreferenceFragment;
import com.ayuget.redface.ui.fragment.PostsFragment;
import com.ayuget.redface.ui.fragment.TopicFragment;
import com.ayuget.redface.ui.fragment.TopicListFragment;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.template.AvatarTemplate;
import com.ayuget.redface.ui.template.EditIconTemplate;
import com.ayuget.redface.ui.template.OverflowIconTemplate;
import com.ayuget.redface.ui.template.PostExtraDetailsTemplate;
import com.ayuget.redface.ui.template.PostTemplate;
import com.ayuget.redface.ui.template.PostsTemplate;
import com.ayuget.redface.ui.template.SmileyTemplate;
import com.ayuget.redface.ui.template.SmileysTemplate;
import com.ayuget.redface.ui.view.SmileySelectorView;
import com.ayuget.redface.ui.view.TopicPageView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        includes = {
                AccountModule.class,
        },
        injects = {
                TopicsActivity.class,
                TopicListFragment.class,
                TopicPageView.class,
                PostsFragment.class,
                TopicFragment.class,
                DefaultFragment.class,
                DetailsDefaultFragment.class,
                AccountActivity.class,
                ReplyActivity.class,
                SmileySelectorView.class,
                NestedPreferenceFragment.class,
                HomePreferenceFragment.class,
                SettingsActivity.class,
                EditPostActivity.class,
                MetaPageFragment.class
        },
        library =  true,
        complete = false
)
public class UIModule {
    @Provides @Singleton AvatarTemplate provideAvatarTemplate(RedfaceApp app) {
        return new AvatarTemplate(app.getApplicationContext());
    }

    @Provides @Singleton SmileyTemplate provideSmileyTemplate(RedfaceApp app) {
        return new SmileyTemplate(app.getApplicationContext());
    }

    @Provides @Singleton PostTemplate providePostTemplate(RedfaceApp app, AvatarTemplate avatarTemplate, EditIconTemplate editIconTemplate, PostExtraDetailsTemplate extraDetailsTemplate, OverflowIconTemplate overflowIconTemplate) {
        return new PostTemplate(app.getApplicationContext(), avatarTemplate, editIconTemplate, extraDetailsTemplate, overflowIconTemplate);
    }

    @Provides @Singleton PostsTemplate providePostsTemplate(RedfaceApp app, PostTemplate postTemplate, ThemeManager themeManager) {
        return new PostsTemplate(app.getApplicationContext(), postTemplate, themeManager);
    }

    @Provides @Singleton SmileysTemplate provideSmileysTemplate(RedfaceApp app, SmileyTemplate smileyTemplate, ThemeManager themeManager) {
        return new SmileysTemplate(app.getApplicationContext(), smileyTemplate, themeManager);
    }

    @Provides @Singleton EditIconTemplate provideEditIconTemplate(RedfaceApp app, UserManager userManager) {
        return new EditIconTemplate(app.getApplicationContext(), userManager);
    }

    @Provides @Singleton
    OverflowIconTemplate provideOverflowTemplate(RedfaceApp app, UserManager userManager) {
        return new OverflowIconTemplate(app.getApplicationContext(), userManager);
    }

    @Provides @Singleton
    PostExtraDetailsTemplate providePostExtraDetailsTemplate(RedfaceApp app) {
        return new PostExtraDetailsTemplate(app.getApplicationContext());
    }

    @Provides @Singleton ThemeManager provideThemeManager(RedfaceSettings settings) {
        return new ThemeManager(settings);
    }
}
