/*
 * Copyright 2016 nbonnec
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

package com.ayuget.redface.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ayuget.redface.R;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.phrase.Phrase;

import timber.log.Timber;

public class GoToPageDialog {

    private ThemeManager themeManager;

    private Context context;

    private MaterialEditText goToPageEditText;

    private int pageCounts;

    public interface GoToPageDialogCallback {
        void onSuccess(int pageNumber);
        void onError();
    }

    private GoToPageDialogCallback goToPageDialogCallback;

    public GoToPageDialog(Context context, ThemeManager themeManager, int pageCounts, GoToPageDialogCallback goToPageDialogCallback) {
        this.context = context;
        this.themeManager = themeManager;
        this.goToPageDialogCallback = goToPageDialogCallback;
        this.pageCounts = pageCounts;
    }

    /**
     * Shows the "Go to page" dialog where the user can enter the page he wants to consult.
     */
    public void show() {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_go_to_page, true)
                .positiveText(R.string.dialog_go_to_page_positive_text)
                .negativeText(android.R.string.cancel)
                .theme(themeManager.getMaterialDialogTheme())
                .onPositive((materialDialog, which) -> {
                    try {
                        int pageNumber = Integer.valueOf(goToPageEditText.getText().toString());
                        goToPageDialogCallback.onSuccess(pageNumber);
                    } catch (NumberFormatException e) {
                        Timber.e(e, "Invalid page number entered : %s", goToPageEditText.getText().toString());
                        goToPageDialogCallback.onError();
                    }
                })
                .build();


        final View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        goToPageEditText = (MaterialEditText) dialog.getCustomView().findViewById(R.id.page_number);

        TextView pagesCountView = (TextView) dialog.getCustomView().findViewById(R.id.pages_count);
        pagesCountView.setText(Phrase.from(context, R.string.pages_count).put("page", pageCounts).format());

        goToPageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    try {
                        int pageNumber = Integer.valueOf(s.toString());
                        positiveAction.setEnabled(pageNumber >= 1 && pageNumber <= pageCounts);
                    }
                    catch (NumberFormatException e) {
                        positiveAction.setEnabled(false);
                    }
                }
                else {
                    positiveAction.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialog.show();
        positiveAction.setEnabled(false);
    }

    public static class Builder {
        public Builder(Context context) {

        }


    }
}
