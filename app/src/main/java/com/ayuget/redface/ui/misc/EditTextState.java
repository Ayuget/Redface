package com.ayuget.redface.ui.misc;


import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EditTextState {
    public abstract String text();
    public abstract int selectionStart();
    public abstract int selectionEnd();

    public static Builder builder() {
        return new AutoValue_EditTextState.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder text(String text);
        public abstract Builder selectionStart(int selectionStart);
        public abstract Builder selectionEnd(int selectionStart);
        public abstract EditTextState build();
    }
}
