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
