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
import com.ayuget.redface.data.api.model.Post;
import com.ayuget.redface.util.DateUtils;
import com.squareup.phrase.Phrase;

public class PostExtraDetailsTemplate extends HTMLTemplate<Post> {
    private static final String EXTRA_DETAILS_TEMPLATE = "extra_details.html";

    private static final String EDITED_HTML = "<span class=\"edited\">{edited_text}</span>";
    private static final String QUOTE_COUNT_HTML = "<i class=\"material-icons\">chat_bubble_outline</i> {quote_count}";

    private Context context;

    public PostExtraDetailsTemplate(Context context) {
        super(context, EXTRA_DETAILS_TEMPLATE);
        this.context = context;
    }

    @Override
    protected void render(Post post, Phrase templateContent, StringBuilder stream)
    {
        StringBuilder extraDetails = new StringBuilder();

        if (post.getLastEditionDate() != null) {
            String editedText = Phrase.from(context, R.string.post_edited_on)
                    .put("date", DateUtils.formatLocale(context, post.getLastEditionDate()))
                    .format()
                    .toString();

            extraDetails.append(Phrase.from(EDITED_HTML).put("edited_text", editedText).format().toString());
        }

        if (post.getQuoteCount() > 0) {
            if (extraDetails.length() > 0) { extraDetails.append(" - "); }
            extraDetails.append(Phrase.from(QUOTE_COUNT_HTML).put("quote_count", post.getQuoteCount()).format().toString());
        }


        if (extraDetails.length() > 0) {
            stream.append(templateContent.put("extra_details", extraDetails.toString()).format().toString());
        }
        else {
            stream.append("");
        }
    }
}
