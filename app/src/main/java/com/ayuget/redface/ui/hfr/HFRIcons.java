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

package com.ayuget.redface.ui.hfr;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Category;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class HFRIcons {
    private static final Map<Integer, Integer> categoriesIcons;

    static {
        Map<Integer, Integer> category2Icon = new LinkedHashMap<>();
        category2Icon.put(1, R.drawable.ic_action_memory);
        category2Icon.put(2, R.drawable.ic_action_wrench);
        category2Icon.put(3, R.drawable.ic_action_videocam);
        category2Icon.put(4, R.drawable.ic_action_windows);
        category2Icon.put(5, R.drawable.ic_action_gamepad);
        category2Icon.put(6, R.drawable.ic_action_shopping_cart);
        category2Icon.put(8, R.drawable.ic_action_school);
        category2Icon.put(9, R.drawable.ic_action_public);
        category2Icon.put(10, R.drawable.ic_action_code_fork);
        category2Icon.put(11, R.drawable.ic_action_linux);
        category2Icon.put(12, R.drawable.ic_action_paint_brush);
        category2Icon.put(13, R.drawable.ic_action_people);
        category2Icon.put(14, R.drawable.ic_action_camera_alt);
        category2Icon.put(15, R.drawable.ic_action_laptop_mac);
        category2Icon.put(16, R.drawable.ic_action_desktop_windows);
        category2Icon.put(21, R.drawable.ic_action_storage);
        category2Icon.put(22, R.drawable.ic_action_cloud);
        category2Icon.put(23, R.drawable.ic_action_smartphone);
        category2Icon.put(25, R.drawable.ic_action_apple);
        categoriesIcons = Collections.unmodifiableMap(category2Icon);
    }

    public static int getCategoryIcon(Category category) {
        if(category != null && categoriesIcons.containsKey(category.id())) {
            return categoriesIcons.get(category.id());
        }
        else {
            return R.drawable.ic_action_label;
        }
    }
}
