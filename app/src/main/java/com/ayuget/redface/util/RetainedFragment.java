package com.ayuget.redface.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Adapted version of "RetainFragment" project :
 * https://github.com/krpiotrek/RetainFragmentSample
 */
public class RetainedFragment<T> extends Fragment {
    @NonNull
    public static <T> RetainedFragment<T> newInstance() {
        RetainedFragment<T> retainFragment = new RetainedFragment<>();
        retainFragment.setRetainInstance(true);
        return retainFragment;
    }

    private T value;

    @NonNull
    public T getValue() {
        return value;
    }

    public void setValue(@NonNull T value) {
        this.value = value;
    }
}
