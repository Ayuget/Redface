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

package com.ayuget.redface.data.api.hfr;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class HashcheckExtractor {
    private static final Pattern HASHCHECK_PATTERN = Pattern.compile("<input\\s*type=\"hidden\"\\s*name=\"hash_check\"\\s*value=\"(.+?)\" />", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static String extract(String htmlContent) {
        Matcher m = HASHCHECK_PATTERN.matcher(htmlContent);

        if (m.find()) {
            String hashCheck = m.group(1) != null ? m.group(1) : m.group(2);
            Timber.d("Hashcheck = %s", hashCheck);
            return hashCheck;
        }
        else {
            return null;
        }
    }
}
