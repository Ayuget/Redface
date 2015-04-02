package com.ayuget.redface.ui.view;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ayuget.redface.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ComposeMessageView extends FrameLayout {
    private static final String LOG_TAG = ComposeMessageView.class.getSimpleName();

    private int maxWidth = Integer.MAX_VALUE;
    private int maxHeight = Integer.MAX_VALUE;

    private OnMessageComposedListener onMessageComposedListener;

    /**
     * Activity currently hosting this view
     */
    private Activity currentActivity;

    /**
     * Is the view showing ? (i.e is there an opening animation in progress)
     */
    private boolean isShowing;

    /**
     * Device screen height
     */
    private int screenHeight;

    /**
     * Device screen height
     */
    private int screenWidth;

    /**
     * Top offset (margin) in pixels for the smiley selector. Marks its default position on the
     * y-axis.
     */
    private int smileySelectorTopOffset;

    /**
     * Reply window max height in pixels
     */
    private int replyWindowMaxHeight;

    /**
     * Toolbar height
     */
    private int toolbarHeight;

    /**
     * Finger draggable view to select smileys
     */
    @InjectView(R.id.smiley_selector_view)
    View smileysSelector;

    /**
     * Main reply window (with user picker, toolbars, ...)
     */
    @InjectView(R.id.main_reply_frame)
    RelativeLayout mainReplyFrame;

    public ComposeMessageView(Context context) {
        super(context);
        initializeScreenMetrics();
    }

    public ComposeMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeScreenMetrics();
    }

    public ComposeMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeScreenMetrics();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Adjust width as necessary
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (maxWidth < width) {
            int mode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, mode);
        }
        // Adjust height as necessary
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (maxHeight < height) {
            int mode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, mode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setMaxWidth(int maxWidth) {
        maxWidth = maxWidth;
        requestLayout();
    }

    public void setMaxHeight(int maxHeight) {
        maxHeight = maxHeight;
        requestLayout();
    }

    public void setOnMessageComposedListener(OnMessageComposedListener onMessageComposedListener) {
        this.onMessageComposedListener = onMessageComposedListener;
    }

    private MarginLayoutParams initializeView(Context context, Activity targetActivity, ViewGroup parent) {
        ComposeMessageView layout = (ComposeMessageView) LayoutInflater.from(context).inflate(R.layout.dialog_reply_alt, this, true);
        layout.setBackgroundColor(getResources().getColor(R.color.app_background_semi_transparent));

        ButterKnife.inject(this, layout);

        initializeWindow(layout);
        initializeSmileySelector();

        layout.setMaxWidth(screenWidth);
        layout.setMaxHeight(screenHeight);

        Log.d(LOG_TAG, String.format("Settings maxWidth=%d, maxHeight=%d", screenWidth, screenHeight));

        return new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

    private void initializeScreenMetrics() {
        setVisibility(View.INVISIBLE);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        smileySelectorTopOffset = (int) (metrics.heightPixels * 0.75);
        replyWindowMaxHeight = (int) (metrics.heightPixels * 0.70);
        toolbarHeight = getToolbarHeight();
    }

    private void initializeWindow(final ComposeMessageView layout) {
        // Dirty hack to set a maximum height on the reply window frame. Should leave enough room for the smiley
        // picker when the soft keyboard is hidden, and hide the smiley picker otherwise. Activity is resized thanks
        // to the adjustResize windowSoftInputMode set in the manifest, and the extra bottom toolbar stays visible
        // and usable.
        //
        // As any hack, this method is probably very buggy...
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                layout.getWindowVisibleDisplayFrame(r);

                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mainReplyFrame.getLayoutParams();
                boolean keyboardIsOpen = r.height() < replyWindowMaxHeight;

                if (keyboardIsOpen) {
                    if (lp.height != r.height()) {
                        lp.height = r.height();
                        mainReplyFrame.setLayoutParams(lp);
                        mainReplyFrame.invalidate();
                    }
                }
                else {
                    if (lp.height != replyWindowMaxHeight) {
                        lp.height = replyWindowMaxHeight;
                        mainReplyFrame.setLayoutParams(lp);
                        mainReplyFrame.invalidate();
                    }
                }
            }
        });
    }

    public void reveal() {
        // get the center for the clipping circle
                int cx = (getLeft() + getRight()) / 2;
                int cy = (getTop() + getBottom()) / 2;

        // get the final radius for the clipping circle
                int finalRadius = Math.max(getWidth(), getHeight());

        // create the animator for this view (the start radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(this, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
                setVisibility(View.VISIBLE);
                anim.start();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    /**
     * Initializes the {@link com.ayuget.redface.ui.view.SmileySelectorView}, by offsetting it at
     * the bottom of the screen and by making it draggable with finger
     */
    private void initializeSmileySelector() {
        // Move the smiley picker to the bottom and resize it properly
        smileysSelector.setMinimumHeight(screenHeight - getToolbarHeight());
        smileysSelector.setY(smileySelectorTopOffset);
    }

    /**
     * Displays the {@link com.ayuget.redface.ui.view.ComposeMessageView} on top of the
     * {@link android.app.Activity} provided.
     *
     * @param targetActivity
     */
    public void show(Activity targetActivity) {
        currentActivity = targetActivity;
        ViewGroup root = (ViewGroup) targetActivity.findViewById(android.R.id.content);
        MarginLayoutParams params = initializeView(targetActivity, targetActivity, root);
        //updateLayoutParamsMargins(targetActivity, params);
        showInternal(targetActivity, params, root);

        reveal();
    }

    public void hide() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }

        isShowing = false;
    }

    private void showInternal(Activity targetActivity, MarginLayoutParams params, ViewGroup parent) {
        parent.removeView(this);

        parent.addView(this, params);

        bringToFront();

        // As requested in the documentation for bringToFront()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            parent.requestLayout();
            parent.invalidate();
        }

        isShowing = true;


        requestLayout();

        Log.d(LOG_TAG, "ComposeMessageView shown !");
    }

    /**
     * Returns toolbar height, in px
     */
    protected int getToolbarHeight() {
        TypedValue tv = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        return getResources().getDimensionPixelSize(tv.resourceId);
    }

    public static class Builder {
        private OnComposeMessageViewShownListener targetActivity;

        private boolean useTabletLayout;

        public Builder() {
        }

        public Builder with(OnComposeMessageViewShownListener targetActivity) {
            if(! (targetActivity instanceof Activity)) {
                throw new IllegalStateException("targetActivity must be an activity");
            }

            this.targetActivity = targetActivity;
            return this;
        }

        public void show() {
            ComposeMessageView composeMessageView = new ComposeMessageView((Activity) targetActivity);
            composeMessageView.show((Activity) targetActivity);
            targetActivity.onComposeMessageViewShown(composeMessageView);
        }

    }

    public interface OnComposeMessageViewShownListener {
        public void onComposeMessageViewShown(ComposeMessageView view);
    }

    public interface OnMessageComposedListener {
        public void onMessageComposed(String message);
    }
}
