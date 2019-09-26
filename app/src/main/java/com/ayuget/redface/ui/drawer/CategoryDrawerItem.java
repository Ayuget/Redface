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

package com.ayuget.redface.ui.drawer;

import androidx.annotation.DrawableRes;

import com.ayuget.redface.data.api.model.Category;

public class CategoryDrawerItem extends DrawerItem {
    private final Category category;

    private final int iconResource;

    public CategoryDrawerItem(Category category, @DrawableRes int iconResource) {
        this.category = category;
        this.iconResource = iconResource;
    }

    public Category getCategory() {
        return category;
    }

    public int getIconResource() {
        return iconResource;
    }

    @Override
    protected Type getItemType() {
        return Type.CATEGORY;
    }
}
