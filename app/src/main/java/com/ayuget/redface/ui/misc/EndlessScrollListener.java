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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    private static final String LOG_TAG = EndlessScrollListener.class.getSimpleName();

    // The minimum amount of items to have below your current scroll position before loading more.
    private int visibleThreshold = 5;
    // The current offset index of data you have loaded
    private int currentPage = 0;
    // The total number of items in the dataset after the last load
    private int previousTotalItemCount = 0;
    // True if we are still waiting for the last set of data to load.
    private boolean loading = true;
    // Sets the starting page index
    private int startingPageIndex = 1;

    private final LinearLayoutManager layoutManager;

    private Toolbar toolbar;

    private boolean hideToolbarOnScroll;

    private boolean animationInProgress = false;

    ValueAnimator toolbarAnimator;

    private boolean toolbarIsHidden = false;



    protected EndlessScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    protected EndlessScrollListener(LinearLayoutManager layoutManager, Toolbar toolbar, boolean hideToolbarOnScroll) {
        this.layoutManager = layoutManager;
        this.toolbar = toolbar;
        this.hideToolbarOnScroll = hideToolbarOnScroll;
    }

    /**
     * This happens many times a second during a scroll, so be wary of the code you place here.
     * We are given a few useful parameters to help us work out if we need to load some more data,
     * but first we check if we are waiting for the previous load to finish.
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        int totalItemCount = layoutManager.getItemCount();
        int visibleItemCount = layoutManager.getChildCount();
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

        if (!animationInProgress && hideToolbarOnScroll && toolbar != null) {
            if (!toolbarIsHidden && firstVisibleItem > 0 && dy > 0) {
                hideToolbar();
            } else if(toolbarIsHidden && dy < 0) {
                showToolbar();
            }
        }

        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) { this.loading = true; }
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
            currentPage++;
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if (!loading && (totalItemCount - visibleItemCount)<=(firstVisibleItem + visibleThreshold)) {
            onLoadMore(currentPage, totalItemCount);
            loading = true;
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        // Nothing to do
    }

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore(int page, int totalItemsCount);

    private void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-toolbar.getHeight());
    }

    private void moveToolbar(final float toTranslationY) {
        if (toolbar.getTranslationY() == toTranslationY) {
            return;
        }
        if (! animationInProgress) {
            toolbarAnimator = ValueAnimator.ofFloat(toolbar.getTranslationY(), toTranslationY).setDuration(200);

            toolbarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    toolbar.setTranslationY(translationY);
                }
            });
            toolbarAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    animationInProgress = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animationInProgress = false;
                    toolbarIsHidden = toTranslationY != 0;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animationInProgress = false;
                    toolbarIsHidden = toTranslationY != 0;
                }
            });

            toolbarAnimator.start();
        }
    }

}
