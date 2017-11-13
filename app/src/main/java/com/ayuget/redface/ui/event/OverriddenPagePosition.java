package com.ayuget.redface.ui.event;

import android.support.annotation.Nullable;

import com.ayuget.redface.ui.misc.PagePosition;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class OverriddenPagePosition {
    @Nullable public abstract PagePosition targetPost();
    @Nullable public abstract Integer targetScrollY();

    public static OverriddenPagePosition toPost(PagePosition targetPost) {
        return new AutoValue_OverriddenPagePosition(targetPost, null);
    }

    public static OverriddenPagePosition toTop() {
        return toPost(PagePosition.top());
    }

    public static OverriddenPagePosition toBottom() {
        return toPost(PagePosition.bottom());
    }

    public static OverriddenPagePosition toScrollY(Integer targetScrollY) {
        return new AutoValue_OverriddenPagePosition(null, targetScrollY);
    }

    public boolean shouldScrollToPost() {
        return targetPost() != null;
    }
}
