package com.ayuget.redface.ui.drawer;


import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

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
