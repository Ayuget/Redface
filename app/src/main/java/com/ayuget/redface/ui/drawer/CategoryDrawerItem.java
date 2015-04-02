package com.ayuget.redface.ui.drawer;

import android.support.annotation.DrawableRes;

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
