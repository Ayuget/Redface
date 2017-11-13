package com.ayuget.redface.data.api.model.misc;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SearchTerms {
    public abstract String words();
    public abstract String author();

    public static SearchTerms create(String words, String author) {
        return new AutoValue_SearchTerms(words, author);
    }

    public boolean hasWords() {
        return this.words().length() > 0;
    }

    public boolean hasAuthor() {
        return this.author().length() > 0;
    }
}
