package com.ayuget.redface.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.DataService;
import com.ayuget.redface.data.api.MDEndpoints;
import com.ayuget.redface.data.api.model.Category;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.network.HTTPClientProvider;
import com.ayuget.redface.ui.drawer.CategoryDrawerItem;
import com.ayuget.redface.ui.drawer.DrawerItem;
import com.ayuget.redface.ui.drawer.SimpleDrawerItem;
import com.ayuget.redface.ui.hfr.HFRIcons;
import com.ayuget.redface.ui.misc.PaletteTransformation;
import com.ayuget.redface.util.UserUtils;
import com.google.common.base.Optional;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import rx.Subscription;

public class BaseDrawerActivity extends BaseActivity {
    private static final String LOG_TAG = BaseDrawerActivity.class.getSimpleName();

    private Subscription categoriesRequest;

    private TextView activeUserNameTextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawerItemsViews = new ArrayList<>();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        Log.d(LOG_TAG, "setContentView(int layoutResID)");

        setupNavigationDrawer();
        setupAccountBox();
    }

    @Override
    public void setContentView(int layoutResID, Bundle savedInstanceState) {
        super.setContentView(layoutResID, savedInstanceState);

        Log.d(LOG_TAG, "setContentView(int layoutResID, Bundle savedInstanceState)");

        setupNavigationDrawer();
        setupAccountBox();
    }

    private void setupNavigationDrawer() {
        Log.d(LOG_TAG, "Setting up navigation drawer");

        // Now retrieve the DrawerLayout so that we can set the status bar color.
        // This only takes effect on Lollipop, or when using translucentStatusBar
        // on KitKat.
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.theme_primary_dark));
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        // Initiate drawer with "static" items
        initiateNavDrawer();

        // Setup user details and load categories for active user
        updateActiveUser();
    }

    private void updateActiveUser() {
        Log.d(LOG_TAG, "Updating active user");
        final User activeUser = userManager.getActiveUser();

        // Reset categories
        initiateNavDrawer();

        // Load categories for active user
        categoriesRequest = dataService.loadCategories(activeUser, new EndlessObserver<List<Category>>() {
            @Override
            public void onNext(List<Category> categories) {
                Log.d(LOG_TAG, "onNext loadCategories ===");
                populateNavDrawerCategories(categories);
                onCategoriesLoaded();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(LOG_TAG, String.format("Error on retrieving categories for user '%s'", activeUser), throwable);
            }
        });

        activeUserName.setText(activeUser.getDisplayUsername(this));

        Optional<Integer> userId = Optional.absent();
        if (! activeUser.isGuest() && activeUser.hasAvatar()) {
            userId = UserUtils.getLoggedInUserId(activeUser, httpClientProvider.getClientForUser(activeUser));
        }

        if (userId.isPresent()) {
            Log.d(LOG_TAG, String.format("Loading avatar for user '%s' (id: '%d')", activeUser.getUsername(), userId.get()));
            Picasso.with(this)
                    .load(mdEndpoints.userAvatar(userId.get()))
                    .into(activeUserPicture, new PaletteTransformation.PaletteCallback(activeUserPicture) {
                        @Override
                        protected void onSuccess(Palette palette) {
                            if (palette != null) {
                                choseAccountView.setBackgroundColor(palette.getDarkVibrantColor(R.color.theme_primary));
                            }
                        }

                        @Override
                        public void onError() {
                            activeUser.setHasAvatar(false);
                            loadDefaultProfileImage();
                        }
                    });
        }
        else {
            loadDefaultProfileImage();
        }

        onUserSwitched(userManager.getActiveUser());
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
                startActivity(new Intent(BaseDrawerActivity.this, AccountActivity.class));
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
                updateActiveUser();

                accountBoxExpanded = false;
                setupAccountBoxToggle();
            }
        });

        accountListContainer.addView(accountView);
    }

    private void initiateNavDrawer() {
        // Initial cleanup
        drawerItemsListContainer.removeAllViews();
        drawerItemsViews.clear();

        List<DrawerItem> initialDrawerItems = new ArrayList<>();

        if (userManager.activeUserIsLoggedIn()) {
            // Profile
            // initialDrawerItems.add(DrawerItem.simple(NAVDRAWER_ITEM_PROFILE, R.drawable.ic_action_user, R.string.navdrawer_item_profile));

            // Private messages
            // initialDrawerItems.add(DrawerItem.simple(NAVDRAWER_ITEM_PRIVATE_MESSAGES, R.drawable.ic_action_monolog, R.string.navdrawer_item_private_messages));

            // My topics
            // initialDrawerItems.add(DrawerItem.simple(NAVDRAWER_ITEM_MY_TOPICS, R.drawable.ic_action_news, R.string.navdrawer_item_my_topics));
        }

        // Settings
        initialDrawerItems.add(DrawerItem.simple(NAVDRAWER_ITEM_SETTINGS, R.drawable.ic_action_settings_grey, R.string.navdrawer_item_settings));

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
                titleView.setText(categoryDrawerItem.getCategory().getName());
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

        // todo
    }

    private void onNavDrawerItemClicked(DrawerItem drawerItem) {
        if (drawerItem.isSimpleItem()) {
            SimpleDrawerItem simpleDrawerItem = (SimpleDrawerItem) drawerItem;

            switch (simpleDrawerItem.getItemId()) {
                case NAVDRAWER_ITEM_PROFILE:
                case NAVDRAWER_ITEM_SETTINGS:
                case NAVDRAWER_ITEM_MY_TOPICS:
                case NAVDRAWER_ITEM_PRIVATE_MESSAGES:
                    startActivity(new Intent(this, SettingsActivity.class));
                    break;
            }
        }
        else if (drawerItem.isCategory()) {
            CategoryDrawerItem categoryDrawerItem = (CategoryDrawerItem) drawerItem;
            onCategoryClicked(categoryDrawerItem.getCategory());
            drawerLayout.closeDrawers();
        }
    }

    /**
     * Called when an item is selected in the navigation drawer
     */
    public void onCategoryClicked(Category category) {
    }

    /**
     * Called when categories for active user have been sucessfully loaded
     */
    public void onCategoriesLoaded() {
    }

    protected void onUserSwitched(User newUser) {
    }
}
