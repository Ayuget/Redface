package com.ayuget.redface.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.common.base.Preconditions;

/**
 * Adapted version of "RetainFragment" project :
 * https://github.com/krpiotrek/RetainFragmentSample
 */
public class RetainedFragmentHelper {
    private static final String RETAIN_FRAGMENT_TAG = "retain_fragment";

    /**
     * @return @Nullable RetainFragment. Keep in mind, even if fragment has been set, system may
     * remove it from memory at arbitrary time.
     */
    @Nullable
    private static <T> RetainedFragment<T> getInstance(FragmentManager fragmentManager, String tag) {
        //noinspection unchecked
        return (RetainedFragment<T>) fragmentManager.findFragmentByTag(tag);
    }

    public static <T> void remove(@NonNull Object tag, @NonNull FragmentManager fragmentManager) {
        RetainedFragment<T> instance = getInstance(fragmentManager, getRetainedFragmentTag(tag));
        if (instance != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(instance)
                    .commit();
        }
    }

    /**
     * @return get Object or null if fragment has been cleaned up.
     */
    @Nullable
    public static <T> T recover(@NonNull Object tag, @NonNull FragmentManager fragmentManager) {
        RetainedFragment<T> instance = RetainedFragmentHelper.getInstance(fragmentManager, getRetainedFragmentTag(tag));
        if (instance == null) {
            return null;
        }

        Preconditions.checkNotNull(instance.getValue());
        return instance.getValue();
    }

    public static <T> void retain(@NonNull Object tag,
                                  @NonNull FragmentManager fragmentManager,
                                  @NonNull T value) {
        RetainedFragment<T> instance = getInstance(fragmentManager, getRetainedFragmentTag(tag));
        if (instance == null) {
            instance = RetainedFragment.newInstance();
            fragmentManager
                    .beginTransaction()
                    .add(instance, getRetainedFragmentTag(tag))
                    .commit();
        }

        instance.setValue(value);
    }

    @NonNull
    private static String getRetainedFragmentTag(@NonNull Object object) {
        return RETAIN_FRAGMENT_TAG + object.getClass().getCanonicalName();
    }
}
