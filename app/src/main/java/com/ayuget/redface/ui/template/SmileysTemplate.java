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

package com.ayuget.redface.ui.template;

import android.content.Context;
import android.text.TextUtils;

import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.ui.misc.ThemeManager;

import java.util.List;

public class SmileysTemplate extends HTMLTemplate<List<Smiley>> {
    private static final String LOG_TAG = SmileysTemplate.class.getSimpleName();

    private static final String SMILEYS_TEMPLATE = "smileys.html";

    private SmileyTemplate smileyTemplate;

    private ThemeManager themeManager;

    public SmileysTemplate(Context context, SmileyTemplate smileyTemplate, ThemeManager themeManager) {
        super(context, SMILEYS_TEMPLATE);
        this.smileyTemplate = smileyTemplate;
        this.themeManager = themeManager;
    }

    @Override
    public String compile(String templateContent) {
        return TextUtils.replace(
                templateContent,
                new String[]{"{css}", "{js}"},
                new String[]{readAssetFile("styles.css"), readAssetFile("hfr.js")}
        ).toString();
    }

    @Override
    protected void render(List<Smiley> smileys, String templateContent, StringBuilder stream) {
        StringBuilder smileysBuffer = new StringBuilder();
        for(Smiley smiley : smileys) {
            smileyTemplate.render(smiley, smileysBuffer);
        }

        stream.append(TextUtils.replace(
                templateContent,
                new String[]{"{smileys}",  "{theme_class}"},
                new String[]{smileysBuffer.toString(), themeManager.getActiveThemeCssClass()}
        ));
    }
}
