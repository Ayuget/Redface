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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.ui.hfr.HFRIcons;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

public class MetaPageTopicsAdapter extends TopicsAdapter implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    List<Category> categories;

    boolean categoryIconsAsTopicIcons;

    public MetaPageTopicsAdapter(Context context, ThemeManager themeManager, boolean isCompactMode) {
        super(context, themeManager, isCompactMode);
        categoryIconsAsTopicIcons = false;
    }

    public void replaceCategories(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public long getHeaderId(int i) {
        if (categoryIconsAsTopicIcons) {
            return -1;
        }
        else {
            return getItem(i).getCategory().getId();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_category_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setText(getItem(position).getCategory().getName());
    }

    public void setCategoryIconsAsTopicIcons(boolean categoryIconsAsTopicIcons) {
        this.categoryIconsAsTopicIcons = categoryIconsAsTopicIcons;
    }

    @Override
    int getTopicIcon(Topic topic) {
        if (categoryIconsAsTopicIcons) {
            return HFRIcons.getCategoryIcon(topic.getCategory());
        }
        else {
            return super.getTopicIcon(topic);
        }
    }
}
