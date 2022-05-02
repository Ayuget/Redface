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

package com.ayuget.redface.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.misc.UiUtils;

import butterknife.BindView;


public class DetailsDefaultFragment extends BaseFragment {
    @BindView(R.id.empty_content_image)
    ImageView emptyContentImage;

    public static DetailsDefaultFragment newInstance() {
        DetailsDefaultFragment fragment = new DetailsDefaultFragment();
        return fragment;
    }

    public DetailsDefaultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflateRootView(R.layout.fragment_details_default, inflater, container);

        UiUtils.setDrawableColor(emptyContentImage.getDrawable(), getResources().getColor(R.color.empty_view_image_color));

        if (savedInstanceState != null) {
            savedInstanceState.clear();
        }

        return rootView;
    }
}
