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
import android.util.Log;

import com.ayuget.redface.util.DateUtils;
import com.squareup.phrase.Phrase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

public abstract class HTMLTemplate<T> {
    private static final String LOG_TAG = HTMLTemplate.class.getSimpleName();

    private final Context context;

    private final String templateFile;

    private Phrase templateContent;

    protected HTMLTemplate(Context context, String templateFile) {
        this.context = context;
        this.templateFile = templateFile;

        this.templateContent = compile(readAssetFile(templateFile));
    }

    /**
     * Reads template file from the assets folder
     */
    protected String readAssetFile(String templateFile) {
        BufferedReader in = null;

        try {
            StringBuilder buffer = new StringBuilder();

            InputStream stream = context.getAssets().open("templates/" + templateFile);
            in = new BufferedReader(new InputStreamReader(stream));
            boolean firstLine = true;

            String line = in.readLine();

            while(line != null) {
                if(firstLine) {
                    firstLine = false;
                }
                else {
                    buffer.append('\n');
                }
                buffer.append(line);
                line = in.readLine();
            }

            String output = buffer.toString();
            return output;
        }
        catch (final IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
        finally {
            if(in != null) {
                // Damn checked IOException...
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }
    }

    /**
     * Formats given date to a string
     */
    protected String formatDate(Date d) {
        return DateUtils.formatLocale(context, d);
    }

    /**
     * Compiles the template, resolving static dependencies (includes external javascript files,
     * stylesheets, ...)
     */
    public Phrase compile(String templateContent) {
        return Phrase.from(templateContent);
    }

    /**
     * Reloads template content from file and recompiles it
     */
    public void reload() throws IOException {
        this.templateContent = compile(readAssetFile(templateFile));
    }

    protected abstract void render(T content, Phrase templateContent, StringBuilder stream);

    /**
     * Renders the template into a stream
     */
    public void render(T content, StringBuilder stream) {
        this.render(content, templateContent, stream);
    }

    /**
     * Renders the template into a string
     */
    public String render(T content) {
        StringBuilder builder = new StringBuilder();
        render(content, builder);
        return builder.toString();
    }
}
