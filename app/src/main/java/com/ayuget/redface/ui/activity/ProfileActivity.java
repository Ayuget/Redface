package com.ayuget.redface.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Profile;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.view.ProfileDetailsItemView;
import com.ayuget.redface.util.HTMLUtils;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.InjectView;

import static com.ayuget.redface.ui.UIConstants.ARG_PROFILE;

public class ProfileActivity extends BaseActivity {
	@Inject
	ThemeManager themeManager;

	@InjectView(R.id.toolbar_actionbar)
	Toolbar toolbar;

	@InjectView(R.id.profile_picture)
	ImageView userProfilePicture;

	@InjectView(R.id.username)
	TextView username;

	@InjectView(R.id.status)
	TextView status;

	@InjectView(R.id.profile_attributes)
	LinearLayout profileAttributes;

	private Profile profile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		Intent intent = getIntent();
		if (intent == null) {
			throw new IllegalStateException("No intent passed");
		}

		profile = intent.getParcelableExtra(ARG_PROFILE);
		if (profile == null) {
			throw new IllegalArgumentException("No profile passed to profile activity");
		}

		toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back);
		toolbar.setNavigationOnClickListener(v -> finish());
		toolbar.setBackgroundColor(getResources().getColor(R.color.app_bar_transparent));

		userProfilePicture.setBackground(getResources().getDrawable(themeManager.getProfileAvatarBackgroundDrawable()));

		username.setText(HTMLUtils.unescapeHTML(profile.username()));
		status.setText(HTMLUtils.unescapeHTML(profile.status()));

		if (profile.avatarUrl() == null) {
			userProfilePicture.setVisibility(View.GONE);
		}
		else {
			Picasso.with(this)
				.load(profile.avatarUrl())
				.into(userProfilePicture);
		}

		addDetailIfPresent(profile.emailAddress(), R.string.profile_email_label, R.drawable.ic_action_mail);
		addDetailIfPresent(profile.birthday(), R.string.profile_birthday_label, R.drawable.ic_action_birthday);
		addDetailIfPresent(profile.sexGenre(), R.string.profile_gender_label, R.drawable.ic_action_genre);
		addDetailIfPresent(profile.city(), R.string.profile_city_label, R.drawable.ic_action_city);
		addDetailIfPresent(profile.employment(), R.string.profile_employment_label, R.drawable.ic_action_employment);
		addDetailIfPresent(profile.hobbies(), R.string.profile_hobbies_label, R.drawable.ic_action_hobbies);
		addDetailIfPresent(String.valueOf(profile.messageCount()), R.string.profile_message_count_label, R.drawable.ic_action_chat);
		addDetailIfPresent(profile.arrivalDate(), R.string.profile_arrival_date_label, R.drawable.ic_action_arrival_date);
		addDetailIfPresent(profile.lastMessageDate(), R.string.profile_last_message_date, R.drawable.ic_action_last_message_date);
		addDetailIfPresent(profile.personalQuote(), R.string.profile_personal_quote_label, R.drawable.ic_action_format_quote);
		addDetailIfPresent(profile.messageSignature(), R.string.profile_signature_label, R.drawable.ic_action_signature);
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
}
