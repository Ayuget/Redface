package com.ayuget.redface.ui;

import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.account.AccountModule;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.settings.RedfaceSettings;
import com.ayuget.redface.ui.fragment.DefaultFragment;
import com.ayuget.redface.ui.fragment.DetailsDefaultFragment;
import com.ayuget.redface.ui.fragment.HomePreferenceFragment;
import com.ayuget.redface.ui.fragment.NestedPreferenceFragment;
import com.ayuget.redface.ui.fragment.PostsFragment;
import com.ayuget.redface.ui.fragment.TopicFragment;
import com.ayuget.redface.ui.fragment.TopicListFragment;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.template.AvatarTemplate;
import com.ayuget.redface.ui.template.EditIconTemplate;
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
                EditPostActivity.class
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

    @Provides @Singleton PostTemplate providePostTemplate(RedfaceApp app, AvatarTemplate avatarTemplate, EditIconTemplate editIconTemplate, PostExtraDetailsTemplate extraDetailsTemplate) {
        return new PostTemplate(app.getApplicationContext(), avatarTemplate, editIconTemplate, extraDetailsTemplate);
    }

    @Provides @Singleton PostsTemplate providePostsTemplate(RedfaceApp app, PostTemplate postTemplate, ThemeManager themeManager) {
        return new PostsTemplate(app.getApplicationContext(), postTemplate, themeManager);
    }

    @Provides @Singleton SmileysTemplate provideSmileysTemplate(RedfaceApp app, SmileyTemplate smileyTemplate, ThemeManager themeManager) {
        return new SmileysTemplate(app.getApplicationContext(), smileyTemplate, themeManager);
    }

    @Provides @Singleton
    EditIconTemplate provideEditIconTemplate(RedfaceApp app, UserManager userManager) {
        return new EditIconTemplate(app.getApplicationContext(), userManager);
    }

    @Provides @Singleton
    PostExtraDetailsTemplate providePostExtraDetailsTemplate(RedfaceApp app) {
        return new PostExtraDetailsTemplate(app.getApplicationContext());
    }

    @Provides @Singleton ThemeManager provideThemeManager(RedfaceSettings settings) {
        return new ThemeManager(settings);
    }
}
