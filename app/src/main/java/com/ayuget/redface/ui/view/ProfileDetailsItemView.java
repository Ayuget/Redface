package com.ayuget.redface.ui.view;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.ayuget.redface.R;
import com.ayuget.redface.ui.misc.UiUtils;
import com.ayuget.redface.util.HTMLUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileDetailsItemView extends RelativeLayout {

	@InjectView(R.id.detail_icon)
	ImageView detailIconView;

	@InjectView(R.id.main_text)
	TextView mainTextView;

	@InjectView(R.id.secondary_text)
	TextView secondaryTextView;

	private final String value;

	private final int labelRes;

	private final int icon;

	public ProfileDetailsItemView(Context context, String value, int labelRes, int icon) {
		super(context);
		this.value = value;
		this.labelRes = labelRes;
		this.icon = icon;
		initialize();
	}

	private void initialize() {
		inflate(getContext(), R.layout.item_profile_detail, this);
		ButterKnife.inject(this);

		detailIconView.setImageDrawable(getContext().getResources().getDrawable(icon));
		mainTextView.setText(value);

		mainTextView.setText(HTMLUtils.unescapeHTML(value));
		secondaryTextView.setText(getContext().getString(labelRes));

		UiUtils.setDrawableColor(detailIconView.getDrawable(), UiUtils.getReplyToolbarIconsColor(getContext()));
	}

	public static com.ayuget.redface.ui.view.ProfileDetailsItemView.Builder from(Context context) {
		return new com.ayuget.redface.ui.view.ProfileDetailsItemView.Builder(context);
	}

	public static class Builder {
		private final Context context;

		private String value;

		private int labelRes;

		private int icon;

		private Builder(Context context) {
			this.context = context;
		}

		public com.ayuget.redface.ui.view.ProfileDetailsItemView.Builder withValue(String value) {
			this.value = value;
			return this;
		}

		public com.ayuget.redface.ui.view.ProfileDetailsItemView.Builder withLabel(@StringRes int labelRes) {
			this.labelRes = labelRes;
			return this;
		}

		public com.ayuget.redface.ui.view.ProfileDetailsItemView.Builder withIcon(@DrawableRes int drawableRes) {
			this.icon = drawableRes;
			return this;
		}

		public com.ayuget.redface.ui.view.ProfileDetailsItemView build() {
			return new com.ayuget.redface.ui.view.ProfileDetailsItemView(context, value, labelRes, icon);
		}
	}
}
