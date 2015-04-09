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
        category2Icon.put(1, R.drawable.ic_memory_grey600_24dp);
        category2Icon.put(2, R.drawable.ic_wrench_grey600_24dp);
        category2Icon.put(3, R.drawable.ic_videocam_grey600_24dp);
        category2Icon.put(4, R.drawable.ic_windows_grey600_24dp);
        category2Icon.put(5, R.drawable.ic_gamepad_grey600_24dp);
        category2Icon.put(6, R.drawable.ic_shopping_cart_grey600_24dp);
        category2Icon.put(8, R.drawable.ic_school_grey600_24dp);
        category2Icon.put(9, R.drawable.ic_public_grey600_24dp);
        category2Icon.put(10, R.drawable.ic_code_fork_grey600_24dp);
        category2Icon.put(11, R.drawable.ic_linux_grey600_24dp);
        category2Icon.put(12, R.drawable.ic_paint_brush_grey600_24dp);
        category2Icon.put(13, R.drawable.ic_people_grey600_24dp);
        category2Icon.put(14, R.drawable.ic_camera_alt_grey600_24dp);
        category2Icon.put(15, R.drawable.ic_laptop_mac_grey600_24dp);
        category2Icon.put(16, R.drawable.ic_desktop_windows_grey600_24dp);
        category2Icon.put(21, R.drawable.ic_storage_grey600_24dp);
        category2Icon.put(22, R.drawable.ic_cloud_grey600_24dp);
        category2Icon.put(23, R.drawable.ic_smartphone_grey600_24dp);
        category2Icon.put(25, R.drawable.ic_apple_grey600_24dp);
        categoriesIcons = Collections.unmodifiableMap(category2Icon);
    }

    public static int getCategoryIcon(Category category) {
        if(categoriesIcons.containsKey(category.getId())) {
            return categoriesIcons.get(category.getId());
        }
        else {
            return R.drawable.ic_label_grey600_24dp;
        }
    }
}
