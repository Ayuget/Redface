package com.ayuget.redface.ui.misc;

import android.support.v4.view.ViewPager;

public abstract class PageSelectedListener implements ViewPager.OnPageChangeListener {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public abstract void onPageSelected(int position);

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
