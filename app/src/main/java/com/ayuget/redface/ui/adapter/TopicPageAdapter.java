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
