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

package com.ayuget.redface.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ayuget.redface.RedfaceApp;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class BaseFragment extends Fragment {
    @Inject Bus bus;

    @Inject
    ThemeManager themeManager;

    private CompositeSubscription subscriptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read fragment args from bundle
        FragmentArgs.inject(this);

        // Inject dependencies
        RedfaceApp app = RedfaceApp.get(getActivity());
        app.inject(this);

        // Proper RxJava subscriptions management with CompositeSubscription
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();

        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        bus.unregister(this);
    }

    protected void emitEvent(Object o) {
        bus.post(o);
    }

    protected void subscribe(Subscription s) {
        subscriptions.add(s);
    }

    protected ViewGroup inflateRootView(@LayoutRes int viewResId, LayoutInflater inflater, ViewGroup container) {
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), themeManager.getActiveThemeStyle());
        LayoutInflater themedInflater = inflater.cloneInContext(contextThemeWrapper);

        ViewGroup rootView = (ViewGroup) themedInflater.inflate(viewResId, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }
}
