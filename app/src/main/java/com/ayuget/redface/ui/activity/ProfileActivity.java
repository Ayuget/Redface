package com.ayuget.redface.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Profile;

import butterknife.InjectView;

import static com.ayuget.redface.ui.UIConstants.ARG_PROFILE;

public class ProfileActivity extends BaseActivity {
	@InjectView(R.id.toolbar_actionbar)
	Toolbar toolbar;

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

		toolbar.setTitle(profile.username());
		toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back);
		toolbar.setNavigationOnClickListener(v -> finish());
	}
}
