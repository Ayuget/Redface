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

package com.ayuget.redface.data.api.hfr.transforms;

import com.ayuget.redface.data.api.model.Smiley;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Func1;

public class HTMLToSmileyList implements Func1<String, List<Smiley>> {
    public static final Pattern SMILEY_PATTERN = Pattern.compile("(?:<img)(?:\\s+)(?:src=\")([^\"]+)(?:\")(?:\\s+)(?:alt=\")([^\"]+)(?:\")(?:[^\\/]*)(?:\\/>)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public List<Smiley> call(String source) {
        Matcher smileysMatcher = SMILEY_PATTERN.matcher(source);

        List<Smiley> foundSmileys = new ArrayList<>();

        while (smileysMatcher.find()) {
            foundSmileys.add(Smiley.create(smileysMatcher.group(2), smileysMatcher.group(1)));
        }

        return foundSmileys;
    }
}
