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
