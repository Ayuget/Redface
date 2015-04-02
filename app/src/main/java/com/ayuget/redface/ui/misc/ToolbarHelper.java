package com.ayuget.redface.ui.misc;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.BaseActivity;

public class ToolbarHelper {
    /**
     * Adds a drawer toggle (hamburger icon) to the toolbar
     */
    public static void addDrowerToggle(BaseActivity activity, Toolbar toolbar) {
        DrawerLayout drawerLayout = activity.getDrawerLayout();
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);

        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
    }
}
