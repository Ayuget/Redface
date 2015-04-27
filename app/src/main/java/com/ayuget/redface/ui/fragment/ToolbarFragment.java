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
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ayuget.redface.R;

public class ToolbarFragment extends BaseFragment {
    private Toolbar toolbar;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar_actionbar);

        if (toolbar != null) {
            // Set an OnMenuItemClickListener to handle menu item clicks
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    return onOptionsItemSelected(menuItem);
                }
            });

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearInternalStack();
                    getActivity().onBackPressed();
                }
            });

            // Inflate a menu to be displayed in the toolbar
            onCreateOptionsMenu(toolbar);

            onToolbarInitialized(toolbar);
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void onCreateOptionsMenu(Toolbar toolbar) {
        // No menu by default
    }

    public void onToolbarInitialized(Toolbar toolbar) {
    }

    public void showUpButton() {
        // Resources comes from AppCompat library
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);;
    }

    public void clearInternalStack() {
    }
}
