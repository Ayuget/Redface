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

package com.ayuget.redface.ui.misc;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.ayuget.redface.R;
import com.rengwuxian.materialedittext.MaterialEditText;

public class UiUtils {
    private static final String LOG_TAG = UiUtils.class.getSimpleName();

    public static int resolveColorAttribute(Context context, int attrName) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(new int[] {attrName});
        try {
            return styledAttributes.getColor(0, 0);
        }
        finally {
            styledAttributes.recycle();
        }
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * Sets the drawable main color
     * @param drawable drawable to style
     * @param color real resolved color, not color resource id
     */
    public static void setDrawableColor(Drawable drawable, int color) {
        drawable.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public static int getAppBackgroundColor(Context context) {
        return resolveColorAttribute(context, R.attr.appBackgroundColor);
    }

    public static int getPrimaryTextColor(Context context) {
        return resolveColorAttribute(context, R.attr.textColorPrimary);
    }

    public static int getSecondaryTextColor(Context context) {
        return resolveColorAttribute(context, R.attr.textColorSecondary);
    }

    public static int getDefaultTopicIconBackgroundColor(Context context) {
        return resolveColorAttribute(context, R.attr.defaultTopicIconBackgroundColor);
    }

    public static int getDefaultTopicIconTextColor(Context context) {
        return resolveColorAttribute(context, R.attr.defaultTopicIconTextColor);
    }

    public static int getReadTopicIconBackgroundColor(Context context) {
        return resolveColorAttribute(context, R.attr.readTopicIconBackgroundColor);
    }

    public static int getReadTopicIconTextColor(Context context) {
        return resolveColorAttribute(context, R.attr.readTopicIconTextColor);
    }

    public static int getReplyToolbarIconsColor(Context context) {
        return resolveColorAttribute(context, R.attr.replyToolbarIconsColor);
    }
}
