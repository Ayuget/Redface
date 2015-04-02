package com.ayuget.redface.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.fragment.PostsFragmentBuilder;

public class TopicPageAdapter extends FragmentPagerAdapter {
    private final Topic topic;
    private final int initialPage;

    public TopicPageAdapter(FragmentManager fm, Topic topic, int initialPage) {
        super(fm);
        this.topic = topic;
        this.initialPage = initialPage;
    }

    @Override
    public Fragment getItem(int i) {
        // i + 1 because we want pages number to start at 1 and not 0
        return new PostsFragmentBuilder(i + 1, initialPage, topic).build();
    }

    @Override
    public int getCount() {
        return topic.getPagesCount();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + String.valueOf(position + 1);
    }


}
