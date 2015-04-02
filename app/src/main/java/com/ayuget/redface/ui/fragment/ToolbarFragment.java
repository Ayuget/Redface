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
