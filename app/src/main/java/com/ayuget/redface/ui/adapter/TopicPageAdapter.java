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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.fragment.PostsFragmentBuilder;
import com.ayuget.redface.ui.misc.PagePosition;

public class TopicPageAdapter extends FragmentStatePagerAdapter {
    private Topic topic;
    private final int initialPage;
    private final PagePosition initialPagePosition;

    public TopicPageAdapter(FragmentManager fm, Topic topic, int initialPage, PagePosition initialPagePosition) {
        super(fm);
        this.topic = topic;
        this.initialPage = initialPage;
        this.initialPagePosition = initialPagePosition;
    }

    public void notifyTopicUpdated(Topic topic) {
        this.topic = topic;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int i) {
        // i + 1 because we want pages number to start at 1 and not 0
        int currentPageNumber = i + 1;
        boolean isInitialPage = currentPageNumber == initialPage;
        PagePosition pageInitialPosition = getPageInitialPosition(currentPageNumber);
        return new PostsFragmentBuilder(isInitialPage, pageInitialPosition, currentPageNumber, topic).build();
    }

    private PagePosition getPageInitialPosition(int currentPageNumber) {
        if (currentPageNumber == initialPage) {
            return initialPagePosition;
        }
        else if (currentPageNumber < initialPage) {
            return PagePosition.bottom();
        }
        else {
            return PagePosition.top();
        }
    }

    @Override
    public int getCount() {
        return topic.pagesCount();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + String.valueOf(position + 1);
    }
}
