package com.ayuget.redface.ui.view;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.misc.SmileyType;
import com.ayuget.redface.ui.misc.UiUtils;
import com.bumptech.glide.Glide;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileDetailsSmileyView extends CoordinatorLayout {
	@InjectView(R.id.smiley_image)
	ImageView smileyImageView;

	@InjectView(R.id.smiley_code)
	TextView smileyCodeView;

	@InjectView(R.id.copy_smiley_code)
	ImageButton copySmileyCodeButton;

	@InjectView(R.id.edit_smiley_keywords)
	ImageButton editSmileyKeywordsButton;

	@InjectView(R.id.add_smiley_to_favorites)
	ImageButton addSmileyToFavoritesButton;

	@InjectView(R.id.remove_smiley_from_favorites)
	ImageButton removeSmileyFromFavoritesButton;

	private final String smileyUrl;
	private final String smileyCode;
	private final SmileyType smileyType;

	public ProfileDetailsSmileyView(@NonNull Context context, @NonNull String smileyUrl, @NonNull String smileyCode, SmileyType smileyType) {
		super(context);
		this.smileyUrl = smileyUrl;
		this.smileyCode = smileyCode;
		this.smileyType = smileyType;
		initialize();
	}

	private void initialize() {
		inflate(getContext(), R.layout.item_profile_custom_smiley, this);
		ButterKnife.inject(this);

		Glide.with(this).load(smileyUrl).into(smileyImageView);
		smileyCodeView.setText(smileyCode);

		TooltipCompat.setTooltipText(copySmileyCodeButton, getContext().getString(R.string.profile_personal_smilies_copy_code));
		TooltipCompat.setTooltipText(editSmileyKeywordsButton, getContext().getString(R.string.profile_personal_smilies_edit_smiley_keywords));

		copySmileyCodeButton.setOnClickListener((click) -> UiUtils.copyTextToClipboard(getContext(), smileyCode, R.string.profile_personal_smilies_code_copied));
	}
}
