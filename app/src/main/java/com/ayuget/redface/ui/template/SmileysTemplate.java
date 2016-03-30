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

import com.ayuget.redface.R;
import com.ayuget.redface.data.api.model.Smiley;
import com.ayuget.redface.ui.misc.ThemeManager;
import com.ayuget.redface.ui.misc.UiUtils;
import com.squareup.phrase.Phrase;

import java.util.List;

public class SmileysTemplate extends HTMLTemplate<List<Smiley>> {
    private static final String SMILEYS_TEMPLATE = "smileys.html";

    private static final String NO_SMILEYS_FOUND_IMG_URL = "http://forum-images.hardware.fr/images/perso/bobox360.gif";

    private SmileyTemplate smileyTemplate;

    private ThemeManager themeManager;

    private final int smileyTouchSize;

    private final int smileyPadding;

    private final String noSmileysFoundStr;

    public SmileysTemplate(Context context, SmileyTemplate smileyTemplate, ThemeManager themeManager) {
        super(context, SMILEYS_TEMPLATE);

        this.smileyTemplate = smileyTemplate;
        this.themeManager = themeManager;
        this.smileyTouchSize = context.getResources().getDimensionPixelSize(R.dimen.smiley_touch_size);
        this.smileyPadding = context.getResources().getDimensionPixelSize(R.dimen.smiley_padding);
        this.noSmileysFoundStr = context.getResources().getString(R.string.no_smileys_found);
    }

    @Override
    public Phrase compile(String templateContent) {
        return Phrase.from(templateContent)
                .put("css", readAssetFile("styles.css"))
                .put("js", readAssetFile("hfr.js"));
    }

    @Override
    protected void render(List<Smiley> smileys, Phrase templateContent, StringBuilder stream) {
        StringBuilder smileysBuffer = new StringBuilder();
        if (smileys.size() > 0) {
            for (Smiley smiley : smileys) {
                smileyTemplate.render(smiley, smileysBuffer);
            }
        } else {

            smileysBuffer.append("<div class=\"nosmiley\">").append(noSmileysFoundStr).append(" <img src=\"").append(NO_SMILEYS_FOUND_IMG_URL).append("\"/></div>");
        }

        stream.append(
                templateContent
                        .put("smileys", smileysBuffer.toString())
                        .put("theme_class", themeManager.getActiveThemeCssClass())
                        .format()
                        .toString()
        );
    }
}
