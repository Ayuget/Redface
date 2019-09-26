package com.ayuget.redface.ui;

import com.ayuget.redface.ui.activity.AccountActivity;
import com.ayuget.redface.ui.activity.EditPostActivity;
import com.ayuget.redface.ui.activity.ExifDetailsActivity;
import com.ayuget.redface.ui.activity.ImageSharingActivity;
import com.ayuget.redface.ui.activity.PrivateMessagesActivity;
import com.ayuget.redface.ui.activity.ProfileActivity;
import com.ayuget.redface.ui.activity.ReplyActivity;
import com.ayuget.redface.ui.activity.SettingsActivity;
import com.ayuget.redface.ui.activity.TopicsActivity;
import com.ayuget.redface.ui.activity.WritePrivateMessageActivity;
import com.ayuget.redface.ui.fragment.DefaultFragment;
import com.ayuget.redface.ui.fragment.DetailsDefaultFragment;
import com.ayuget.redface.ui.fragment.HomePreferenceFragment;
import com.ayuget.redface.ui.fragment.MetaPageFragment;
import com.ayuget.redface.ui.fragment.NestedPreferenceFragment;
import com.ayuget.redface.ui.fragment.PostsFragment;
import com.ayuget.redface.ui.fragment.PrivateMessageListFragment;
import com.ayuget.redface.ui.fragment.TopicFragment;
import com.ayuget.redface.ui.fragment.TopicListFragment;
import com.ayuget.redface.ui.view.SmileySelectorView;
import com.ayuget.redface.ui.view.TopicPageView;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class UiInjectionModule {
    @ContributesAndroidInjector
    abstract TopicsActivity topicsActivity();

    @ContributesAndroidInjector
    abstract TopicListFragment topicListFragment();

    @ContributesAndroidInjector
    abstract TopicPageView topicPageView();

    @ContributesAndroidInjector
    abstract PostsFragment postsFragment();

    @ContributesAndroidInjector
    abstract TopicFragment topicFragment();

    @ContributesAndroidInjector
    abstract DefaultFragment defaultFragment();

    @ContributesAndroidInjector
    abstract DetailsDefaultFragment detailsDefaultFragment();

    @ContributesAndroidInjector
    abstract AccountActivity accountActivity();

    @ContributesAndroidInjector
    abstract ReplyActivity replyActivity();

    @ContributesAndroidInjector
    abstract SmileySelectorView smileySelectorView();

    @ContributesAndroidInjector
    abstract NestedPreferenceFragment nestedPreferenceFragment();

    @ContributesAndroidInjector
    abstract HomePreferenceFragment homePreferenceFragment();

    @ContributesAndroidInjector
    abstract SettingsActivity settingsActivity();

    @ContributesAndroidInjector
    abstract EditPostActivity editPostActivity();

    @ContributesAndroidInjector
    abstract MetaPageFragment metaPageFragment();

    @ContributesAndroidInjector
    abstract PrivateMessagesActivity privateMessagesActivity();

    @ContributesAndroidInjector
    abstract PrivateMessageListFragment privateMessageListFragment();

    @ContributesAndroidInjector
    abstract WritePrivateMessageActivity writePrivateMessageActivity();

    @ContributesAndroidInjector
    abstract ExifDetailsActivity exifDetailsActivity();

    @ContributesAndroidInjector
    abstract ImageSharingActivity imageSharingActivity();

    @ContributesAndroidInjector
    abstract ProfileActivity profileActivity();
}
