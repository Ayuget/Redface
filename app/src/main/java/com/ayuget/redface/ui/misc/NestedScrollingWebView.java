package com.ayuget.redface.ui.misc;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.core.view.MotionEventCompat;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

import timber.log.Timber;

public class NestedScrollingWebView extends WebView implements NestedScrollingChild {
    private int lastY;
    private final int[] scrollingOffset = new int[2];
    private final int[] scrollingConsumed = new int[2];
    private int nestedOffsetY;
    private NestedScrollingChildHelper childHelper;

    public NestedScrollingWebView(Context context) {
        this(context, null);
    }

    public NestedScrollingWebView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public NestedScrollingWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        childHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean returnValue = false;

        MotionEvent event = MotionEvent.obtain(ev);
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            nestedOffsetY = 0;
        }

        int eventY = (int) event.getY();
        event.offsetLocation(0, nestedOffsetY);

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int deltaY = lastY - eventY;

                if (dispatchNestedPreScroll(0, deltaY, scrollingConsumed, scrollingOffset)) {
                    deltaY -= scrollingConsumed[1];
                    lastY = eventY - scrollingOffset[1];
                    event.offsetLocation(0, -scrollingOffset[1]);
                    nestedOffsetY += scrollingOffset[1];
                }
                returnValue = super.onTouchEvent(event);

                if (dispatchNestedScroll(0, scrollingOffset[1], 0, deltaY, scrollingOffset)) {
                    event.offsetLocation(0, scrollingOffset[1]);
                    nestedOffsetY += scrollingOffset[1];
                    lastY -= scrollingOffset[1];
                }
                break;
            case MotionEvent.ACTION_DOWN:
                returnValue = super.onTouchEvent(event);
                lastY = eventY;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                returnValue = super.onTouchEvent(event);
                stopNestedScroll();
                break;
        }
        return returnValue;
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        childHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return childHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        Timber.d("Starting nested scrolling");
        return childHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        childHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return childHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

}

