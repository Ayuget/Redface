package com.ayuget.redface.ui.event;

public class NestedPreferenceSelectedEvent {
    private final String fragmentKey;

    public NestedPreferenceSelectedEvent(String fragmentKey) {
        this.fragmentKey = fragmentKey;
    }

    public String getFragmentKey() {
        return fragmentKey;
    }
}
