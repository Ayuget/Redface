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
import androidx.annotation.StringRes;

import com.ayuget.redface.data.api.model.Category;

public abstract class DrawerItem {
    protected static enum Type {
        SIMPLE_ITEM,
        SEPARATOR,
        CATEGORY
    }

    protected abstract Type getItemType();

    public boolean isSeparator() {
        return getItemType() == Type.SEPARATOR;
    }
    public boolean isSimpleItem() {
        return getItemType() == Type.SIMPLE_ITEM;
    }
    public boolean isCategory() {
        return getItemType() == Type.CATEGORY;
    }

    public static SimpleDrawerItem simple(int itemId, @DrawableRes int iconResource, @StringRes int titleResource) {
        return new SimpleDrawerItem(itemId, iconResource, titleResource);
    }

    public static CategoryDrawerItem category(Category category, @DrawableRes int iconResource) {
        return new CategoryDrawerItem(category, iconResource);
    }

    public static SeparatorDrawerItem separator() {
        return new SeparatorDrawerItem();
    }
}
