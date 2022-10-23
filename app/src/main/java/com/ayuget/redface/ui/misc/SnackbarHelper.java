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

package com.ayuget.redface.ui.misc;

import android.app.Activity;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.ayuget.redface.R;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {
	private SnackbarHelper() {
	}

	public static Snackbar make(View targetView, @StringRes int textRes, boolean isError) {
		Snackbar snack = Snackbar.make(targetView, textRes, Snackbar.LENGTH_LONG);

		if (isError) {
			snack.getView().setBackgroundResource(R.color.theme_primary_light);
		}

		return snack;
	}

	public static Snackbar make(View targetView, CharSequence textRes, boolean isError) {
		Snackbar snack = Snackbar.make(targetView, textRes, Snackbar.LENGTH_LONG);

		if (isError) {
			snack.getView().setBackgroundResource(R.color.theme_primary_light);
		}

		return snack;
	}

	public static void showWithAction(Activity targetActivity, @StringRes int textRes, @StringRes int actionRes, View.OnClickListener actionClickListener) {
		makeWithAction(targetActivity, textRes, actionRes, actionClickListener).show();
	}

	public static Snackbar makeWithAction(Activity targetActivity, @StringRes int textRes, @StringRes int actionRes, View.OnClickListener actionClickListener) {
		Snackbar snackbar = make(targetActivity.findViewById(android.R.id.content), textRes, false);
		snackbar.setAction(actionRes, actionClickListener);
		return snackbar;
	}

	public static Snackbar makeWithAction(Fragment targetFragment, String textRes, @StringRes int actionRes, View.OnClickListener actionClickListener) {
		Snackbar snackbar = make(targetFragment.getView(), textRes, false);
		snackbar.setAction(actionRes, actionClickListener);
		return snackbar;
	}

	public static Snackbar make(Activity targetActivity, @StringRes int textRes) {
		return make(targetActivity.findViewById(android.R.id.content), textRes, false);
	}

	public static Snackbar make(Activity targetActivity, CharSequence textRes) {
		return make(targetActivity.findViewById(android.R.id.content), textRes, false);
	}

	public static Snackbar make(Fragment targetFragment, @StringRes int textRes) {
		return make(targetFragment.getView(), textRes, false);
	}

	public static Snackbar make(Fragment targetFragment, CharSequence textRes) {
		return make(targetFragment.getView(), textRes, false);
	}

	public static Snackbar makeError(Activity targetActivity, @StringRes int textRes) {
		return make(targetActivity.findViewById(android.R.id.content), textRes, true);
	}

	public static Snackbar makeError(Activity targetActivity, CharSequence textRes) {
		return make(targetActivity.findViewById(android.R.id.content), textRes, true);
	}

	public static Snackbar makeError(Fragment targetFragment, @StringRes int textRes) {
		return make(targetFragment.getView(), textRes, true);
	}

	public static Snackbar makeError(Fragment targetFragment, CharSequence textRes) {
		return make(targetFragment.getView(), textRes, true);
	}
}
