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

package com.ayuget.redface.data.api.model;

import android.content.Context;

import com.ayuget.redface.R;

public enum TopicFilter {
    NONE,
    FAVORITE,
    PARTICIPATED,
    READ;

    public String resolve(Context context) {
        if (this == NONE) {
            return context.getResources().getString(R.string.action_topics_filter_all);
        }
        else if (this == FAVORITE) {
            return context.getResources().getString(R.string.action_topics_filter_favorites);
        }
        else if (this == PARTICIPATED) {
            return context.getResources().getString(R.string.action_topics_filter_participated);
        }
        else {
            return context.getResources().getString(R.string.action_topics_filter_read);
        }
    }
}
