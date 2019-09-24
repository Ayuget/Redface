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

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.activity.BaseActivity;

public class ToggleToolbarFragment extends ToolbarFragment {
    protected ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();

        drawerLayout = activity.getDrawerLayout();
        drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, getToolbar(), R.string.drawer_open, R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
    }

    @Override
    protected void setupToolbarNavigation() {
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    public void onPause() {
        drawerLayout.removeDrawerListener(drawerToggle);
        super.onPause();
    }
}
