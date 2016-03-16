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

package com.ayuget.redface.ui.view;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ayuget.redface.R;
import com.ayuget.redface.ui.misc.UiUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ImageDetailsItemView extends RelativeLayout {
    @InjectView(R.id.detail_icon)
    ImageView detailIconView;

    @InjectView(R.id.main_text)
    TextView mainTextView;

    @InjectView(R.id.secondary_text)
    TextView secondaryTextView;

    private final String mainText;

    private final String secondaryText;

    private final int icon;

    public ImageDetailsItemView(Context context, String mainText, String secondaryText, int icon) {
        super(context);
        this.mainText = mainText;
        this.secondaryText = secondaryText;
        this.icon = icon;
        initialize();
    }

    private void initialize() {
        inflate(getContext(), R.layout.item_image_detail, this);
        ButterKnife.inject(this);

        detailIconView.setImageDrawable(getContext().getResources().getDrawable(icon));
        mainTextView.setText(mainText);
        secondaryTextView.setText(secondaryText);

        UiUtils.setDrawableColor(detailIconView.getDrawable(), UiUtils.getReplyToolbarIconsColor(getContext()));
    }

    public static Builder from(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        private final Context context;

        private String mainText;

        private String secondaryText;

        private int icon;

        private Builder(Context context) {
            this.context = context;
        }

        public Builder withMainText(String mainText) {
            this.mainText = mainText;
            return this;
        }

        public Builder withSecondaryText(String secondaryText) {
            this.secondaryText = secondaryText;
            return this;
        }

        public Builder withIcon(@DrawableRes int drawableRes) {
            this.icon = drawableRes;
            return this;
        }

        public ImageDetailsItemView build() {
            return new ImageDetailsItemView(context, mainText, secondaryText, icon);
        }
    }
}
