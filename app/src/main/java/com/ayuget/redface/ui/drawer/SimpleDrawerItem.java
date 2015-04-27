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

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public class SimpleDrawerItem extends DrawerItem {
    private final int itemId;

    private final int iconResource;

    private final int titleResource;

    public SimpleDrawerItem(int itemId, @DrawableRes int iconResource, @StringRes int titleResource) {
        this.itemId = itemId;
        this.iconResource = iconResource;
        this.titleResource = titleResource;
    }

    @Override
    protected Type getItemType() {
        return Type.SIMPLE_ITEM;
    }

    public int getItemId() {
        return itemId;
    }

    public int getIconResource() {
        return iconResource;
    }

    public int getTitleResource() {
        return titleResource;
    }
}
