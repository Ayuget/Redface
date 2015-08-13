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

package com.ayuget.redface.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.DataService;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.Response;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.data.api.model.Topic;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.data.state.ResponseStore;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.SmileySelectedEvent;
import com.ayuget.redface.ui.misc.BindableAdapter;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.template.SmileysTemplate;
import com.ayuget.redface.ui.view.SmileySelectorView;
import com.ayuget.redface.util.UserUtils;
import com.google.common.base.Optional;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

public class ReplyActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
    private static final String LOG_TAG = ReplyActivity.class.getSimpleName();

    private static final String ARG_TOPIC = "topic";

    /**
     * @todo fix this, ugly...
     */
    private static final List<Smiley> DEFAULT_SMILEYS = Arrays.asList(
            Smiley.make(":O", "http://forum-images.hardware.fr/icones/redface.gif"),
            Smiley.make(":)", "http://forum-images.hardware.fr/icones/smile.gif"),
            Smiley.make(":(", "http://forum-images.hardware.fr/icones/frown.gif"),
            Smiley.make(":D", "http://forum-images.hardware.fr/icones/biggrin.gif"),
            Smiley.make(";)", "http://forum-images.hardware.fr/icones/wink.gif"),
            Smiley.make(":ouch:", "http://forum-images.hardware.fr/icones/smilies/ouch.gif"),
            Smiley.make(":??:", "http://forum-images.hardware.fr/icones/confused.gif"),
            Smiley.make(":p", "http://forum-images.hardware.fr/icones/tongue.gif"),
            Smiley.make(":pfff:", "http://forum-images.hardware.fr/icones/smilies/pfff.gif"),
            Smiley.make(":ange:", "http://forum-images.hardware.fr/icones/smilies/ange.gif"),
            Smiley.make(":non:", "http://forum-images.hardware.fr/icones/smilies/non.gif"),
            Smiley.make(":bounce:", "http://forum-images.hardware.fr/icones/smilies/bounce.gif"),
            Smiley.make(":fou:", "http://forum-images.hardware.fr/icones/smilies/fou.gif"),
            Smiley.make(":jap:", "http://forum-images.hardware.fr/icones/smilies/jap.gif"),
            Smiley.make(":lol:", "http://forum-images.hardware.fr/icones/smilies/lol.gif"),
            Smiley.make(":wahoo:", "http://forum-images.hardware.fr/icones/smilies/wahoo.gif"),
            Smiley.make(":kaola:", "http://forum-images.hardware.fr/icones/smilies/kaola.gif"),
            Smiley.make(":love:", "http://forum-images.hardware.fr/icones/smilies/love.gif"),
            Smiley.make(":heink:", "http://forum-images.hardware.fr/icones/smilies/heink.gif"),
            Smiley.make(":cry:", "http://forum-images.hardware.fr/icones/smilies/cry.gif"),
            Smiley.make(":whistle:", "http://forum-images.hardware.fr/icones/smilies/whistle.gif"),
            Smiley.make(":sol:", "http://forum-images.hardware.fr/icones/smilies/sol.gif"),
            Smiley.make(":pt1cable:", "http://forum-images.hardware.fr/icones/smilies/pt1cable.gif"),
            Smiley.make(":sleep:", "http://forum-images.hardware.fr/icones/smilies/sleep.gif"),
            Smiley.make(":sweat:", "http://forum-images.hardware.fr/icones/smilies/sweat.gif"),
            Smiley.make(":hello:", "http://forum-images.hardware.fr/icones/smilies/hello.gif"),
            Smiley.make(":na:", "http://forum-images.hardware.fr/icones/smilies/na.gif"),
            Smiley.make(":sarcastic:", "http://forum-images.hardware.fr/icones/smilies/sarcastic.gif")
    );


    /**
     * The active pointer is the one currently use to move the smiley view
     */
    private int activePointerId = UIConstants.INVALID_POINTER_ID;

    /**
     * Top offset (margin) in pixels for the smiley selector. Marks its default position on the
     * y-axis.
     */
    private int smileySelectorTopOffset;

    /**
     * Reply window max height in pixels
     */
    private int replyWindowMaxHeight;

    private int screenHeight;

    private float lastTouchY;

    private boolean isUpwardMovement;

    private int toolbarHeight;

    private boolean smileysToolbarAnimationInProgress;

    /**
     * Main reply window (with user picker, toolbars, ...)
     */
    @InjectView(R.id.main_reply_frame)
    RelativeLayout mainReplyFrame;

    /**
     * Primary dialog toolbar, with user selection and send button
     */
    @InjectView(R.id.toolbar_reply_actions)
    Toolbar actionsToolbar;

    /**
     * Secondary action toolbar (bold, italic, links, ...)
     */
    @InjectView(R.id.toolbar_reply_extra)
    Toolbar extrasToolbar;

    /**
     * Finger draggable view to select smileys
     */
    @InjectView(R.id.smiley_selector_view)
    View smileysSelector;

    /**
     * Toolbar to switch between popular / recent / favorite smileys
     */
    @InjectView(R.id.smileys_toolbar)
    Toolbar smileysToolbar;

    /**
     * Reply text box
     */
    @InjectView(R.id.reply_text)
    EditText replyEditText;

    /**
     * Smiley list
     */
    @InjectView(R.id.smileyList)
    SmileySelectorView smileyList;

    /**
     * Root ViewGroup for the reply window
     */
    @InjectView(R.id.reply_window_root)
    FrameLayout replyWindowRoot;

    /**
     * Smileys search box
     */
    @InjectView(R.id.smileys_search)
    SearchView smileysSearch;

    @InjectView(R.id.sending_message_spinner)
    View sendingMessageSpinner;

    @Inject
    UserManager userManager;

    @Inject
    SmileysTemplate smileysTemplate;

    @Inject
    MDEndpoints mdEndpoints;

    @Inject
    DataService dataService;

    @Inject
    HTTPClientProvider httpClientProvider;

    @Inject
    MDService mdService;

    @Inject
    ResponseStore responseStore;

    private Topic currentTopic;

    private String initialReplyContent;

    private SubscriptionHandler<User, Response> replySubscriptionHandler = new SubscriptionHandler<>();

    private boolean replyIsSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_reply);

        Intent intent = getIntent();
        if (intent != null) {
            currentTopic = intent.getParcelableExtra(ARG_TOPIC);
            initialReplyContent = intent.getStringExtra(UIConstants.ARG_REPLY_CONTENT);

            if (currentTopic == null) {
                throw new IllegalStateException("Current topic is null");
            }

            if (initialReplyContent != null) {
                replyEditText.setText(initialReplyContent);
                replyEditText.setSelection(replyEditText.getText().length());
            }
        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        smileySelectorTopOffset = (int) (metrics.heightPixels * 0.75);
        replyWindowMaxHeight = (int) (metrics.heightPixels * 0.70);
        toolbarHeight = getToolbarHeight();

        setupSmileySelector();

        actionsToolbar.inflateMenu(R.menu.menu_reply);
        actionsToolbar.setOnMenuItemClickListener(this);

        setupUserSwitcher(getLayoutInflater(), userManager.getRealUsers());

        // Dirty hack to set a maximum height on the reply window frame. Should leave enough room for the smiley
        // picker when the soft keyboard is hidden, and hide the smiley picker otherwise. Activity is resized thanks
        // to the adjustResize windowSoftInputMode set in the manifest, and the extra bottom toolbar stays visible
        // and usable.
        //
        // As any hack, this method is probably very buggy...
        replyWindowRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                replyWindowRoot.getWindowVisibleDisplayFrame(r);

                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mainReplyFrame.getLayoutParams();
                boolean keyboardIsOpen = r.height() < replyWindowMaxHeight;

                if (keyboardIsOpen) {
                    if (lp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        mainReplyFrame.setLayoutParams(lp);
                    }
                }
                else {
                    if (lp.height != replyWindowMaxHeight) {
                        lp.height = replyWindowMaxHeight;
                        mainReplyFrame.setLayoutParams(lp);
                    }
                }
            }
        });

        styleToolbarButtons(extrasToolbar);
        styleToolbarButtons(smileysToolbar);
        styleToolbarMenu(actionsToolbar);
        styleSmileySearchView();

        smileysSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.trim().length() > 0) {
                    subscribe(dataService.searchForSmileys(s.trim(), new EndlessObserver<List<Smiley>>() {
                        @Override
                        public void onNext(List<Smiley> smileys) {
                            smileyList.setSmileys(smileys);
                        }
                    }));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        // Load  default smileys
        loadDefaultSmileys();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!isReplySuccessful() && replyEditText != null) {
            String actualReply = replyEditText.getText().toString();
            boolean hasResponse = actualReply.length() > 0;
            boolean textWasModified = (initialReplyContent == null) || !initialReplyContent.equals(actualReply);

            if(hasResponse && textWasModified) {
                responseStore.storeResponse(userManager.getActiveUser(), currentTopic, replyEditText.getText().toString());
            }
            else {
                responseStore.removeResponse(userManager.getActiveUser(), currentTopic);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        String storedResponse = responseStore.getResponse(userManager.getActiveUser(), currentTopic);

        if (storedResponse != null && replyEditText.getText().length() == 0) {
            replyEditText.setText(storedResponse);
            replyEditText.setSelection(replyEditText.getText().length());
        }
    }

    /**
     * Loads default smileys in the smiley selector
     */
    @OnClick(R.id.default_smileys)
    protected void loadDefaultSmileys() {
        smileyList.setSmileys(DEFAULT_SMILEYS);
    }

    /**
     * Loads recently used smileys in the smiley selector
     */
    @OnClick(R.id.recent_smileys)
    protected void loadRecentSmileys() {
        subscribe(dataService.getRecentlyUsedSmileys(userManager.getActiveUser(), new EndlessObserver<List<Smiley>>() {
            @Override
            public void onNext(List<Smiley> smileys) {
                smileyList.setSmileys(smileys);
            }
        }));
    }

    /**
     * Loads popular smileys in the smiley selector
     */
    @OnClick(R.id.popular_smileys)
    protected void loadPopularSmileys() {
        subscribe(dataService.getPopularSmileys(new EndlessObserver<List<Smiley>>() {
            @Override
            public void onNext(List<Smiley> smileys) {
                smileyList.setSmileys(smileys);
            }
        }));
    }

    protected void showSendingMessageSpinner() {
        sendingMessageSpinner.setVisibility(View.VISIBLE);
        actionsToolbar.getMenu().getItem(0).setVisible(false);
    }

    protected void hideSendingMessageSpinner() {
        sendingMessageSpinner.setVisibility(View.GONE);
        actionsToolbar.getMenu().getItem(0).setVisible(true);
    }

    /**
     * Initializes both the smiley selector
     */
    protected void setupSmileySelector() {
        smileyList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = MotionEventCompat.getActionMasked(event);

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        final int pointerIndex = MotionEventCompat.getActionIndex(event);
                        lastTouchY = MotionEventCompat.getY(event, pointerIndex);
                        activePointerId = MotionEventCompat.getPointerId(event, 0);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        if (smileyList.getScrollY() == 0) {
                            final int pointerIndex = MotionEventCompat.findPointerIndex(event, activePointerId);

                            if (pointerIndex != -1) {
                                final float y = MotionEventCompat.getY(event, pointerIndex);

                                // Distance
                                float dy = y - lastTouchY;
                                isUpwardMovement = dy < 0;
                                float targetY = smileysSelector.getY() + dy;

                                if (targetY < toolbarHeight) {
                                    float difference = toolbarHeight - targetY;
                                    dy += difference;
                                } else if (targetY > smileySelectorTopOffset) {
                                    float difference = targetY - smileySelectorTopOffset;
                                    dy -= difference;
                                }

                                smileysSelector.setY(smileysSelector.getY() + dy);

                                // Show or hide the smileys toolbar based on current position
                                if (isUpwardMovement && smileysSelector.getY() < replyWindowMaxHeight) {
                                    showSmileysToolbar();
                                } else {
                                    hideSmileysToolbar();
                                }
                            }

                            break;
                        }
                    }
                    case MotionEvent.ACTION_UP: {
                        int upAnimationThreshold = replyWindowMaxHeight - toolbarHeight;

                        float yTranslation;
                        ViewPropertyAnimator viewPropertyAnimator = smileysSelector.animate();

                        if (isUpwardMovement && smileysSelector.getY() == upAnimationThreshold) {
                            // Do not move in that case
                            yTranslation = 0;
                        } else if (isUpwardMovement && smileysSelector.getY() < upAnimationThreshold) {
                            // Moving too far, let's avoid this
                            yTranslation = -(smileysSelector.getY() - toolbarHeight);
                        } else {
                            // Replace the smiley selector at its original position
                            yTranslation = smileySelectorTopOffset - smileysSelector.getY();
                        }

                        if (yTranslation != 0) {
                            viewPropertyAnimator
                                    .translationYBy(yTranslation)
                                    .setDuration(150)
                                    .start();
                        }

                        break;
                    }
                }

                if (smileysSelector.getY() != smileySelectorTopOffset) {
                    return (smileysSelector.getY() != toolbarHeight);
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @OnClick(R.id.show_smileys_picker)
    protected void showSmileyPicker() {
        float neededTranslation = -(smileysSelector.getY() - toolbarHeight);
        ViewPropertyAnimator viewPropertyAnimator = smileysSelector.animate();

        viewPropertyAnimator
                .translationYBy(neededTranslation)
                .setDuration(150)
                .start();

        showSmileysToolbar();
        hideSoftKeyboard();
    }

    /**
     * Smoothly hides the smileys toolbar
     */
    protected void  hideSmileysToolbar() {
        Log.d(LOG_TAG, "Hiding smileys toolbar");
        smileysToolbar.animate().translationY(-toolbarHeight).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    /**
     * Smoothly shows the smiley toolbar
     */
    protected void  showSmileysToolbar() {
        Log.d(LOG_TAG, "Showing smileys toolbar");
        smileysToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
    }

    @Override
    protected void initializeTheme() {
        setTheme(themeManager.getReplyWindowStyle());
    }

    protected boolean canSwitchUser() {
        return false;
    }

    protected void setupUserSwitcher(LayoutInflater inflater, List<User> users) {
        if (users.size() == 0) {
            Log.e(LOG_TAG, "Empty user list");
        }
        else if (users.size() == 1 || !canSwitchUser()) {
            View userView = setupUserView(inflater, canSwitchUser() ? users.get(0) : userManager.getActiveUser());
            actionsToolbar.addView(userView);
        }
        else {
            // Setup spinner for user selection
            Log.d(LOG_TAG, String.format("Initializing spinner for '%d' users", users.size()));
            View spinnerContainer = inflater.inflate(R.layout.reply_user_spinner, actionsToolbar, false);
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            actionsToolbar.addView(spinnerContainer, 0, lp);

            Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.reply_user_spinner);

            final UserAdapter userAdapter = new UserAdapter(this, users);
            spinner.setAdapter(userAdapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    User user = userAdapter.getItem(position);
                    userManager.setActiveUser(user);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    protected View setupUserView(LayoutInflater inflater, User user) {
        View userView = inflater.inflate(R.layout.dialog_reply_spinner_item, actionsToolbar, false);
        ImageView avatarView = (ImageView) userView.findViewById(R.id.user_avatar);
        TextView usernameView = (TextView) userView.findViewById(R.id.user_username);

        avatarView.setImageResource(R.drawable.profile_background_red);
        usernameView.setText(user.getUsername());

        if (! user.isGuest()) {
            loadUserAvatarInto(user, avatarView);
        }

        return userView;
    }

    @Subscribe public void smileySelected(SmileySelectedEvent event) {
        insertText(String.format(" %s ", event.getSmileyCode()));

        replaceSmileySelector();
        hideSmileysToolbar();
    }

    protected void loadUserAvatarInto(User user, ImageView imageView) {
        Optional<Integer> userId = UserUtils.getLoggedInUserId(user, httpClientProvider.getClientForUser(user));

        if (userId.isPresent()) {
            Picasso.with(this)
                    .load(mdEndpoints.userAvatar(userId.get()))
                    .into(imageView);
        }
    }

    /**
     * Returns toolbar height, in px
     */
    protected int getToolbarHeight() {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        return getResources().getDimensionPixelSize(tv.resourceId);
    }

    /**
     * Inserts a text at current caret position
     * @param text text to insert
     */
    protected void insertText(String text) {
        int selectionStart = replyEditText.getSelectionStart();
        int selectionEnd = replyEditText.getSelectionEnd();

        if (selectionStart != -1 && selectionEnd != -1) {
            replyEditText.getText().replace(selectionStart, selectionEnd, text);
        }
        else if (selectionStart != -1) {
            replyEditText.getText().insert(selectionStart, text);
        }
    }

    protected void insertTag(String tag) {
        int selectionStart = replyEditText.getSelectionStart();
        int selectionEnd = replyEditText.getSelectionEnd();

        String selectedText =  (selectionEnd == - 1 || selectionEnd <= selectionStart) ? "" : replyEditText.getText().toString().substring(selectionStart, selectionEnd);

        String tagOpen = String.format("[%s]", tag);
        String tagClose = String.format("[/%s]", tag);
        insertText(tagOpen + selectedText + tagClose);

        replyEditText.setSelection(selectionStart + tagOpen.length() + selectedText.length());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Move the smiley picker to the bottom and resize it properly
        smileysSelector.setMinimumHeight(screenHeight - getToolbarHeight());
        smileysSelector.setY(smileySelectorTopOffset);

        // Hide the smileys toolbar
        smileysToolbar.setTranslationY(-getToolbarHeight());
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_reply:
                // Disable button to prevent user from clicking the button twice and posting the
                // same reply multiple times. This could happen often because there is a small delay
                // between the click and the activity being closed.
                menuItem.setEnabled(false);
                showSendingMessageSpinner();
                postReply();
                break;
        }
        return false;
    }

    @OnClick({R.id.make_text_bold_button, R.id.make_text_italic_button, R.id.insert_quote_button, R.id.insert_link_button, R.id.insert_spoiler_button})
    public void onExtraToolbarButtonClicked(ImageButton button) {
        Log.d(LOG_TAG, "Button  clicked !");
        switch (button.getId()) {
            case R.id.insert_spoiler_button:
                insertTag("spoiler");
                break;
            case R.id.insert_link_button:
                insertTag("url");
                break;
            case R.id.insert_quote_button:
                insertTag("quote");
                break;
            case R.id.make_text_bold_button:
                insertTag("b");
                break;
            case R.id.make_text_italic_button:
                insertTag("i");
                break;
        }
    }

    /**
     * Posts the reply on the server
     */
    protected void postReply() {
        User activeUser = userManager.getActiveUser();
        String message = replyEditText.getText().toString();

        subscribe(replySubscriptionHandler.load(activeUser, mdService.replyToTopic(activeUser, currentTopic, message, true), new EndlessObserver<Response>() {
            @Override
            public void onNext(Response response) {
                if (response.isSuccessful()) {
                    onReplySuccess();
                } else {
                    onReplyFailure();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(LOG_TAG, "Unknown exception while replying", throwable);
                onReplyFailure();
            }
        }));
    }

    protected void onReplySuccess() {
        clearResponseFromCache(userManager.getActiveUser());

        // Flag that reply is successful to prevent it to be cached in the
        // response cache (onPause happens later in this activity lifecycle)
        setReplySuccessful(true);

        replyToActivity(RESULT_OK, false);
    }

    protected void onReplyFailure() {
        replyToActivity(UIConstants.REPLY_RESULT_KO, false);
    }

    protected void replyToActivity(int returnCode, boolean wasEdit) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(UIConstants.ARG_REPLY_TOPIC, currentTopic);
        returnIntent.putExtra(UIConstants.ARG_REPLY_WAS_EDIT, wasEdit);
        setResult(returnCode, returnIntent);
        finish();
    }

    /**
     * Animates the smiley selector back to its original position
     */
    private void replaceSmileySelector() {
        smileysSelector.animate()
                .translationYBy(smileySelectorTopOffset - smileysSelector.getY())
                .setDuration(150)
                .start();
    }

    private void styleToolbarButtons(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View childView = toolbar.getChildAt(i);

            if (childView instanceof ImageButton) {
                ImageButton imageButton = (ImageButton) childView;
                UiUtils.setDrawableColor(imageButton.getDrawable(), UiUtils.getReplyToolbarIconsColor(this));
            }
        }
    }

    private void styleToolbarMenu(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getMenu().size(); i++) {
            MenuItem menuItem = toolbar.getMenu().getItem(i);
            Drawable itemIcon = menuItem.getIcon();

            if (itemIcon != null) {
                UiUtils.setDrawableColor(itemIcon, UiUtils.getReplyToolbarIconsColor(ReplyActivity.this));
            }
        }
    }

    private void styleSmileySearchView() {
        ImageView searchButton = (ImageView) smileysSearch.findViewById(R.id.search_button);
        ImageView searchMagButton = (ImageView) smileysSearch.findViewById(R.id.search_mag_icon);
        ImageView closeButton = (ImageView) smileysSearch.findViewById(R.id.search_close_btn);
        ImageView searchGoButton = (ImageView) smileysSearch.findViewById(R.id.search_go_btn);
        ImageView voiceSearchButton = (ImageView) smileysSearch.findViewById(R.id.search_voice_btn);

        UiUtils.setDrawableColor(searchButton.getDrawable(), UiUtils.getReplyToolbarIconsColor(ReplyActivity.this));
        UiUtils.setDrawableColor(searchMagButton.getDrawable(), UiUtils.getReplyToolbarIconsColor(ReplyActivity.this));
        UiUtils.setDrawableColor(closeButton.getDrawable(), UiUtils.getReplyToolbarIconsColor(ReplyActivity.this));
        UiUtils.setDrawableColor(searchGoButton.getDrawable(), UiUtils.getReplyToolbarIconsColor(ReplyActivity.this));
        UiUtils.setDrawableColor(voiceSearchButton.getDrawable(), UiUtils.getReplyToolbarIconsColor(ReplyActivity.this));
    }

    public Topic getCurrentTopic() {
        return currentTopic;
    }

    protected void clearResponseFromCache(User user) {
        responseStore.removeResponse(user, currentTopic);
    }

    public boolean isReplySuccessful() {
        return replyIsSuccessful;
    }

    public void setReplySuccessful(boolean replyIsSuccessful) {
        this.replyIsSuccessful = replyIsSuccessful;
    }

    private static class UserViewHolder {
        public TextView username;
        public ImageView avatar;
    }

    private class UserAdapter extends BindableAdapter<User> {
        private final List<User> users;

        private UserAdapter(Context context, List<User> users) {
            super(context);

            this.users = users;
        }

        @Override
        public View newView(LayoutInflater inflater, int position, ViewGroup container) {
            View convertView = getLayoutInflater().inflate(R.layout.dialog_reply_spinner_item, container, false);
            UserViewHolder viewHolder = new UserViewHolder();
            viewHolder.username = (TextView) convertView.findViewById(R.id.user_username);
            viewHolder.avatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            convertView.setTag(viewHolder);
            return convertView;
        }

        @Override
        public void bindView(User user, int position, View view) {
            UserViewHolder viewHolder = (UserViewHolder) view.getTag();
            viewHolder.username.setText(user.getUsername());
            viewHolder.avatar.setImageResource(R.drawable.profile_background_red);

            if (! user.isGuest()) {
                loadUserAvatarInto(user, viewHolder.avatar);
            }
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public User getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
