package com.ayuget.redface.ui.template;

import android.content.Context;
import android.text.TextUtils;

import com.ayuget.redface.data.api.model.Smiley;

public class SmileyTemplate extends HTMLTemplate<Smiley> {
    private static final String LOG_TAG = SmileyTemplate.class.getSimpleName();

    private static final String SMILEY_TEMPLATE = "smiley.html";

    public SmileyTemplate(Context context) {
        super(context, SMILEY_TEMPLATE);
    }

    @Override
    protected void render(Smiley smiley, String templateContent, StringBuilder stream) {
        stream.append(TextUtils.replace(
                templateContent,
                new String[]{"{smileyCode}", "{smileyUrl}"},
                new String[]{smiley.getCode(), smiley.getImageUrl()}
        ));
    }
}
