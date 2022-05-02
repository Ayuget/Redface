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

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.ayuget.redface.R;

public class ToolbarFragment extends BaseFragment {
    private Toolbar toolbar;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        toolbar = view.findViewById(R.id.toolbar_actionbar);

        if (toolbar != null) {
            onCreateOptionsMenu(toolbar);
            onToolbarInitialized(toolbar);
        }
        if (savedInstanceState != null) {
            savedInstanceState.clear();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (toolbar != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
            setupToolbarNavigation();
        }
    }

    protected void setupToolbarNavigation() {
        toolbar.setNavigationOnClickListener(v -> {
            clearInternalStack();
            getActivity().onBackPressed();
        });
    }

    @Override
    public void onPause() {
        if (toolbar != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setOnMenuItemClickListener(null);
            toolbar.setNavigationOnClickListener(null);
        }

        super.onPause();
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void onCreateOptionsMenu(Toolbar toolbar) {
        // No menu by default
    }

    /**
     * Callback called when the toolbar has been initialized
     * (click callbacks, ...)
     */
    public void onToolbarInitialized(Toolbar toolbar) {
    }

    @SuppressLint("RestrictedApi")
    public void showUpButton() {
        // Resources comes from AppCompat library
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.white));
        }
    }

    public void clearInternalStack() {
    }
}
