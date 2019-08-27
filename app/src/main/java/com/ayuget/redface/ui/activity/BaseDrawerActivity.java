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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.DataService;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.state.CategoriesStore;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.drawer.CategoryDrawerItem;
import com.ayuget.redface.ui.drawer.DrawerItem;
import com.ayuget.redface.ui.drawer.SimpleDrawerItem;
import com.ayuget.redface.ui.hfr.HFRIcons;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.util.UserUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import timber.log.Timber;

public class BaseDrawerActivity extends BaseActivity {
    private List<View> drawerItemsViews;

    boolean accountBoxExpanded = false;

    // Regular navigation drawer items
    private static final int NAVDRAWER_ITEM_PROFILE = 1;
    private static final int NAVDRAWER_ITEM_PRIVATE_MESSAGES = 2;
    private static final int NAVDRAWER_ITEM_MY_TOPICS = 3;
    private static final int NAVDRAWER_ITEM_SETTINGS = 4;

    // Durations for animations
    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;

    @Inject
    DataService dataService;

    @Inject
    UserManager userManager;

    @Inject
    HTTPClientProvider httpClientProvider;

    @Inject
    CategoriesStore categoriesStore;

    @Inject
    MDEndpoints mdEndpoints;

    @InjectView(R.id.navdrawer_items_list)
    ViewGroup drawerItemsListContainer;

    @InjectView(R.id.profile_picture)
    ImageView activeUserPicture;

    @InjectView(R.id.active_username)
    TextView activeUserName;

    @InjectView(R.id.expand_account_box_indicator)
    ImageView expandAccountBoxIndicator;

    @InjectView(R.id.chosen_account_view)
    View choseAccountView;

    @InjectView(R.id.account_list)
    LinearLayout accountListContainer;

    @InjectView(R.id.hfr_drawer_layout)
    DrawerLayout drawerLayout;

    private User currentDrawerUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawerItemsViews = new ArrayList<>();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        setupNavigationDrawer();
        setupAccountBox();
    }

    @Override
    public void setContentView(int layoutResID, Bundle savedInstanceState) {
        super.setContentView(layoutResID, savedInstanceState);

        setupNavigationDrawer();
        setupAccountBox();
    }

    private void setupNavigationDrawer() {
        Timber.d("Setting up navigation drawer");

        // Now retrieve the DrawerLayout so that we can set the status bar color.
        // This only takes effect on Lollipop, or when using translucentStatusBar
        // on KitKat.
        drawerLayout.setStatusBarBackgroundColor(UiUtils.getStatusBarBackgroundColor(this));
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);

        // Initiate drawer with "static" items
        initiateNavDrawer();

        // Setup user details and load categories for active user
        updateActiveUser();
    }


    @Override
    protected void onStart() {
        super.onStart();
        updateUserIfNeeded();
    }

    private void updateUserIfNeeded() {
        if (currentDrawerUser == null || !currentDrawerUser.equals(userManager.getActiveUser())) {
            Timber.d("Updating active user and drawer categories");
            onActiveUserChanged();
        }
    }

    private void updateActiveUser() {
        Timber.d("Updating active user to %s (@%d)", userManager.getActiveUser(), System.identityHashCode(this));
        currentDrawerUser = userManager.getActiveUser();

        // Reset categories
        initiateNavDrawer();

        // Load categories for active user
        subscribe(dataService.loadCategories(currentDrawerUser, new EndlessObserver<List<Category>>() {
            @Override
            public void onNext(List<Category> categories) {
                populateNavDrawerCategories(categories);
                onCategoriesLoaded();
            }

            @Override
            public void onError(Throwable throwable) {
                Timber.e(throwable, "Error on retrieving categories for user '%s'", currentDrawerUser);
            }
        }));

        activeUserName.setText(currentDrawerUser.getDisplayUsername(this));

        displayUserAvatar();
    }

    private void displayUserAvatar() {

        Integer userId = null;
        if (! currentDrawerUser.isGuest() && currentDrawerUser.hasAvatar()) {
            userId = UserUtils.identifyUserFromCookies(httpClientProvider.getUserCookieStore(currentDrawerUser));
        }

        if (userId != null) {
            loadUserAvatarFromProfile(userId);
        }
        else {
            loadDefaultProfileImage();
        }
    }

    private void loadUserAvatarFromProfile(int userId) {
        Timber.d("Loading profile for user '%s' (id: '%d')", currentDrawerUser.getUsername(), userId);
        // TODO cache profile
        subscribe(dataService.loadProfile(currentDrawerUser, userId, new EndlessObserver<Profile>() {
            @Override
            public void onNext(Profile profile) {
                currentDrawerUser.setProfile(profile);
                Picasso.with(BaseDrawerActivity.this)
                        .load(profile.avatarUrl())
                        .into(activeUserPicture);
            }

            @Override
            public void onError(Throwable throwable) {
                Timber.e(throwable, "Error on retrieving profile for user '%s'", currentDrawerUser);
                loadDefaultProfileImage();
            }
        }));
    }

    private void loadDefaultProfileImage() {
        activeUserPicture.setImageResource(R.drawable.profile_background_red);
        choseAccountView.setBackgroundColor(getResources().getColor(R.color.theme_primary));
    }

    private void setupAccountBox() {
        expandAccountBoxIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountBoxExpanded = !accountBoxExpanded;
                setupAccountBoxToggle();
            }
        });

        setupAccountBoxToggle();

        populateAccountList(userManager.getAllUsers());
    }

    private void setupAccountBoxToggle() {
        expandAccountBoxIndicator.setImageResource(accountBoxExpanded
                ? R.drawable.ic_action_arrow_drop_up
                : R.drawable.ic_arrow_drop_down_white_24dp);

        int hideTranslateY = -accountListContainer.getHeight() / 4; // last 25% of animation
        if (accountBoxExpanded && accountListContainer.getTranslationY() == 0) {
            // initial setup
            accountListContainer.setAlpha(0);
            accountListContainer.setTranslationY(hideTranslateY);
        }

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                drawerItemsListContainer.setVisibility(accountBoxExpanded ? View.INVISIBLE : View.VISIBLE);
                accountListContainer.setVisibility(accountBoxExpanded ? View.VISIBLE : View.INVISIBLE);
            }


            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        });

        if (accountBoxExpanded) {
            accountListContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(accountListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(accountListContainer, View.TRANSLATION_Y, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    ObjectAnimator.ofFloat(drawerItemsListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    subSet);
            set.start();
        } else {
            drawerItemsListContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(accountListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(accountListContainer, View.TRANSLATION_Y,
                            hideTranslateY)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    subSet,
                    ObjectAnimator.ofFloat(drawerItemsListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.start();
        }

        set.start();
    }

    private void populateAccountList(List<User> users) {
        accountListContainer.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        for (User user : users) {
            addUserToAccountList(layoutInflater, user);
        }

        View addAccountView = layoutInflater.inflate(R.layout.navigation_drawer_add_account, accountListContainer, false);
        addAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add account intent
                Intent intent = new Intent(BaseDrawerActivity.this, AccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        accountListContainer.addView(addAccountView);
    }

    private void addUserToAccountList(LayoutInflater layoutInflater, final User user) {
        View accountView = layoutInflater.inflate(R.layout.navigation_drawer_account, accountListContainer, false);
        ((TextView) accountView.findViewById(R.id.account_username)).setText(user.getDisplayUsername(this));

        accountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userManager.setActiveUser(user);
                onActiveUserChanged();
            }
        });

        accountListContainer.addView(accountView);
    }

    private void onActiveUserChanged() {
        currentDrawerUser = userManager.getActiveUser();

        updateActiveUser(); // Sets up avatar (drawer header)
        onUserSwitched(currentDrawerUser);
        accountBoxExpanded = false;
        setupAccountBoxToggle(); // Hide account box toggle
    }

    private void initiateNavDrawer() {
        // Initial cleanup
        drawerItemsListContainer.removeAllViews();
        drawerItemsViews.clear();

        List<DrawerItem> initialDrawerItems = new ArrayList<>();

        if (userManager.isActiveUserLoggedIn()) {
            // Profile
            initialDrawerItems.add(DrawerItem.simple(NAVDRAWER_ITEM_PROFILE, R.drawable.ic_action_user, R.string.navdrawer_item_profile));

            // My topics
            initialDrawerItems.add(DrawerItem.simple(NAVDRAWER_ITEM_MY_TOPICS, R.drawable.ic_action_news, R.string.navdrawer_item_my_topics));

            // Private messages
            initialDrawerItems.add(DrawerItem.simple(NAVDRAWER_ITEM_PRIVATE_MESSAGES, R.drawable.ic_action_mail, R.string.navdrawer_item_private_messages));
        }

        // Settings
        initialDrawerItems.add(DrawerItem.simple(NAVDRAWER_ITEM_SETTINGS, R.drawable.ic_action_settings, R.string.navdrawer_item_settings));

        // Separate static items from dynamic ones (categories)
        initialDrawerItems.add(DrawerItem.separator());

        populateNavDrawer(initialDrawerItems);
    }

    private void populateNavDrawer(List<DrawerItem> drawerItems) {
        for (DrawerItem drawerItem : drawerItems) {
            View drawerItemView = makeNavDrawerItem(drawerItem, drawerItemsListContainer);
            drawerItemsViews.add(drawerItemView);
            drawerItemsListContainer.addView(drawerItemView);
        }
    }

    private void populateNavDrawerCategories(List<Category> categories) {
        List<DrawerItem> drawerItems = new ArrayList<>();

        for (Category category : categories) {
            drawerItems.add(DrawerItem.category(category, HFRIcons.getCategoryIcon(category)));
        }

        populateNavDrawer(drawerItems);
    }

    private View makeNavDrawerItem(final DrawerItem drawerItem, ViewGroup container) {
        int layoutToInflate = drawerItem.isSeparator() ? R.layout.navigation_drawer_separator : R.layout.navigation_drawer_item;

        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (drawerItem.isSeparator()) {
            // Done !
            return view;
        }
        else  {
            TextView titleView = (TextView) view.findViewById(R.id.item_name);
            ImageView iconView = (ImageView) view.findViewById(R.id.item_icon);

            if (drawerItem.isSimpleItem()) {
                SimpleDrawerItem simpleDrawerItem = (SimpleDrawerItem) drawerItem;
                iconView.setImageResource(simpleDrawerItem.getIconResource());
                titleView.setText(getText(simpleDrawerItem.getTitleResource()));
            }
            else {
                CategoryDrawerItem categoryDrawerItem = (CategoryDrawerItem) drawerItem;
                iconView.setImageResource(categoryDrawerItem.getIconResource());
                titleView.setText(categoryDrawerItem.getCategory().name());
            }
        }

        styleNavDrawerItem(view, drawerItem, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(drawerItem);
            }
        });

        return view;
    }

    private void styleNavDrawerItem(View view, DrawerItem drawerItem, boolean selected) {
        if (drawerItem.isSeparator()) {
            // Nothing to style !
            return;
        }

        ImageView itemIcon = (ImageView) view.findViewById(R.id.item_icon);

        if (itemIcon != null) {
            UiUtils.setDrawableColor(itemIcon.getDrawable(), UiUtils.getReplyToolbarIconsColor(this));
        }

        // todo
    }

    private void onNavDrawerItemClicked(DrawerItem drawerItem) {
        if (drawerItem.isSimpleItem()) {
            SimpleDrawerItem simpleDrawerItem = (SimpleDrawerItem) drawerItem;

            switch (simpleDrawerItem.getItemId()) {
                case NAVDRAWER_ITEM_MY_TOPICS:
                    if (this instanceof TopicsActivity) {
                        onMyTopicsClicked();
                    }
                    else {
                        Intent intent = new Intent(this, TopicsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(UIConstants.ARG_SELECTED_CATEGORY, categoriesStore.getMetaCategory());
                        startActivity(intent);
                    }

                    drawerLayout.closeDrawers();
                    break;
                case NAVDRAWER_ITEM_PRIVATE_MESSAGES: {
                    Intent intent = new Intent(this, PrivateMessagesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                }
                case NAVDRAWER_ITEM_PROFILE: {
                    Intent intent = new Intent(this, AccountActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(UIConstants.ARG_RELOGIN_MODE, true);
                    startActivity(intent);
                    break;
                }
                case NAVDRAWER_ITEM_SETTINGS: {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                }
            }
        }
        else if (drawerItem.isCategory()) {
            CategoryDrawerItem categoryDrawerItem = (CategoryDrawerItem) drawerItem;

            dataService.clearTopicListCache();

            if (this instanceof TopicsActivity) {
                onCategoryClicked(categoryDrawerItem.getCategory());
            }
            else {
                Intent intent = new Intent(this, TopicsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(UIConstants.ARG_SELECTED_CATEGORY, categoryDrawerItem.getCategory());
                startActivity(intent);
            }
        }

        drawerLayout.closeDrawers();
    }

    /**
     * Called when an item is selected in the navigation drawer
     */
    public void onCategoryClicked(Category category) {
        onCategoryClicked(category, false);
    }

    /**
     * Called when an item is selected in the navigation drawer
     */
    public void onCategoryClicked(Category category, boolean addToBackstack) {
    }

    /**
     * Called when the "My Topics" item in the navigation drawer has been called
     */
    public void onMyTopicsClicked() {
    }

    /**
     * Called when categories for active user have been sucessfully loaded
     */
    public void onCategoriesLoaded() {
    }

    protected void onUserSwitched(User newUser) {
    }
}
