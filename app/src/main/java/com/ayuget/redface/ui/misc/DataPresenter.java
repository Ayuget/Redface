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

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.google.common.base.Preconditions;

public class DataPresenter {
    private final View errorView;

    private final View loadingView;

    private final View dataView;

    private final View emptyView;

    private final Button errorViewButton;

    private final Button emptyViewButton;

    public interface OnRefreshRequestedListener {
        void onRefresh();
    }

    private OnRefreshRequestedListener onRefreshRequestedListener;

    private DataPresenter(View rootView, int errorViewRes, int errorViewButtonRes, int loadingViewRes, int dataViewRes, int emptyViewRes, int emptyViewButtonRes) {
        this.errorView = findRequiredView(rootView, "error", errorViewRes);
        this.loadingView = findRequiredView(rootView, "loading", loadingViewRes);
        this.dataView = findRequiredView(rootView, "data", dataViewRes);
        this.emptyView = findRequiredView(rootView, "empty", emptyViewRes);
        this.errorViewButton = (Button) findRequiredView(rootView, "error_button", errorViewButtonRes);
        this.emptyViewButton = (Button) findRequiredView(rootView, "empty_button", emptyViewButtonRes);

        setupClickCallbacks();
    }

    private void setupClickCallbacks() {
        this.errorViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRefreshRequestedListener != null) {
                    onRefreshRequestedListener.onRefresh();
                }
            }
        });

        this.emptyViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRefreshRequestedListener != null) {
                    onRefreshRequestedListener.onRefresh();
                }
            }
        });
    }

    public static Builder from(@NonNull View rootView) {
        return new Builder(rootView);
    }

    public void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        dataView.setVisibility(View.GONE);
    }

    public void showErrorView() {
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
        dataView.setVisibility(View.GONE);
    }

    public void showDataView() {
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        dataView.setVisibility(View.VISIBLE);
    }

    public void showLoadingView() {
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        dataView.setVisibility(View.GONE);
    }

    private View findRequiredView(View rootView, String name, @IdRes int id) {
        View view = rootView.findViewById(id);

        if (view == null) {
            throw new IllegalStateException("Required view '"
            + name
            + "' with ID"
            + id
            + " was not found");
        }

        return view;
    }

    public void setOnRefreshRequestedListener(OnRefreshRequestedListener onRefreshRequestedListener) {
        this.onRefreshRequestedListener = onRefreshRequestedListener;
    }

    public static class Builder {
        private final View rootView;

        private int errorViewRes;

        private int errorViewButtonRes;

        private int loadingViewRes;

        private int dataViewRes;

        private int emptyViewRes;

        private int emptyViewButtonRes;

        private Builder(@NonNull View rootView) {
            this.rootView = rootView;
        }

        public Builder withErrorView(@IdRes int errorViewRes, @IdRes int reloadButtonRes) {
            this.errorViewRes = errorViewRes;
            this.errorViewButtonRes = reloadButtonRes;
            return this;
        }

        public Builder withLoadingView(@IdRes int loadingViewRes) {
            this.loadingViewRes = loadingViewRes;
            return this;
        }

        public Builder withDataView(@IdRes int dataViewRes) {
            this.dataViewRes = dataViewRes;
            return this;
        }

        public Builder withEmptyView(@IdRes int emptyViewRes, @IdRes int reloadButtonRes) {
            this.emptyViewRes = emptyViewRes;
            this.emptyViewButtonRes = reloadButtonRes;
            return this;
        }

        public DataPresenter build() {
            Preconditions.checkNotNull(rootView, "rootView == null");
            return new DataPresenter(rootView, errorViewRes, errorViewButtonRes, loadingViewRes, dataViewRes, emptyViewRes, emptyViewButtonRes);
        }
    }
}
