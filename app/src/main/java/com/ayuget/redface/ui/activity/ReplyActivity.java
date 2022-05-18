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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MotionEventCompat;

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
import com.ayuget.redface.image.HostedImage;
import com.ayuget.redface.image.ImageHostingService;
import com.ayuget.redface.image.ImageQuality;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.event.SmileySelectedEvent;
import com.ayuget.redface.ui.misc.BindableAdapter;
import com.ayuget.redface.ui.misc.EditTextState;
import com.ayuget.redface.ui.misc.Smileys;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.template.SmileysTemplate;
import com.ayuget.redface.ui.view.SmileySelectorView;
import com.ayuget.redface.util.ImageUtils;
import com.ayuget.redface.util.RetainedFragmentHelper;
import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx_activity_result.RxActivityResult;
import timber.log.Timber;

import static android.os.Build.VERSION_CODES.KITKAT;

public class ReplyActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {
	private static final String ARG_TOPIC = "topic";
	private static final long IMAGE_SELECTION_VIEW_ANIMATION_TRANSITION_TIME = 300;
	private static final String UPLOADED_IMAGE_BB_CODE = "[url=%s][img]%s[/img][/url]";
	private static final String IMAGE_FROM_URL_BB_CODE = "[img]%s[/img]";
	private static final int REPLACE_SMILEY_SELECTOR_THRESHOLD = 6; // in pixels
	private static final float SELECTOR_DRAGGED_THRESHOLD = 10.0f;

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

	private float lastDy;

	private boolean isUpwardMovement;

	private boolean smileyPanelDragged;

	private int toolbarHeight;

	private boolean smileysToolbarAnimationInProgress;

	/**
	 * Main reply window (with user picker, toolbars, ...)
	 */
	@BindView(R.id.main_reply_frame)
	RelativeLayout mainReplyFrame;

	/**
	 * Primary dialog toolbar, with user selection and send button
	 */
	@BindView(R.id.toolbar_reply_actions)
	Toolbar actionsToolbar;

	/**
	 * Secondary action toolbar (bold, italic, links, ...)
	 */
	@BindView(R.id.toolbar_reply_extra)
	Toolbar extrasToolbar;

	/**
	 * Finger draggable view to select smileys
	 */
	@BindView(R.id.smiley_selector_view)
	View smileysSelector;

	/**
	 * Toolbar to switch between popular / recent / favorite smileys
	 */
	@BindView(R.id.smileys_toolbar)
	Toolbar smileysToolbar;

	/**
	 * Reply text box
	 */
	@BindView(R.id.reply_text)
	EditText replyEditText;

	/**
	 * Smiley list loading indicator
	 */
	@BindView(R.id.loading_indicator)
	View smileysLoadingIndicator;

	/**
	 * Smiley list
	 */
	@BindView(R.id.smileyList)
	SmileySelectorView smileyList;

	/**
	 * Root ViewGroup for the reply window
	 */
	@BindView(R.id.reply_window_root)
	FrameLayout replyWindowRoot;

	/**
	 * Smileys search box
	 */
	@BindView(R.id.smileys_search)
	SearchView smileysSearch;

	@BindView(R.id.sending_message_spinner)
	View sendingMessageSpinner;

	@BindView(R.id.image_selection_view)
	View imageSelectionView;

	@BindView(R.id.add_image_from_gallery)
	Button addImageFromGalleryButton;

	@BindView(R.id.add_image_from_url)
	Button addImageFromUrlButton;

	@BindView(R.id.image_upload_progress_bar)
	ProgressBar imageUploadProgressBar;

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

	@Inject
	ImageHostingService imageHostingService;

	private Topic currentTopic;

	private String initialReplyContent;

	private SubscriptionHandler<User, Response> replySubscriptionHandler = new SubscriptionHandler<>();

	private boolean replyIsSuccessful = false;

	private ViewTreeObserver.OnGlobalLayoutListener replyWindowResizeListener;
	private SearchView.OnQueryTextListener searchQueryListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResource());

		Intent intent = getIntent();
		if (intent != null) {
			currentTopic = intent.getParcelableExtra(ARG_TOPIC);
			initialReplyContent = intent.getStringExtra(UIConstants.ARG_REPLY_CONTENT);

			if (initialReplyContent != null) {
				replyEditText.setText(initialReplyContent);
				replyEditText.setSelection(replyEditText.getText().length());
			}
		}

		restoreImageUploadObservableSubscription(savedInstanceState);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		screenHeight = metrics.heightPixels;
		smileySelectorTopOffset = (int) (metrics.heightPixels * 0.75);
		replyWindowMaxHeight = (int) (metrics.heightPixels * 0.70);
		toolbarHeight = getToolbarHeight();

		setupSmileySelector();

		actionsToolbar.inflateMenu(R.menu.menu_reply);


		setupUserSwitcher(getLayoutInflater(), userManager.getRealUsers());

		// Dirty hack to set a maximum height on the reply window frame. Should leave enough room for the smiley
		// picker when the soft keyboard is hidden, and hide the smiley picker otherwise. Activity is resized thanks
		// to the adjustResize windowSoftInputMode set in the manifest, and the extra bottom toolbar stays visible
		// and usable.
		//
		// As any hack, this method is probably very buggy...
		replyWindowResizeListener = () -> {
			Rect r = new Rect();
			replyWindowRoot.getWindowVisibleDisplayFrame(r);

			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mainReplyFrame.getLayoutParams();
			boolean keyboardIsOpen = r.height() < replyWindowMaxHeight;

			if (keyboardIsOpen) {
				if (lp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
					lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
					mainReplyFrame.setLayoutParams(lp);
				}
			} else {
				if (lp.height != replyWindowMaxHeight) {
					lp.height = replyWindowMaxHeight;
					mainReplyFrame.setLayoutParams(lp);
				}
			}
		};

		searchQueryListener = new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				if (s.trim().length() > 0) {
					smileyList.reset();
					smileysLoadingIndicator.setVisibility(View.VISIBLE);
					subscribe(dataService.searchForSmileys(userManager.getActiveUser(), s.trim(), new EndlessObserver<List<Smiley>>() {
						@Override
						public void onNext(List<Smiley> smileys) {
							smileysLoadingIndicator.setVisibility(View.GONE);
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
		};

		styleToolbarButtons(extrasToolbar);
		styleToolbarMenu(actionsToolbar);
		setupImageSelectionButtons();

		smileyList.setBus(bus);
		smileyList.setMdEndpoints(mdEndpoints);
		smileyList.setSmileysTemplate(smileysTemplate);

		// Load default smileys
		loadDefaultSmileys();

		replyEditText.requestFocus();
	}

	protected int getLayoutResource() {
		return R.layout.dialog_reply;
	}

	private void restoreImageUploadObservableSubscription(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			Observable<HostedImage> imageUploadObservable = RetainedFragmentHelper.recover(this, getSupportFragmentManager());
			if (imageUploadObservable != null) {
				showImageSelectionView();
				showImageUploadIndicator();

				subscribeToImageUploadObservable(imageUploadObservable);
			}
			savedInstanceState.clear();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		actionsToolbar.setOnMenuItemClickListener(this);
		smileysSearch.setOnQueryTextListener(searchQueryListener);
		replyWindowRoot.getViewTreeObserver().addOnGlobalLayoutListener(replyWindowResizeListener);

		if (currentTopic != null) {
			restoreSavedResponseIfPresent();
		}
	}


	@Override
	protected void onPause() {
		actionsToolbar.setOnMenuItemClickListener(null);
		smileysSearch.setOnQueryTextListener(null);
		replyWindowRoot.getViewTreeObserver().removeOnGlobalLayoutListener(replyWindowResizeListener);

		if (!isReplySuccessful() && replyEditText != null && currentTopic != null) {
			saveCurrentResponse();
		}

		super.onPause();
	}

	private void saveCurrentResponse() {
		String actualReply = replyEditText.getText().toString();
		boolean hasResponse = actualReply.length() > 0;
		boolean textWasModified = (initialReplyContent == null) || !initialReplyContent.equals(actualReply);

		if (hasResponse && textWasModified) {
			responseStore.storeResponse(userManager.getActiveUser(), currentTopic, replyEditText.getText().toString());
		} else {
			responseStore.removeResponse(userManager.getActiveUser(), currentTopic);
		}
	}

	private void restoreSavedResponseIfPresent() {
		String storedResponse = responseStore.getResponse(userManager.getActiveUser(), currentTopic);
		boolean isReplyTextareaEmpty = replyEditText.getText().length() == 0;

		if (storedResponse != null && isReplyTextareaEmpty) {
			replyEditText.setText(storedResponse);
			replyEditText.setSelection(replyEditText.getText().length());
		}
	}

	private Intent buildImageSelectionIntent() {
		Intent intent;
		if (Build.VERSION.SDK_INT < KITKAT) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
		} else {
			intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}

		intent.setType("image/*");
		return intent;
	}

	private void setupImageSelectionButtons() {
		styleImageSelectionButtons(addImageFromGalleryButton);
		styleImageSelectionButtons(addImageFromUrlButton);

		addImageFromGalleryButton.setOnClickListener(v -> RxActivityResult.on(ReplyActivity.this).startIntent(buildImageSelectionIntent())
				.subscribe(result -> {
					Intent data = result.data();
					int resultCode = result.resultCode();

					if (resultCode == RESULT_OK) {

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
							getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
						}

						new AlertDialog.Builder(this)
								.setPositiveButton(R.string.image_sharing_confirmation_positive, (dialog, which) -> result.targetUI().uploadImageToHostingService(data.getData()))
								.setNegativeButton(R.string.image_sharing_confirmation_negative, (dialog, which) -> hideImageSelectionView())
								.setMessage(R.string.image_sharing_confirmation)
								.show();
					}
				}));

		addImageFromUrlButton.setOnClickListener(v -> insertImageFromUrl());
	}

	/**
	 * Loads default smileys in the smiley selector
	 */
	@OnClick(R.id.default_smileys)
	protected void loadDefaultSmileys() {
		smileyList.setSmileys(Smileys.defaultSmileys());
	}

	/**
	 * Loads favorite smileys in the smiley selector
	 */
	@OnClick(R.id.favorite_smileys_tab)
	protected void loadFavoriteSmileys() {
		subscribe(mdService.getFavoriteSmileys(userManager.getActiveUser())
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new EndlessObserver<List<Smiley>>() {
					@Override
					public void onNext(List<Smiley> favoriteSmilies) {
						ArrayList<Smiley> smiliesList = new ArrayList<>(userManager.getActiveUser().getProfile().personalSmilies());
						smiliesList.addAll(favoriteSmilies);

						smileyList.setSmileys(smiliesList);
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
		smileyList.setOnTouchListener((v, event) -> {
			final int action = MotionEventCompat.getActionMasked(event);

			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					final int pointerIndex = event.getActionIndex();
					lastTouchY = event.getY(pointerIndex);
					activePointerId = event.getPointerId(0);
					smileyPanelDragged = false; // Movement starts
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					if (smileyList.getScrollY() == 0) {
						final int pointerIndex = event.findPointerIndex(activePointerId);

						if (pointerIndex != -1) {
							final float y = event.getY(pointerIndex);

							// Distance
							float dy = y - lastTouchY;
							lastDy = dy;
							isUpwardMovement = dy < 0;

							float targetY = smileysSelector.getY() + dy;
							if (targetY < toolbarHeight) {
								float difference = toolbarHeight - targetY;
								dy += difference;
							} else if (targetY > smileySelectorTopOffset) {
								float difference = targetY - smileySelectorTopOffset;
								dy -= difference;
							}

							if (!smileyPanelDragged) {
								smileyPanelDragged = Math.abs(dy) > SELECTOR_DRAGGED_THRESHOLD;
							}

							smileysSelector.setY(smileysSelector.getY() + dy);

							// Show or hide the smileys toolbar based on current position
							if (isUpwardMovement && smileysSelector.getY() < replyWindowMaxHeight) {
								showSmileysToolbar();
							} else if (dy > REPLACE_SMILEY_SELECTOR_THRESHOLD) {
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
						if (isUpwardMovement || (!isUpwardMovement && lastDy > REPLACE_SMILEY_SELECTOR_THRESHOLD)) {
							// If the upward movement is below the animation threshold, or if the
							// movement is downwards & sufficient, replace the smiley selector at
							// its original position (bottom)
							yTranslation = smileySelectorTopOffset - smileysSelector.getY();
							hideSmileysToolbar();
						} else {
							// Moved downwards, but not enough, replace smiley selector at the top
							yTranslation = -(smileysSelector.getY() - toolbarHeight);
							showSmileysToolbar();
						}
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

			// Consuming touch only if panel has been dragged during current motion. Clicks can
			// sometimes be mistaken as small movements, hence the "threshold".
			return smileyPanelDragged;
		});
	}

	/**
	 * Hides the soft keyboard
	 */
	public void hideSoftKeyboard() {
		if (getCurrentFocus() != null) {
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
	protected void hideSmileysToolbar() {
		smileysToolbar.animate().translationY(-toolbarHeight).setInterpolator(new AccelerateDecelerateInterpolator()).start();
	}

	/**
	 * Smoothly shows the smiley toolbar
	 */
	protected void showSmileysToolbar() {
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
			Timber.e("Empty user list");
		} else if (users.size() == 1 || !canSwitchUser()) {
			View userView = setupUserView(inflater, canSwitchUser() ? users.get(0) : userManager.getActiveUser());
			actionsToolbar.addView(userView);
		} else {
			// Setup spinner for user selection
			Timber.d("Initializing spinner for '%d' users", users.size());
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

	@Override
	public void onBackPressed() {
		if (isImageSelectionViewVisible()) {
			hideImageSelectionView();
		} else {
			super.onBackPressed();
		}

	}

	protected View setupUserView(LayoutInflater inflater, User user) {
		View userView = inflater.inflate(R.layout.dialog_reply_spinner_item, actionsToolbar, false);
		ImageView avatarView = (ImageView) userView.findViewById(R.id.user_avatar);
		TextView usernameView = (TextView) userView.findViewById(R.id.user_username);

		avatarView.setImageResource(R.drawable.profile_background_red);
		usernameView.setText(user.getUsername());

		if (!user.isGuest()) {
			loadUserAvatarInto(user, avatarView);
		}

		return userView;
	}

	@Subscribe
	public void smileySelected(SmileySelectedEvent event) {
		insertSmiley(event.getSmileyCode());
	}

	protected void insertSmiley(String smileyCode) {
		Timber.d("Smiley '%s' has been selected", smileyCode);
		UiUtils.insertText(replyEditText, String.format(" %s ", smileyCode));

		replaceSmileySelector();
		hideSmileysToolbar();
	}

	protected void loadUserAvatarInto(User user, ImageView imageView) {
		if (user.hasAvatar() && user.getProfile() != null && user.getProfile().avatarUrl() != null) {
			Glide.with(this)
					.load(user.getProfile().avatarUrl())
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

	protected void insertSmileyOrTag(boolean isSmiley, String tag) {
		int selectionStart = replyEditText.getSelectionStart();
		int selectionEnd = replyEditText.getSelectionEnd();

		String selectedText = (selectionEnd == -1 || selectionEnd <= selectionStart) ? "" : replyEditText.getText().toString().substring(selectionStart, selectionEnd);

		String tagOpen = isSmiley ? "[:" : String.format("[%s]", tag);
		String tagClose = isSmiley ? "]" : String.format("[/%s]", tag);
		UiUtils.insertText(replyEditText, tagOpen + selectedText + tagClose);

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

	@OnClick({R.id.insert_manual_smiley_button, R.id.make_text_bold_button, R.id.make_text_italic_button, R.id.insert_quote_button, R.id.insert_link_button, R.id.insert_spoiler_button, R.id.insert_underline_button, R.id.insert_strike_button})
	public void onExtraToolbarButtonClicked(ImageButton button) {
		switch (button.getId()) {
			case R.id.insert_manual_smiley_button:
				insertSmileyOrTag(true, null);
				break;
			case R.id.insert_spoiler_button:
				insertSmileyOrTag(false, "spoiler");
				break;
			case R.id.insert_image_button:
				insertSmileyOrTag(false, "img");
				break;
			case R.id.insert_link_button:
				insertSmileyOrTag(false, "url");
				break;
			case R.id.insert_quote_button:
				insertSmileyOrTag(false, "quote");
				break;
			case R.id.make_text_bold_button:
				insertSmileyOrTag(false, "b");
				break;
			case R.id.make_text_italic_button:
				insertSmileyOrTag(false, "i");
				break;
			case R.id.insert_underline_button:
				insertSmileyOrTag(false, "u");
				break;
			case R.id.insert_strike_button:
				insertSmileyOrTag(false, "strike");
				break;
		}
	}

	@OnClick(R.id.insert_image_button)
	public void onImageInsertionRequested() {
		if (isImageSelectionViewVisible()) {
			hideImageSelectionView();
		} else {
			showImageSelectionView();
		}
	}

	private void showImageSelectionView() {
		if (isImageSelectionViewVisible()) {
			return;
		}

		imageSelectionView.setVisibility(View.VISIBLE);
		imageSelectionView.setAlpha(0.0f);
		imageSelectionView
				.animate()
				.setDuration(IMAGE_SELECTION_VIEW_ANIMATION_TRANSITION_TIME)
				.alpha(1.0f)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						Timber.d("Image selection view is now visible");
					}
				});
	}

	private void hideImageSelectionView() {
		if (!isImageSelectionViewVisible()) {
			return;
		}

		hideImageUploadIndicator();

		imageSelectionView
				.animate()
				.setDuration(IMAGE_SELECTION_VIEW_ANIMATION_TRANSITION_TIME)
				.alpha(0.0f)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						imageSelectionView.setVisibility(View.GONE);
						Timber.d("Image selection view is now hidden");
					}
				});
	}

	private boolean isImageSelectionViewVisible() {
		return imageSelectionView.getVisibility() == View.VISIBLE;
	}

	private void subscribeToImageUploadObservable(Observable<HostedImage> imageUploadObservable) {
		imageUploadObservable.subscribe(hostedImage -> {
			Timber.d("Successfully uploaded image ! -> %s", hostedImage);

			String mediumImageUrl = hostedImage.variant(ImageQuality.MEDIUM);
			EditTextState editTextState = UiUtils.insertTextAndSaveState(replyEditText, String.format(UPLOADED_IMAGE_BB_CODE, hostedImage.url(), mediumImageUrl != null ? mediumImageUrl : hostedImage.url()));

			// No need to restore background job anymore
			RetainedFragmentHelper.remove(this, getSupportFragmentManager());

			SnackbarHelper.makeWithAction(ReplyActivity.this, R.string.image_upload_success, R.string.image_upload_variants, c -> showImageVariantsPicker(hostedImage, editTextState)).show();
			hideImageSelectionView();
		}, t -> {
			Timber.e(t, "Got an error while uploading image");
			SnackbarHelper.makeError(ReplyActivity.this, R.string.image_upload_failed).show();

			// display error message in text area
			EditTextState editTextState = UiUtils.insertTextAndSaveState(replyEditText, " !! ERROR !! " + t.getMessage() + " !!! " + t.getCause() + " !!!");
			RetainedFragmentHelper.remove(this, getSupportFragmentManager());

			hideImageSelectionView();
		});
	}

	private void insertImageFromUrl() {
		Timber.d("Inserting image from URL !");

		final View insertImageView = LayoutInflater.from(this).inflate(R.layout.insert_image_from_url, null);
		final EditText editText = (EditText) insertImageView.findViewById(R.id.image_to_insert_url);

		new AlertDialog.Builder(this)
				.setTitle(R.string.image_enter_url)
				.setView(insertImageView)
				.setPositiveButton(R.string.image_enter_url_ok, (dialog, which) -> {
					UiUtils.insertText(replyEditText, String.format(IMAGE_FROM_URL_BB_CODE, editText.getText().toString()));
					hideImageSelectionView();
				})
				.setNegativeButton(R.string.image_enter_url_cancel, (dialog, which) -> hideImageSelectionView())
				.show();
	}

	/**
	 * Uploads user selected image to remote hosting service
	 */
	private void uploadImageToHostingService(Uri selectedImageUri) {
		Timber.d("Uploading selected image '%s'", selectedImageUri);
		showImageUploadIndicator();

		Observable<HostedImage> imageUploadObservable = Observable.fromCallable(() -> getContentResolver().openInputStream(selectedImageUri))
				.map(ImageUtils::readStreamFully)
				.flatMap(b -> imageHostingService.hostFromLocalImage(b))
				.observeOn(AndroidSchedulers.mainThread())
				.cache();

		RetainedFragmentHelper.retain(this, getSupportFragmentManager(), imageUploadObservable);

		subscribeToImageUploadObservable(imageUploadObservable);
	}

	private void showImageUploadIndicator() {
		imageUploadProgressBar.setVisibility(View.VISIBLE);
	}

	private void hideImageUploadIndicator() {
		imageUploadProgressBar.setVisibility(View.GONE);
	}

	@SuppressWarnings("StaticPseudoFunctionalStyleMethod")
	private void showImageVariantsPicker(HostedImage hostedImage, EditTextState editTextState) {
		Timber.d("Requesting variants !!");

		List<Map.Entry<ImageQuality, Integer>> availableVariants = new ArrayList<>(imageHostingService.availableImageVariants().entrySet());

		List<String> variantNames = new ArrayList<>();
		for (Map.Entry<ImageQuality, Integer> availableVariant : availableVariants) {
			variantNames.add(getString(availableVariant.getValue()));
		}

		ArrayAdapter<String> imageVariantsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, variantNames);

		new AlertDialog.Builder(this)
				.setTitle(R.string.image_upload_select_variant)
				.setAdapter(imageVariantsAdapter, (dialog, which) -> {
					ImageQuality selectedImageQuality = availableVariants.get(which).getKey();
					Timber.d("Selected '%s' image quality !", selectedImageQuality);

					String variantUrl = hostedImage.variant(selectedImageQuality);
					if (variantUrl == null) {
						variantUrl = hostedImage.url();
					}

					UiUtils.insertTextFromState(replyEditText, String.format(UPLOADED_IMAGE_BB_CODE, hostedImage.url(), variantUrl), editTextState);
				})
				.show();
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
				Timber.e(throwable, "Unknown exception while replying");
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
	public void replaceSmileySelector() {
		smileysSelector.animate()
				.translationYBy(smileySelectorTopOffset - smileysSelector.getY())
				.setDuration(150)
				.start();
	}

	private void styleToolbarButtons(ViewGroup toolbar) {
		for (int i = 0; i < toolbar.getChildCount(); i++) {
			View childView = toolbar.getChildAt(i);

			if (childView instanceof ImageButton) {
				ImageButton imageButton = (ImageButton) childView;
				UiUtils.setDrawableColor(imageButton.getDrawable(), UiUtils.getReplyToolbarIconsColor(this));
			} else if (childView instanceof HorizontalScrollView || childView instanceof LinearLayout) {
				styleToolbarButtons((ViewGroup) childView);
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

	private void styleImageSelectionButtons(Button button) {
		for (Drawable drawable : button.getCompoundDrawables()) {
			if (drawable != null) {
				UiUtils.setDrawableColor(drawable, UiUtils.getReplyToolbarIconsColor(ReplyActivity.this));
			}
		}
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

			if (!user.isGuest()) {
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
