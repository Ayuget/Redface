package com.ayuget.redface.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;

import com.ayuget.redface.R;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.MDService;
import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.profile.ProfileManager;
import com.ayuget.redface.ui.misc.SmileyAction;
import com.ayuget.redface.ui.misc.SmileyFavoriteActionResult;
import com.ayuget.redface.ui.misc.SmileyType;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.ui.view.ProfileConnectionStatusView;
import com.ayuget.redface.ui.view.ProfileDetailsItemView;
import com.ayuget.redface.ui.view.ProfileDetailsSmileyView;
import com.ayuget.redface.util.HTMLUtils;
import com.bumptech.glide.Glide;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.ayuget.redface.ui.UIConstants.ARG_IS_OWN_PROFILE;
import static com.ayuget.redface.ui.UIConstants.ARG_PROFILE_ID;

public class ProfileActivity extends BaseActivity implements ProfileDetailsSmileyView.OnSmileyActionPerformedListener {
    @Inject
    ThemeManager themeManager;

    @Inject
    MDService mdService;

    @Inject
    ProfileManager profileManager;

    @Inject
    UserManager userManager;

    @BindView(R.id.loading_indicator)
    RelativeLayout loadingIndicator;

    @BindView(R.id.error_layout)
    LinearLayout errorLayout;

    @BindView(R.id.error_reload_button)
    Button errorReloadButton;

    @BindView(R.id.profile_wrapper)
    LinearLayout profileWrapper;

    @BindView(R.id.toolbar_actionbar)
    Toolbar toolbar;

    @BindView(R.id.profile_picture)
    ImageView userProfilePicture;

    @BindView(R.id.username)
    TextView username;

    @BindView(R.id.status)
    TextView status;

    @BindView(R.id.profile_attributes)
    LinearLayout profileAttributes;

    @BindView(R.id.favorite_smilies)
    LinearLayout favoriteSmilies;

    private SubscriptionHandler<Integer, Profile> profileHandler = new SubscriptionHandler<>();
    private int profileId;
    private boolean isOwnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        if (intent == null) {
            throw new IllegalStateException("No intent passed");
        }

        profileId = intent.getIntExtra(ARG_PROFILE_ID, -1);
        if (profileId == -1) {
            throw new IllegalArgumentException("No profile id passed to profile activity");
        }

        isOwnProfile = intent.getBooleanExtra(ARG_IS_OWN_PROFILE, false);

        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back);
        UiUtils.setDrawableColor(toolbar.getNavigationIcon(), UiUtils.resolveColorAttribute(this, R.attr.textColorPrimary));

        toolbar.setNavigationOnClickListener(v -> finish());
        userProfilePicture.setBackground(getResources().getDrawable(themeManager.getProfileAvatarBackgroundDrawable()));
        errorReloadButton.setOnClickListener((click) -> loadProfile());

        if (isOwnProfile) {
            showConnectionStatus();
        }

        Profile profile = profileManager.getProfile(profileId);
        if (profile == null) {
            showLoadingIndicator();
            loadProfile();
        } else {
            showProfile(profile);
        }
        if (savedInstanceState != null) {
            savedInstanceState.clear();
        }
    }

    private void loadProfile() {
        subscribe(profileHandler.load(profileId, profileManager.loadProfile(userManager.getActiveUser(), profileId), new EndlessObserver<Profile>() {
            @Override
            public void onNext(Profile profile) {
                showProfile(profile);
            }

            @Override
            public void onError(Throwable throwable) {
                showErrorView();

                if (isOwnProfile) { // Make sure user is always capable of updating its credentials
                    errorLayout.addView(new ProfileConnectionStatusView(ProfileActivity.this));
                }
            }
        }));
    }

    private void showLoadingIndicator() {
        profileWrapper.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showErrorView() {
        profileWrapper.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void showConnectionStatus() {
        TextView forumConnectionHeading = new TextView(new ContextThemeWrapper(this, R.style.Redface_ProfileDetails_Heading), null, 0);
        forumConnectionHeading.setText(R.string.profile_account_connection_title);
        profileAttributes.addView(forumConnectionHeading);

        profileAttributes.addView(new ProfileConnectionStatusView(this));
    }

    private void showProfile(Profile profile) {
        profileWrapper.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);

        username.setText(HTMLUtils.unescapeHTML(profile.username()));
        status.setText(HTMLUtils.unescapeHTML(profile.status()));

        if (profile.avatarUrl() == null) {
            userProfilePicture.setVisibility(View.GONE);
        } else {
            Glide.with(this)
                    .load(profile.avatarUrl())
                    .into(userProfilePicture);
        }

        TextView generalInfoHeading = new TextView(new ContextThemeWrapper(this, R.style.Redface_ProfileDetails_Heading), null, 0);
        generalInfoHeading.setText(R.string.profile_general_info);
        profileAttributes.addView(generalInfoHeading);

        addDetailIfPresent(profile.emailAddress(), R.string.profile_email_label, R.drawable.ic_action_mail);
        addDetailIfPresent(profile.birthday(), R.string.profile_birthday_label, R.drawable.ic_action_birthday);
        addDetailIfPresent(profile.sexGenre(), R.string.profile_gender_label, R.drawable.ic_action_genre);
        addDetailIfPresent(profile.city(), R.string.profile_city_label, R.drawable.ic_action_city);
        addDetailIfPresent(profile.employment(), R.string.profile_employment_label, R.drawable.ic_action_employment);
        addDetailIfPresent(profile.hobbies(), R.string.profile_hobbies_label, R.drawable.ic_action_hobbies);

        TextView forumInfoHeading = new TextView(new ContextThemeWrapper(this, R.style.Redface_ProfileDetails_Heading), null, 0);
        forumInfoHeading.setText(R.string.profile_forum_info);
        profileAttributes.addView(forumInfoHeading);

        addDetailIfPresent(String.valueOf(profile.messageCount()), R.string.profile_message_count_label, R.drawable.ic_action_chat);
        addDetailIfPresent(profile.arrivalDate(), R.string.profile_arrival_date_label, R.drawable.ic_action_arrival_date);
        addDetailIfPresent(profile.lastMessageDate(), R.string.profile_last_message_date, R.drawable.ic_action_last_message_date);
        addDetailIfPresent(profile.personalQuote(), R.string.profile_personal_quote_label, R.drawable.ic_action_format_quote);
        addDetailIfPresent(profile.messageSignature(), R.string.profile_signature_label, R.drawable.ic_action_signature);

        if (profile.personalSmilies().size() > 0) {
            TextView personalSmiliesHeading = new TextView(new ContextThemeWrapper(this, R.style.Redface_ProfileDetails_Heading), null, 0);
            personalSmiliesHeading.setText(R.string.profile_personal_smilies_label);

            profileAttributes.addView(personalSmiliesHeading);

            for (Smiley smiley : profile.personalSmilies()) {
                profileAttributes.addView(new ProfileDetailsSmileyView(this, isOwnProfile, smiley, SmileyType.PERSONAL, this));
            }
        }

        if (isOwnProfile) {
            loadFavoriteSmileys();
        }
    }

    protected void loadFavoriteSmileys() {
        subscribe(mdService.getFavoriteSmileys(userManager.getActiveUser())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EndlessObserver<List<Smiley>>() {
                    @Override
                    public void onNext(List<Smiley> smilies) {
                        favoriteSmilies.removeAllViews();
                        listFavoritesSmileys(smilies);
                    }
                }));
    }

    protected void listFavoritesSmileys(List<Smiley> favoriteSmileys) {
        if (favoriteSmileys.size() == 0) {
            return;
        }

        TextView favoriteSmileysHeading = new TextView(new ContextThemeWrapper(this, R.style.Redface_ProfileDetails_Heading), null, 0);
        favoriteSmileysHeading.setText(R.string.profile_favorite_smilies_label);

        favoriteSmilies.addView(favoriteSmileysHeading);

        for (Smiley smiley : favoriteSmileys) {
            favoriteSmilies.addView(new ProfileDetailsSmileyView(this, isOwnProfile, smiley, SmileyType.FAVORITE, this));
        }
    }

    /**
     * Adds an image detail with it's dedicated icon, if both main and secondary texts are present.
     */
    protected void addDetailIfPresent(String value, @StringRes int labelRes, @DrawableRes int icon) {
        if (value != null) {
            ProfileDetailsItemView detailView = ProfileDetailsItemView.from(this)
                    .withValue(value)
                    .withLabel(labelRes)
                    .withIcon(icon)
                    .build();

            profileAttributes.addView(detailView);
        }
    }

    @Override
    public void onSmileyActionPerformed(Smiley smiley, SmileyAction smileyAction) {
        switch (smileyAction) {
            case COPY_CODE_TO_CLIPBOARD:
                UiUtils.copyTextToClipboard(this, smiley.code(), R.string.profile_personal_smilies_code_copied);
                break;
            case ADD_TO_FAVORITES:
                addSmileyToFavorites(smiley);
                break;
            case REMOVE_FROM_FAVORITES:
                removeSmileyFromFavorites(smiley);
                break;
        }
    }

    private void addSmileyToFavorites(Smiley smiley) {
        subscribe(mdService.addSmileyToFavorites(userManager.getActiveUser(), smiley).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EndlessObserver<SmileyFavoriteActionResult>() {
                    @Override
                    public void onNext(SmileyFavoriteActionResult result) {
                        showAddSmileyAsFavoriteResult(result);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Timber.e(throwable, "Error when adding smiley to favorites");
                        SnackbarHelper.makeError(ProfileActivity.this, R.string.add_smiley_as_favorite_unknown_error).show();
                    }
                }));
    }

    private void showAddSmileyAsFavoriteResult(SmileyFavoriteActionResult result) {
        switch (result) {
            case ADDED_AS_FAVORITE:
                SnackbarHelper.make(ProfileActivity.this, R.string.add_smiley_as_favorite_success).show();
                break;
            case NOT_ADDED_MAX_REACHED:
                SnackbarHelper.make(ProfileActivity.this, R.string.add_smiley_as_favorite_max_reached_out_error).show();
                break;
            case NOT_ADDED_ALREADY_IN_LIST:
                SnackbarHelper.make(ProfileActivity.this, R.string.add_smiley_as_favorite_already_added_error).show();
                break;
            default:
                SnackbarHelper.makeError(ProfileActivity.this, R.string.add_smiley_as_favorite_unknown_error).show();
        }
    }

    private void removeSmileyFromFavorites(Smiley smiley) {
        subscribe(mdService.removeSmileyFromFavorites(userManager.getActiveUser(), smiley).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new EndlessObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean removeSucceeded) {
                        if (removeSucceeded) {
                            SnackbarHelper.make(ProfileActivity.this, R.string.remove_smiley_from_favorites_success).show();
                        } else {
                            SnackbarHelper.makeError(ProfileActivity.this, R.string.remove_smiley_from_favorites_failure).show();
                        }

                        loadFavoriteSmileys();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Timber.e(throwable, "Error when removing smiley from favorites");
                        SnackbarHelper.makeError(ProfileActivity.this, R.string.remove_smiley_from_favorites_failure).show();
                    }
                }));
    }
}
